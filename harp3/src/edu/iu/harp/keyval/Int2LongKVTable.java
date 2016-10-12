/*
 * Copyright 2013-2016 Indiana University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.iu.harp.keyval;

import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import edu.iu.harp.partition.Partition;
import edu.iu.harp.partition.PartitionCombiner;
import edu.iu.harp.partition.PartitionStatus;
import edu.iu.harp.resource.Writable;

class Int2LongKVPartitionCombiner extends
  PartitionCombiner<Int2LongKVPartition> {

  private TypeLongCombiner valCombiner;

  Int2LongKVPartitionCombiner(
    TypeLongCombiner combiner) {
    this.valCombiner = combiner;
  }

  @Override
  public PartitionStatus
    combine(Int2LongKVPartition op,
      Int2LongKVPartition np) {
    Int2LongOpenHashMap nMap = np.getKVMap();
    ObjectIterator<Int2LongMap.Entry> iterator =
      nMap.int2LongEntrySet().fastIterator();
    while (iterator.hasNext()) {
      Int2LongMap.Entry entry = iterator.next();
      op.putKeyVal(entry.getIntKey(),
        entry.getLongValue(), valCombiner);
    }
    return PartitionStatus.COMBINED;
  }
}

public class Int2LongKVTable extends
  KVTable<Int2LongKVPartition> {

  private final TypeLongCombiner valCombiner;

  public Int2LongKVTable(int tableID,
    TypeLongCombiner combiner) {
    super(tableID,
      new Int2LongKVPartitionCombiner(combiner));
    this.valCombiner = combiner;
  }

  public void addKeyVal(int key, long val) {
    Int2LongKVPartition partition =
      getOrCreateKVPartition(key);
    addKVInPartition(partition, key, val);
  }

  private ValStatus addKVInPartition(
    Int2LongKVPartition partition, int key,
    long val) {
    return partition.putKeyVal(key, val,
      valCombiner);
  }

  public long getVal(int key) {
    Partition<Int2LongKVPartition> partition =
      getKVPartition(key);
    if (partition != null) {
      return partition.get().getVal(key);
    } else {
      return Int2LongKVPartition.defaultReturnVal;
    }
  }

  private Int2LongKVPartition
    getOrCreateKVPartition(int key) {
    int partitionID = getKVPartitionID(key);
    Partition<Int2LongKVPartition> partition =
      this.getPartition(partitionID);
    if (partition == null) {
      partition =
        new Partition<>(partitionID,
          Writable
            .create(Int2LongKVPartition.class));
      partition.get().initialize();
      this.insertPartition(partition);
    }
    return partition.get();
  }

  private Partition<Int2LongKVPartition>
    getKVPartition(int key) {
    int partitionID = getKVPartitionID(key);
    return this.getPartition(partitionID);
  }

  protected int getKVPartitionID(int key) {
    return key;
  }
}
