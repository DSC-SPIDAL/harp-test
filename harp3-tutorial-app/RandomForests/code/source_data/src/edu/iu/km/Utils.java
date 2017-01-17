package edu.iu.km;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/*
 * generate data and initial centroids
 */
public class Utils {

	/*
	* This function takes the number of data points that should be generated, the size of
	* data point, the number of map tasks, and information about the distributed file system.
	* The specified number of points are randomly generated and evenly distributed across the
	* map tasks. numMapTasks chunks of data are written to the distributed file system, fs.
	* The argument dataDir is the directory on the distributed file system and fs is the
	* distributed file system.
	*/
	static void generateData( int numOfDataPoints, int vectorSize, int numMapTasks,
			FileSystem fs,  String localDirStr, Path dataDir) throws IOException, InterruptedException, ExecutionException {
				// One file for each of the map tasks will be created
				int numOfpointFiles = numMapTasks;
				// Want to evenly distribute the data points across the map tasks
				int pointsPerFile = numOfDataPoints / numOfpointFiles;
				// Need to make sure to know how many data points need to be handled as a slightly special case
	 			int pointsRemainder = numOfDataPoints % numOfpointFiles;
		 		System.out.println("Writing " + numOfDataPoints + " vectors to "+ numMapTasks +" file evenly");

				// Check whether or not the data directory exists
			 	if (fs.exists(dataDir)) {
					// It does, so delete
					// Want to start with new data, so need to clear out the data that is
					// already in the distributed file system
					fs.delete(dataDir, true);
				}
			 // Check local directory
			 File localDir = new File(localDirStr);
			 // If existed, regenerate data. Generating new data, so need to remove the
			 // data that is already associated with that location on the local machine
			 if (localDir.exists() && localDir.isDirectory()) {
				 for (File file : localDir.listFiles()) {
					 file.delete();
				 }
				 localDir.delete();
			 }
			 // Create a fresh version of the directory on teh local machine
			 boolean success = localDir.mkdir();
			 if (success) {
				 System.out.println("Directory: " + localDirStr + " created");
			 }
			 // Check to make sure that a non-zero number of points were specified to
			 // be generated
			 if (pointsPerFile == 0) {
				 throw new IOException("No point to write.");
			 }
			 // Start generating random points
			 double point;
			 // Used to track the data points that were left over and not able to be
			 // evenly distributed across the map tasks
			 int hasRemainder=0;
			 Random random = new Random();
			 // Generate pointsPerFile for each of the files that correspond with a
			 // map task
			 for (int k = 0; k < numOfpointFiles; k++) {
				 try {
					 // Name the file according to which partition of the data it
					 // represents
					 String filename =Integer.toString(k);
					 // Create the file
					 File file = new File(localDirStr + File.separator + "data_" + filename);
					 // Create the objects that will be used to write the data to the file
					 FileWriter fw = new FileWriter(file.getAbsoluteFile());
					 BufferedWriter bw = new BufferedWriter(fw);
					 // Check whether or not the number of data points can be evenly
					 // distributed across the map tasks
					 if(pointsRemainder > 0){
						 // There are points that need to be handled and added to a partition
						 // of data, so add it as a remainder to this partition
						 hasRemainder = 1;
						 // Decrease the number of points that need to be specially handled
						 // by one
						 pointsRemainder--;
					 }else{
						 // There are not points that need to be handled specially and added
						 // to a partition
						 hasRemainder = 0;
					 }
					 // Get the number of points that will be sotred in this file
					 // It is the number of points per partition plus the a portion of the
					 // points that could not be evenly distributed among the files/map tasks
					 int pointsForThisFile = pointsPerFile + hasRemainder;
					 // Generate each of the points
					 for (int i = 0; i < pointsForThisFile; i++) {
						 // Generate each value in the point
						 for (int j = 0; j < vectorSize; j++) {
							 // Generate the random value that will be assigned to the column j
							 // The range is a constant value and is therefore stored in the
							 // KMeansConstants class
							 //point = random.nextDouble() * DATA_RANGE;
							 point = random.nextDouble() * KMeansConstants.DATA_RANGE;
							 //System.out.println(point+"\t");
							 // Check whether or not this is the last column value that needs
							 // to be created
							 if(j == vectorSize-1){
								 // Write the point to the file
								 bw.write(point+"");
								 // Move ontp the next line, ie. the next point
								 bw.newLine();
							 }else{
								 // Write the point to the file
								 bw.write(point+" ");
							 }
						 }
					 }
					 // Close the writer
					 bw.close();
					 System.out.println("Done written"+ pointsForThisFile + "points" +"to file "+ filename);
				 } catch (FileNotFoundException e) {
					 e.printStackTrace();
				 } catch (IOException e) {
					 e.printStackTrace();
				 }
			 }
			 // Create the local directory as a path object
			 Path localPath = new Path(localDirStr);
			 // Copy from the generated data from the locally written files to the
			 // distributed file system
			 fs.copyFromLocalFile(localPath, dataDir);
	 }

	 /*
	 * This function takes the number of data points that should be generated, the size of
 	 * data point, the number of map tasks, and information about the distributed file system.
 	 * A centroid is generated for each map task. The centroid is written to the distributed
	 * file system where the map task it belongs to can use it.
	 */
	 static void generateInitialCentroids(int numCentroids, int vectorSize, Configuration configuration) throws IOException {
					// Initialize the random number generator object
					Random random = new Random();
					// This is the string that will be written out holding the centroids
					String centroids="";
					// WOrking variable
					double point;
					// Create each of the k centroids
				  for(int c = 0; c < numCentroids; c++){
						// Randomly create each column value for the current centroid
						for (int j = 0; j < vectorSize; j++) {
							// Generate the random value that will be assigned to the column j
							// The range is a constant value and is therefore stored in the
							// KMeansConstants class
							point = random.nextDouble() * KMeansConstants.DATA_RANGE;
							// Check whether or not this is the last column value that needs
							// to be created
							if(j == vectorSize-1){
								// Write the point to the string and move onto the next line
								centroids += point + "\n";
							}else{
								// Write the point to the string
								centroids += point + " ";
						}
					  }
					}

				// Dummy string array to load centroid string into configuration
				String[] strings = new String[1];
				strings[0] = centroids;

				// Add the string representing the centroids to the configuration
				configuration.setStrings(KMeansConstants.CFILE, strings);
	}
}
