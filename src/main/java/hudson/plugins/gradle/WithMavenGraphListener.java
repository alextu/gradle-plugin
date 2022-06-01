package hudson.plugins.gradle;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.actions.WorkspaceAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graph.StepNode;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.support.actions.EnvironmentAction;

import java.io.IOException;

//@Extension
public class WithMavenGraphListener implements GraphListener {
    private FilePath currentWorkspace = null;

    @Override
    public void onNewHead(FlowNode node) {
        if (node instanceof StepStartNode) {
            WorkspaceAction action = node.getAction(WorkspaceAction.class);
            if (action != null && action.getWorkspace() != null && currentWorkspace == null) {
                // TODO not threadsafe ?
                currentWorkspace = action.getWorkspace();
                System.out.println("In step start " + currentWorkspace);
                // TODO: inject env var here ? how ?
                EnvironmentAction environmentAction = node.getAction(EnvironmentAction.class);
                System.out.println("Env action: " +environmentAction);
                try {
                    EnvVars environment = environmentAction.getEnvironment();
                    System.out.println("Env: " + environment);
                    environment.put("MAVEN_OPTS", "-Dmaven.ext.class.path=/tmp/gradle-enterprise-maven-extension-1.14.1.jar");
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // EnvVars mavenOpts = new EnvVars("MAVEN_OPTS", "-Dmaven.ext.class.path=/tmp/gradle-enterprise-maven-extension-1.14.1.jar");
            }
            StepDescriptor descriptor = ((StepNode) node).getDescriptor();
            System.out.println("Node id:" + node.getId());
            System.out.println("Node class:" + node.getClass());
            System.out.println("Node display name:" + node.getDisplayName());
            // Note that we could inject it at a higher level, if we think it's best
            if (descriptor != null && "withMaven".equals(descriptor.getFunctionName())) {
                // TODO: executed twice: "Provide Maven environment : Start" and "Provide Maven environment : Body Start"
                System.out.println("withMaven workspace: " + currentWorkspace);
                injectExtensionFiles(currentWorkspace);
            }
        }
    }

    // TODO: might not work with remote agents
    void injectExtensionFiles(FilePath workspace) {
        try {
            workspace.child(".mvn").mkdirs();
            workspace.child( ".mvn/extensions.xml").copyFrom(getClass().getResourceAsStream("extensions.xml"));
            workspace.child( ".mvn/gradle-enterprise.xml").copyFrom(getClass().getResourceAsStream("gradle-enterprise.xml"));
            System.out.println("ls : " + workspace.listDirectories());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
