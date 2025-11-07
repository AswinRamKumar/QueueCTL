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
            description = "Number of worker threads to start",
            defaultValue = "1"
    )
    private int count;

    @Override
    public Integer call() {
        try {
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
