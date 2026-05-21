package dev.hermes.core;

import com.badlogic.gdx.Gdx;

/** Test-only libGDX bootstrap for classpath asset loading. */
public final class TestGdx {

  private TestGdx() {}

  public static void initClasspathFiles() {
    if (Gdx.files == null) {
      Gdx.files = new ClasspathFiles();
    }
  }
}
