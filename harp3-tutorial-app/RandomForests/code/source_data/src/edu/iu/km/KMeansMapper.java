package edu.iu.km;

import java.lang.Math;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.CollectiveMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.iu.harp.example.DoubleArrPlus;
import edu.iu.harp.partition.Partition;
import edu.iu.harp.partition.Table;
import edu.iu.harp.resource.DoubleArray;


public class KMeansMapper  extends CollectiveMapper<String, String, Object, Object> {

	private int vectorSize, numIterations;
	private Log log;


	@Override
	protected void setup(Context context) throws IOException, InterruptedException {

		// Set up the configuration for the environment
		Configuration configuration = context.getConfiguration();
		// Get the size of the data points
		vectorSize = configuration.getInt(KMeansConstants.VECTOR_SIZE, 20);

		// Get the size of the data points
		numIterations = configuration.getInt(KMeansConstants.NUM_ITERATIONS, 2);

		//Initialize log
		log = LogFactory.getLog(KMeansMapper.class);
	}

	protected void mapCollective( KeyValReader reader, Context context) throws IOException, InterruptedException {

		/*
		 * vals in the keyval pairs from reader are data file paths.
		 * read data from file paths.
		 * load initial centroids
		 * do{
		 * 	computations
		 *  generate new centroids
		 * }while(<maxIteration)
		 *
		 */
		 // This is a list of the files with the data points
		 List<String> pointFiles = new ArrayList<String>();
		 // Read all of the keys from the reader where the values are the data file paths
	    while (reader.nextKeyValue()) {
				// Get the current key
	    	String key = reader.getCurrentKey();
				// Get the current value which is a path to a data file
	    	String value = reader.getCurrentValue();
	    	log.info("Key: " + key + ", Value: " + value);
				// Add this file to the string list of file paths
	    	pointFiles.add(value);
	    }
			// Get the necessary configuration information
	    	Configuration conf = context.getConfiguration();

			//centroids is the centroid table. 
			// Initialized by parsing content in conf.
			Table<DoubleArray> cenTable = new Table<>(0, new DoubleArrPlus());
			// Get the centroids string from the configuration
			String centroidstr = conf.getStrings(KMeansConstants.CFILE)[0];
			// Load the centroids as a Table<DoubleArray> from the string
			cenTable = loadCentroids(centroidstr);

			log.info("Size of centroid table: " + cenTable.getNumPartitions());

			// do {
			// Have each map task update its centroids estimate based on the data, and iterate
	    	runKMean(pointFiles, cenTable, conf, context, numIterations);	    
	 //    	log.info("Done iteration: " + numIterations);
		// 	numIterations --;

		// } while(numIterations > 0);

			log.info("Done running kMeans");


	}


	private void runKMean(List<String> fileNames, Table<DoubleArray> centroids, Configuration conf, Context context, int numIterations) throws IOException {
		
		if(this.isMaster()) {
			log.info("I am the master!!");
		}

		log.info("Loading data points");
	// Load the data from the distributed file system
		ArrayList<DoubleArray> dataPoints = loadData(fileNames, vectorSize, conf);
		log.info("Done loading data points");

		do {
	  		log.info("Doing localAggregate: " + numIterations);
			// This represents the cumulative centroid as calculated by this process
		  	Table<DoubleArray> meanTable = localAggregate(dataPoints, centroids);
		  	log.info("Done localAggregate: " + numIterations);

			log.info("Doing allreduce: " + numIterations);
			// Collapses all the cumulative centroids as calculated by all processes
			allreduce("main", "allreduce"+numIterations, meanTable);
			log.info("Done allreduce: " + numIterations);
			
			// Computes new centroids by normalizing the collapsed centroids
			log.info("Doing calculateMean: " + numIterations);
			calculateMean(meanTable);
			log.info("Done calculateMean: " + numIterations);

			//If master, and last iteration, prints meanTable
			if(this.isMaster() && numIterations == 1){
			  printTable(meanTable);
			  outputResults(meanTable, conf, context, dataPoints);
			}

			// Update centroids to be the newly computed values in meanTable
			centroids = meanTable;

			log.info("Done iteration: " + numIterations);
			numIterations --;

	} while(numIterations > 0);
		
	}


