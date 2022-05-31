package hudson.plugins.gradle

import hudson.tasks.Maven
import jenkins.model.Jenkins
import jenkins.mvn.DefaultGlobalSettingsProvider
import jenkins.mvn.DefaultSettingsProvider
import jenkins.mvn.GlobalMavenConfig
import jenkins.plugins.git.GitSampleRepoRule
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.ToolInstallations

import java.nio.file.Files

class WithMavenGraphListenerIntegrationTest extends AbstractIntegrationTest {

    @Rule
    public GitSampleRepoRule gitRepoRule = new GitSampleRepoRule();

    def 'run listener on pipeline build'() {
        given:
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
        node {
            git(\$/$gitRepoRule/\$)
            withMaven(maven: '$mavenInstallationName') {
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
