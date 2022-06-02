package hudson.plugins.gradle;

import hudson.EnvVars;
import hudson.Platform;
import jenkins.security.MasterToSlaveCallable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class EnvInjectMasterEnvVarsSetter extends MasterToSlaveCallable<Void, RuntimeException> {

    private EnvVars enVars;

    public EnvInjectMasterEnvVarsSetter(EnvVars enVars) {
        this.enVars = enVars;
    }

    @Override
    public Void call() {
        try {
            Field platformField = EnvVars.class.getDeclaredField("platform");
            platformField.setAccessible(true);
            platformField.set(enVars, Platform.current());
            Field masterEnvVarsFiled = EnvVars.class.getDeclaredField("masterEnvVars");
            masterEnvVarsFiled.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(masterEnvVarsFiled, masterEnvVarsFiled.getModifiers() & ~Modifier.FINAL);
            masterEnvVarsFiled.set(null, enVars);
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        } catch (NoSuchFieldException nsfe) {
            throw new RuntimeException(nsfe);
        }

        return null;
    }

}
