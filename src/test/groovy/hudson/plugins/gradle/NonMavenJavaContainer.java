package hudson.plugins.gradle;

import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;

@DockerFixture(id = "non-maven-java", ports = {22, 8080})
public class NonMavenJavaContainer extends JavaContainer {
}
