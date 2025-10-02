package org.kergru.library.util;

import java.nio.file.*;

public class RealmPathResolver {

  public static Path resolveRealmImportDir() {
    // 1. Vorrang: explizit gesetzte System-Property (z. B. in Gradle CI)
    String configured = System.getProperty("realm.import.dir");
    if (configured != null) {
      Path path = Paths.get(configured).toAbsolutePath().normalize();
      if (Files.exists(path)) {
        return path;
      } else {
        throw new IllegalStateException("Configured realm.import.dir does not exist: " + path);
      }
    }

    // 2. Fallback: vom aktuellen Arbeitsverzeichnis aus
    Path cwd = Paths.get(System.getProperty("user.dir")).toAbsolutePath();

    Path fromCwd = cwd.resolve("docker/keycloak-init");
    if (Files.exists(fromCwd)) {
      return fromCwd;
    }

    // 3. Fallback: wenn Test aus einem Modul gestartet wird
    Path fromParent = cwd.resolve("../docker/keycloak-init").normalize();
    if (Files.exists(fromParent)) {
      return fromParent;
    }

    throw new IllegalStateException(
        "Realm import directory not found. Checked: " +
            cwd.resolve("docker/keycloak-init") + " and " +
            fromParent
    );
  }
}
