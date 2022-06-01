package hudson.plugins.gradle;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;

import java.io.IOException;

@Extension
public class MavenGeComputerListener extends ComputerListener {

    public static final String GE_MVN_LIB_NAME = "gradle-enterprise-maven-extension-1.14.1.jar";
    public static final String GE_MVN_LIB_PATH = "/tmp/gradle/" + GE_MVN_LIB_NAME;

    @Override
    public void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
        // TODO: install GE in known location
        FilePath lib = c.getNode().createPath(GE_MVN_LIB_PATH);
        lib.copyFrom(getClass().getResourceAsStream(GE_MVN_LIB_NAME));
        super.onOnline(c, listener);
    }

    @Override
    public void onConfigurationChange() {
        super.onConfigurationChange();
    }
}
