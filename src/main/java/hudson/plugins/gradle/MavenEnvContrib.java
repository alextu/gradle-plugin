package hudson.plugins.gradle;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;

import javax.annotation.Nonnull;
import java.io.IOException;

import static hudson.plugins.gradle.MavenGeComputerListener.GE_MVN_LIB_PATH;

@Extension
public class MavenEnvContrib extends EnvironmentContributor {

    @Override
    public void buildEnvironmentFor(@Nonnull Job j, @Nonnull EnvVars envs, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        super.buildEnvironmentFor(j, envs, listener);
    }

    @Override
    public void buildEnvironmentFor(@Nonnull Run r, @Nonnull EnvVars envs, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        envs.put("MAVEN_OPTS", "-Dmaven.ext.class.path=" + GE_MVN_LIB_PATH);
        super.buildEnvironmentFor(r, envs, listener);
    }
}
