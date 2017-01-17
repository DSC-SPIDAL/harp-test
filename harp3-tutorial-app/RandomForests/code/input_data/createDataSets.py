#! /usr/bin/python

import os, sys, random;
import numpy as np;

'''
The code in this file splits the PAMAP2 data set into 8 data subsets. The data
subsets represents data collected from each of the 6 populated continents, where
the two most populated continents Asia and Africa each have two locations. The
data stored at each location represents data that was collected in that
geographical region. Therefore, there is a different probability distribution
over the class labels for each geographical area (ie. each data location).
'''

# The location of the original PAMAP2 training set relative to the location of this
# script
train_data_loc = "PAMAP2_Dataset/Preprocessed/training.csv";
test_data_loc = "PAMAP2_Dataset/Preprocessed/test.csv";
# The location where the data subsets are to be written
out_dir = "PAMAP2_Dataset/Preprocessed/";
# The number of data subsets that are to be created
num_loc = 8;

# Holds all of the PAMAP2 data
train_data = np.loadtxt(train_data_loc, dtype='float', delimiter=' ');
all_data = train_data;
#test_data = np.loadtxt(test_data_loc, dtype='float', delimiter=' ');
# This the entire data set
#all_data = np.zeros((train_data.shape[0] + test_data.shape[0], train_data.shape[1]));
#all_data[:train_data.shape[0]:,:] = train_data;
#all_data[train_data.shape[0]:,:] = test_data;
# Count the number of times each label occurs
labels = np.unique(all_data[:,-1]).astype(int);
print "alsdkfj"
label_counts = np.bincount(all_data[:,-1].astype(int));

# Create the specified number of data subsets
for i in range(num_loc):
    # This array tracks the number of data points for each class included at
    # this location
    num_dp = np.zeros((labels.shape[0]));
    # Randomly select some percentage of the data points for a given class to be
    # included at this location
    for j in range(labels.shape[0]):
        # Randomly select a percentage of the data points to grab
        num_dp[j] = int(label_counts[labels[j]] * random.random());
    # Create the data matrix for this location
    loc_data = np.zeros((np.sum(num_dp), all_data.shape[1]));
    # Randomly add the appropriate number of data points of each class to the
    # matrix of data points for this location
    for j in range(labels.shape[0]):
        # Get those data points with the given label
        ldata = np.nonzero(all_data[:,-1] == labels[j])[0];
        # Select the data points which the given label that will be housed
        # at this location
        loc_dp = np.random.choice(ldata, num_dp[j]);
        # Add the associated data points to the data matrix for this location
        # Get the start index
        start = np.sum(num_dp[:j]);
        # Get the end index. Make sure to handle the case where this is the
        if j < (labels.shape[0] - 1): end = np.sum(num_dp[:j+1])
        else: end = loc_data.shape[0];
        loc_data[start:end,:] = all_data[loc_dp];

    # Switch the last column and the label colums
    '''lvals = loc_data[:,1];
    loc_data[:,1] = loc_data[:,-1];
    loc_data[:,-1] = lvals;'''

    # Write location-based data set out to the processed data set directory
    np.savetxt(os.sep.join([out_dir, "loc_%d.csv" % i]), loc_data, delimiter=' ');
