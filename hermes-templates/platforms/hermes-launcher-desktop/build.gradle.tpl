buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    classpath "io.github.fourlastor:construo:${findProperty('construoVersion') ?: '2.1.0'}"
  }
}

plugins {
  id 'application'
}

apply plugin: 'io.github.fourlastor.construo'

// Construo jlink must match the JDK used for runtime images (17); game bytecode stays Java 11.
java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

tasks.withType(JavaCompile).configureEach {
  options.release = 11
}

import io.github.fourlastor.construo.Target
import io.github.fourlastor.construo.task.jvm.CreateRuntimeImageTask

def construoName = providers.provider { findProperty('hermesConstruoName') ?: 'HermesGame' }
def construoHumanName = providers.provider { findProperty('hermesConstruoHumanName') ?: construoName.get() }
def construoBundleId = providers.provider { findProperty('hermesConstruoBundleId') ?: 'dev.hermes.game' }
def projectVersion = providers.provider { findProperty('hermesProjectVersion') ?: '0.1.0' }
def macIconPath = providers.provider { findProperty('hermesMacIcon') }
def winIconPath = providers.provider { findProperty('hermesWinIcon') }

dependencies {
  {{hermesCoreDependency}}
  {{gameDependency}}
  implementation "com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion"
  implementation "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"

  constraints {
    implementation("org.lwjgl:lwjgl:$lwjgl3Version")
    implementation("org.lwjgl:lwjgl-glfw:$lwjgl3Version")
    implementation("org.lwjgl:lwjgl-jemalloc:$lwjgl3Version")
    implementation("org.lwjgl:lwjgl-openal:$lwjgl3Version")
    implementation("org.lwjgl:lwjgl-opengl:$lwjgl3Version")
    implementation("org.lwjgl:lwjgl-stb:$lwjgl3Version")
  }
}

application {
  mainClass = 'dev.hermes.launcher.desktop.Lwjgl3Launcher'
  applicationName = construoName.get()
}

def os = System.properties['os.name'].toLowerCase(Locale.ROOT)

tasks.named('run', JavaExec).configure {
  if (os.contains('mac')) {
    jvmArgs('-XstartOnFirstThread')
  }
}

tasks.withType(JavaExec).configureEach {
  environment('__GL_THREADED_OPTIMIZATIONS', '0')
}

jar {
  archiveFileName.set(construoName.map { n -> "${n}-${projectVersion.get()}.jar" })
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  dependsOn configurations.runtimeClasspath
  from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
  exclude('META-INF/INDEX.LIST', 'META-INF/*.SF', 'META-INF/*.DSA', 'META-INF/*.RSA')
  dependencies {
    exclude('META-INF/INDEX.LIST', 'META-INF/maven/**')
  }
  manifest {
    attributes 'Main-Class': application.mainClass, 'Enable-Native-Access': 'ALL-UNNAMED', 'Multi-Release': 'true'
  }
  doLast {
    file(archiveFile).setExecutable(true, false)
  }
}

construo {
  name.set(construoName)
  humanName.set(construoHumanName)
  jlink {
    guessModulesFromJar.set(false)
    modules.addAll('java.base', 'java.management', 'java.desktop', 'jdk.unsupported')
  }
  targets.configure {
    register('linuxX64', Target.Linux) {
      architecture.set(Target.Architecture.X86_64)
      jdkUrl.set('https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.18%2B8/OpenJDK17U-jdk_x64_linux_hotspot_17.0.18_8.tar.gz')
    }
    register('macM1', Target.MacOs) {
      architecture.set(Target.Architecture.AARCH64)
      jdkUrl.set('https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.18%2B8/OpenJDK17U-jdk_aarch64_mac_hotspot_17.0.18_8.tar.gz')
      identifier.set(construoBundleId)
      macIcon.set(macIconPath.map { project.layout.projectDirectory.file(it) })
    }
    register('macX64', Target.MacOs) {
      architecture.set(Target.Architecture.X86_64)
      jdkUrl.set('https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.18%2B8/OpenJDK17U-jdk_x64_mac_hotspot_17.0.18_8.tar.gz')
      identifier.set(construoBundleId)
      macIcon.set(macIconPath.map { project.layout.projectDirectory.file(it) })
    }
    register('winX64', Target.Windows) {
      architecture.set(Target.Architecture.X86_64)
      jdkUrl.set('https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.18%2B8/OpenJDK17U-jdk_x64_windows_hotspot_17.0.18_8.zip')
      icon.set(winIconPath.map { project.layout.projectDirectory.file(it) })
    }
  }
}

// Construo defaults jlink to java.home; use the downloaded JDK so versions match target jmods.
tasks.withType(CreateRuntimeImageTask).configureEach { CreateRuntimeImageTask task ->
  String target = task.name.substring('createRuntimeImage'.length())
  String targetKey = target.substring(0, 1).toLowerCase(Locale.ROOT) + target.substring(1)
  task.dependsOn("downloadJdk${target}", "unzipJdk${target}")
  task.jdkRoot.set(
      tasks.named("unzipJdk${target}").map {
        File dir = layout.buildDirectory.dir("construo/jdk/${targetKey}").get().asFile
        File jlink =
            fileTree(dir).matching { include '**/bin/jlink', '**/bin/jlink.exe' }.files.find { it?.isFile() }
        if (jlink == null) {
          throw new GradleException("No jlink under ${dir} (run unzipJdk${target} first)")
        }
        layout.projectDirectory.dir(jlink.parentFile.parentFile.absolutePath)
      })
}

tasks.withType(JavaExec).configureEach {
  environment('__GL_THREADED_OPTIMIZATIONS', '0')
}
