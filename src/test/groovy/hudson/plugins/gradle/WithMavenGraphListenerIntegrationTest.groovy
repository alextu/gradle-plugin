package hudson.plugins.gradle

import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import hudson.plugins.sshslaves.SSHLauncher
import hudson.slaves.DumbSlave
import hudson.slaves.RetentionStrategy
import hudson.tasks.Maven
import jenkins.model.Jenkins
import jenkins.mvn.DefaultGlobalSettingsProvider
import jenkins.mvn.DefaultSettingsProvider
import jenkins.mvn.GlobalMavenConfig
import jenkins.plugins.git.GitSampleRepoRule
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.test.acceptance.docker.DockerRule
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.ToolInstallations

import java.nio.file.Files

class WithMavenGraphListenerIntegrationTest extends AbstractIntegrationTest {

    private static final String SSH_CREDENTIALS_ID = "test";
    private static final String AGENT_NAME = "remote";
    private static final String SLAVE_BASE_PATH = "/home/test/slave";

    @Rule
    public GitSampleRepoRule gitRepoRule = new GitSampleRepoRule()

    @Rule
    public DockerRule<JavaGitContainer> javaGitContainerRule = new DockerRule<>(JavaGitContainer.class);

    def 'run listener on pipeline build'() {
        given:
        registerAgentForContainer(javaGitContainerRule.get())

        and:
        gitRepoRule.init()
        Files.copy(WithMavenGraphListenerIntegrationTest.class.getResourceAsStream("pom.xml"), gitRepoRule.getRoot().toPath().resolve("pom.xml"))
        gitRepoRule.git("add", "--all");
        gitRepoRule.git("commit", "--message=addFiles")
        def pipelineJob = j.createProject(WorkflowJob)

        def mavenInstallation = ToolInstallations.configureMaven35()
        Jenkins.get().getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(mavenInstallation);
        def mavenInstallationName = mavenInstallation.getName();

        GlobalMavenConfig globalMavenConfig = j.get(GlobalMavenConfig.class);
        globalMavenConfig.setGlobalSettingsProvider(new DefaultGlobalSettingsProvider());
        globalMavenConfig.setSettingsProvider(new DefaultSettingsProvider());
        pipelineJob.setDefinition(new CpsFlowDefinition("""
node {
   stage('Build') {
        node('$AGENT_NAME') {
            withMaven(maven: '$mavenInstallationName') {
                sh "ls /tmp/gradle"
                sh "env"
                sh "mvn package"
            }
        }
   }
}
""", false))

        when:
        def build = j.buildAndAssertSuccess(pipelineJob)

        then:
        println JenkinsRule.getLog(build)
    }

    private void registerAgentForContainer(SshdContainer container) throws Exception {
        addTestSshCredentials();
        registerAgentForSlaveContainer(container);
    }

    private void registerAgentForSlaveContainer(SshdContainer slaveContainer) throws Exception {
        SSHLauncher sshLauncher = new SSHLauncher(slaveContainer.ipBound(22), slaveContainer.port(22), SSH_CREDENTIALS_ID);

        DumbSlave agent = new DumbSlave(AGENT_NAME, SLAVE_BASE_PATH, sshLauncher);
        agent.setNumExecutors(1);
        agent.setRetentionStrategy(RetentionStrategy.INSTANCE);

        j.jenkins.addNode(agent);
    }

    private void addTestSshCredentials() {
        Credentials credentials = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, SSH_CREDENTIALS_ID, null, "test", "test");

        SystemCredentialsProvider.getInstance()
            .getDomainCredentialsMap()
            .put(Domain.global(), Collections.singletonList(credentials));
    }

//    def 'build scan is discovered from Maven build'() {
//        given:
//        def p = j.createFreeStyleProject()
//        p.buildersList.add(new CreateFileBuilder('pom.xml',
//            '''<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
//  <modelVersion>4.0.0</modelVersion>
//  <groupId>hudson.plugins.gradle</groupId>
//  <artifactId>maven-build-scan</artifactId>
//  <packaging>jar</packaging>
//  <version>1.0</version>
//
//  <properties>
//    <maven.compiler.source>1.8</maven.compiler.source>
//    <maven.compiler.target>1.8</maven.compiler.target>
//  </properties>
//
//</project>'''))
//        //p.buildersList.add(new CreateFileBuilder('.mvn/extensions.xml', buildScanExtension))
//        ///p.buildersList.add(new CreateFileBuilder('.mvn/gradle-enterprise.xml', gradleEnterpriseConfiguration))
//        def mavenInstallation = ToolInstallations.configureMaven35()
//        p.buildersList.add(new Maven('package', mavenInstallation.name, null, '', '', false, null, null))
//
//        when:
//        def build = j.buildAndAssertSuccess(p)
//
//        then:
//        println JenkinsRule.getLog(build)
//    }

}
