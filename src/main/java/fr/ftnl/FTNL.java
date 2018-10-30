package fr.ftnl;

import java.io.IOException;
import java.nio.file.Path;

import javax.security.auth.login.LoginException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.ftnl.command.CommandManager;
import fr.ftnl.listener.ListenerManager;
import fr.ftnl.modules.LicorneModule;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

public class FTNL {
  private static final Logger LOG = LogManager.getLogger();
  private final String token;
  private final int[] shards;
  private final int shardTotal;
  private final Path configFolder;
  private final ListenerManager listenerManager;
  private final CommandManager commandManager;
  private final GlobalConfig config;
  private final MongoClient mongo;
  private final MongoDatabase database;

  public FTNL(String token, int[] shards, int shardTotal, Path configFolder) {
    this.token = token;
    this.shards = shards;
    this.shardTotal = shardTotal;
    this.configFolder = configFolder;

    this.listenerManager = new ListenerManager(this);
    this.commandManager = new CommandManager(this);
    
    this.config = GlobalConfig.load(this);

    this.mongo = MongoClients.create(config.getMongoURI());
    this.database = this.mongo.getDatabase(config.getDatabase());

    registerModule(new LicorneModule(this));
  }

  public void start() throws LoginException, IOException {
    JDABuilder shardBuilder = new JDABuilder(this.token);
    shardBuilder.addEventListener(this.listenerManager);
    shardBuilder.addEventListener(this.commandManager);

    for (int i : this.shards) {
      LOG.info("Starting shard [{} / {}]", i, shardTotal);
      JDA jda = shardBuilder.useSharding(i, this.shardTotal)
        .build();
      LOG.info(jda.getShardInfo());
    }

    LOG.info("Starting prometheus exporter HTTP server at {}", this.config.getPrometheusHttpServer());
    new HTTPServer(this.config.getPrometheusHttpServer(), CollectorRegistry.defaultRegistry);
  }

  /**
   * Register a module
   * 
   * @param module the module
   * @see ListenerManager#register(Object)
   * @see CommandManager#register(Object)
   */
  public void registerModule(Object module) {
    Class<?> clazz = module.getClass();
    LOG.info("Registering module {}", clazz.getCanonicalName());

    this.listenerManager.register(module);
    this.commandManager.register(module);
  }

  /**
   * @return the configFolder
   */
  public Path getConfigFolder() {
    return configFolder;
  }

  /**
   * @return the shards
   */
  public int[] getShards() {
    return shards;
  }

  /**
   * @return the shardTotal
   */
  public int getShardTotal() {
    return shardTotal;
  }

  /**
   * @return the listenerManager
   */
  public ListenerManager getListenerManager() {
    return listenerManager;
  }

  /**
   * @return the commandManager
   */
  public CommandManager getCommandManager() {
    return commandManager;
  }

  /**
   * @return the config
   */
  public GlobalConfig getConfig() {
    return config;
  }

  /**
   * @return the mongo
   */
  public MongoClient getMongo() {
    return mongo;
  }

  /**
   * @return the database
   */
  public MongoDatabase getDatabase() {
    return database;
  }
}