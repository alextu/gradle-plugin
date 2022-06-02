package hudson.plugins.gradle;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class BuildScanInjectionGradleImpl implements BuildScanInjection {

  private static final Logger LOGGER = Logger.getLogger(BuildScanInjectionGradleImpl.class.getName());

  private static final String JENKINSGRADLEPLUGIN_BUILD_SCAN_OVERRIDE_HOME = "JENKINSGRADLEPLUGIN_BUILD_SCAN_OVERRIDE_HOME";
  private static final String RESOURCE_INIT_SCRIPT_GRADLE = "scripts/init-script.gradle";
  private static final String INIT_DIR = "init.d";
  private static final String GRADLE_DIR = ".gradle";
  private static final String GRADLE_INIT_FILE = "init-build-scan.gradle";

  @Override
  public void process(VirtualChannel channel, EnvVars envGlobal, EnvVars envComputer) {
    try {
      String initScriptDirectory = getInitScriptDirectory(envComputer);

      if (!isEnabled(envGlobal)) {
        removeInitScript(channel, initScriptDirectory);
      } else {
        copyInitScript(channel, initScriptDirectory);
      }
    } catch (IllegalStateException e) {
      LOGGER.warning("Error: " + e.getMessage());
    }
  }

  private String getInitScriptDirectory(EnvVars envComputer) {
    String homeOverride = getEnv(envComputer, JENKINSGRADLEPLUGIN_BUILD_SCAN_OVERRIDE_HOME);
    if (homeOverride != null) {
      return homeOverride + "/" + GRADLE_DIR + "/" + INIT_DIR;
    } else {
      return getEnv(envComputer, "HOME") + "/" + GRADLE_DIR + "/" + INIT_DIR;
    }
  }

  private void copyInitScript(VirtualChannel channel, String initScriptDirectory) {
    try {
      FilePath gradleInitScriptFile = getInitScriptFile(channel, initScriptDirectory);
      if (!gradleInitScriptFile.exists()) {
        FilePath gradleInitScriptDirectory = new FilePath(channel, initScriptDirectory);
        if (!gradleInitScriptDirectory.exists()) {
          LOGGER.fine("create init script directory");
          gradleInitScriptDirectory.mkdirs();
        }

        LOGGER.fine("copy init script file");
        gradleInitScriptFile.copyFrom(
                Objects.requireNonNull(BuildScanInjectionListener.class.getClassLoader().getResourceAsStream(RESOURCE_INIT_SCRIPT_GRADLE))
        );
      }
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  private void removeInitScript(VirtualChannel channel, String initScriptDirectory) {
    try {
      FilePath gradleInitScriptFile = getInitScriptFile(channel, initScriptDirectory);
      if (gradleInitScriptFile.exists()) {
        LOGGER.fine("delete init script file");
        if (!gradleInitScriptFile.delete()) {
          throw new IllegalStateException("Error while deleting init script");
        }
      }
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  private FilePath getInitScriptFile(VirtualChannel channel, String initScriptDirectory) {
    if (initScriptDirectory == null) {
      throw new IllegalStateException("init script directory is null");
    }
    return new FilePath(channel, initScriptDirectory + "/" + GRADLE_INIT_FILE);
  }

}
