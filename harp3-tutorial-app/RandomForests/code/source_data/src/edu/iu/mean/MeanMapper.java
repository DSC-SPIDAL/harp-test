package edu.iu.mean;

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
import edu.iu.harp.example.DoubleArrPlus;
import edu.iu.harp.partition.Partition;
import edu.iu.harp.partition.Table;
import edu.iu.harp.resource.DoubleArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MeanMapper extends CollectiveMapper<String, String, Object, Object> {

	private int vectorSize;
	private Log log;
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		//initialization
		System.out.println("Setup");		
		Configuration configuration = context.getConfiguration();
    	vectorSize =configuration.getInt(MeanConstants.VECTOR_SIZE, 20);
    	System.out.println("Done setup");

    	//Initialize log
		log = LogFactory.getLog(MeanMapper.class);
	}

	protected void mapCollective( KeyValReader reader, Context context) throws IOException, InterruptedException {

		log.info("In mapCollective");
	    List<String> pointFiles = new ArrayList<String>();
	    while (reader.nextKeyValue()) {
	    	String key = reader.getCurrentKey();
	    	String value = reader.getCurrentValue();
	    	LOG.info("Key: " + key + ", Value: " + value);
	    	pointFiles.add(value);
	    }
	    Configuration conf = context.getConfiguration();
	    runMean(pointFiles, conf, context);
	  }
	 
	  private void runMean(List<String> fileNames, Configuration conf, Context context) throws IOException {

		  //load data 
		  ArrayList<DoubleArray> dataPoints = loadData(fileNames, vectorSize, conf);
		 
		  Table<DoubleArray> meanTable;
		  
		  meanTable = localAggregate(dataPoints);
		  
		  allreduce("main", "allreduce",meanTable);
		  
		  calculateMean(meanTable);
		  
		  if(this.isMaster()){
			  printTable(meanTable);
			  outputResults(meanTable, conf, context);
		  }

		  log.info("Done!");
	 }
	  
	  private void calculateMean( Table<DoubleArray> meanTable ){
		  DoubleArray array = meanTable.getPartition(0).get();
		  double numSize = array.get()[array.size()-1];
		  for(int i=0;i<array.size()-1; i++){
			  array.get()[i] /= numSize;
		  }
	  }
	  
	  private  Table<DoubleArray>  localAggregate( ArrayList<DoubleArray> dataPoints ){
		  Table<DoubleArray> meanTable = new Table<>(0, new DoubleArrPlus());
		  DoubleArray array = DoubleArray.create(vectorSize+1, false);
		  double[] localSumAndSize = array.get();

		  for(DoubleArray dp : dataPoints){
			  double [] aData = dp.get();
			  for(int i=0; i<vectorSize; i++){
				  localSumAndSize[i] += aData[i];
			  }
		  }
		  localSumAndSize[vectorSize] = dataPoints.size();
		  
		  Partition<DoubleArray> ap = new Partition<DoubleArray>(0, array);
		  meanTable.addPartition(ap);
		  return meanTable;
	  }
	  
	  
	  private void outputResults(Table<DoubleArray>  dataTable,Configuration conf, Context context){
		  String output="";
		  for( Partition<DoubleArray> ap: dataTable.getPartitions()){
			  double res[] = ap.get().get();
			  for(int i=0; i<vectorSize;i++)
				 output+= res[i]+"\t";
			  output+="\n";
		  }
		  try {
				context.write(null, new Text(output));
		  } catch (IOException e) {
				e.printStackTrace();
		  } catch (InterruptedException e) {
			e.printStackTrace();
		  }
	  }
	  
	  //load data form HDFS
	  private ArrayList<DoubleArray>  loadData(List<String> fileNames,  int vectorSize, Configuration conf) throws IOException{
		  ArrayList<DoubleArray> data = new  ArrayList<DoubleArray> ();
		  for(String filename: fileNames){
			  FileSystem fs = FileSystem.get(conf);
			  Path dPath = new Path(filename);
			  FSDataInputStream in = fs.open(dPath);
			  BufferedReader br = new BufferedReader( new InputStreamReader(in));
			  String line="";
			  String[] vector=null;
			  while((line = br.readLine()) != null){
				  vector = line.split("\\s+");
				  if(vector.length != vectorSize){
					  System.out.println("Errors while loading data.");
					  System.exit(-1);
				  }else{
					  double[] aDataPoint = new double[vectorSize];
					  
					  for(int i=0; i<vectorSize; i++){
						  aDataPoint[i] = Double.parseDouble(vector[i]);
					  }
					  DoubleArray da = new DoubleArray(aDataPoint, 0, vectorSize);
					  data.add(da);
				  }
			  }
		  }
		  return data;
	  }
	  
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