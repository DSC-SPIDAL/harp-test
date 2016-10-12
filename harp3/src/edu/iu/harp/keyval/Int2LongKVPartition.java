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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Int2LongKVPartition extends
  KVPartition {

  private Int2LongOpenHashMap kvMap;
  final static long defaultReturnVal =
    Long.MIN_VALUE;

  public Int2LongKVPartition() {
    super();
    kvMap = null;
  }

  public void initialize() {
    if (this.kvMap != null) {
      this.kvMap.clear();
    } else {
      this.kvMap = new Int2LongOpenHashMap();
      this.kvMap
        .defaultReturnValue(defaultReturnVal);
    }
  }

  public ValStatus putKeyVal(int key, long val,
    TypeLongCombiner combiner) {
    long curVal = this.kvMap.put(key, val);
    if (curVal == defaultReturnVal) {
      return ValStatus.ADDED;
    } else {
      this.kvMap.put(key,
        combiner.combine(curVal, val));
      return ValStatus.COMBINED;
    }
  }

  public long getVal(int key) {
    return this.kvMap.get(key);
  }

  public Int2LongOpenHashMap getKVMap() {
    return kvMap;
  }

  public int size() {
    return this.kvMap.size();
  }

  public boolean isEmpty() {
    return this.kvMap.isEmpty();
  }

  @Override
  public void clear() {
    this.kvMap.clear();
  }

  @Override
  public int getNumWriteBytes() {
    return 4 + kvMap.size() * 12;
  }

  @Override
  public void write(DataOutput out)
    throws IOException {
    out.writeInt(kvMap.size());
    ObjectIterator<Int2LongMap.Entry> iterator =
      kvMap.int2LongEntrySet().fastIterator();
    while (iterator.hasNext()) {
      Int2LongMap.Entry entry = iterator.next();
      out.writeInt(entry.getIntKey());
      out.writeLong(entry.getLongValue());
    }
  }

  @Override
  public void read(DataInput in)
    throws IOException {
    int size = in.readInt();
    if (kvMap != null) {
      kvMap.clear();
    } else {
      kvMap = new Int2LongOpenHashMap(size);
      kvMap.defaultReturnValue(defaultReturnVal);
    }
    for (int i = 0; i < size; i++) {
      kvMap.put(in.readInt(), in.readLong());
    }
  }
}
