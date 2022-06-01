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

import static hudson.plugins.gradle.MavenGeComputerListener.GE_MVN_LIB;

@Extension
public class MavenEnvContrib extends EnvironmentContributor {

    @Override
    public void buildEnvironmentFor(@Nonnull Job j, @Nonnull EnvVars envs, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        super.buildEnvironmentFor(j, envs, listener);
    }

    @Override
    public void buildEnvironmentFor(@Nonnull Run r, @Nonnull EnvVars envs, @Nonnull TaskListener listener) throws IOException, InterruptedException {
        System.out.println("In buildEnv: " + r.getCharacteristicEnvVars());
        final FilePath child = r.getExecutor().getOwner().getNode().getRootPath().child(GE_MVN_LIB);
        System.out.println(r.getExecutor().toString());
        System.out.println("Super excited whether this works!!" + child.getRemote());
        envs.put("MAVEN_OPTS","-Dmaven.ext.class.path=$GE_MAVEN_OPTS");
        envs.put("TEST_ENV","$HOME");
        super.buildEnvironmentFor(r, envs, listener);
    }
}
