package com.google.cloud.tools.maven.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.AppEngineStandardStaging;
import com.google.cloud.tools.maven.AbstractSingleYamlDeployMojo;
import com.google.cloud.tools.maven.CloudSdkAppEngineFactory;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.Assert;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class SingleYamlStandradDeployTestHelper<M extends AbstractSingleYamlDeployMojo> extends ExternalResource {

	@Mock
	private AppEngineStandardStaging standardStagingMock;

	@Mock
	private AppEngineDeployment deploymentMock;

	@Mock
	private CloudSdkAppEngineFactory factoryMock;

	@InjectMocks
	protected M mojo;

	private TemporaryFolder temporaryFolder;

	public SingleYamlStandradDeployTestHelper(M mojo, TemporaryFolder temporaryFolder) {
		this.mojo = mojo;
		this.temporaryFolder = temporaryFolder;
	}

	@Override
	public void before() throws IOException {
		mojo.setStagingDirectory(temporaryFolder.newFolder("staging"));
		mojo.setSourceDirectory(temporaryFolder.newFolder("source"));
		MockitoAnnotations.initMocks(this);

		// create appengine-web.xml to mark it as standard environment
		File webInfDirectory = mojo.getSourceDirectory().toPath().resolve("WEB-INF").toFile();
		Assert.assertTrue(webInfDirectory.mkdir());
		File appengineWebXml = webInfDirectory.toPath().resolve("appengine-web.xml").toFile();
		Assert.assertTrue(appengineWebXml.createNewFile());
		Files.write("<appengine-web-app></appengine-web-app>", appengineWebXml, Charsets.UTF_8);
		when(factoryMock.standardStaging()).thenReturn(standardStagingMock);
		when(factoryMock.deployment()).thenReturn(deploymentMock);
	}

	@Override
	public void after() {
		verify(standardStagingMock).stageStandard(mojo);
		assertEquals(Paths.get(temporaryFolder.getRoot().getAbsolutePath(), "staging", "WEB-INF", "appengine-generated")
				.toString(), mojo.getAppEngineDirectory().getAbsolutePath());
	}

	public AppEngineDeployment getDeploymentMock() {
		return deploymentMock;
	}
}