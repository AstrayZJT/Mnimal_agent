package com.example.langgraph4jdemo.generation;

import com.example.langgraph4jdemo.auth.AppUser;
import com.example.langgraph4jdemo.config.GenerationWorkflowProperties;
import com.example.langgraph4jdemo.langchain.WritingAssistant;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncEdgeAction;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
import org.bsc.langgraph4j.utils.EdgeMappings;
import org.bsc.langgraph4j.serializer.std.ObjectStreamStateSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

@Service
public class GenerationWorkflowService {

    private static final String DRAFT_NODE = "draft";
    private static final String JUDGE_NODE = "judge";
    private static final String REVISE_NODE = "revise";
    private static final String FINALIZE_NODE = "finalize";

    private final WritingAssistant writingAssistant;
    private final ObjectMapper objectMapper;
    private final GenerationWorkflowProperties workflowProperties;
    private final CompiledGraph<GenerationWorkflowState> compiledGraph;

    public GenerationWorkflowService(WritingAssistant writingAssistant,
                                     ObjectMapper objectMapper,
                                     GenerationWorkflowProperties workflowProperties,
                                     ObjectStreamStateSerializer<GenerationWorkflowState> stateSerializer,
                                     BaseCheckpointSaver checkpointSaver) {
        this.writingAssistant = writingAssistant;
        this.objectMapper = objectMapper;
        this.workflowProperties = workflowProperties;
        this.compiledGraph = buildGraph(stateSerializer, checkpointSaver);
    }

    public GenerationWorkflowResult run(AppUser user, GenerationRequest request, String threadId) {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();

        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put(GenerationWorkflowState.TOPIC, request.topic().trim());
        inputs.put(GenerationWorkflowState.AUDIENCE, clean(request.audience(), "general readers"));
        inputs.put(GenerationWorkflowState.TONE, clean(request.tone(), "balanced"));
        inputs.put(GenerationWorkflowState.NOTES, cleanNullable(request.notes()));
        inputs.put(GenerationWorkflowState.THREAD_ID, threadId);
        inputs.put(GenerationWorkflowState.TARGET_SCORE, workflowProperties.scoreThreshold());
        inputs.put(GenerationWorkflowState.MAX_REVISION_ROUNDS, workflowProperties.maxRevisionRounds());
        inputs.put(GenerationWorkflowState.REVISION_COUNT, 0);
        inputs.put(GenerationWorkflowState.TRACE_LOG, List.of(
                "start thread=" + threadId,
                "user=" + user.getUsername()
        ));

        GenerationWorkflowState state = compiledGraph.invoke(inputs, config)
                .orElseThrow(() -> new IllegalStateException("Graph execution returned no state"));

        return new GenerationWorkflowResult(
                threadId,
                state.draftText(),
                safeFinalText(state),
                state.qualityScore(),
                state.revisionCount(),
                state.feedback(),
                state.revisionAdvice(),
                state.traceText()
        );
    }

