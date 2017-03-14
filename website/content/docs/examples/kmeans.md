---
title: K-Means
---

<img src="/img/kmeans.png" width="80%" >

K-Means is a powerful and easily understood clustering algorithm. The aim of the algorithm is to divide a given set of points into `K` partitions. `K` needs to be specified by the user. In order to understand K-Means, first you need to understand the proceeding concepts and their meanings.

* `Centroids`:
    Centroids can be defined as the center of each cluster. If we are performing clustering with k=3, we will have 3 centroids. To perform K-Means clustering, the users needs to provide an initial set of centroids.

* `Distance`:
    In order to group data points as close together or as far-apart, we need to define a distance between two given data points. In K-Means, clustering distance is normally calculated as the Euclidean Distance between two data points.

The K-Means algorithm simply repeats the following set of steps until there is no change in the partition assignments. In that, it has clarified which data point is assigned to which partition.

1. Choose K points as the initial set of centroids.

2. Assign each data point in the data set to the closest centroid (this is done by calculating the distance between the data point and each centroid).

3. Calculate the new centroids based on the clusters that were generated in step 2. Normally this is done by calculating the mean of each cluster.

4. Repeat step 2 and 3 until data points do not change cluster assignments, which means that their centroids are set.


## Step 1 --- The Main Method
The tasks of the main class is to configure and run the job iteratively.
```java
generate N data points (D dimensions), write to HDFS
generate M centroids, write to HDFS
for iterations{
    configure a job
    launch the job
}
```

## Step 2 --- The mapCollective function
This is the definition of map-collective task. It reads data from context and then call runKmeans function to actually run kmeans Mapper task.
```java
protected void mapCollective( KeyValReader reader, Context context) throws IOException, InterruptedException {
    LOG.info("Start collective mapper.");
    long startTime = System.currentTimeMillis();
    List<String> pointFiles = new ArrayList<String>();
    while (reader.nextKeyValue()) {
	   	String key = reader.getCurrentKey();
	   	String value = reader.getCurrentValue();
    	LOG.info("Key: " + key + ", Value: " + value);
	    pointFiles.add(value);
	}
	Configuration conf = context.getConfiguration();
	runKmeans(pointFiles, conf, context);
    LOG.info("Total iterations in master view: " + (System.currentTimeMillis() - startTime));
}
```


## Step 3 --- The runKmeans function

