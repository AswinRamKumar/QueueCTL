package com.queuectl.cli;

import picocli.CommandLine.Command;
import java.util.concurrent.Callable;

@Command(
        name = "worker",
        description = "Manage worker processes",
        subcommands = {
                WorkerStart.class,
                WorkerStop.class
        }
)
public class WorkerCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        System.out.println("Use subcommands: start | stop");
        return 0;
    }
}
