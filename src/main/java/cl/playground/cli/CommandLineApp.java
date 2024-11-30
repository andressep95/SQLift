package cl.playground.cli;

import cl.playground.cli.commands.GenerateCommand;
import cl.playground.cli.commands.InitCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "sqlj", mixinStandardHelpOptions = true, version = "1.0",
        description = "Comandos para manejar archivos YAML de SQLJ",
        subcommands = {
                InitCommand.class,
                GenerateCommand.class
        }
)
public class CommandLineApp implements Runnable {
    public static void main(String[] args) {
        int exitCode = new CommandLine(
                new CommandLineApp()
        ).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
