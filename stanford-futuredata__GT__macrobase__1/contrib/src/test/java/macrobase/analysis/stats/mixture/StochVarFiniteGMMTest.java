package macrobase.analysis.stats.mixture;

import macrobase.analysis.stats.distribution.MultivariateNormal;
import macrobase.conf.MacroBaseConf;
import macrobase.datamodel.Datum;
import macrobase.ingest.CSVIngester;
import macrobase.util.Drainer;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static junit.framework.TestCase.assertEquals;

public class StochVarFiniteGMMTest {
	private static final Logger log = LoggerFactory.getLogger(StochVarFiniteGMMTest.class);

	@Test
	/**
	 * Tests Bayesian Gaussian Mixture Model on a three well separated clusters.
	 */
	public void bivariateWellSeparatedNormalTest() throws Exception {
		MacroBaseConf conf = new MacroBaseConf().set(MacroBaseConf.RANDOM_SEED, 44)
				.set(MacroBaseConf.TRANSFORM_TYPE, "SVI_GMM").set(GMMConf.SVI_FORGETTING_RATE, 0.01)
				.set(GMMConf.NUM_MIXTURES, 3)
				.set(GMMConf.MIXTURE_CENTERS_FILE, "src/test/resources/data/3gaussians-700.points-centers.json")
				.set(MacroBaseConf.DATA_LOADER_TYPE, "CSV_LOADER")
				.set(MacroBaseConf.CSV_COMPRESSION, CSVIngester.Compression.GZIP)
				.set(MacroBaseConf.CSV_INPUT_FILE, "src/test/resources/data/3gaussians-700points.csv.gz")
				.set(MacroBaseConf.METRICS, "XX, YY").set(MacroBaseConf.ATTRIBUTES, "");
		List<Datum> data = Drainer.drainIngest(conf);
		assertEquals(700, data.size());

		StochVarFiniteGMM finiteGMM = new StochVarFiniteGMM(conf);
		List<RealVector> calculatedMeans;

		// Make sure we have 3 clusters. Sometimes initialization is not great.
		finiteGMM.train(data);
		log.debug("finitesh training");

		calculatedMeans = finiteGMM.getClusterCenters();
		List<RealMatrix> calculatedCovariances = finiteGMM.getClusterCovariances();

		double[][] clusterMeans = { { 2, 11 }, { 1, 1 }, { 10, 3 }, };
		List<RealVector> vectorClusterMeans = new ArrayList<>(3);
		for (int k = 0; k < 3; k++) {
			vectorClusterMeans.add(new ArrayRealVector(clusterMeans[k]));
		}
		double[][][] clusterCovariances = { { { 0.5, 0.4 }, { 0.4, 0.5 } }, { { 0.3, 0 }, { 0, 0.6 } },
				{ { 0.9, 0.2 }, { 0.2, 0.3 } }, };

		ExpectMaxGMM gmm = new ExpectMaxGMM(conf);
		gmm.train(data);
		List<RealVector> emMeans = gmm.getClusterCenters();
		List<RealMatrix> emCovariances = gmm.getClusterCovariances();

		for (int i = 0; i < 3; i++) {
			boolean identified = false;
			for (int j = 0; j < 3; j++) {
				if (calculatedMeans.get(i).getDistance(vectorClusterMeans.get(j)) < 0.1) {
					for (int p = 0; p < 2; p++) {
						for (int q = 0; q < 2; q++) {
							// Make sure covariance is in the ballpark. Since we only had 700 points.
							assertEquals(clusterCovariances[j][p][q], calculatedCovariances.get(i).getEntry(p, q), 0.6);
						}
					}
					identified = true;
					break;
				}
			}
			assertEquals(true, identified);
			for (int z = 0; z < 3; z++) {
				if (emMeans.get(z).getDistance(calculatedMeans.get(i)) < 0.1) {
					for (int p = 0; p < 2; p++) {
						for (int q = 0; q < 2; q++) {
							// Make sure we have a closer estimate to EM algorithm means
							assertEquals(emCovariances.get(z).getEntry(p, q),
									calculatedCovariances.get(i).getEntry(p, q), 0.1);
						}
					}
					break;
				}
			}
		}

		// Make sure centers belong to only one cluster.
		double[] maxProbas = { 0, 0, 0 };
		for (int i = 0; i < 3; i++) {
			double[] probas = finiteGMM
					.getClusterProbabilities(new Datum(new ArrayList<Integer>(), vectorClusterMeans.get(i)));
			for (int j = 0; j < 3; j++) {
				maxProbas[j] = Math.max(probas[j], maxProbas[j]);
			}
		}
		for (int j = 0; j < 3; j++) {
			assertEquals(maxProbas[j], 1, 0.01);
		}
	}

	@Test
	/**
	 * Tests Gaussian Mixture Model on a three not so well separated clusters.
	 */
	public void bivariateOkSeparatedNormalTest() throws Exception {
		MacroBaseConf conf = new MacroBaseConf().set(MacroBaseConf.RANDOM_SEED, 4)
				.set(GMMConf.SVI_FORGETTING_RATE, 0.01).set(MacroBaseConf.TRANSFORM_TYPE, "SVI_GMM")
				.set(GMMConf.MAX_ITERATIONS_TO_CONVERGE, 20).set(GMMConf.NUM_MIXTURES, 3)
				.set(MacroBaseConf.DATA_LOADER_TYPE, "CSV_LOADER")
				.set(MacroBaseConf.CSV_COMPRESSION, CSVIngester.Compression.GZIP)
				.set(MacroBaseConf.CSV_INPUT_FILE, "src/test/resources/data/3gaussians-7000points.csv.gz")
				.set(MacroBaseConf.METRICS, "XX, YY").set(MacroBaseConf.ATTRIBUTES, "");
		List<Datum> data = Drainer.drainIngest(conf);
		int totalPoints = 7000;
		assertEquals(totalPoints, data.size());

		double[][] clusterMeans = { { 1.5, 2 }, { 2, 0 }, { 4.5, 1 }, };
		double[] clusterWeights = { 2000, 3000, 2000, };
		List<RealVector> vectorClusterMeans = new ArrayList<>(3);
		for (int k = 0; k < 3; k++) {
			vectorClusterMeans.add(new ArrayRealVector(clusterMeans[k]));
		}
		double[][][] clusterCovariances = { { { 0.5, 0.4 }, { 0.4, 0.5 } }, { { 0.3, 0 }, { 0, 0.6 } },
				{ { 0.9, 0.2 }, { 0.2, 0.3 } }, };

		StochVarFiniteGMM finiteGMM = new StochVarFiniteGMM(conf);
		finiteGMM.train(data);

		List<MultivariateNormal> normals = new ArrayList<>(3);
		for (int i = 0; i < 3; i++) {
			normals.add(new MultivariateNormal(vectorClusterMeans.get(i), new BlockRealMatrix(clusterCovariances[i])));
		}

		Random rand = conf.getRandom();
		for (int i = 0; i < 10; i++) {
			Datum d = data.get(rand.nextInt(totalPoints));
			double density = 0;
			for (int j = 0; j < 3; j++) {
				density += clusterWeights[j] / totalPoints * normals.get(j).density(d.metrics());
			}
			// Finite Model takes longer to converge, and since we are limiting num
			// iterations, take a conservative limit on deviation.
			assertEquals(density, Math.exp(finiteGMM.score(d)), 0.06);
		}

	}
}