		/*
	* This function takes the table of summed columns values for each centroid. The value of each
	* column is the result of summing accross the columns values of each data point assigned to
	* that centroid. The last value in each DoubleArray is the number of data points that were
	* assigned to the given centroid. This value is used to get the mean value across data points
	* for a given column. The averaged columne values are the result of this function.
	*/
	private void calculateMean(Table<DoubleArray> meanTable){
			// Iterate over all centroid sums (based on points assigned to them) and find centroid value by normalizing
		for( Partition<DoubleArray> ap: meanTable.getPartitions()){
			//The relevant centroid point
			DoubleArray point = ap.get();
			// Memory holder for number of data points that were assigned to the centriods
		double numSize = point.get()[point.size()-1];
		// Divide each sum by the number of points to get the mean value for
			// that column
		  for(int i=0;i < point.size()-1; i++){
			  point.get()[i] /= numSize;
		  }
		}
	}

	/*
	* This function takes an ArrayList<DoubleArray> representing each of the k
	* centroids and a data point (double[]). The euclidean distance between the
	* data point and each of the centroids is computed. The index of the centroid
	* with the closest distance to the data point is returned as an int.
	*/
	private int findClosestCentroid(ArrayList<DoubleArray> carrays, double[] dp) {
		// This is used to track the smallest observed distance
		double min_dist = Double.POSITIVE_INFINITY;;
		// This is used to track the index of the centroid that has the smallest
		// distance to the data point
		int cid = -1;

		// Loop through the centroids
		for (int c = 0; c < carrays.size(); c++) {
			// The cth centroid
			double[] carray = carrays.get(c).get();
			// This is used to track the current sum of differences squared
			double euclid_dist = 0.0;
			// Loop through the data point
			for (int i = 0; i < vectorSize; i++) {
				// Update the euclidean distance tracker with the difference squared
				// between these two columns
				euclid_dist += ((dp[i] - carray[i]) * (dp[i] - carray[i]));
			}
			// Get the square root of the euclidean distance value
			euclid_dist = Math.sqrt(euclid_dist);
			// Check if this is the centroid, of those that have been observed, that
			// the data point is closest to
			if (euclid_dist < min_dist) {
				// Update the index tracking the closest centroid
				cid = c;
				// Update the minimum observed distances
				min_dist = euclid_dist;
			}
		}
		return cid;
	}

	private Table<DoubleArray> localAggregate(ArrayList<DoubleArray> dataPoints, Table<DoubleArray> centroids){

		// This will hold the mean values
	  Table<DoubleArray> meanTable = new Table<>(0, new DoubleArrPlus());
		// Needs to have c instances of DoubleArray where c is the number of centroids, to accumulate sum for new centroids
	  ArrayList<DoubleArray> narrays = new ArrayList<DoubleArray>();
	  // To convert centroids present in centroids  table to an iterable arraylist
	  ArrayList<DoubleArray> carrays =new ArrayList<DoubleArray>();
	  // Add c instances of DoubleArray to narrays and copy over centroids to carrays
		for( Partition<DoubleArray> ap: centroids.getPartitions()){			  
			narrays.add(DoubleArray.create(vectorSize+1, false));
			carrays.add(ap.get());
		}

		// Iterate through the data points
	  for(DoubleArray dp : dataPoints){
			// Get the data point
		  double[] aData = dp.get();
			// Find the closest centroid
			int cid = findClosestCentroid(carrays, aData);
			// This tracks the sum for each column local to the map task and the number
			// of points that contributed to the sum
		  double[] localSumAndSize = narrays.get(cid).get();
			// Add the value for each column to the current sum for that column
		  for(int i=0; i<vectorSize; i++){
			  localSumAndSize[i] += aData[i];
		  }
			// The last value in localSumAndSize is the number of points that contributed
			// to the column sums. This value is needed to get the mean
			localSumAndSize[vectorSize] += 1;
	  }
		// Put the values that will be used to update the centroids into an array
		// so that it can be added to meanTable
	  	for (int c = 0; c < narrays.size(); c++ ) {
			Partition<DoubleArray> ap = new Partition<DoubleArray>(c, narrays.get(c));
			meanTable.addPartition(ap);
		}

		// Add the sum and number observed points array to the mean table
	  // meanTable.addPartition(ap);
	  return meanTable;
	}