Harp provides several collective communication operations. Here are some examples provided to show how to apply these collective communication methods to K-Means.

  <ul class="nav nav-pills">
    <li class="active"><a data-toggle="pill" href="#allreduce">Allreduce</a></li>
    <li><a data-toggle="pill" href="#broadcast-reduce">Broadcast-Reduce</a></li>
    <li><a data-toggle="pill" href="#push-pull">Push-Pull</a></li>
    <li><a data-toggle="pill" href="#regroup-allgather">Regroup-Allgather</a></li>
  </ul>

  <div class="tab-content">
    <div id="allreduce" class="tab-pane fade in active">
      <h4>Use AllReduce collective communication to do synchronization</h4>
      <div class="highlight" style="background: #272822"><pre style="line-height: 125%"><span></span><span style="color: #3976cc">private</span> <span style="color: #3976cc">void</span> <span style="color: #02a894">runKmeans</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">List</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">String</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">fileNames</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">Configuration</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">Context</span> <span style="color: #6b6b6b">context</span><span style="color: #c03221">)</span> <span style="color: #3976cc">throws</span> <span style="color: #6b6b6b">IOException</span> <span style="color: #c03221">{</span>
      <span style="color: #03254e">// -----------------------------------------------------</span>
      <span style="color: #03254e">// Load centroids</span>
      <span style="color: #03254e">//for every partition in the centoid table, we will use the last element to store the number of points </span>
      <span style="color: #03254e">// which are clustered to the particular partitionID</span>
      <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">DoubleArray</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">cenTable</span> <span style="color: #c03221">=</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;&gt;(</span><span style="color: #7d518c">0</span><span style="color: #c03221">,</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">DoubleArrPlus</span><span style="color: #c03221">());</span>
      <span style="color: #3976cc">if</span> <span style="color: #c03221">(</span><span style="color: #3976cc">this</span><span style="color: #c03221">.</span><span style="color: #02a894">isMaster</span><span style="color: #c03221">())</span> <span style="color: #c03221">{</span>
      		<span style="color: #6b6b6b">loadCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">vectorSize</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">.</span><span style="color: #02a894">get</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">KMeansConstants</span><span style="color: #c03221">.</span><span style="color: #02a894">CFILE</span><span style="color: #c03221">),</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">);</span>
      <span style="color: #c03221">}</span>
      <span style="color: #6b6b6b">System</span><span style="color: #c03221">.</span><span style="color: #02a894">out</span><span style="color: #c03221">.</span><span style="color: #02a894">println</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;After loading centroids&quot;</span><span style="color: #c03221">);</span>
      <span style="color: #6b6b6b">printTable</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
      <span style="color: #03254e">//broadcast centroids</span>
      <span style="color: #6b6b6b">broadcastCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
      <span style="color: #03254e">//after broadcasting</span>
      <span style="color: #6b6b6b">System</span><span style="color: #c03221">.</span><span style="color: #02a894">out</span><span style="color: #c03221">.</span><span style="color: #02a894">println</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;After brodcasting centroids&quot;</span><span style="color: #c03221">);</span>
      <span style="color: #6b6b6b">printTable</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
      <span style="color: #03254e">//load data </span>
      <span style="color: #6b6b6b">ArrayList</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">DoubleArray</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">dataPoints</span> <span style="color: #c03221">=</span> <span style="color: #6b6b6b">loadData</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">fileNames</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">vectorSize</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">);</span>
      <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">DoubleArray</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">previousCenTable</span> <span style="color: #c03221">=</span>  <span style="color: #3976cc">null</span><span style="color: #c03221">;</span>
      <span style="color: #03254e">//iterations</span>
      <span style="color: #3976cc">for</span><span style="color: #c03221">(</span><span style="color: #3976cc">int</span> <span style="color: #6b6b6b">iter</span><span style="color: #c03221">=</span><span style="color: #7d518c">0</span><span style="color: #c03221">;</span> <span style="color: #6b6b6b">iter</span> <span style="color: #c03221">&lt;</span> <span style="color: #6b6b6b">iteration</span><span style="color: #c03221">;</span> <span style="color: #6b6b6b">iter</span><span style="color: #c03221">++){</span>
            <span style="color: #6b6b6b">previousCenTable</span> <span style="color: #c03221">=</span>  <span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">;</span>
      	    <span style="color: #6b6b6b">cenTable</span> <span style="color: #c03221">=</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;&gt;(</span><span style="color: #7d518c">0</span><span style="color: #c03221">,</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">DoubleArrPlus</span><span style="color: #c03221">());</span>
      	    <span style="color: #6b6b6b">System</span><span style="color: #c03221">.</span><span style="color: #02a894">out</span><span style="color: #c03221">.</span><span style="color: #02a894">println</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;Iteraton No.&quot;</span><span style="color: #c03221">+</span><span style="color: #6b6b6b">iter</span><span style="color: #c03221">);</span>
      		<span style="color: #03254e">//compute new partial centroid table using previousCenTable and data points</span>
      		<span style="color: #6b6b6b">computation</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">previousCenTable</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">dataPoints</span><span style="color: #c03221">);</span>
      		<span style="color: #03254e">//AllReduce; </span>
      		<span style="color: #03254e">/****************************************/</span>
      		<span style="color: #6b6b6b">allreduce</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;main&quot;</span><span style="color: #c03221">,</span> <span style="color: #9b802e">&quot;allreduce_&quot;</span><span style="color: #c03221">+</span><span style="color: #6b6b6b">iter</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
      		<span style="color: #03254e">//we can calculate new centroids</span>
      		<span style="color: #6b6b6b">calculateCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
      		<span style="color: #03254e">/****************************************/</span>
      		<span style="color: #6b6b6b">printTable</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
      	<span style="color: #c03221">}</span>
      	<span style="color: #03254e">//output results</span>
     <span style="color: #3976cc">if</span><span style="color: #c03221">(</span><span style="color: #3976cc">this</span><span style="color: #c03221">.</span><span style="color: #02a894">isMaster</span><span style="color: #c03221">()){</span>
      	<span style="color: #6b6b6b">outputCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span>  <span style="color: #6b6b6b">conf</span><span style="color: #c03221">,</span>   <span style="color: #6b6b6b">context</span><span style="color: #c03221">);</span>
      <span style="color: #c03221">}</span>
