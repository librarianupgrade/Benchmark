package com.spotify.plugin.dockerfile;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "push", defaultPhase = LifecyclePhase.DEPLOY, requiresProject = true, threadSafe = true)
public class PushMojo extends AbstractDockerMojo {

	/**
	 * The repository to put the built image into, for example <tt>spotify/foo</tt>.  You should also
	 * set the <tt>tag</tt> parameter, otherwise the tag <tt>latest</tt> is used by default.
	 */
	@Parameter(property = "dockerfile.repository")
	private String repository;

	/**
	 * The tag to apply to the built image.
	 */
	@Parameter(property = "dockerfile.tag")
	private String tag;

	/**
	 * Disables the push goal; it becomes a no-op.
	 */
	@Parameter(property = "dockerfile.push.skip", defaultValue = "false")
	private boolean skipPush;

	@Override
	protected void execute(DockerClient dockerClient) throws MojoExecutionException, MojoFailureException {
		final Log log = getLog();

		if (skipPush) {
			log.info("Skipping execution because 'dockerfile.push.skip' is set");
			return;
		}

		if (repository == null) {
			repository = readMetadata(Metadata.REPOSITORY);
		}

		// Do this hoop jumping so that the override order is correct
		if (tag == null) {
			tag = readMetadata(Metadata.TAG);
		}
		if (tag == null) {
			tag = "latest";
		}

		if (repository == null) {
			throw new MojoExecutionException("Can't push image; image repository not known "
					+ "(specify dockerfile.repository parameter, or run the tag goal before)");
		}

		try {
			dockerClient.push(formatImageName(repository, tag), LoggingProgressHandler.forLog(log, verbose));
		} catch (DockerException | InterruptedException e) {
			throw new MojoExecutionException("Could not push image", e);
		}
	}
}
