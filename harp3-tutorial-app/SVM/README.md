## Project: Parallel SVM on Harp    

@author: Yiming Zou, Yueqi Tan

### Background

Support vector machines(SVM) are supervised learning models with associated learning algorithms that analyze data used for classification and regression analysis. Harp is good for SVM implementation but it needs to overcome synchronizing the global support vectors. What we are following is the Iterative SVM version implemented on Hadoop. In each iteration, each machine computes the support vectors and does an all-reduce to collective the whole global support vector set and treat it as the extra training data.

The Harp based binary class support vector machine algorithm works as follows. The training set of the algorithm is split into subsets. Each node within a distributed system classifies sub dataset locally via SVM algorithm and gets α values (i.e. support vectors), and then passes the calculated SVs to global SVs to merge them. In Map stage of MapReduce job, the subset of training set is combined with global support vectors. In Collect step, we do an allreduce operation to broadcast the local support vectors and combine to get the global support vectors. 

The datasets used are Iris, MNIST, PASCAL VOL.

### Usage

Migrate the source code
```bash
cp -r edu/iu/svm /harp3-project/harp3-app/src/
cp -r libsvm /harp3-project/harp3-app/src/
```

Open `build.xml` and add
```xml
<include name=“edu/iu/svm/**” />
<include name=“libsvm/**” />
```

Compile Harp SVM
```bash
ant
```

Run Harp SVM
```bash
hadoop jar build/harp3-app-hadoop-2.6.0.jar edu.iu.svm.IterativeSVM <number of mappers> <number of iteration> <output path in HDFS> <dataset path>
```

Fetch the result
```bash
hdfs dfs -get <output path in HDFS> <path you want to store the output>
```




