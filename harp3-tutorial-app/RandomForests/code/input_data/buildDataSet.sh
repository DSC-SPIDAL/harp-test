#!/bin/bash

python formatDataset.py
python createDataSets.py
python fixData.py
cd PAMAP2_Dataset/Preprocessed
for i in `seq 1 9`; do val=`echo "$i" - 1 |bc`; mv subject10"$i".dat subject10"$val".csv;done
cat subject108.csv >> subject107.csv
cd -
