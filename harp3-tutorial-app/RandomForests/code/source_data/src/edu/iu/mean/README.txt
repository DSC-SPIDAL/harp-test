An example for you to reference.


#Compile

cd $HARP3_PROJECT_HOME/harp3-app
ant
cp $HARP3_PROJECT_HOME/harp3-app/build/harp3-app-hadoop-2.6.0.jar $HADOOP_HOME

#run
cd $HADOOP_HOME
hadoop jar harp3-app-hadoop-2.6.0.jar edu.iu.mean.MeanMapCollective 10 3 2 /mean /tmp/mean


#fetch results

  #root directory
hdfs dfs -ls /mean

  #generated data
hdfs dfs -cat /mean/data/

  #mean
hdfs dfs -cat /mean/out/mean_out/*
