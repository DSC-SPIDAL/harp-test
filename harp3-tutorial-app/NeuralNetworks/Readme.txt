This code required configuring the jobless library. To do so copy jblas-1.2.4.jar to this directory:
software/hadoop-2.6.0/share/hadoop/common/lib/jblas-1.2.4.jar

#Compile

cd $HARP3_PROJECT_HOME/harp3-app; ant; cp $HARP3_PROJECT_HOME/harp3-app/build/harp3-app-hadoop-2.6.0.jar $HADOOP_HOME;

#run
cd $HADOOP_HOME; hadoop jar harp3-app-hadoop-2.6.0.jar edu.iu.NN.NNMapCollective 1 10 2 100 /NN /tmp/NN

Command Line Arguments:
   <num of layers>: the number of neural network layers (should be always 1 for this version)   <size of layers>: the number of neurons in a layer   <number of map tasks>: number of map tasks   <number of epchs>: the number of training epochs   <work dir>: the root directory for this running in HDFS   <local dir>: this argument to determines the local directory

# To check the output:
hadoop dfs -cat /NN/out/NN_out/*


To run the sequential code:

1. set CLASSPATH=the directory with the path to the folder NeuralNetwork-master\NeuralNetwork-master\src\main\java;the directory with the path to the jblas-1.2.4.jar
2. cd to NeuralNetwork-master\NeuralNetwork-master\src\main\java\org\dvincent1337
3. javac neuralNet\NeuralNetwork.java
4. cd ../..
5. java org.dvincent1337.neuralNet.NeuralNetwork


