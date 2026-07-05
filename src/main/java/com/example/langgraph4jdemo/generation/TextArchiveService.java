package com.example.langgraph4jdemo.generation;

import com.example.langgraph4jdemo.config.AppProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class TextArchiveService {

    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final AppProperties appProperties;

    public TextArchiveService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public Path write(GeneratedTextRecord record) throws IOException {
        Path baseDir = Path.of(appProperties.generatedOutputDir());
        Path userDir = baseDir.resolve(sanitizeSegment(record.getUser().getUsername()));
        Files.createDirectories(userDir);

        String fileName = FILE_TIME.format(record.getCreatedAt())
                + "-" + record.getId()
                + "-" + slugify(record.getTopic())
                + ".md";

        Path file = userDir.resolve(fileName);
        Files.writeString(file, renderMarkdown(record), StandardCharsets.UTF_8);
        return file.toAbsolutePath();
    }

    private String renderMarkdown(GeneratedTextRecord record) {
        StringBuilder builder = new StringBuilder();
        builder.append("# Generated Text\n\n");
        builder.append("- User: ").append(record.getUser().getDisplayName()).append(" (@").append(record.getUser().getUsername()).append(")\n");
        builder.append("- Topic: ").append(nullToEmpty(record.getTopic())).append("\n");
        builder.append("- Tone: ").append(nullToEmpty(record.getTone())).append("\n");
        builder.append("- Audience: ").append(nullToEmpty(record.getAudience())).append("\n");
        builder.append("- Created At: ").append(record.getCreatedAt()).append("\n\n");
        if (StringUtils.hasText(record.getNotes())) {
            builder.append("## Notes\n\n");
            builder.append(record.getNotes()).append("\n\n");
        }
        if (StringUtils.hasText(record.getDraftText())) {
            builder.append("## Draft\n\n");
            builder.append(record.getDraftText()).append("\n\n");
        }
        builder.append("## Final\n\n");
        builder.append(record.getFinalText()).append("\n");
        return builder.toString();
    }

    private String sanitizeSegment(String value) {
        if (!StringUtils.hasText(value)) {
            return "user";
        }
        return value.trim().replaceAll("[\\\\/:*?\"<>|\\s]+", "-").toLowerCase(Locale.ROOT);
    }

    private String slugify(String value) {
        if (!StringUtils.hasText(value)) {
            return "content";
        }
        String slug = value.trim()
                .replaceAll("[^\\p{L}\\p{N}]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.isBlank() ? "content" : slug.toLowerCase(Locale.ROOT);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
