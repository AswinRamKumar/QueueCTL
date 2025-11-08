package com.queuectl.cli;

import com.queuectl.db.models.Job;
import com.queuectl.db.repository.JobStore;
import com.queuectl.util.ConfigLoader;
import com.queuectl.util.ConfigStore;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.time.LocalDateTime;
import java.util.UUID;

@Command(
        name = "enqueue",
        description = "Add a new job to the queue"
)
public class EnqueueCommand implements Runnable {

    @Parameters(paramLabel = "<command>", description = "Command to execute")
    private String command;

    @Override
    public void run() {
        try {
            String userConfig = ConfigStore.get("max_retries");// GET properties from config.
            String defaultConfig = ConfigLoader.get("max_retries");// GET properties from application.
//
            int maxRetries = 3; // if  both are absent then use DEFAULT
            if (userConfig != null) {
                maxRetries = Integer.parseInt(userConfig);
            } else if (defaultConfig != null) {
                maxRetries = Integer.parseInt(defaultConfig);
            }

            String id = UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();

            Job job = new Job(0L, id, command, "pending", 0, maxRetries, now, now, now, null);
            JobStore store = new JobStore();
            store.insert(job);

            System.out.println("Enqueued job:");
            System.out.println(" id: " + id);
            System.out.println(" command: " + command);
            System.out.println(" state: pending");
            System.out.println(" max_retries: " + maxRetries);
            System.out.println(" created_at: " + now);

        } catch (Exception e) {
            System.err.println("Failed to enqueue job: " + e.getMessage());
        }
    }
}
