#! /usr/bin/python

import os, sys, random;
import numpy as np;


files = "PAMAP2_Dataset/Preprocessed/loc_";
numFiles = 8

np.set_printoptions(suppress=True)

for fileNum in range(numFiles):
	fileName = files + str(fileNum) + ".csv"
	data = np.loadtxt(fileName, dtype='float', delimiter=' ')
	np.savetxt(fileName, data, delimiter=' ',fmt='%.5f ' * 53 + '%i' )