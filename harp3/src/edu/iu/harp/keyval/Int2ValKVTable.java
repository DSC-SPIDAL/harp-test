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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import org.apache.log4j.Logger;

import edu.iu.harp.partition.Partition;
import edu.iu.harp.partition.PartitionCombiner;
import edu.iu.harp.partition.PartitionStatus;
import edu.iu.harp.resource.Writable;

class Int2ValKVPartitionCombiner<V extends Value, P extends Int2ValKVPartition<V>>
  extends PartitionCombiner<P> {

  private ValCombiner<V> valCombiner;

  Int2ValKVPartitionCombiner(
    ValCombiner<V> combiner) {
    this.valCombiner = combiner;
  }

  @Override
  public PartitionStatus combine(P op, P np) {
    // remove method in iterator has a bug
    // the rest keys may not be traversed if
    // the last key was removed because the
    // position of keys could be shifted.
    Int2ObjectOpenHashMap<V> nMap = np.getKVMap();
    ObjectIterator<Int2ObjectMap.Entry<V>> iterator =
      nMap.int2ObjectEntrySet().fastIterator();
    IntArrayList rmKeys = new IntArrayList();
    while (iterator.hasNext()) {
      Int2ObjectMap.Entry<V> entry =
        iterator.next();
      int key = entry.getIntKey();
      ValStatus status =
        op.putKeyVal(key, entry.getValue(),
          valCombiner);
      if (status == ValStatus.ADDED) {
        rmKeys.add(key);
        if (!op.getFreeVals().isEmpty()) {
          np.getFreeVals().add(
            op.getFreeVals().remove(0));
        }
      }
    }
    for (int rmKey : rmKeys) {
      nMap.remove(rmKey);
    }
    return PartitionStatus.COMBINED;
  }
}

/**
 * a key-value table with int as key and a float
 * array as value
 * 
 * @author zhangbj
 */
public abstract class Int2ValKVTable<V extends Value, P extends Int2ValKVPartition<V>>
  extends KVTable<P> {

  /** Class logger */
  @SuppressWarnings("unused")
  private static final Logger LOG = Logger
    .getLogger(Int2ValKVTable.class);

  private final Class<V> vClass;
  private final Class<P> pClass;
  private final ValCombiner<V> combiner;

  public Int2ValKVTable(int tableID,
    ValCombiner<V> combiner, Class<V> vClass,
    Class<P> pClass) {
    super(tableID,
      new Int2ValKVPartitionCombiner<V, P>(
        combiner));
    this.vClass = vClass;
    this.pClass = pClass;
    this.combiner = combiner;
  }

  public ValStatus addKeyVal(int key, V val) {
    P partition = getOrCreateKVPartition(key);
    return addKVInPartition(partition, key, val);
  }

  private ValStatus addKVInPartition(P partition,
    int key, V val) {
    return partition
      .putKeyVal(key, val, combiner);
  }

  public V getVal(int key) {
    Partition<P> partition = getKVPartition(key);
    if (partition != null) {
      return partition.get().getVal(key);
    } else {
      return null;
    }
  }

  public V removeVal(int key) {
    Partition<P> partition = getKVPartition(key);
    if (partition != null) {
      return partition.get().removeVal(key);
    } else {
      return null;
    }
  }

  private P getOrCreateKVPartition(int key) {
    int partitionID = getKVPartitionID(key);
    Partition<P> partition =
      this.getPartition(partitionID);
    if (partition == null) {
      partition =
        new Partition<>(partitionID,
          Writable.create(pClass));
      partition.get().initialize(vClass);
      this.insertPartition(partition);
    }
    return partition.get();
  }

  private Partition<P> getKVPartition(int key) {
    int partitionID = getKVPartitionID(key);
    return this.getPartition(partitionID);
  }

  /**
   * Return the partition ID of a given key
   * 
   * @param key
   * @return
   */
  protected int getKVPartitionID(int key) {
    return key;
  }
}
