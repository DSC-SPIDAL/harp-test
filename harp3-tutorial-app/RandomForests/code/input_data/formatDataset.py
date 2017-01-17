#! /usr/bin/python

import os;
import random;

'''
This files converts the files describing the PAMAP2 Dataset into CSV formatted
files and writes them to a separate folder.
'''

datadir = 'PAMAP2_Dataset/Protocol';
prep_datadir = 'PAMAP2_Dataset/Preprocessed'

# Check to see if the prepped data location exists
if not os.path.isdir(prep_datadir):
    os.mkdir(prep_datadir);

# The overall training set
trainingset = '';
# The overall test set
testset = '';

# Loop through the directory of data files
for dataf in os.listdir(datadir):
    # Check to make sure that this is a data file
    if dataf.find('.dat') >= 0:
        # This string is used to track the preprocessed data
        prepdata = "";

        # Open and read the file
        with open(os.sep.join([datadir, dataf]), 'r+') as f: data = f.read();
        # Replace the whitespaces with commas
        data = data.strip().split('\n');
        # Write out the data file as a CSV
        #with open(os.sep.join([datadir, dataf.replace('.dat', '.csv')]), 'w+') as f: f.write(data);
        # Split each file's data into an 80/20 split
        num_train = int(len(data) * .8);
        # Randomly select num_train training instances
        train_indices = random.sample(range(len(data)), num_train);
        # Get the indices that will make up the test set
        test_indices = list(set(range(len(data))) - set(train_indices));

        # Get the data points at the indices of the trianing set and add it to
        # the train data set
        for t in train_indices:
            #if data[t].find('NaN') < 0:
            line = data[t];
            #print line
            line = line.split(' ');
            # Remove the NaN feature
            # Get the label
            label = line.pop(1);
            # Make the label a 0 or a 1 depending on its value
            if label in set(["0", "1", "2", "3", "9", "10", "11", "16", "17", "18"]):
                label = "1";
            else:
                label = "0";
            # Add the label back into the line
            line.append(label)
            line = ' '.join(line);

            prepdata += line + '\n';
            trainingset += line + '\n';

        # Get the data points at the indices of the test set and add it to
        # the test data set
        for t in test_indices:
            #if data[t].find('NaN') < 0:
            line = data[t];
            #print line
            line = line.split(' ');
            # Remove the NaN feature
            # Get the label
            label = line.pop(1);
            # Make the label a 0 or a 1 depending on its value
            if label in set(["0", "1", "2", "3", "9", "10", "11", "16", "17", "18"]):
                label = "1";
            else:
                label = "0";
            # Add the label back into the line
            line.append(label)
            line = ' '.join(line);

            testset += line + '\n';

        # Write out the prepped data
        with open(os.sep.join([prep_datadir, dataf]), 'w+') as f: f.write(prepdata);

# Write out the train and test sets
with open(os.sep.join([prep_datadir, 'training.csv']), 'w+') as f:
    f.write(trainingset);
with open(os.sep.join([prep_datadir, 'test.csv']), 'w+') as f:
    f.write(testset);
with open(os.sep.join([prep_datadir, 'data.csv']), 'w+') as f:
    f.write(trainingset + '\n' + testset);