<span style="color: #c03221">}</span>
 </pre></div>
    </div>
    <div id="broadcast-reduce" class="tab-pane fade">
      <h4>Use broadcast and reduce collective communication to do synchronization</h4>
     
<p>The video below is the step by step guide on how this collective communication works for K-means. The data is partitions into K different partitions with K centroids. Data is then broadcasted to all the different partitions. And the centroids for each of the partition is grouped together and sent to the master node.</p>

<p>Once all the local centroids from the partition is collected in the global centroid table, the updated table is transferred to the root node and then broadcasted again. This step keeps repeating itself till the convergence is reached.
</p>
<div class="highlight" style="background: #272822"><pre style="line-height: 125%"><span></span><span style="color: #3976cc">private</span> <span style="color: #3976cc">void</span> <span style="color: #02a894">runKmeans</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">List</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">String</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">fileNames</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">Configuration</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">Context</span> <span style="color: #6b6b6b">context</span><span style="color: #c03221">)</span> <span style="color: #3976cc">throws</span> <span style="color: #6b6b6b">IOException</span> <span style="color: #c03221">{</span>
     <span style="color: #03254e">// -----------------------------------------------------</span>
     <span style="color: #03254e">// Load centroids</span>
     <span style="color: #03254e">//for every partition in the centoid table, we will use the last element to store the number of points </span>
     <span style="color: #03254e">// which are clustered to the particular partitionID</span>
     <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">DoubleArray</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">cenTable</span> <span style="color: #c03221">=</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;&gt;(</span><span style="color: #7d518c">0</span><span style="color: #c03221">,</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">DoubleArrPlus</span><span style="color: #c03221">());</span>
     <span style="color: #3976cc">if</span> <span style="color: #c03221">(</span><span style="color: #3976cc">this</span><span style="color: #c03221">.</span><span style="color: #02a894">isMaster</span><span style="color: #c03221">())</span> <span style="color: #c03221">{</span>
        <span style="color: #6b6b6b">loadCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">vectorSize</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">.</span><span style="color: #02a894">get</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">KMeansConstants</span><span style="color: #c03221">.</span><span style="color: #02a894">CFILE</span><span style="color: #c03221">),</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">);</span>
     <span style="color: #c03221">}</span>
     <span style="color: #6b6b6b">System</span><span style="color: #c03221">.</span><span style="color: #02a894">out</span><span style="color: #c03221">.</span><span style="color: #02a894">println</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;After loading centroids&quot;</span><span style="color: #c03221">);</span>
     <span style="color: #6b6b6b">printTable</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
     <span style="color: #03254e">//broadcast centroids</span>
     <span style="color: #6b6b6b">broadcastCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
     <span style="color: #03254e">//after broadcasting</span>
     <span style="color: #6b6b6b">System</span><span style="color: #c03221">.</span><span style="color: #02a894">out</span><span style="color: #c03221">.</span><span style="color: #02a894">println</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;After brodcasting centroids&quot;</span><span style="color: #c03221">);</span>
     <span style="color: #6b6b6b">printTable</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
     <span style="color: #03254e">//load data </span>
     <span style="color: #6b6b6b">ArrayList</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">DoubleArray</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">dataPoints</span> <span style="color: #c03221">=</span> <span style="color: #6b6b6b">loadData</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">fileNames</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">vectorSize</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">);</span>
     <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">DoubleArray</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">previousCenTable</span> <span style="color: #c03221">=</span>  <span style="color: #3976cc">null</span><span style="color: #c03221">;</span>
     <span style="color: #03254e">//iterations</span>
     <span style="color: #3976cc">for</span><span style="color: #c03221">(</span><span style="color: #3976cc">int</span> <span style="color: #6b6b6b">iter</span><span style="color: #c03221">=</span><span style="color: #7d518c">0</span><span style="color: #c03221">;</span> <span style="color: #6b6b6b">iter</span> <span style="color: #c03221">&lt;</span> <span style="color: #6b6b6b">iteration</span><span style="color: #c03221">;</span> <span style="color: #6b6b6b">iter</span><span style="color: #c03221">++){</span>
        <span style="color: #6b6b6b">previousCenTable</span> <span style="color: #c03221">=</span>  <span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">;</span>
     	<span style="color: #6b6b6b">cenTable</span> <span style="color: #c03221">=</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;&gt;(</span><span style="color: #7d518c">0</span><span style="color: #c03221">,</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">DoubleArrPlus</span><span style="color: #c03221">());</span>
     	<span style="color: #6b6b6b">System</span><span style="color: #c03221">.</span><span style="color: #02a894">out</span><span style="color: #c03221">.</span><span style="color: #02a894">println</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;Iteraton No.&quot;</span><span style="color: #c03221">+</span><span style="color: #6b6b6b">iter</span><span style="color: #c03221">);</span>
     	<span style="color: #03254e">//compute new partial centroid table using previousCenTable and data points</span>
     	<span style="color: #6b6b6b">computation</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">previousCenTable</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">dataPoints</span><span style="color: #c03221">);</span>
     	<span style="color: #03254e">/****************************************/</span>
     	<span style="color: #6b6b6b">reduce</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;main&quot;</span><span style="color: #c03221">,</span> <span style="color: #9b802e">&quot;reduce_&quot;</span><span style="color: #c03221">+</span><span style="color: #6b6b6b">iter</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span> <span style="color: #3976cc">this</span><span style="color: #c03221">.</span><span style="color: #02a894">getMasterID</span><span style="color: #c03221">());</span>
     	<span style="color: #3976cc">if</span><span style="color: #c03221">(</span><span style="color: #3976cc">this</span><span style="color: #c03221">.</span><span style="color: #02a894">isMaster</span><span style="color: #c03221">())</span>
     		<span style="color: #6b6b6b">calculateCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
     	<span style="color: #6b6b6b">broadcast</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;main&quot;</span><span style="color: #c03221">,</span> <span style="color: #9b802e">&quot;bcast_&quot;</span><span style="color: #c03221">+</span><span style="color: #6b6b6b">iter</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span> <span style="color: #3976cc">this</span><span style="color: #c03221">.</span><span style="color: #02a894">getMasterID</span><span style="color: #c03221">(),</span> <span style="color: #3976cc">false</span><span style="color: #c03221">);</span>
     	<span style="color: #03254e">/****************************************/</span>
     	<span style="color: #6b6b6b">printTable</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
    <span style="color: #c03221">}</span>
     	<span style="color: #03254e">//output results</span>
    <span style="color: #3976cc">if</span><span style="color: #c03221">(</span><span style="color: #3976cc">this</span><span style="color: #c03221">.</span><span style="color: #02a894">isMaster</span><span style="color: #c03221">()){</span>
     	<span style="color: #6b6b6b">outputCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span>  <span style="color: #6b6b6b">conf</span><span style="color: #c03221">,</span>   <span style="color: #6b6b6b">context</span><span style="color: #c03221">);</span>
    <span style="color: #c03221">}</span>
