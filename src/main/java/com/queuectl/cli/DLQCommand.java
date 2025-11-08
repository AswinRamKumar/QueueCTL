package com.queuectl.cli;

import picocli.CommandLine.Command;

@Command(
        name = "dlq",
        description = "Manage Dead Letter Queue jobs : View dlq and Retry a dead job",
        subcommands = {
                DLQList.class,
                DLQRetry.class
        }
)
public class DLQCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Use: queuectl dlq [list|retry]");
    }
}
