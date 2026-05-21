package dev.hermes.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import com.badlogic.gdx.graphics.GL20;

/** Test-only libGDX bootstrap for classpath asset loading and mock GL shader compile. */
public final class TestGdx {

  private static volatile boolean glInitialized;

  private TestGdx() {}

  public static void initClasspathFiles() {
    if (Gdx.files == null) {
      Gdx.files = new ClasspathFiles();
    }
  }

  /** Initializes mock OpenGL so {@link com.badlogic.gdx.graphics.glutils.ShaderProgram} can compile in tests. */
  public static void initHeadlessGl() {
    synchronized (TestGdx.class) {
      if (Gdx.app == null) {
        new HeadlessApplication(new ApplicationAdapter() {});
      }
      if (!glInitialized) {
        GL20 gl = ShaderCompileGlMock.create();
        if (Gdx.graphics instanceof MockGraphics) {
          ((MockGraphics) Gdx.graphics).setGL20(gl);
        } else {
          MockGraphics graphics = new MockGraphics();
          graphics.setGL20(gl);
          Gdx.graphics = graphics;
        }
        Gdx.gl20 = gl;
        Gdx.gl = gl;
        glInitialized = true;
      }
      Gdx.files = new ClasspathFiles();
    }
  }
}
