package hudson.plugins.gradle;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.BuildStepListener;
import hudson.tasks.BuildStep;
import jenkins.YesNoMaybe;

@Extension(dynamicLoadable= YesNoMaybe.YES)
public class GlobalBuildStepListener extends BuildStepListener {
    @Override
    public void started(AbstractBuild build, BuildStep bs, BuildListener listener) {
        // TODO: only relevant for Freestyle projects
        System.out.println("IN STARTED BUILD STEP");
    }

    @Override
    public void finished(AbstractBuild build, BuildStep bs, BuildListener listener, boolean canContinue) {
        // TODO: only relevant for Freestyle projects
        System.out.println("IN FINISHED BUILD STEP");
    }
}
