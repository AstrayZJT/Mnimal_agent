package com.example.langgraph4jdemo.generation;

import com.example.langgraph4jdemo.auth.AppUser;
import com.example.langgraph4jdemo.auth.CurrentUserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/generations")
public class GenerationController {

    private final ContentGenerationService contentGenerationService;
    private final CurrentUserService currentUserService;

    public GenerationController(ContentGenerationService contentGenerationService, CurrentUserService currentUserService) {
        this.contentGenerationService = contentGenerationService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public GenerationResponse generate(@Valid @RequestBody GenerationRequest request, HttpSession session) {
        AppUser user = currentUserService.requireCurrentUser(session);
        return contentGenerationService.generate(user, request);
    }

    @GetMapping
    public List<GenerationSummary> list(HttpSession session) {
        AppUser user = currentUserService.requireCurrentUser(session);
        return contentGenerationService.recent(user);
    }

    @GetMapping("/{id}")
    public GenerationResponse detail(@PathVariable Long id, HttpSession session) {
        AppUser user = currentUserService.requireCurrentUser(session);
        return contentGenerationService.requireDetail(user, id);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> file(@PathVariable Long id, HttpSession session) {
        AppUser user = currentUserService.requireCurrentUser(session);
        GeneratedTextRecord record = contentGenerationService.requireRecord(user, id);
        if (record.getArchivePath() == null) {
            throw new GeneratedTextNotFoundException("文件尚未生成");
        }

        Path path = Path.of(record.getArchivePath());
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            throw new GeneratedTextNotFoundException("磁盘上的文件已不存在");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"generated-text-" + record.getId() + ".md\"")
                .contentType(MediaType.parseMediaType("text/markdown;charset=UTF-8"))
                .body(resource);
    }
}
