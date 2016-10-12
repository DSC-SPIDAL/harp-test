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

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.iu.harp.partition.Partition;
import edu.iu.harp.partition.PartitionCombiner;
import edu.iu.harp.partition.PartitionStatus;
import edu.iu.harp.resource.Writable;

class Key2ValKVPartitionCombiner<K extends Key, V extends Value, P extends Key2ValKVPartition<K, V>>
  extends PartitionCombiner<P> {

  private ValCombiner<V> valCombiner;

  Key2ValKVPartitionCombiner(
    ValCombiner<V> combiner) {
    this.valCombiner = combiner;
  }

  @Override
  public PartitionStatus combine(P op, P np) {
    // remove method in iterator has a bug
    // the rest keys may not be traversed if
    // the last key was removed because the
    // position of keys could be shifted.
    Object2ObjectOpenHashMap<K, V> nMap =
      np.getKVMap();
    ObjectIterator<Object2ObjectMap.Entry<K, V>> iterator =
      nMap.object2ObjectEntrySet().fastIterator();
    List<K> rmKeys = new LinkedList<>();
    while (iterator.hasNext()) {
      Object2ObjectMap.Entry<K, V> entry =
        iterator.next();
      K key = entry.getKey();
      V val = entry.getValue();
      ValStatus status =
        op.putKeyVal(key, val, valCombiner);
      if (status == ValStatus.ADDED) {
        rmKeys.add(key);
        if (!op.getFreeKeys().isEmpty()) {
          np.getFreeKeys().add(
            op.getFreeKeys().remove(0));
        }
        if (!op.getFreeVals().isEmpty()) {
          np.getFreeVals().add(
            op.getFreeVals().remove(0));
        }
      }
    }
    for (K rmKey : rmKeys) {
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
public abstract class Key2ValKVTable<K extends Key, V extends Value, P extends Key2ValKVPartition<K, V>>
  extends KVTable<P> {

  /** Class logger */
  @SuppressWarnings("unused")
  private static final Logger LOG = Logger
    .getLogger(Key2ValKVTable.class);

  private final Class<K> kClass;
  private final Class<V> vClass;
  private final Class<P> pClass;
  private final ValCombiner<V> valCombiner;

  public Key2ValKVTable(int tableID,
    ValCombiner<V> combiner, Class<K> kClass,
    Class<V> vClass, Class<P> pClass) {
    super(tableID,
      new Key2ValKVPartitionCombiner<K, V, P>(
        combiner));
    this.kClass = kClass;
    this.vClass = vClass;
    this.pClass = pClass;
    this.valCombiner = combiner;
  }

  public ValStatus addKeyVal(K key, V val) {
    P partition = getOrCreateKVPartition(key);
    return addKVInPartition(partition, key, val);
  }

  private ValStatus addKVInPartition(P partition,
    K key, V val) {
    return partition.putKeyVal(key, val,
      valCombiner);
  }

  public V getVal(K key) {
    Partition<P> partition = getKVPartition(key);
    if (partition != null) {
      return partition.get().getVal(key);
    } else {
      return null;
    }
  }

  public V removeVal(K key) {
    Partition<P> partition = getKVPartition(key);
    if (partition != null) {
      return partition.get().removeVal(key);
    } else {
      return null;
    }
  }

  private P getOrCreateKVPartition(K key) {
    int partitionID = getKVPartitionID(key);
    Partition<P> partition =
      this.getPartition(partitionID);
    if (partition == null) {
      partition =
        new Partition<>(partitionID,
          Writable.create(pClass));
      partition.get().initialize(kClass, vClass);
      this.insertPartition(partition);
    }
    return partition.get();
  }

  private Partition<P> getKVPartition(K key) {
    int partitionID = getKVPartitionID(key);
    return this.getPartition(partitionID);
  }

  protected int getKVPartitionID(K key) {
    return key.hashCode();
  }
}
