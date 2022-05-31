package hudson.plugins.gradle;

import hudson.Extension;
import hudson.FilePath;
import org.jenkinsci.plugins.workflow.actions.WorkspaceAction;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graph.StepNode;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;

import java.io.IOException;

@Extension
public class WithMavenGraphListener implements GraphListener {
    private FilePath currentWorkspace = null;

    @Override
    public void onNewHead(FlowNode node) {
        if (node instanceof StepStartNode) {
            WorkspaceAction action = node.getAction(WorkspaceAction.class);
            if (action != null) {
                currentWorkspace = action.getWorkspace();
                System.out.println("In step start " + currentWorkspace);
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