<span style="color: #c03221">}</span>
     </pre></div>
     </div>
    <div id="push-pull" class="tab-pane fade">
      <h4>Use push and pull collective communication to do synchronization</h4> 
    <div class="highlight" style="background: #272822"><pre style="line-height: 125%"><span></span><span style="color: #3976cc">private</span> <span style="color: #3976cc">void</span> <span style="color: #02a894">runKmeans</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">List</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">String</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">fileNames</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">Configuration</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">Context</span> <span style="color: #6b6b6b">context</span><span style="color: #c03221">)</span> <span style="color: #3976cc">throws</span> <span style="color: #6b6b6b">IOException</span> <span style="color: #c03221">{</span>
		  <span style="color: #03254e">// -----------------------------------------------------</span>
		  <span style="color: #03254e">// Load centroids</span>
		  <span style="color: #03254e">//for every partition in the centoid table, we will use the last element to store the number of points </span>
		  <span style="color: #03254e">// which are clustered to the particular partitionID</span>
		  <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">DoubleArray</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">cenTable</span> <span style="color: #c03221">=</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;&gt;(</span><span style="color: #7d518c">0</span><span style="color: #c03221">,</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">DoubleArrPlus</span><span style="color: #c03221">());</span>
		  <span style="color: #3976cc">if</span> <span style="color: #c03221">(</span><span style="color: #3976cc">this</span><span style="color: #c03221">.</span><span style="color: #02a894">isMaster</span><span style="color: #c03221">())</span> <span style="color: #c03221">{</span>
			  <span style="color: #6b6b6b">loadCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">vectorSize</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">.</span><span style="color: #02a894">get</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">KMeansConstants</span><span style="color: #c03221">.</span><span style="color: #02a894">CFILE</span><span style="color: #c03221">),</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">);</span>
		  <span style="color: #c03221">}</span>
		  <span style="color: #6b6b6b">System</span><span style="color: #c03221">.</span><span style="color: #02a894">out</span><span style="color: #c03221">.</span><span style="color: #02a894">println</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;After loading centroids&quot;</span><span style="color: #c03221">);</span>
		  <span style="color: #6b6b6b">printTable</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
		  <span style="color: #03254e">//broadcast centroids</span>
		  <span style="color: #6b6b6b">broadcastCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
		  <span style="color: #03254e">//after broadcasting</span>
		  <span style="color: #6b6b6b">System</span><span style="color: #c03221">.</span><span style="color: #02a894">out</span><span style="color: #c03221">.</span><span style="color: #02a894">println</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;After brodcasting centroids&quot;</span><span style="color: #c03221">);</span>
		  <span style="color: #6b6b6b">printTable</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
		  <span style="color: #03254e">//load data </span>
		  <span style="color: #6b6b6b">ArrayList</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">DoubleArray</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">dataPoints</span> <span style="color: #c03221">=</span> <span style="color: #6b6b6b">loadData</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">fileNames</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">vectorSize</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">);</span>
		  <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">DoubleArray</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">globalTable</span> <span style="color: #c03221">=</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">DoubleArray</span><span style="color: #c03221">&gt;(</span><span style="color: #7d518c">0</span><span style="color: #c03221">,</span>  <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">DoubleArrPlus</span><span style="color: #c03221">());</span>
		  <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">DoubleArray</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">previousCenTable</span> <span style="color: #c03221">=</span>  <span style="color: #3976cc">null</span><span style="color: #c03221">;</span>
		  <span style="color: #03254e">//iterations</span>
		  <span style="color: #3976cc">for</span><span style="color: #c03221">(</span><span style="color: #3976cc">int</span> <span style="color: #6b6b6b">iter</span><span style="color: #c03221">=</span><span style="color: #7d518c">0</span><span style="color: #c03221">;</span> <span style="color: #6b6b6b">iter</span> <span style="color: #c03221">&lt;</span> <span style="color: #6b6b6b">iteration</span><span style="color: #c03221">;</span> <span style="color: #6b6b6b">iter</span><span style="color: #c03221">++){</span>
			  <span style="color: #03254e">// clean contents in the table.</span>
			  <span style="color: #6b6b6b">globalTable</span><span style="color: #c03221">.</span><span style="color: #02a894">release</span><span style="color: #c03221">();</span>
			  <span style="color: #6b6b6b">previousCenTable</span> <span style="color: #c03221">=</span>  <span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">;</span>
			  <span style="color: #6b6b6b">cenTable</span> <span style="color: #c03221">=</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;&gt;(</span><span style="color: #7d518c">0</span><span style="color: #c03221">,</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">DoubleArrPlus</span><span style="color: #c03221">());</span>
			  <span style="color: #6b6b6b">System</span><span style="color: #c03221">.</span><span style="color: #02a894">out</span><span style="color: #c03221">.</span><span style="color: #02a894">println</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;Iteraton No.&quot;</span><span style="color: #c03221">+</span><span style="color: #6b6b6b">iter</span><span style="color: #c03221">);</span>
			  <span style="color: #03254e">//compute new partial centroid table using previousCenTable and data points</span>
			  <span style="color: #6b6b6b">computation</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">previousCenTable</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">dataPoints</span><span style="color: #c03221">);</span>
			  <span style="color: #03254e">/****************************************/</span>
			  <span style="color: #6b6b6b">push</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;main&quot;</span><span style="color: #c03221">,</span> <span style="color: #9b802e">&quot;push_&quot;</span><span style="color: #c03221">+</span><span style="color: #6b6b6b">iter</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">globalTable</span> <span style="color: #c03221">,</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">Partitioner</span><span style="color: #c03221">(</span><span style="color: #3976cc">this</span><span style="color: #c03221">.</span><span style="color: #02a894">getNumWorkers</span><span style="color: #c03221">()));</span>
			  <span style="color: #03254e">//we can calculate new centroids</span>
			  <span style="color: #6b6b6b">calculateCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">globalTable</span><span style="color: #c03221">);</span>
			  <span style="color: #6b6b6b">pull</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;main&quot;</span><span style="color: #c03221">,</span> <span style="color: #9b802e">&quot;pull_&quot;</span><span style="color: #c03221">+</span><span style="color: #6b6b6b">iter</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">globalTable</span><span style="color: #c03221">,</span> <span style="color: #3976cc">true</span><span style="color: #c03221">);</span>
			  <span style="color: #03254e">/****************************************/</span>
			  <span style="color: #6b6b6b">printTable</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
		  <span style="color: #c03221">}</span>
		  <span style="color: #03254e">//output results</span>
		  <span style="color: #3976cc">if</span><span style="color: #c03221">(</span><span style="color: #3976cc">this</span><span style="color: #c03221">.</span><span style="color: #02a894">isMaster</span><span style="color: #c03221">()){</span>
			  <span style="color: #6b6b6b">outputCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span>  <span style="color: #6b6b6b">conf</span><span style="color: #c03221">,</span>   <span style="color: #6b6b6b">context</span><span style="color: #c03221">);</span>
		  <span style="color: #c03221">}</span>
	 <span style="color: #c03221">}</span>
