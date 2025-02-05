package com.integration.demo.it;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

@Testcontainers
 class DataBaseServiceIT {
	
	MountableFile warFile = MountableFile.forHostPath(
            Paths.get("build","libs","demo.war").toAbsolutePath(), 0777);
	
	static Network NET = Network.newNetwork();
	
	@Container
	public static final MariaDBContainer<?> MARIA_DB_CONTAINER = new MariaDBContainer<>("mariadb:10.6")
		.withExposedPorts(3306).withDatabaseName("Integration")
		.withPassword("mypass")
		.withNetwork(NET)
		.withNetworkAliases("mysql")
			.waitingFor(new LogMessageWaitStrategy().withRegEx(".*database system is ready to accept connections.*\\s")
					.withTimes(2).withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS)))
	 .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
		        cmd.getHostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(3306), new ExposedPort(3306)))));
	
	
	
	
	GenericContainer<?> microContainer = new GenericContainer<>("tomcat:8.5")
		    .withExposedPorts(8080)
		    .dependsOn(MARIA_DB_CONTAINER)
		    .withNetwork(NET)
		    .withCopyFileToContainer(warFile, "/usr/local/tomcat/webapps/demo.war")
		    .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
		        cmd.getHostConfig().withPortBindings(
		            new PortBinding(Ports.Binding.bindPort(8080), ExposedPort.tcp(8080))
		        )
		    ));
	
	
	  @Test
	   void testCheckContainerIsRunning() throws InterruptedException{
		  microContainer.start();
		  System.out.println(microContainer.getLogs());
		  String url = "http://" + microContainer.getHost() + ":" + microContainer.getMappedPort(8080) + "/demo/";
		    System.out.println("Testing URL: " + url);
	     assertTrue(microContainer.isRunning());
	  }
}
