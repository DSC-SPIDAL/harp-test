## Project: Multiclass Logistic Regression using Harp    

@author: Chao-Hong Chen, Qiuwei Shou

### Background

Logistic Regression is regression analysis method used by statisticians. Logistic Regression is a type of regression model where the class labels are categorical and tries to find the relationship between the set of independent variables and a dependent variable. Most commonly used logistic regression model utilizes binary dependent variables (i.e. there are only two values - true/false, pass/fail, etc ). When more than two categorical variables (discrete number of outcomes) are involved, we call it
multinomial/multi-class logistic regression.

We choose Stochastic Gradient descent (SGD) to solve MLR by running SGD seperately for each class. This tutorial works on the [RCV1](http://jmlr.csail.mit.edu/papers/volume5/lewis04a/lyrl2004_rcv1v2_README.htm) dataset.

### Usage

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
