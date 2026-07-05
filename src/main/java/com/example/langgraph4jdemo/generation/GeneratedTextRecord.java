package com.example.langgraph4jdemo.generation;

import com.example.langgraph4jdemo.auth.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "generated_texts")
public class GeneratedTextRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, length = 200)
    private String topic;

    @Column(length = 120)
    private String audience;

    @Column(length = 40)
    private String tone;

    @Column(length = 1000)
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String draftText;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String finalText;

    @Column(length = 500)
    private String archivePath;

    @Column(length = 120)
    private String workflowThreadId;

    private Integer qualityScore;

    private Integer revisionCount;

    @Column(columnDefinition = "TEXT")
    private String workflowTrace;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected GeneratedTextRecord() {
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDraftText() {
        return draftText;
    }

    public void setDraftText(String draftText) {
        this.draftText = draftText;
    }

    public String getFinalText() {
        return finalText;
    }

    public void setFinalText(String finalText) {
        this.finalText = finalText;
    }

    public String getArchivePath() {
        return archivePath;
    }

    public void setArchivePath(String archivePath) {
        this.archivePath = archivePath;
    }

    public String getWorkflowThreadId() {
        return workflowThreadId;
    }

    public void setWorkflowThreadId(String workflowThreadId) {
        this.workflowThreadId = workflowThreadId;
    }

    public Integer getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(Integer qualityScore) {
        this.qualityScore = qualityScore;
    }

    public Integer getRevisionCount() {
        return revisionCount;
    }

    public void setRevisionCount(Integer revisionCount) {
        this.revisionCount = revisionCount;
    }

    public String getWorkflowTrace() {
        return workflowTrace;
    }

    public void setWorkflowTrace(String workflowTrace) {
        this.workflowTrace = workflowTrace;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
