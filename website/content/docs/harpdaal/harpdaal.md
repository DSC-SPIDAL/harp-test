---
title: Harp-DAAL Framework
---

[harp3-daal-app](https://github.iu.edu/IU-Big-Data-Lab/Harp/tree/master/harp3-daal-app) includes the application implemented within the Harp-DAAL framework. 

## Introduction of Harp-DAAL 

Intel® Data Analytics Acceleration Library (DAAL) is a library from Intel that aims to provide the users of some highly optimized building blocks for data analytics and machine learning applications. 
For each of its kernel, DAAL has three modes:

* A Batch Processing mode is the default mode that works on an entire dataset that fits into the memory space of a single node.
* A Online Processing mode works on the blocked dataset that is streamed into the memory space of a single node.
* A Distributed Processing mode works on datasets that are stored in distributed systems like multiple nodes of a cluster.

Within DAAL's framework, the communication layer of the Distributed Processing mode is left to the users, which could be any of the user-defined middleware for communication. 
The goal of Harp-DAAL project is thus to fit Harp, a plug-in into Hadoop ecosystem, into the Distributed Processing mode of DAAL. Compared to contemporary communication libraries, 
Harp has the advantages as follows:

* Harp has MPI-like collective communication operations that are highly optimized for big data problems.
* Harp has efficient and innovative computation models for different machine learning problems.

![Combination of Harp and DAAL](/img/harpdaal/Harp-DAAL-Structure.png)

The original Harp project has all of its codes written in Java, which is a common choice within the Hadoop ecosystem. 
The downside of the pure Java implementation is the slow speed of the computation kernels that are limited by Java's data management. 
Since manycore architectures devices are becoming a mainstream choice for both server and personal computer market, 
the computation kernels should also fully take advantage of the architecture's new features, which are also beyond the capability of the Java language. 
Thus, a reasonable solution for Harp is to accomplish the computation tasks by invoking C++ based kernels from libraries such as DAAL. 

![Harp-DAAL within HPC-BigData Stack](/img/harpdaal/Harp-DAAL-Diag.png)

## Compile and Run Harp-DAAL 

To compile Harp-DAAL, users shall first install Intel's DAAL repository. The source code is available in their github page
https://github.com/01org/daal
After installation, please follow the procedure as below:

1.setup the DAALROOT environment in the .bashrc file
```bash
export DAALROOT=/path-to-daal-src/
```
2.Enter the harp3-daal-app directory, and build the apps
```bash
cd harp3-daal-app
ant
```
3.Create scripts to run the examples within harp3-daal-app

## Interface of Harp-DAAL

Harp-DAAL now provides a group of classes under the path *Harp/harp3-daal-app/src/edu/iu/daal*, which manipulates the data transfer
between Harp's data structure and that of DAAL.

* RotatorDaal: a rotator which internally converts the H matrix from Harp table to DAAL's NumericTable
* RotateTaskDaal: the tasks executed by RotatorDaal
* HomogenTableHarpMap: convert data between DAAL's HomogenNumericTable and Harp's map
* HomogenTableHarpTable: convert data between DAAL's HomogenNumericTable and Harp's table

Within the *RotatorDaal*, the data transfer between Harp and DAAL is also overlapped by the computation work in another pipeline. Thus, if there is enough computation workload, the 
overhead of data conversion could be significantly reduced. It is also very straightforward to invoke these conversion tools. 

```java

//create a conversion class between harp map and daal's table
HomogenTableHarpMap<double[]> convert_wTable = new HomogenTableHarpMap<double[]>(wMap, wMap_index, wMap_daal, wMap_size, r, numThreads);
convert_wTable.HarpToDaalDouble();

//create a conversion class between a harp table and a daal table
converter = new HomogenTableHarpTable<I, P, Table<P> >(table, this.daal_table, table.getNumPartitions(), this.rdim, this.numThreads);
converter.HarpToDaalDouble();

```

More details of the usage of Harp-DAAL interface can be found in examples.
















