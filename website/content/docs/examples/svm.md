---
title: Harp Support Vector Machine
---


# ABSTRACT
Support vector machines(SVM)[1] are supervised learning models with associated learning algorithms that analyze data used for classification and regression analysis. Harp[2] is good for SVM implementation but it needs to overcome synchronizing the global support vectors. What we are following is the Iterative SVM version[3] implemented on Hadoop. In each iteration, each machine computes the support vectors and does an all-reduce to collective the whole global support vector set and treat it as the extra training data.


# METHOD
The Harp based binary class support vector machine algorithm works as follows. The training set of the algorithm is split into subsets. Each node within a distributed system classifies sub dataset locally via SVM algorithm and gets α values (i.e. support vectors (SVs)), and then passes the calculated SVs to global SVs to merge them. In Map stage of MapReduce job, the subset of training set is combined with global support vectors. In Collect step, we do a all reduce operation to broadcast the local support vectors and combine to get the global support vectors. 

The algorithm can be explained as follows. First, each node gets the global support vectors set, then merges global SVs set with subsets of local training dataset and classifies using SVM algorithm. Finally, all the computed SVs set in nodes are merged. Thus, algorithm saves global SVs set with new ones and does the next round to accurate the result. 

For training SVM classifier functions, we used LibSVM with various kernels. Appropriate parameters and values were found by cross validation test. We used 10-fold cross validation method. 

We have three datasets for use: Iris, MNIST[4], PASCAL VOL.


# PROCEDURE

![procedure](/img/svm/procedure.png)

# SVM
Support Vector Machine (SVM) is a discriminative classifier formally defined by a separating hyperplane. In other words, given labeled training data (supervised learning), the algorithm outputs an optimal hyperplane which categorizes new examples[5].

![svm](/img/svm/svm.png)


# RESULT
We can see from the figure that with larger number of mappers, the number of support vectors has greater  gradient decent. Less iterations are needed to reach the final result which however remains the same.

![result](/img/svm/result.png)


# COMPARISON
The speed up time both has an linear acceleration along with the number of mappers. Harp performance is better than Hadoop since it reduces I/Os of communication.

![comparison](/img/svm/comparison.png)


# COMCLUSION
From the result of our implementation, we can see that support vector machine models have better performance on Hadoop by adding on the plugin Harp. The performance is optimized by 80% of MNIST-Harp compared to MNIST-Hadoop. Hadoop generates read and write operations at each iteration while Harp stores support vectors in cache so the runtime is largely reduced. Finally, for each model we can find a global support vector set which is stable and therefore represent the output result. 


# REFERENCE
[1] Suykens, Johan AK, and Joos Vandewalle. “Least squares support vector machine classifiers.” Neural processing letters 9.3 (1999): 293-300.

[2] Zhang, Bingjing, Yang Ruan, and Judy Qiu. “Harp: Collective communication on hadoop.” Cloud Engineering (IC2E), 2015 IEEE International Conference on. IEEE, 2015.

[3] Çatak, Ferhat Özgür, and Mehmet Erdal Balaban. “A MapReduce-based distributed SVM algorithm for binary classification.” Turkish Journal of Electrical Engineering & Computer Sciences 24.3 (2016): 863-873.

[4] LeCun, Yann, Corinna Cortes, and Christopher JC Burges. “The MNIST database of handwritten digits.” (1998).

[5] docs.opencv.org/doc/tutorials/ml/introduction_to_svm/introduction_to_svm.html

