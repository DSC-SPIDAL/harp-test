#!/bin/bash
set -x

cat /dev/null > result
for i in $(seq 16); do
    START=$(date +%s)

    hadoop jar build/harp3-app-hadoop-2.6.0.jar edu.iu.MLR.MLRMapCollective 1.0 100 47236 4 $i /rcv1v2/rcv1.topics.txt /rcv1v2/rcv1-v2.topics.qrels /rcv1v2/input /MLR_${i}/
    
    END=$(date +%s)
    DIFF=$(( $END - $START ))
    echo "$i, $DIFF" >> result
    hdfs dfs -get /MLR_${i}/weights W_${i}
done

