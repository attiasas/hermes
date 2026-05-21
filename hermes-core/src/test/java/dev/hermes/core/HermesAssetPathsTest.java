package dev.hermes.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HermesAssetPathsTest {

  @BeforeAll
  static void initGdx() {
    if (Gdx.files == null) {
      Gdx.files = new ClasspathFiles();
    }
  }

  @Test
  void internal_findsPackagedScenePath() {
    assertTrue(HermesAssetPaths.internal("scenes/main.json").exists());
  }
}
