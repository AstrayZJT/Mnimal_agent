package com.example.langgraph4jdemo.generation;

import com.example.langgraph4jdemo.auth.AppUser;
import com.example.langgraph4jdemo.config.AppProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ContentGenerationService {

    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final GenerationWorkflowService generationWorkflowService;
    private final GeneratedTextRecordRepository generatedTextRecordRepository;
    private final TextArchiveService textArchiveService;
    private final AppProperties appProperties;

    public ContentGenerationService(GenerationWorkflowService generationWorkflowService,
                                    GeneratedTextRecordRepository generatedTextRecordRepository,
                                    TextArchiveService textArchiveService,
                                    AppProperties appProperties) {
        this.generationWorkflowService = generationWorkflowService;
        this.generatedTextRecordRepository = generatedTextRecordRepository;
        this.textArchiveService = textArchiveService;
        this.appProperties = appProperties;
    }

    @Transactional
    public GenerationResponse generate(AppUser user, GenerationRequest request) {
        String threadId = "generation-" + UUID.randomUUID();
        GenerationWorkflowResult workflowResult = generationWorkflowService.run(user, request, threadId);

        GeneratedTextRecord record = new GeneratedTextRecord();
        record.setUser(user);
        record.setTopic(request.topic().trim());
        record.setAudience(clean(request.audience(), "general readers"));
        record.setTone(clean(request.tone(), "balanced"));
        record.setNotes(cleanNullable(request.notes()));
        record.setDraftText(workflowResult.draftText());
        record.setFinalText(workflowResult.finalText());
        record.setWorkflowThreadId(threadId);
        record.setQualityScore(workflowResult.qualityScore());
        record.setRevisionCount(workflowResult.revisionCount());
        record.setWorkflowTrace(workflowResult.traceLog());
        record = generatedTextRecordRepository.saveAndFlush(record);

        try {
            Path archivePath = textArchiveService.write(record);
            record.setArchivePath(archivePath.toString());
            record = generatedTextRecordRepository.save(record);
        } catch (IOException e) {
            throw new IllegalStateException("保存生成文本到磁盘失败", e);
        }

        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<GenerationSummary> recent(AppUser user) {
        return generatedTextRecordRepository
                .findByUserOrderByCreatedAtDesc(user, PageRequest.of(0, appProperties.historyLimit()))
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public GenerationResponse requireDetail(AppUser user, Long id) {
        return generatedTextRecordRepository.findByIdAndUser(id, user)
                .map(this::toResponse)
                .orElseThrow(() -> new GeneratedTextNotFoundException("没有找到这条生成记录"));
    }

    @Transactional(readOnly = true)
    public GeneratedTextRecord requireRecord(AppUser user, Long id) {
        return generatedTextRecordRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new GeneratedTextNotFoundException("没有找到这条生成记录"));
    }

    private GenerationResponse toResponse(GeneratedTextRecord record) {
        return new GenerationResponse(
                record.getId(),
                record.getFinalText(),
                record.getCreatedAt().format(DISPLAY_TIME),
                "/api/generations/" + record.getId() + "/file"
        );
    }

    private GenerationSummary toSummary(GeneratedTextRecord record) {
        String preview = record.getFinalText();
        if (preview != null && preview.length() > 120) {
            preview = preview.substring(0, 120) + "...";
        }
        return new GenerationSummary(
                record.getId(),
                record.getCreatedAt().format(DISPLAY_TIME),
                preview == null ? "" : preview,
                "/api/generations/" + record.getId() + "/file"
        );
    }

    private String clean(String value, String fallback) {
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }

    private String cleanNullable(String value) {
        return value != null && !value.isBlank() ? value.trim() : null;
    }
}
