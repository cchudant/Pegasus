package fr.ftnl;

import java.net.InetSocketAddress;

import fr.ftnl.config.ConfigLoader;

/*
 * Will be populated by SnakeYAML.
 */
public class GlobalConfig {
  private final String prefix;
  private final String mongoURI;
  private final String database;
  private final InetSocketAddress prometheusHttpServer;

  /**
   * @return the prefix
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * @return the mongoURI
   */
  public String getMongoURI() {
    return mongoURI;
  }

  /**
   * @return the database
   */
  public String getDatabase() {
    return database;
  }

  /**
   * @return the prometheusHttpServer
   */
  public InetSocketAddress getPrometheusHttpServer() {
    return prometheusHttpServer;
  }

  /* Config loading */

  private GlobalConfig(Raw raw) {
    this.prefix = raw.prefix;
    this.mongoURI = raw.mongoURI;
    this.database = raw.database;

    // Check address validity
    String[] pair = raw.prometheusHttpServer.split(":");
    assert pair.length == 2 : new IllegalArgumentException("malformated host and port pair");
    String host = pair[0];
    int port = Integer.parseInt(pair[1]);

    this.prometheusHttpServer = new InetSocketAddress(host, port);
  }

  public static GlobalConfig load(FTNL main) {
    return new GlobalConfig(ConfigLoader.load(main, Raw.class, "ftnl.yml"));
  }

  static class Raw {
    public String prefix;
    public String mongoURI;
    public String database;
    public String prometheusHttpServer;
  }
}