</pre></div>
    </div>
    <div id="regroup-allgather" class="tab-pane fade">
      <h3>Use Regroup and allgather collective communication to do synchronization</h3>
	<div class="highlight" style="background: #272822"><pre style="line-height: 125%"><span></span><span style="color: #3976cc">private</span> <span style="color: #3976cc">void</span> <span style="color: #02a894">runKmeans</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">List</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">String</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">fileNames</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">Configuration</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">Context</span> <span style="color: #6b6b6b">context</span><span style="color: #c03221">)</span> <span style="color: #3976cc">throws</span> <span style="color: #6b6b6b">IOException</span> <span style="color: #c03221">{</span>
		  <span style="color: #03254e">// -----------------------------------------------------</span>
		  <span style="color: #03254e">// Load centroids</span>
		  <span style="color: #03254e">//for every partition in the centoid table, we will use the last element to store the number of points </span>
		  <span style="color: #03254e">// which are clustered to the particular partitionID</span>
		  <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">DoubleArray</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">cenTable</span> <span style="color: #c03221">=</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;&gt;(</span><span style="color: #7d518c">0</span><span style="color: #c03221">,</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">DoubleArrPlus</span><span style="color: #c03221">());</span>
		  <span style="color: #3976cc">if</span> <span style="color: #c03221">(</span><span style="color: #3976cc">this</span><span style="color: #c03221">.</span><span style="color: #02a894">isMaster</span><span style="color: #c03221">())</span> <span style="color: #c03221">{</span>
			  <span style="color: #6b6b6b">loadCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">vectorSize</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">.</span><span style="color: #02a894">get</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">KMeansConstants</span><span style="color: #c03221">.</span><span style="color: #02a894">CFILE</span><span style="color: #c03221">),</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">);</span>
		  <span style="color: #c03221">}</span>
		  <span style="color: #6b6b6b">System</span><span style="color: #c03221">.</span><span style="color: #02a894">out</span><span style="color: #c03221">.</span><span style="color: #02a894">println</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;After loading centroids&quot;</span><span style="color: #c03221">);</span>
		  <span style="color: #6b6b6b">printTable</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
		  <span style="color: #03254e">//broadcast centroids</span>
		  <span style="color: #6b6b6b">broadcastCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
		  <span style="color: #03254e">//after broadcasting</span>
		  <span style="color: #6b6b6b">System</span><span style="color: #c03221">.</span><span style="color: #02a894">out</span><span style="color: #c03221">.</span><span style="color: #02a894">println</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;After brodcasting centroids&quot;</span><span style="color: #c03221">);</span>
		  <span style="color: #6b6b6b">printTable</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
		  <span style="color: #03254e">//load data </span>
		  <span style="color: #6b6b6b">ArrayList</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">DoubleArray</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">dataPoints</span> <span style="color: #c03221">=</span> <span style="color: #6b6b6b">loadData</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">fileNames</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">vectorSize</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">conf</span><span style="color: #c03221">);</span>
		  <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;</span><span style="color: #6b6b6b">DoubleArray</span><span style="color: #c03221">&gt;</span> <span style="color: #6b6b6b">previousCenTable</span> <span style="color: #c03221">=</span>  <span style="color: #3976cc">null</span><span style="color: #c03221">;</span>
		  <span style="color: #03254e">//iterations</span>
		  <span style="color: #3976cc">for</span><span style="color: #c03221">(</span><span style="color: #3976cc">int</span> <span style="color: #6b6b6b">iter</span><span style="color: #c03221">=</span><span style="color: #7d518c">0</span><span style="color: #c03221">;</span> <span style="color: #6b6b6b">iter</span> <span style="color: #c03221">&lt;</span> <span style="color: #6b6b6b">iteration</span><span style="color: #c03221">;</span> <span style="color: #6b6b6b">iter</span><span style="color: #c03221">++){</span>
			  <span style="color: #6b6b6b">previousCenTable</span> <span style="color: #c03221">=</span>  <span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">;</span>
			  <span style="color: #6b6b6b">cenTable</span> <span style="color: #c03221">=</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">Table</span><span style="color: #c03221">&lt;&gt;(</span><span style="color: #7d518c">0</span><span style="color: #c03221">,</span> <span style="color: #3976cc">new</span> <span style="color: #6b6b6b">DoubleArrPlus</span><span style="color: #c03221">());</span>
			  <span style="color: #6b6b6b">System</span><span style="color: #c03221">.</span><span style="color: #02a894">out</span><span style="color: #c03221">.</span><span style="color: #02a894">println</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;Iteraton No.&quot;</span><span style="color: #c03221">+</span><span style="color: #6b6b6b">iter</span><span style="color: #c03221">);</span>
			  <span style="color: #03254e">//compute new partial centroid table using previousCenTable and data points</span>
			  <span style="color: #6b6b6b">computation</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">previousCenTable</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">dataPoints</span><span style="color: #c03221">);</span>
			  <span style="color: #03254e">/****************************************/</span>
			  <span style="color: #03254e">//regroup and allgather to synchronized centroids</span>
			  <span style="color: #6b6b6b">regroup</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;main&quot;</span><span style="color: #c03221">,</span> <span style="color: #9b802e">&quot;regroup_&quot;</span><span style="color: #c03221">+</span><span style="color: #6b6b6b">iter</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span> <span style="color: #3976cc">null</span><span style="color: #c03221">);</span>
			  <span style="color: #03254e">//we can calculate new centroids</span>
			  <span style="color: #6b6b6b">calculateCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
			  <span style="color: #6b6b6b">allgather</span><span style="color: #c03221">(</span><span style="color: #9b802e">&quot;main&quot;</span><span style="color: #c03221">,</span> <span style="color: #9b802e">&quot;allgather_&quot;</span><span style="color: #c03221">+</span><span style="color: #6b6b6b">iter</span><span style="color: #c03221">,</span> <span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
			  <span style="color: #03254e">/****************************************/</span>
			  <span style="color: #6b6b6b">printTable</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">);</span>
		  <span style="color: #c03221">}</span>
		  <span style="color: #03254e">//output results</span>
		  <span style="color: #3976cc">if</span><span style="color: #c03221">(</span><span style="color: #3976cc">this</span><span style="color: #c03221">.</span><span style="color: #02a894">isMaster</span><span style="color: #c03221">()){</span>
			  <span style="color: #6b6b6b">outputCentroids</span><span style="color: #c03221">(</span><span style="color: #6b6b6b">cenTable</span><span style="color: #c03221">,</span>  <span style="color: #6b6b6b">conf</span><span style="color: #c03221">,</span>   <span style="color: #6b6b6b">context</span><span style="color: #c03221">);</span>
		  <span style="color: #c03221">}</span>
	 <span style="color: #c03221">}</span>
