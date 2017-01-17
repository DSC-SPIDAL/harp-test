package edu.iu.mean;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import edu.iu.fileformat.MultiFileInputFormat;

public class MeanMapCollective  extends Configured implements Tool {

	public static void main(String[] argv) throws Exception {
		int res = ToolRunner.run(new Configuration(), new MeanMapCollective(), argv);
		System.exit(res);
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length < 4) {
			System.err.println("Usage: MeansMapCollective <numOfDataPoints> <size of vector> "
					+ "<number of map tasks> <workDir> <localDir>");			
			ToolRunner.printGenericCommandUsage(System.err);
				return -1;
		}

		int numOfDataPoints = Integer.parseInt(args[0]);
		int sizeOfVector = Integer.parseInt(args[1]);
		int numMapTasks = Integer.parseInt(args[2]);
		String workDir = args[3];
		String localDir = args[4];

		for(String arg: args){
			System.out.print(arg+";");
		}
		System.out.println();
		
		launch(numOfDataPoints, sizeOfVector, numMapTasks, workDir, localDir);
		System.out.println("Harp Mean Completed"); 
		return 0;
	}
	void launch(int numOfDataPoints, int sizeOfVector, int numMapTasks, String workDir, String localDir)
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
		
		long startTime = System.currentTimeMillis();
		
		runMeansAllReduce(numOfDataPoints, sizeOfVector, numMapTasks, configuration, workDirPath,
				dataDir, outDir);
		long endTime = System.currentTimeMillis();
		System.out.println("Total Harp Mean Execution Time: "+ (endTime - startTime));
	}
	
	
	private void runMeansAllReduce(int numOfDataPoints, int vectorSize, 
				int numMapTasks, Configuration configuration, 
			Path workDirPath, Path dataDir, Path outDir)
			throws IOException,URISyntaxException, InterruptedException,ClassNotFoundException {
			
		System.out.println("Starting Job");
		boolean jobSuccess = true;
		int jobRetryCount = 0;
		
		do {
			// ----------------------------------------------------------------------
			Job meansJob = configureMeansJob(numOfDataPoints, vectorSize, numMapTasks,
					configuration, workDirPath, dataDir, outDir);
			
			jobSuccess = meansJob.waitForCompletion(true);
			
			if (!jobSuccess) {
				System.out.println("Mean Job failed. ");
				jobRetryCount++;
				if (jobRetryCount == 3) {
					break;
				}
			}else{
				break;
			}
		} while (true);
	}
	
	private Job configureMeansJob(int numOfDataPoints, int vectorSize, 
			int numMapTasks, Configuration configuration,Path workDirPath, Path dataDir,
			Path outDir) throws IOException, URISyntaxException {
			
		Job job = Job.getInstance(configuration, "mean_job");
		Configuration jobConfig = job.getConfiguration();
		Path jobOutDir = new Path(outDir, "mean_out");
		FileSystem fs = FileSystem.get(configuration);
		if (fs.exists(jobOutDir)) {
			fs.delete(jobOutDir, true);
		}
		FileInputFormat.setInputPaths(job, dataDir);
		FileOutputFormat.setOutputPath(job, jobOutDir);
		job.setInputFormatClass(MultiFileInputFormat.class);
		job.setJarByClass(MeanMapCollective.class);
		job.setMapperClass(MeanMapper.class);
		org.apache.hadoop.mapred.JobConf jobConf = (JobConf) job.getConfiguration();
		jobConf.set("mapreduce.framework.name", "map-collective");
		jobConf.setNumMapTasks(numMapTasks);
		jobConf.setInt("mapreduce.job.max.split.locations", 10000);
		job.setNumReduceTasks(0);
		jobConfig.setInt(MeanConstants.VECTOR_SIZE,vectorSize);
		return job;
	}
}
