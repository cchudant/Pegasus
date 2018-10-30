package fr.ftnl.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.yaml.snakeyaml.Yaml;

import fr.ftnl.FTNL;

public final class ConfigLoader {
  private ConfigLoader() {}

  public static <T> T load(FTNL main, Class<T> clazz, String fileName) {
    try {
      return _load(main, clazz, fileName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static <T> T _load(FTNL main, Class<T> clazz, String fileName) throws IOException {
    Path folder = main.getConfigFolder();
    Path file = folder.resolve(fileName);

    if (!Files.isRegularFile(file)) {
      Files.createDirectories(folder);
      _extractFromJar('/' + fileName, file);
    }

    Yaml yaml = new Yaml();
    try (InputStream in = Files.newInputStream(file)) {
      return yaml.loadAs(in, clazz);
    }
  }

  private static void _extractFromJar(String resource, Path file) throws IOException {
    try (InputStream in = ConfigLoader.class.getResourceAsStream(resource)) {
      Files.copy(in, file);
    }
  }
}