package hudson.plugins.gradle;

import hudson.model.Action;
import hudson.model.InvisibleAction;

import javax.annotation.CheckForNull;

public class MavenGeEnvVarAction extends InvisibleAction {

    private final boolean geInstalled;

    public MavenGeEnvVarAction(boolean geInstalled) {
        this.geInstalled = geInstalled;
    }

    public boolean isGeInstalled() {
        return geInstalled;
    }
}
