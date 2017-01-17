package edu.iu.km;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import edu.iu.fileformat.MultiFileInputFormat;

public class KMeansMapCollective  extends Configured implements Tool {

	public static void main(String[] argv) throws Exception {
		int res = ToolRunner.run(new Configuration(), new KMeansMapCollective(), argv);
		System.exit(res);
	}


	@Override
	public int run(String[] args) throws Exception {
		//keep this unchanged.
		if (args.length < 6) {
			System.err.println("Usage: KMeansMapCollective <numOfDataPoints> <num of Centroids> "
					+ "<size of vector> <number of map tasks> <number of iteration> <workDir> <localDir>");
			ToolRunner.printGenericCommandUsage(System.err);
				return -1;
		}

		/*
		 * Generate data randomly
		 * Generate initial centroids
		 * Configure jobs
		 *   **for inputFormatClass: use job.setInputFormatClass(MultiFileInputFormat.class);
		 * Launch jobs
		 */


		// The number of data points over which clusters and centroids should be computed
		int numOfDataPoints = Integer.parseInt(args[0]);
		// The number of centroids that need to be computed
		int numCentroids = Integer.parseInt(args[1]);
		// This is the size of each data points (ie. the number of values in each data point)
		int sizeOfVector = Integer.parseInt(args[2]);
		// This is the number of map tasks that should be used to compute the centroids and
		// the clusters
		int numMapTasks = Integer.parseInt(args[3]);
		// This is the numebr of iteration kMeans needs to be run for
		int numIterations = Integer.parseInt(args[4]);
		// This is where the data is written in the distributed file system
		String workDir = args[5];
		// This is where the data is written locally before being transferred to the
		// distributed file system
		String localDir = args[6];

		for(String arg: args){
			System.out.print(arg+";");
		}
		System.out.println();

		launch(numOfDataPoints, numCentroids, sizeOfVector, numIterations, numMapTasks, workDir, localDir);

		System.out.println("Harp KMean Completed");
		return 0;
	}

	void launch(int numOfDataPoints, int numCentroids, int sizeOfVector, int numIterations, int numMapTasks, String workDir, String localDir)
			throws IOException, URISyntaxException, InterruptedException, ExecutionException, ClassNotFoundException {

			Configuration configuration = getConf();
			Path workDirPath = new Path(workDir);
			FileSystem fs = FileSystem.get(configuration);
			Path dataDir = new Path(workDirPath, "data");
			Path outDir = new Path(workDirPath, "out");
			if (fs.exists(outDir)) {
				fs.delete(outDir, true);
			}
			fs.mkdirs(outDir);

			System.out.println("Generate data.");
			Utils.generateData(numOfDataPoints, sizeOfVector, numMapTasks, fs, localDir, dataDir);
			// This will generate the initial centroids. The initial centroids are stored in teh configuration
			// with the key "centroids". The values is a string where the centroids are separated by newlines
			// and each value in the centroid is separated by a space
			Utils.generateInitialCentroids(numCentroids, sizeOfVector, configuration);
			// Start tracking the time so that the performance can be analzed
			long startTime = System.currentTimeMillis();

			runKMeansAllReduce(numOfDataPoints, sizeOfVector, numIterations, numMapTasks, configuration, workDirPath, dataDir, outDir);
			long endTime = System.currentTimeMillis();
			System.out.println("Total Harp KMeans Execution Time: "+ (endTime - startTime));
	}

	private void runKMeansAllReduce(int numOfDataPoints, int vectorSize, int numIterations, int numMapTasks, Configuration configuration,
			Path workDirPath, Path dataDir, Path outDir) throws IOException,URISyntaxException, InterruptedException,ClassNotFoundException {

			System.out.println("Starting Job");
			boolean jobSuccess = true;
			int jobRetryCount = 0;

			do {
				// ----------------------------------------------------------------------
				Job kmeansJob = configureKMeansJob(numOfDataPoints, vectorSize, numIterations, numMapTasks, configuration, workDirPath, dataDir, outDir);

				jobSuccess = kmeansJob.waitForCompletion(true);

				if (!jobSuccess) {
					System.out.println("KMeans Job failed. ");
					jobRetryCount++;
					if (jobRetryCount == 3) {
						break;
					}
				}else{
					break;
				}
			} while (true);
		}

	private Job configureKMeansJob(int numOfDataPoints, int vectorSize,
			int numIterations, int numMapTasks, Configuration configuration,Path workDirPath, Path dataDir,
			Path outDir) throws IOException, URISyntaxException, InterruptedException, ClassNotFoundException {


			Job job = Job.getInstance(configuration, "kmean_job");
			Configuration jobConfig = job.getConfiguration();
			Path jobOutDir = new Path(outDir, "kmean_out");
			FileSystem fs = FileSystem.get(configuration);
			if (fs.exists(jobOutDir)) {
				fs.delete(jobOutDir, true);
			}
			FileInputFormat.setInputPaths(job, dataDir);
			FileOutputFormat.setOutputPath(job, jobOutDir);
			job.setInputFormatClass(MultiFileInputFormat.class);
			job.setJarByClass(KMeansMapCollective.class);
			job.setMapperClass(KMeansMapper.class);
			JobConf jobConf = (JobConf) job.getConfiguration();
			jobConf.set("mapreduce.framework.name", "map-collective");
			jobConf.setNumMapTasks(numMapTasks);
			jobConf.setInt("mapreduce.job.max.split.locations", 10000);
			job.setNumReduceTasks(0);
			jobConfig.setInt(KMeansConstants.VECTOR_SIZE,vectorSize);			  	
			jobConfig.setInt(KMeansConstants.NUM_ITERATIONS,numIterations);
			jobConfig.setStrings(KMeansConstants.CFILE, configuration.getStrings(KMeansConstants.CFILE));
			return job;
	}
}
