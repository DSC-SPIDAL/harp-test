---
title: Harp Multiclass Logistic Regression with Stochastic Gradient Descent
---

Multiclass logistic regression (MLR) is a classification method that generalizes logistic regression to multiclass problems, i.e. with more than two possible discrete outcomes. That is, it is a model that is used to predict the probabilities of the different possible outcomes of a categorically distributed dependent variable, given a set of independent variables.

The process of the MLR algorithm is:

1. Use the weight `W` to predict the label of current data point.

2. Compare the output and the answer.

3. Use SGD to approximate `W`.

4. Repeat step 1 to 3 with each label and their weights.

Stochastic gradient descent (SGD) is a stochastic approximation of the gradient descent optimization method for minimizing an objective function that is written as a sum of differentiable functions. In other words, SGD tries to find minimums or maximums by iteration. As the algorithm sweeps through the training set, it performs the update for each training example. Several passes can be made over the training set until the algorithm converges.

The SGD algorithm can be described as following:

1. Randomly assign the weight `W`.

2. Shuffle `N` data points.

3. Go through `N` data points and do gradient descent.

4. Repeat step 2 and 3 `K` times.

We use Harp to accelerate this sequential algorithm with regroup, rotate, allgather, and dynamic scheduling. Like Harp K-Means, you need to add your file path in `$HARP3_PROJECT_HOME/harp3-app/build.xml` and use `ant` to compile.
```xml
...
<src path="src" />
    <include name="edu/iu/fileformat/**" />
    <include name="edu/iu/benchmark/**" />
    <include name="edu/iu/dymoro/**" />
    <include name="edu/iu/kmeans/**" />
    <include name="edu/iu/lda/**" />
    <include name="edu/iu/sgd/**" />
    <include name="edu/iu/ccd/**" />
    <include name="edu/iu/wdamds/**" />
    <include name="<your file path>" />
    ...
```

Definitions:

* `N` is the number of data points
* `M` is the number of features
* `W` is the `N*M` weight matrix
* `K` is the number of iteration