</pre></div>
   </div>
  </div>


## Step 4 --- Compute local centroids

```java
private void computation(Table<DoubleArray> cenTable, Table<DoubleArray> previousCenTable,ArrayList<DoubleArray> dataPoints){
    double err=0;
    for(DoubleArray aPoint: dataPoints){
    //for each data point, find the nearest centroid
        double minDist = -1;
        double tempDist = 0;
        int nearestPartitionID = -1;
        for(Partition ap: previousCenTable.getPartitions()){
            DoubleArray aCentroid = (DoubleArray) ap.get();
            tempDist = calcEucDistSquare(aPoint, aCentroid, vectorSize);
            if(minDist == -1 || tempDist < minDist){
                minDist = tempDist;
                nearestPartitionID = ap.id();
            }
        }
        err+=minDist;

        //for the certain data point, found the nearest centroid.
        // add the data to a new cenTable.
        double[] partial = new double[vectorSize+1];
        for(int j=0; j < vectorSize; j++){
            partial[j] = aPoint.get()[j];
        }
        partial[vectorSize]=1;

        if(cenTable.getPartition(nearestPartitionID) == null){
            Partition<DoubleArray> tmpAp = new Partition<DoubleArray>(nearestPartitionID, new DoubleArray(partial, 0, vectorSize+1));
            cenTable.addPartition(tmpAp);
        }else{
             Partition<DoubleArray> apInCenTable = cenTable.getPartition(nearestPartitionID);
             for(int i=0; i < vectorSize +1; i++){
             apInCenTable.get().get()[i] += partial[i];
             }
        }
    }
    System.out.println("Errors: "+err);
}
```

