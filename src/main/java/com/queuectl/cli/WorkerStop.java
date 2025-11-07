package com.queuectl.cli;

import picocli.CommandLine.Command;
import java.io.File;
import java.nio.file.Files;
import java.util.Optional;

@Command(
        name = "stop",
        description = "Stop all running worker processes gracefully"
)
public class WorkerStop implements Runnable {

    @Override
    public void run() {
        File pidFile = new File("worker.pid");
        if (!pidFile.exists()) {
            System.err.println("No active worker.pid found — are workers running?");
            return;
        }

        try {
            String pidStr = Files.readString(pidFile.toPath()).trim();
            long pid = Long.parseLong(pidStr);

            Optional<ProcessHandle> handle = ProcessHandle.of(pid);
            if (handle.isPresent() && handle.get().isAlive()) {
                handle.get().destroy();
                System.out.println("Stopped worker process (PID " + pid + ").");
            } else {
                System.out.println("No active process found for PID " + pid + ".");
            }

            pidFile.delete();

        } catch (Exception e) {
            System.err.println("Error stopping workers: " + e.getMessage());
        }
    }
}
