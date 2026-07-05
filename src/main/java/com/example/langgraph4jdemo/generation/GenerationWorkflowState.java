package com.example.langgraph4jdemo.generation;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class GenerationWorkflowState extends AgentState implements Serializable {

    public static final String TOPIC = "topic";
    public static final String AUDIENCE = "audience";
    public static final String TONE = "tone";
    public static final String NOTES = "notes";
    public static final String THREAD_ID = "threadId";
    public static final String TARGET_SCORE = "targetScore";
    public static final String MAX_REVISION_ROUNDS = "maxRevisionRounds";
    public static final String REVISION_COUNT = "revisionCount";
    public static final String DRAFT_TEXT = "draftText";
    public static final String FINAL_TEXT = "finalText";
    public static final String QUALITY_SCORE = "qualityScore";
    public static final String PASSED = "passed";
    public static final String FEEDBACK = "feedback";
    public static final String REVISION_ADVICE = "revisionAdvice";
    public static final String TRACE_LOG = "traceLog";

    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            entry(TOPIC, Channels.base((current, incoming) -> incoming)),
            entry(AUDIENCE, Channels.base((current, incoming) -> incoming)),
            entry(TONE, Channels.base((current, incoming) -> incoming)),
            entry(NOTES, Channels.base((current, incoming) -> incoming)),
            entry(THREAD_ID, Channels.base((current, incoming) -> incoming)),
            entry(TARGET_SCORE, Channels.base((current, incoming) -> incoming)),
            entry(MAX_REVISION_ROUNDS, Channels.base((current, incoming) -> incoming)),
            entry(REVISION_COUNT, Channels.base((current, incoming) -> incoming)),
            entry(DRAFT_TEXT, Channels.base((current, incoming) -> incoming)),
            entry(FINAL_TEXT, Channels.base((current, incoming) -> incoming)),
            entry(QUALITY_SCORE, Channels.base((current, incoming) -> incoming)),
            entry(PASSED, Channels.base((current, incoming) -> incoming)),
            entry(FEEDBACK, Channels.base((current, incoming) -> incoming)),
            entry(REVISION_ADVICE, Channels.base((current, incoming) -> incoming)),
            entry(TRACE_LOG, Channels.base(GenerationWorkflowState::mergeTrace))
    );

    public GenerationWorkflowState(Map<String, Object> initData) {
        super(initData);
    }

    public String topic() {
        return this.<String>value(TOPIC).orElse("");
    }

    public String audience() {
        return this.<String>value(AUDIENCE).orElse("general readers");
    }

    public String tone() {
        return this.<String>value(TONE).orElse("balanced");
    }

    public String notes() {
        return this.<String>value(NOTES).orElse(null);
    }

    public String threadId() {
        return this.<String>value(THREAD_ID).orElse("");
    }

    public int targetScore() {
        return this.<Integer>value(TARGET_SCORE).orElse(85);
    }

    public int maxRevisionRounds() {
        return this.<Integer>value(MAX_REVISION_ROUNDS).orElse(2);
    }

    public int revisionCount() {
        return this.<Integer>value(REVISION_COUNT).orElse(0);
    }

    public String draftText() {
        return this.<String>value(DRAFT_TEXT).orElse(null);
    }

    public String finalText() {
        return this.<String>value(FINAL_TEXT).orElse(null);
    }

    public Integer qualityScore() {
        return this.<Integer>value(QUALITY_SCORE).orElse(null);
    }

    public boolean passed() {
        return this.<Boolean>value(PASSED).orElse(Boolean.FALSE);
    }

    public String feedback() {
        return this.<String>value(FEEDBACK).orElse(null);
    }

    public String revisionAdvice() {
        return this.<String>value(REVISION_ADVICE).orElse(null);
    }

    public List<String> traceLog() {
        return this.<List<String>>value(TRACE_LOG).orElseGet(ArrayList::new);
    }

    public String traceText() {
        return String.join("\n", traceLog());
    }

    private static List<String> mergeTrace(List<String> current, List<String> incoming) {
        List<String> merged = new ArrayList<>();
        if (current != null) {
            merged.addAll(current);
        }
        if (incoming != null) {
            merged.addAll(incoming);
        }
        return merged;
    }
}
