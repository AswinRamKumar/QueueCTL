package com.queuectl;

import com.queuectl.cli.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.ParameterException;

import java.util.concurrent.Callable;

@Command(
        name = "queuectl",
        mixinStandardHelpOptions = true,
        subcommands = {
                EnqueueCommand.class,
                WorkerCommand.class,
                ListJobs.class,
                DLQCommand.class,
                ConfigCommand.class,
                StatusCommand.class
        }
)
public class Main implements Callable<Integer> {

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Main());
        int exitCode;

        try {
            exitCode = cmd.execute(args);
        } catch (ParameterException pe) {
            System.err.println("Invalid command usage: " + pe.getMessage());
            cmd.usage(System.err);
            exitCode = 2;
        } catch (ExecutionException ee) {
            System.err.println("Error: " + ee.getCause().getMessage());
            exitCode = 1;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            exitCode = 99;
        } finally {
            System.out.flush();
            System.err.flush();
        }
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }
}
