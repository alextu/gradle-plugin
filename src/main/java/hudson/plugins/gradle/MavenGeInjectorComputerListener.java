package hudson.plugins.gradle;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Extension
public class MavenGeInjectorComputerListener extends ComputerListener {

    private static final String GE_MVN_LIB_NAME = "gradle-enterprise-maven-extension-1.14.1.jar";
    private static final String CCUD_LIB_NAME = "common-custom-user-data-maven-extension-1.10.1.jar";
    // Maven system properties passed on the CLI to a Maven build
    private static final String GE_URL_MAVEN_PROPERTY = "gradle.enterprise.url";
    private static final String GE_ALLOW_UNTRUSTED_MAVEN_PROPERTY = "gradle.enterprise.allowUntrustedServer";
    // Environment variables set in Jenkins Global configuration
    private static final String INJECT_BUILD_SCAN_VAR = "JENKINSGRADLEPLUGIN_BUILD_SCAN_INJECTION";
    private static final String GE_ALLOW_UNTRUSTED_VAR = "JENKINSGRADLEPLUGIN_GRADLE_ENTERPRISE_ALLOW_UNTRUSTED_SERVER";
    private static final String GE_URL_VAR = "JENKINSGRADLEPLUGIN_GRADLE_ENTERPRISE_URL";

    @Override
    public void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
        if (c.getNode() == null) {
            return;
        }
        FilePath rootPath = c.getNode().getRootPath();
        if (getGlobalEnvVar(INJECT_BUILD_SCAN_VAR) != null) {
            if (rootPath != null) {
                String cp = constructExtClasspath(copyResourceToAgent(GE_MVN_LIB_NAME, rootPath), copyResourceToAgent(CCUD_LIB_NAME, rootPath));
                EnvVars envs = new EnvVars();
                injectSysPropInMavenOpts(rootPath, envs, "maven.ext.class.path", cp);

                if (getGlobalEnvVar(GE_ALLOW_UNTRUSTED_VAR) != null) {
                    injectSysPropInMavenOpts(rootPath, envs, GE_ALLOW_UNTRUSTED_MAVEN_PROPERTY, getGlobalEnvVar(GE_ALLOW_UNTRUSTED_VAR));
                }
                if (getGlobalEnvVar(GE_URL_VAR) != null) {
                    injectSysPropInMavenOpts(rootPath, envs, GE_URL_MAVEN_PROPERTY, getGlobalEnvVar(GE_URL_VAR));
                }
            }
        }
        super.onOnline(c, listener);
    }

    private String constructExtClasspath(FilePath...libs) {
        return Stream.of(libs).map(FilePath::getRemote).collect(Collectors.joining(":"));
    }

    private String getGlobalEnvVar(String varName) {
        EnvironmentVariablesNodeProperty envProperty = Jenkins.get().getGlobalNodeProperties()
            .get(EnvironmentVariablesNodeProperty.class);
        return envProperty.getEnvVars().get(varName);
    }

    private void injectSysPropInMavenOpts(FilePath rootPath, EnvVars envs, String sysProp, String value) throws IOException, InterruptedException {
        appendEnv(envs, "MAVEN_OPTS", "-D" + sysProp + "=" + value);
        rootPath.act(new EnvInjectMasterEnvVarsSetter(envs));
    }

    private FilePath copyResourceToAgent(String resourceName, FilePath rootPath) throws IOException, InterruptedException {
        FilePath lib = rootPath.createTempDir("ge", "lib").child(GE_MVN_LIB_NAME);
        InputStream libIs = getClass().getResourceAsStream(resourceName);
        if (libIs == null) {
            throw new IllegalStateException("Could not find resource: " + resourceName);
        }
        lib.copyFrom(libIs);
        return lib;
    }

    private void appendEnv(EnvVars envs, String envVar, String value) {
        String current = envs.get(envVar);
        if (current != null) {
            current = current + " " + value;
        } else {
            current = value;
        }
        envs.put(envVar, current);
    }

}
