## Random Forests 

@author: Katherine Metcalf and Raksha Kumaraswamy.

### Background

Random Forests are a powerful nonparametric statistical class of learning algorithms for both classification and regression tasks. The learned model is a set of trees, where each tree is built by utilizing a bootstrapped random subsample of the training data, hence the name ”Random Forests” - a random approach to bootstrap samples, combined with a model that is a set of trees, a forest.

This tutorial works on the [AirLine](http://stat-computing.org/dataexpo/2009/the-data.html) dataset.

### Usage

Compiling:
To compile the code and create an executable jar please utilize the attached build.xml as it is different from the original build.xml provided, as the sequential code has dependencies with external jar's that need to be built into the final executable jar. 

The created jar, harp3-app-hadoop-2.6.0.jar is built into the build folder specified in the XML file. It takes the following arguments:
<trainFolder> <trainNameFormat> <testFile> <doBootstrapSampling> <workDir> <localDir> <numTrees>

	<trainFolder>
		- path to train folder 
	<trainNameFormat>
		- format of training file names under previously specified folder (based on the datafile we create, described below - either 'subject10' or 'loc_')
	<testFile>
		- path to test file
	<doBootstrapSampling>
		- Global data created if 1. Else, utilizes the specified files as local files based on numMappers (max 8)
	<workDir>
		- HDFS directory
	<localDir>
		- temporary local directory to write files into. Final results are also written into this directory.
	<numTrees>
		- num trees built by each mapper

Data: 
We utilize the PAMAP2 data which is available at: https://archive.ics.uci.edu/ml/datasets/PAMAP2+Physical+Activity+Monitoring
We do not attach the data to the Github repo because it is too large, and can be obtained easily. To format the data and convert it to the necessary format, please run the attached buildDataSet.sh script. This needs to be kept above the PAMAP2 folder, or the relative paths can be changed in the folder as desired. Primarily, it utilizes the data.csv to create a training.csv and test.csv. After this, it utilizes the training.csv to create the corresponding Local-Label data in 'loc_' files, and Local-Feature-Label data in 'subject10' files.

Good luck!
- Katherine Metcalf & Raksha Kumaraswamy.
