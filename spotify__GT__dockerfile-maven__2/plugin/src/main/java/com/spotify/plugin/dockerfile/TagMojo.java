package com.spotify.plugin.dockerfile;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.text.MessageFormat;

@Mojo(name = "tag", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true)
public class TagMojo extends AbstractDockerMojo {

	/**
	 * The repository to put the built image into, for example <tt>spotify/foo</tt>.  You should also
	 * set the <tt>tag</tt> parameter, otherwise the tag <tt>latest</tt> is used by default.
	 */
	@Parameter(property = "dockerfile.repository", required = true)
	private String repository;

	/**
	 * The tag to apply to the built image.
	 */
	@Parameter(property = "dockerfile.tag", defaultValue = "latest", required = true)
	private String tag;

	/**
	 * Whether to force re-assignment of an already assigned tag.
	 */
	@Parameter(property = "dockerfile.force", defaultValue = "true", required = true)
	private boolean force;

	/**
	 * Disables the tag goal; it becomes a no-op.
	 */
	@Parameter(property = "dockerfile.tag.skip", defaultValue = "false")
	private boolean skipTag;

	@Override
	protected void execute(DockerClient dockerClient) throws MojoExecutionException, MojoFailureException {
		final Log log = getLog();

		if (skipTag) {
			log.info("Skipping execution because 'dockerfile.tag.skip' is set");
			return;
		}

		final String imageId = readMetadata(Metadata.IMAGE_ID);
		final String imageName = formatImageName(repository, tag);

		final String message = MessageFormat.format("Tagging image {0} as {1}", imageId, imageName);
		log.info(message);

		try {
			dockerClient.tag(imageId, imageName, force);
		} catch (DockerException | InterruptedException e) {
			throw new MojoExecutionException("Could not tag Docker image", e);
		}

		writeImageInfo(repository, tag);

		writeMetadata(log);
	}
}
