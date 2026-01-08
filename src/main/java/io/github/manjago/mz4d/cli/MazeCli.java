package io.github.manjago.mz4d.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "mz4d",
        description = "Maze 4 dimension - telegram bot game",
        mixinStandardHelpOptions = true,
        version = "Mz4d 1.0.0",
        subcommands = {
                CommandLine.HelpCommand.class
        }
)
public class MazeCli implements Runnable {
    @Override
    @SuppressWarnings("java:S106")
    public void run() {
        // If no subcommand, show help
        CommandLine.usage(this, System.out);
    }

    static void main(String[] args) {
        int exitCode = new CommandLine(new MazeCli())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
        System.exit(exitCode);
    }

}
