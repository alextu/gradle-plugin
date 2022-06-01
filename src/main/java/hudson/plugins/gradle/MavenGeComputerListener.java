package hudson.plugins.gradle;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;

import java.io.IOException;

@Extension
public class MavenGeComputerListener extends ComputerListener {

    public static final String GE_MVN_LIB = "gradle-enterprise-maven-extension-1.14.1.jar";

    @Override
    public void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
        // TODO: install GE in known location
        FilePath lib = c.getNode().getRootPath().child(GE_MVN_LIB);
        System.out.println("in onOnline: " + lib);
        lib.copyFrom(getClass().getResourceAsStream(GE_MVN_LIB));
        super.onOnline(c, listener);
    }

    @Override
    public void onConfigurationChange() {
        super.onConfigurationChange();
    }
}