## Step 5 --- Calculate new centroids

```java
private void calculateCentroids( Table<DoubleArray> cenTable){
    for( Partition<DoubleArray> partialCenTable: cenTable.getPartitions()){
        double[] doubles = partialCenTable.get().get();
        for(int h = 0; h < vectorSize; h++){
            doubles[h] /= doubles[vectorSize];
        }
        doubles[vectorSize] = 0;
	}
	System.out.println("after calculate new centroids");
    printTable(cenTable);
}
```

## COMPILE
```bash
cd $HARP_ROOT_DIR
mvn clean package
cd $HARP_ROOT_DIR/harp-tutorial-app
cp target/harp-tutorial-app-1.0-SNAPSHOT.jar $HADOOP_HOME
cd $HADOOP_HOME
```

## USAGE
Run Harp K-Means:
```bash
hadoop jar harp-tutorial-app-1.0-SNAPSHOT.jar edu.iu.kmeans.common.KmeansMapCollective <numOfDataPoints> <num of Centroids> <size of vector> <number of map tasks> <number of iteration> <workDir> <localDir> <communication operation>

   <numOfDataPoints>: the number of data points you want to generate randomly
   <num of centriods>: the number of centroids you want to clustering the data to
   <size of vector>: the number of dimension of the data
   <number of map tasks>: number of map tasks
   <number of iteration>: the number of iterations to run
   <work dir>: the root directory for this running in HDFS
   <local dir>: the harp kmeans will firstly generate files which contain data points to local directory. Set this argument to determine the local directory.
   <communication operation> includes:
		[allreduce]: use allreduce operation to synchronize centroids
		[regroup-allgather]: use regroup and allgather operation to synchronize centroids
		[broadcast-reduce]: use broadcast and reduce operation to synchronize centroids
		[push-pull]: use push and pull operation to synchronize centroids
```

For example:

```bash
hadoop jar harp-tutorial-app-1.0-SNAPSHOT.jar edu.iu.kmeans.common.KmeansMapCollective 1000 10 10 2 10 /kmeans /tmp/kmeans allreduce
```

Fetch the results:
```bash
hdfs dfs -ls /
hdfs dfs -cat /kmeans/centroids/*
```






