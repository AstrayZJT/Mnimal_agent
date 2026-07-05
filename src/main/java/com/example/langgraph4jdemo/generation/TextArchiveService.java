package com.example.langgraph4jdemo.generation;

import com.example.langgraph4jdemo.config.AppProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class TextArchiveService {

    private final AppProperties appProperties;

    public TextArchiveService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public Path write(GeneratedTextRecord record) throws IOException {
        Path baseDir = Path.of(appProperties.generatedOutputDir());
        Path userDir = baseDir.resolve("private").resolve(String.valueOf(record.getUser().getId()));
        Files.createDirectories(userDir);

        Path file = userDir.resolve(record.getId() + ".md");
        Files.writeString(file, record.getFinalText());
        return file.toAbsolutePath();
    }
}
