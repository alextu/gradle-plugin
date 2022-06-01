package hudson.plugins.gradle;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.EnvironmentContributor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.actions.WorkspaceAction;

import javax.annotation.Nonnull;
import java.io.IOException;

@Extension
public class MavenEnvContrib extends EnvironmentContributor {

    @Override
    public void buildEnvironmentFor(@Nonnull Job j, @Nonnull EnvVars envs, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        super.buildEnvironmentFor(j, envs, listener);
    }

    @Override
    public void buildEnvironmentFor(@Nonnull Run r, @Nonnull EnvVars envs, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        System.out.println("In buildEnv: " + r.getCharacteristicEnvVars());
        envs.put("MAVEN_OPTS","-Dmaven.ext.class.path=/" + MavenGeComputerListener.GE_MVN_LIB);
        super.buildEnvironmentFor(r, envs, listener);
    }
}
