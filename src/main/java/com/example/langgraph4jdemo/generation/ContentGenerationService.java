package com.example.langgraph4jdemo.generation;

import com.example.langgraph4jdemo.auth.AppUser;
import com.example.langgraph4jdemo.config.AppProperties;
import com.example.langgraph4jdemo.langchain.WritingAssistant;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ContentGenerationService {

    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final WritingAssistant writingAssistant;
    private final GeneratedTextRecordRepository generatedTextRecordRepository;
    private final TextArchiveService textArchiveService;
    private final AppProperties appProperties;

    public ContentGenerationService(WritingAssistant writingAssistant,
                                    GeneratedTextRecordRepository generatedTextRecordRepository,
                                    TextArchiveService textArchiveService,
                                    AppProperties appProperties) {
        this.writingAssistant = writingAssistant;
        this.generatedTextRecordRepository = generatedTextRecordRepository;
        this.textArchiveService = textArchiveService;
        this.appProperties = appProperties;
    }

    @Transactional
    public GenerationResponse generate(AppUser user, GenerationRequest request) {
        String prompt = buildPrompt(request);
        String draft = writingAssistant.createDraft(prompt);
        String finalText = writingAssistant.polish(draft);

        GeneratedTextRecord record = new GeneratedTextRecord();
        record.setUser(user);
        record.setTopic(request.topic().trim());
        record.setAudience(clean(request.audience(), "general readers"));
        record.setTone(clean(request.tone(), "balanced"));
        record.setNotes(cleanNullable(request.notes()));
        record.setDraftText(draft);
        record.setFinalText(finalText);
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
                record.getTopic(),
                record.getAudience(),
                record.getTone(),
                record.getNotes(),
                record.getDraftText(),
                record.getFinalText(),
                record.getArchivePath(),
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
                record.getTopic(),
                record.getTone(),
                record.getCreatedAt().format(DISPLAY_TIME),
                preview == null ? "" : preview,
                "/api/generations/" + record.getId() + "/file"
        );
    }

    private String buildPrompt(GenerationRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(request.topic().trim());
        if (StringUtils.hasText(request.audience())) {
            builder.append(" | audience: ").append(request.audience().trim());
        }
        if (StringUtils.hasText(request.tone())) {
            builder.append(" | tone: ").append(request.tone().trim());
        }
        if (StringUtils.hasText(request.notes())) {
            builder.append(" | notes: ").append(request.notes().trim());
        }
        return builder.toString();
    }

    private String clean(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String cleanNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