    private CompiledGraph<GenerationWorkflowState> buildGraph(ObjectStreamStateSerializer<GenerationWorkflowState> stateSerializer,
                                                              BaseCheckpointSaver checkpointSaver) {
        try {
            StateGraph<GenerationWorkflowState> graph = new StateGraph<>(
                    GenerationWorkflowState.SCHEMA,
                    stateSerializer
            );

            graph.addNode(DRAFT_NODE, (state, config) -> CompletableFuture.completedFuture(draftNode(state)));
            graph.addNode(JUDGE_NODE, (state, config) -> CompletableFuture.completedFuture(judgeNode(state)));
            graph.addNode(REVISE_NODE, (state, config) -> CompletableFuture.completedFuture(reviseNode(state)));
            graph.addNode(FINALIZE_NODE, (state, config) -> CompletableFuture.completedFuture(finalizeNode(state)));

            graph.addEdge(START, DRAFT_NODE);
            graph.addEdge(DRAFT_NODE, JUDGE_NODE);
            graph.addConditionalEdges(
                    JUDGE_NODE,
                    AsyncEdgeAction.edge_async((GenerationWorkflowState state) -> routeAfterJudge(state)),
                    EdgeMappings.builder()
                            .to(REVISE_NODE)
                            .to(FINALIZE_NODE)
                            .build()
            );
            graph.addEdge(REVISE_NODE, JUDGE_NODE);
            graph.addEdge(FINALIZE_NODE, END);

            return graph.compile(CompileConfig.builder()
                    .checkpointSaver(checkpointSaver)
                    .releaseThread(false)
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build generation workflow graph", e);
        }
    }

    private Map<String, Object> draftNode(GenerationWorkflowState state) {
        String prompt = buildDraftPrompt(state);
        String draft = writingAssistant.respond(prompt).trim();
        return updateState(
                GenerationWorkflowState.DRAFT_TEXT, draft,
                GenerationWorkflowState.TRACE_LOG, List.of("draft node -> " + summarize(draft))
        );
    }

    private Map<String, Object> judgeNode(GenerationWorkflowState state) {
        String prompt = buildJudgePrompt(state);
        String raw = writingAssistant.respond(prompt);
        GenerationAssessment assessment = parseAssessment(raw);
        int score = assessment.getScore();
        boolean passed = score >= state.targetScore();
        List<String> trace = new ArrayList<>();
        trace.add("judge node -> score=" + score + ", passed=" + passed);
        trace.add("judge reason -> " + safeText(assessment.getReason()));
        trace.add("judge advice -> " + safeText(assessment.getRevisionAdvice()));
        return updateState(
                GenerationWorkflowState.QUALITY_SCORE, score,
                GenerationWorkflowState.PASSED, passed,
                GenerationWorkflowState.FEEDBACK, assessment.getReason(),
                GenerationWorkflowState.REVISION_ADVICE, assessment.getRevisionAdvice(),
                GenerationWorkflowState.TRACE_LOG, trace
        );
    }

    private Map<String, Object> reviseNode(GenerationWorkflowState state) {
        String prompt = buildRevisePrompt(state);
        String revised = writingAssistant.respond(prompt).trim();
        int nextRevisionCount = state.revisionCount() + 1;
        return updateState(
                GenerationWorkflowState.DRAFT_TEXT, revised,
                GenerationWorkflowState.REVISION_COUNT, nextRevisionCount,
                GenerationWorkflowState.TRACE_LOG, List.of(
                        "revise node -> round " + nextRevisionCount,
                        "revise node -> " + summarize(revised)
                )
        );
    }

    private Map<String, Object> finalizeNode(GenerationWorkflowState state) {
        String prompt = buildFinalizePrompt(state);
        String finalText = writingAssistant.respond(prompt).trim();
        if (!StringUtils.hasText(finalText)) {
            finalText = safeFinalText(state);
        }
        return updateState(
                GenerationWorkflowState.FINAL_TEXT, finalText,
                GenerationWorkflowState.TRACE_LOG, List.of("finalize node -> " + summarize(finalText))
        );
    }

    private String routeAfterJudge(GenerationWorkflowState state) {
        if (state.qualityScore() == null) {
            return FINALIZE_NODE;
        }
        boolean needsRevision = state.qualityScore() < state.targetScore()
                && state.revisionCount() < state.maxRevisionRounds();
        return needsRevision ? REVISE_NODE : FINALIZE_NODE;
    }

    private String buildDraftPrompt(GenerationWorkflowState state) {
        return """
                你正在为一个 Java 后端开发实习生生成一篇可直接发布的中文文章。
                任务：根据要求生成第一版正文，只输出正文，不要解释、不要编号、不要 Markdown 代码块。

                题目：%s
                受众：%s
                语气：%s
                补充要求：%s

                要求：
                1. 结构清晰，内容完整。
                2. 结合受众背景给出可执行的建议。
                3. 不要输出元信息、评分信息或写作过程。
                """.formatted(
                state.topic(),
                state.audience(),
                state.tone(),
                safeText(state.notes())
        );
    }

    private String buildJudgePrompt(GenerationWorkflowState state) {
        return """
                你是一个内容质检员。请根据下面要求，给当前稿件打分并判断是否通过。

                评分标准：
                - 满分 100
                - 重点看主题契合度、结构完整度、可执行性、表达清晰度
                - 只有达到 %d 分及以上才算通过

                请只输出 JSON，不要输出代码块，不要输出额外解释。
                JSON 格式：
                {"score": 0, "passed": false, "reason": "", "revisionAdvice": ""}

                题目：%s
                受众：%s
                语气：%s
                补充要求：%s

                当前稿件：
                %s
                """.formatted(
                state.targetScore(),
                state.topic(),
                state.audience(),
                state.tone(),
                safeText(state.notes()),
                safeText(state.draftText())
        );
    }

    private String buildRevisePrompt(GenerationWorkflowState state) {
        return """
                你正在改写一篇已经被评审过的文章。
                请结合评审意见对当前稿件进行增强，只输出最终改写后的正文，不要输出说明。

                题目：%s
                受众：%s
                语气：%s
                补充要求：%s
                当前评分：%s
                评审意见：%s
                改写建议：%s

                当前稿件：
                %s
                """.formatted(
                state.topic(),
                state.audience(),
                state.tone(),
                safeText(state.notes()),
                state.qualityScore() == null ? "-" : state.qualityScore().toString(),
                safeText(state.feedback()),
                safeText(state.revisionAdvice()),
                safeText(state.draftText())
        );
    }

    private String buildFinalizePrompt(GenerationWorkflowState state) {
        return """
                你正在输出最终成品。
                请根据题目、受众、语气、补充要求、评分结果和当前稿件，生成一版可直接交付的最终文章，只输出正文，不要解释。

                题目：%s
                受众：%s
                语气：%s
                补充要求：%s
                当前评分：%s
                评审意见：%s
                改写建议：%s

                当前稿件：
                %s
                """.formatted(
                state.topic(),
                state.audience(),
                state.tone(),
                safeText(state.notes()),
                state.qualityScore() == null ? "-" : state.qualityScore().toString(),
                safeText(state.feedback()),
                safeText(state.revisionAdvice()),
                safeText(state.draftText())
        );
    }

    private GenerationAssessment parseAssessment(String raw) {
        String normalized = normalizeJson(raw);
        try {
            return objectMapper.readValue(normalized, GenerationAssessment.class);
        } catch (Exception ignored) {
            GenerationAssessment fallback = new GenerationAssessment();
            fallback.setScore(extractScore(normalized));
            fallback.setPassed(fallback.getScore() >= workflowProperties.scoreThreshold());
            fallback.setReason(extractField(normalized, "reason"));
            fallback.setRevisionAdvice(extractField(normalized, "revisionAdvice"));
            return fallback;
        }
    }

    private String normalizeJson(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "{\"score\":0,\"passed\":false,\"reason\":\"empty response\",\"revisionAdvice\":\"retry\"}";
        }

        String value = raw.trim();
        if (value.startsWith("```")) {
            value = value.replaceAll("^```(?:json)?\\s*", "");
            value = value.replaceAll("\\s*```$", "");
        }
        return value;
    }

    private int extractScore(String text) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"score\"\\s*:\\s*(\\d+)").matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private String extractField(String text, String field) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"").matcher(text);
        return matcher.find() ? matcher.group(1) : "";
    }

    private Map<String, Object> updateState(Object... keyValues) {
        Map<String, Object> update = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            update.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return update;
    }

    private String clean(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String cleanNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String safeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String safeFinalText(GenerationWorkflowState state) {
        if (StringUtils.hasText(state.finalText())) {
            return state.finalText();
        }
        if (StringUtils.hasText(state.draftText())) {
            return state.draftText();
        }
        return "";
    }

    private String summarize(String text) {
        String value = safeText(text).replaceAll("\\s+", " ");
        if (value.length() <= 80) {
            return value;
        }
        return value.substring(0, 80) + "...";
    }
}
