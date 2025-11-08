package com.queuectl.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import com.queuectl.engine.WorkerManager;
import java.util.concurrent.Callable;
import java.io.FileWriter;

@Command(
        name = "start",
        description = "Start one or more background workers"
)
public class WorkerStart implements Callable<Integer> {

    @Option(
            names = "--count",
            description = "Number of worker threads to start (1–100)",
            defaultValue = "1"
    )
    private int count;

    private static final int MAX_WORKERS = 100;

    @Override
    public Integer call() {
        try {
            if (count <= 0) {
                System.err.println("Worker count must be greater than 0.");
                return 1;
            }
            if (count > MAX_WORKERS) {
                System.err.println("Worker count too high (" + count + "). Limiting to " + MAX_WORKERS + ".");
                count = MAX_WORKERS;
            }

            WorkerManager manager = new WorkerManager();
            manager.resumePendingJobs();
            manager.startWorkers(count);

            long pid = ProcessHandle.current().pid();
            try (FileWriter fw = new FileWriter("worker.pid")) {
                fw.write(String.valueOf(pid));
            }
            System.out.println("Worker process PID: " + pid);

            System.out.println(count + " worker(s) running. Press ENTER to stop...");
            new java.util.Scanner(System.in).nextLine();

            manager.stopAll();
            return 0;

        } catch (Exception e) {
            System.err.println("Error starting workers: " + e.getMessage());
            return 1;
        }
    }
}
