package fr.ftnl.bootstrap;

import java.io.File;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.ftnl.FTNL;
import picocli.CommandLine;
import picocli.CommandLine.Option;

final class Bootstrap {
  private static final Logger LOG = LogManager.getLogger();
  private Bootstrap() {}

  private static class Arguments {
    @Option(names = {"-s", "--shards"}, split = ",", defaultValue = "0")
    int[] shards;
    @Option(names = {"-st", "--shard-total"}, defaultValue = "1")
    int shardTotal;
    @Option(names = "--config-folder", defaultValue = "./config")
    File configFolder;

    @Override
    public String toString() {
      return
        "{ shards: " + Arrays.toString(shards) +
        ", shardTotal: " + shardTotal +
        ", configFolder: " + configFolder +
        " }";

    }
  }

  public static void main(String... args) {
    LOG.traceEntry();
    Arguments a = CommandLine.populateCommand(new Arguments(), args);
    LOG.info("Starting with arguments: {}", a);
    
    try {
      new FTNL(System.getenv("DISCORD_TOKEN"), a.shards, a.shardTotal, a.configFolder.toPath())
        .start();
    } catch (Throwable t) {
      // GOTTA CATCH THEM ALL
      LOG.error("Uncaught error", t);
      LOG.error("Exiting...");
      System.exit(1);
    }
  }
}