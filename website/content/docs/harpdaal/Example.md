---
title: Harp-DAAL-SGD
---

## Matrix Factorization based on Stochastic Gradient Descent (SGD)

Matrix Factorization based on Stochastic Gradient Descent (MF-SGD for short) is an algorithm widely used in recommender systems. MF-SGD is one of the implemented algorithm within Harp, however, DAAL current does not have 
a MF-SGD kernel. We first implement a MF-SGD kernel inside DAAL's framework and then interface it with that of Harp. MF-SGD aims to factorize a sparse matrix into two dense matrices named mode W and model H as follows. 

$$V = W H$$

Matrix $V$ includes both training data and test data, a machine learning inspired kernel with use the training data to approximate the model matrices $W$ and $H$. A standard SGD procedure will update the model $W$ and $H$
when it trains each training data, i.e., an entry in matrix $V$ in the following formula. 

$$E_{ij} = V_{ij} - \sum_{k=0}^r W_{ik} H_{kj}$$

$$W^t_{i*} = W^{t-1}_{i*} - \eta (E^{t-1}_{ij}\cdot H^{t-1}_{*j} - \lambda \cdot W^{t-1}_{i*})$$

$$H^t_{*j} = H^{t-1}_{*j} - \eta (E^{t-1}_{ij}\cdot W^{t-1}_{i*} - \lambda \cdot H^{t-1}_{*j})$$

## Implementation of SGD within Harp-DAAL Framework

We chose a taskflow based programming model in Harp-DAAL-SGD application. Within this model, a model matrix $W$ is initially 
released into the DAAL's data structures, which is a *HomogenNumericTable*, and stay within DAAL till the end of program's life cycle.  
The other model data, matrix $H$, is loaded into DAAL's data structure after every occurrence of the model rotation. 
The training model data, $V$, is stored in Harp's side. 
Each training point is represented by a $Task$ object. Each $Task$ object consists of three fields:

* Position of the associated row in matrix $W$
* Position of the associated column in matrix $H$
* The value of the training data $V$

The taskflow is organized on the Harp side, which then delivers the tasks into DAAL's computation kernels. The DAAL sgd kernel will get the corresponding row and column from the $W$ and $H$ matrices stored in its 
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
If the computation time is not dominant in the total execution time, which is related to the dimension of $W$ and $H$, then the 
additional overhead of using DAAL will make the codes less competitive than the original pure Java based implementation. 
Once the dimension rises, the computation becomes more intensive, and the advantages of DAAL and TBB will
appear and outperform the original implementation on Java threads.   