	/*
	* This function takes an ArrayList<DoubleArray> representing each of the k
	* centroids and a data point (double[]). The euclidean distance between the
	* data point and each of the centroids is computed. The squared distance to 
	* the closest cetroid to the data point is returned as a double.
	*/
	private double findClosestCentroidSE(ArrayList<DoubleArray> carrays, double[] dp) {
		// This is used to track the smallest observed distance
		double min_dist = Double.POSITIVE_INFINITY;;

		// Loop through the centroids
		for (int c = 0; c < carrays.size(); c++) {
			// The cth centroid
			double[] carray = carrays.get(c).get();
			// This is used to track the current sum of differences squared
			double euclid_dist = 0.0;
			// Loop through the data point
			for (int i = 0; i < vectorSize; i++) {
				// Update the euclidean distance tracker with the difference squared
				// between these two columns
				euclid_dist += ((dp[i] - carray[i]) * (dp[i] - carray[i]));
			}
			// Get the square root of the euclidean distance value
			euclid_dist = Math.sqrt(euclid_dist);
			// Check if this is the centroid, of those that have been observed, that
			// the data point is closest to
			if (euclid_dist < min_dist) {
				// Update the minimum observed distances
				min_dist = euclid_dist;
			}
		}
		return min_dist;
	}


	/*
	* This function takes an ArrayList<DoubleArray> representing each of the datapoints
	* and a table of centroids. It calculates the sum of squared errors for the final
	* clusters obtained.
	*/
	private double calculateSSE(ArrayList<DoubleArray> dataPoints, Table<DoubleArray> centroids) {

		// To convert centroids present in centroids  table to an iterable arraylist
	  	ArrayList<DoubleArray> carrays =new ArrayList<DoubleArray>();
	  	// Add c instances of DoubleArray to narrays and copy over centroids to carrays
		for( Partition<DoubleArray> ap: centroids.getPartitions()){			  
			carrays.add(ap.get());
		}

		//To store the SSE
		double SSE = 0, pointSSE;

		// Iterate through the data points
	  	for(DoubleArray dp : dataPoints){
			// Get the data point
			double[] aData = dp.get();
			// Find the closest centroid's SSE
			SSE += findClosestCentroidSE(carrays, aData);
	  	}

	  	SSE /= dataPoints.size();

	  	return SSE;
	}

	/*
	* This function writes the current results to the distributed file system context and
	* environment.
	*/
	private void outputResults(Table<DoubleArray> centroids, Configuration conf, Context context, ArrayList<DoubleArray> dataPoints){
	  String output="";
	  for( Partition<DoubleArray> ap: centroids.getPartitions()){
		  double res[] = ap.get().get();
		  for(int i=0; i<vectorSize;i++)
			 output+= res[i]+"\t";
		  output+="\n";
	  }
	  output += "SSE: " + calculateSSE(dataPoints, centroids) + "\n";
	  try {
			context.write(null, new Text(output));
	  } catch (IOException e) {
			e.printStackTrace();
	  } catch (InterruptedException e) {
		e.printStackTrace();
	  }
	}

