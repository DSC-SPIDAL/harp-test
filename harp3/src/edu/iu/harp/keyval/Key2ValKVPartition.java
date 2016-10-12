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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.iu.harp.resource.Writable;

/**
 * Each value is a double array... We assume it is
 * not big, each of them just have few elements.
 * 
 * @author zhangbj
 * 
 */
public abstract class Key2ValKVPartition<K extends Key, V extends Value>
  extends KVPartition {

  /** Class logger */
  private static final Logger LOG = Logger
    .getLogger(Key2ValKVPartition.class);

  private Object2ObjectOpenHashMap<K, V> kvMap;
  private Class<K> kClass;
  private Class<V> vClass;
  private LinkedList<K> freeKeys;
  private LinkedList<V> freeVals;

  public Key2ValKVPartition() {
    super();
    kvMap = null;
    kClass = null;
    vClass = null;
    freeKeys = new LinkedList<>();
    freeVals = new LinkedList<>();
  }

  public void initialize(Class<K> kClass,
    Class<V> vClass) {
    if (this.kvMap == null) {
      this.kvMap =
        new Object2ObjectOpenHashMap<>();
      this.kvMap.defaultReturnValue(null);
    }
    this.kClass = kClass;
    this.vClass = vClass;
  }

  public ValStatus putKeyVal(K key, V val,
    ValCombiner<V> combiner) {
    if (key == null || val == null) {
      return ValStatus.ADD_FAILED;
    } else {
      V curVal = kvMap.putIfAbsent(key, val);
      if (curVal == null) {
        return ValStatus.ADDED;
      } else {
        return combiner.combine(curVal, val);
      }
    }
  }

  public V removeVal(K key) {
    return this.kvMap.remove(key);
  }

  public Class<K> getKeyClass() {
    return this.kClass;
  }

  public Class<V> getVClass() {
    return this.vClass;
  }

  public V getVal(K key) {
    return this.kvMap.get(key);
  }

  public Object2ObjectOpenHashMap<K, V>
    getKVMap() {
    return kvMap;
  }

  public int size() {
    return this.kvMap.size();
  }

  public boolean isEmpty() {
    return this.kvMap.isEmpty();
  }

  List<K> getFreeKeys() {
    return this.freeKeys;
  }

  List<V> getFreeVals() {
    return freeVals;
  }

  @Override
  public void clear() {
    if (!this.kvMap.isEmpty()) {
      for (Object2ObjectMap.Entry<K, V> entry : this.kvMap
        .object2ObjectEntrySet()) {
        entry.getKey().clear();
        entry.getValue().clear();
        this.freeKeys.add(entry.getKey());
        this.freeVals.add(entry.getValue());
      }
      this.kvMap.clear();
    }
  }

  @Override
  public int getNumWriteBytes() {
    // mapSize
    int size = 4;
    // kClass name
    size +=
      (this.kClass.getName().length() * 2 + 4);
    // vClass name
    size +=
      (this.vClass.getName().length() * 2 + 4);
    // Key + each array size
    ObjectIterator<Object2ObjectMap.Entry<K, V>> iterator =
      this.kvMap.object2ObjectEntrySet()
        .fastIterator();
    while (iterator.hasNext()) {
      Object2ObjectMap.Entry<K, V> entry =
        iterator.next();
      size +=
        (entry.getKey().getNumWriteBytes() + entry
          .getValue().getNumWriteBytes());
    }
    return size;
  }

  @Override
  public void write(DataOutput out)
    throws IOException {
    out.writeInt(this.kvMap.size());
    out.writeUTF(this.kClass.getName());
    out.writeUTF(this.vClass.getName());
    ObjectIterator<Object2ObjectMap.Entry<K, V>> iterator =
      this.kvMap.object2ObjectEntrySet()
        .fastIterator();
    while (iterator.hasNext()) {
      Object2ObjectMap.Entry<K, V> entry =
        iterator.next();
      entry.getKey().write(out);
      entry.getValue().write(out);
    }
  }

  @Override
  public void read(DataInput in)
    throws IOException {
    int size = in.readInt();
    if (this.kvMap == null) {
      this.kvMap =
        new Object2ObjectOpenHashMap<>(size);
      this.kvMap.defaultReturnValue(null);
    }
    try {
      this.kClass =
        Writable.forClass(in.readUTF());
      this.vClass =
        Writable.forClass(in.readUTF());
      for (int i = 0; i < size; i++) {
        K key = null;
        V val = null;
        if (freeKeys.isEmpty()
          && freeVals.isEmpty()) {
          key = Writable.newInstance(kClass);
          val = Writable.newInstance(vClass);
        } else {
          key = freeKeys.removeFirst();
          val = freeVals.removeFirst();
        }
        key.read(in);
        val.read(in);
        this.kvMap.put(key, val);
      }
    } catch (Exception e) {
      LOG.error("Fail to initialize keyvals.", e);
      throw new IOException(e);
    }
  }
}
