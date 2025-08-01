package com.google.cloud.tools.maven.util;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.AppEngineFlexibleStaging;
import com.google.cloud.tools.maven.AbstractSingleYamlDeployMojo;
import com.google.cloud.tools.maven.CloudSdkAppEngineFactory;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

public class SingleYamlFlexibleDeployTestHelper<M extends AbstractSingleYamlDeployMojo> extends ExternalResource {

	@Mock
	private AppEngineFlexibleStaging flexibleStagingMock;

	@Mock
	private AppEngineDeployment deploymentMock;

	@Mock
	private CloudSdkAppEngineFactory factoryMock;

	@InjectMocks
	protected M mojo;

	private TemporaryFolder temporaryFolder;

	public SingleYamlFlexibleDeployTestHelper(M mojo, TemporaryFolder temporaryFolder) {
		this.mojo = mojo;
		this.temporaryFolder = temporaryFolder;
	}

	@Override
	public void before() throws IOException {
		mojo.setStagingDirectory(temporaryFolder.newFolder("staging"));
		mojo.setSourceDirectory(temporaryFolder.newFolder("source"));
		MockitoAnnotations.initMocks(this);

		when(factoryMock.flexibleStaging()).thenReturn(flexibleStagingMock);
		when(factoryMock.deployment()).thenReturn(deploymentMock);
	}

	@Override
	public void after() {
		verify(flexibleStagingMock).stageFlexible(mojo);
	}

	public AppEngineDeployment getDeploymentMock() {
		return deploymentMock;
	}
}