	/*
	* This function takes a String list, the size of each data vector, and the configuration obejct for
	* the distributed file system sent it. It reads the data from each of the files on the distributed
	* file system into an array list. The data point values are represented as doubles.
	*/
	private ArrayList<DoubleArray> loadData(List<String> fileNames,  int vectorSize, Configuration conf) throws IOException{
		// The ArrayList that will be returned with the data
		ArrayList<DoubleArray> data = new ArrayList<DoubleArray>();
		// Process each file
	  for(String filename: fileNames){
			// Get the information about how the file system is configured
		  FileSystem fs = FileSystem.get(conf);
			// Get the location of the data file in the distrubted file system
		  Path dPath = new Path(filename);
			// Create the objects that are needed to read in the data
		  FSDataInputStream in = fs.open(dPath);
		  try {
			InputStreamReader inIn = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(inIn);
			// This line is used to more through each data file
			String line="";
			// This will hold each data points as it is read in
			String[] vector=null;
			// Read each line in the data file
			while((line = br.readLine()) != null){
				// The data values are separated by new lines, so separated each line
				// into a data vector by splitting on the blank spaces
			  vector = line.split("\\s+");
				// Check to make sure that the correct number of values were found
			  if(vector.length != vectorSize){						
				  System.err.println("Errors while loading data.");
				  System.exit(-1);
			  }else{
					// This will hold the data point where the values have been converted
					// to doubles
				  double[] aDataPoint = new double[vectorSize];
					// Convert each string value to a double
				  for(int i=0; i<vectorSize; i++){
					  aDataPoint[i] = Double.parseDouble(vector[i]);
				  }
					// Add the vector of doubles to the double array that tracks the
					// vector, 0 for some reason, and the size of the vector
				  DoubleArray da = new DoubleArray(aDataPoint, 0, vectorSize);
					// Add it to the data array list
				  data.add(da);
			  }
			}
		  } finally {
			// Close the input stream
			in.close();
		  }			  			 
	  }
	  return data;
	}

	/*
	* This function takes the size of each data vector, the configuration obejct, and the context object for
	* the distributed file system. It reads the data from the default write location, which should hold a
	* file representing the current centroids.
	*/
	private Table<DoubleArray>  loadCentroids(String centstr) throws IOException{
		// This will hold the mean values
	  Table<DoubleArray> centroids = new Table<>(0, new DoubleArrPlus());
		// Needs to have c instances of DoubleArray where c is the number of centroids
	  ArrayList<DoubleArray> carrays = new ArrayList<DoubleArray>();

		// This line is used to move through the data string and represent each
		// centroid
		String[] line = null;
		// This will all of the centroids as space seperated strings
		String[] vector = centstr.split("\n");

		// Loop through the vector of string centroid representations
		for (int c = 0; c < vector.length; c++) {
			// Get the string representation of the centroid and split it into a
			// vector of strings
			line = vector[c].split("\\s+");
			// Create the vector that will hold the centroid values
			double[] centroid = new double[line.length];
			// Loop over every value that makes the centroid
			for (int i = 0; i < line.length; i++) {
				// Add this value to vector representing the centroid
				centroid[i] = Double.parseDouble(line[i]);;
			}
			// Add the centroid to the carrays ArrayList
			carrays.add(new DoubleArray(centroid,0,line.length));
		}
		// Put the ListArray of centroids into a partition
		for (int c = 0; c < carrays.size(); c++ ) {
			Partition<DoubleArray> ap = new Partition<DoubleArray>(c, carrays.get(c));
			// Add the sum and number observed points array to the centroid table
			centroids.addPartition(ap);
		}

	  return centroids;
	}


	/*
	* This function prints the data table out to the command line for visual inspection.
	*/
	private void printTable(Table<DoubleArray> dataTable){
	  for( Partition<DoubleArray> ap: dataTable.getPartitions()){
		  double res[] = ap.get().get();
		  System.out.print("ID: "+ap.id() + ":");
		  for(int i=0; i<res.length;i++)
			  System.out.print(res[i]+"\t");
		  System.out.println();
	  }
	}
}
