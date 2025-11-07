package com.queuectl.db.models;

import java.time.LocalDateTime;

public class Job {

    private long seq;
    private String id;
    private String command;
    private String state;
    private int attempts;
    private int maxRetries;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime nextRunAt;
    private String lastError;

    public Job() {}

    public Job(long seq, String id, String command, String state, int attempts,
               int maxRetries, LocalDateTime createdAt, LocalDateTime updatedAt,
               LocalDateTime nextRunAt, String lastError) {
        this.seq = seq;
        this.id = id;
        this.command = command;
        this.state = state;
        this.attempts = attempts;
        this.maxRetries = maxRetries;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.nextRunAt = nextRunAt;
        this.lastError = lastError;
    }

    public long getSeq() { return seq; }
    public void setSeq(long seq) { this.seq = seq; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getNextRunAt() { return nextRunAt; }
    public void setNextRunAt(LocalDateTime nextRunAt) { this.nextRunAt = nextRunAt; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}
