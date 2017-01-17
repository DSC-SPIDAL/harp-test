Put data on hdfs
```
hdfs dfs -mkdir /rcv1v2
hdfs dfs -put input_data/* /rcv1v2
```

Compile SequentialMLR
```
cd source_code/SequentialMLR
javac SequentialMLR.java
javac Verify.java
```

Compile harpMLR
```
cd source_code/harpMLR/
ant
```

Run the benchmark in the report
```
cd source_code/harpMLR/
./run.sh
```

Run SequentialMLR
```
Usage: SequentialMLR <alpha> <#iter> <#terms> <topics> <qrels> <training data> <output file>
```
Example
```
cd source_code/SequentialMLR/
java SequentialMLR 1.0 100 47236 ../../input_data/rcv1.topics.txt ../../input_data/rcv1-v2.topics.qrels ../../input_data/lyrl2004_vectors_train.dat weights
```

Run harpMLR
```
Usage: MLRMapCollective <alpha> <#iter> <#terms> <#Map Task> <#thread> <topics> <qrels>  <training data> <output file>
```
Example
```
cd source_code/harpMLR/
hadoop jar build/harp3-app-hadoop-2.6.0.jar edu.iu.MLR.MLRMapCollective 1.0 100 47236 2 16 /rcv1v2/rcv1.topics.txt /rcv1v2/rcv1-v2.topics.qrels /rcv1v2/input /MLR/
```
