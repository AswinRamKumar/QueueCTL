package com.queuectl.cli;

import picocli.CommandLine.Command;
import java.util.concurrent.Callable;

@Command(
        name = "config",
        description = "Manage system configuration values (e.g. retries, backoff base)",
        subcommands = {
                ConfigSet.class,
                ConfigGet.class
        }
)
public class ConfigCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        System.out.println("Usage: queuectl config [set|get]");
        return 0;
    }
}
