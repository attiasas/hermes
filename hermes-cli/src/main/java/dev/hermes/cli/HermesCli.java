package dev.hermes.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "hermes",
    mixinStandardHelpOptions = true,
    versionProvider = HermesVersionProvider.class,
    subcommands = {NewCommand.class, DoctorCommand.class, StudioCommand.class})
public final class HermesCli implements Runnable {

  @Option(
      names = {"-V", "--version"},
      versionHelp = true,
      description = "Print version and exit")
  boolean versionRequested;

  @Override
  public void run() {
    CommandLine.usage(this, System.out);
  }

  public static void main(String[] args) {
    int exit = new CommandLine(new HermesCli()).execute(args);
    System.exit(exit);
  }
}
