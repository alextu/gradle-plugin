package hudson.plugins.gradle;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Executor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

//@Extension
public class MavenRunListener extends RunListener<Run> {

    @Override
    public void onStarted(Run run, TaskListener listener) {
        Executor executor = run.getExecutor();
        System.out.println("Executor: " + executor);
        if (executor != null) {
            try {
                FilePath currentWorkspace = executor.getCurrentWorkspace();
                System.out.println("Current workspace: " + currentWorkspace);
                // No workspa
                FilePath extJar = currentWorkspace.child("ext.jar");
                extJar.copyFrom(Files.newInputStream(Paths.get("/tmp/common-custom-user-data-maven-extension-1.10.1.jar")));
                EnvVars environment = run.getEnvironment(listener);
                String mavenOpts = environment.get("MAVEN_OPTS");
                String value = "-Dmaven.ext.class.path=" + extJar.getRemote();
                if (mavenOpts != null) {
                    value = mavenOpts + " " + value;
                }
                environment.put("MAVEN_OPTS", value);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        super.onStarted(run, listener);
    }

    @Override
    public void onFinalized(Run run) {
        Executor executor = run.getExecutor();
        System.out.println("Finalizing: " + executor);
        super.onFinalized(run);
    }
}
