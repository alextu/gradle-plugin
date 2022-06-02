package hudson.plugins.gradle;

import hudson.EnvVars;
import hudson.remoting.VirtualChannel;

public interface BuildScanInjection {

  String JENKINSGRADLEPLUGIN_BUILD_SCAN_INJECTION = "JENKINSGRADLEPLUGIN_BUILD_SCAN_INJECTION";

  default String getEnv(EnvVars env, String key) {
    return env != null ? env.get(key) : null;
  }

  default boolean isEnabled(EnvVars env) {
    return Boolean.parseBoolean(getEnv(env, JENKINSGRADLEPLUGIN_BUILD_SCAN_INJECTION));
  }

  void process(VirtualChannel channel, EnvVars envGlobal, EnvVars envComputer);
}
