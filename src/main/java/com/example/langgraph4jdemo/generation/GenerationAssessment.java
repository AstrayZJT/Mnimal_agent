package com.example.langgraph4jdemo.generation;

public class GenerationAssessment {

    private int score;
    private boolean passed;
    private String reason;
    private String revisionAdvice;

    public GenerationAssessment() {
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRevisionAdvice() {
        return revisionAdvice;
    }

    public void setRevisionAdvice(String revisionAdvice) {
        this.revisionAdvice = revisionAdvice;
    }
}
