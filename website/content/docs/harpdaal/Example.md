---
title: Harp-DAAL-SGD
---

## Matrix Factorization based on Stochastic Gradient Descent (MF-SGD)

Matrix Factorization based on Stochastic Gradient Descent (MF-SGD for short) is an algorithm widely used in recommender systems. 
It aims to factorize a sparse matrix into two low-rank matrices named mode W and model H as follows. 

<img src="/img/harpdaal/vWH.png" width="10%" height="10%"><br>

The rating Matrix V includes both training data and test data. A learning algorithm uses the training data to update matrices W and H. 
For instance, a standard SGD procedure will update the model W and H
while iterating over each training data point, i.e., an entry in matrix V in the following formula. 

<img src="/img/harpdaal/error-compute.png" width="25%" height="25%"><br>

<img src="/img/harpdaal/update-W.png" width="40%" height="40%"><br>

<img src="/img/harpdaal/update-H.png" width="40%" height="40%"><br>

After the training process, the test data points in matrix V could be used to verify the effectiveness of the training matrix by computing the RMSE values of 
the difference 

<img src="/img/harpdaal/error-compute.png" width="25%" height="25%"><br>

## Implementation of SGD within Harp-DAAL Framework

The main body of Harp-DAAL-SGD is the *mapCollective* function of class *SGDDaalCollectiveMapper*. 

```java

protected void mapCollective(KeyValReader reader, 
            Context context) throws IOException, InterruptedException {

            LinkedList<String> vFiles = getVFiles(reader);

            try {
                runSGD(vFiles, context.getConfiguration(), context);
            } catch (Exception e) {
                LOG.error("Fail to run SGD.", e);
            }

}

```

We chose a taskflow based programming model in Harp-DAAL-SGD application. Within this model, a model matrix W is initially 
released into the DAAL's data structures, which is a *HomogenNumericTable*, and stay within DAAL till the end of program's life cycle. 
The other model data, matrix H, is loaded into DAAL's data structure after every occurrence of the model rotation. 
The training model data, V, is stored in Harp's side. 
Each training point is represented by a Task object. Each $Task$ object consists of three fields:

* Position of the associated row in matrix W
* Position of the associated column in matrix H
* The value of the training data V

The taskflow is organized on the Harp side, which then delivers the tasks into DAAL's computation kernels. The DAAL sgd kernel will get the corresponding row and column from the W and H matrices stored in its 
data structure, and complete the computation and updating work. 

## TBB versus Java Multithreading

In the original Bingjing's SGD implementation, It uses raw Java threads to accomplish the tasks in parallel. 
Accordingly, it implements its own scheduler and the policy, the timer and the pipeline. In contrast, our Harp-DAAL-SGD uses Intel's Threading Building Block
(TBB) to compute the SGD tasks in parallel. TBB provides the users of many parallel algorithm templates, and we use the *parallel_for* template to achieve the computation within SGD. 

Compared to the raw Java threads based parallel computing, the use of TBB has the following benefits:

* The users only take care of the parallel tasks instead of the raw threads.
* For each TBB thread, the C++ codes have more parallelism from the optimization of compilers. 
* TBB's scheduler will probably enjoy a better load balance than that of user's own scheduler. 

Although DAAL with TBB could give us faster computation of operations such as vector inner product and matrix-vector multiplication, it still has some additional overhead within the Harp environment. DAAL has a different
Data structure than Harp, so we need to convert data from Harp to DAAL, from Java side to C++ side. 
If the computation time is not dominant in the total execution time, which is related to the dimension of W and H, then the 
additional overhead of using DAAL will make the codes less competitive than the original pure Java based implementation. 
Once the dimension rises, the computation becomes more intensive, and the advantages of DAAL and TBB will
appear and outperform the original implementation on Java threads. 


