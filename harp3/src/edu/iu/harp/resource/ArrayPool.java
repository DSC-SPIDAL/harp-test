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

package edu.iu.harp.resource;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import edu.iu.harp.io.Constant;

public abstract class ArrayPool<T> {
  /** Class logger */
  private static final Logger LOG = Logger
    .getLogger(ArrayPool.class);
  // A map between size and buffer array (2
  // hashset. 0: free 1: in-use)
  private Int2ObjectOpenHashMap<ArrayStore> arrayMap;

  private class ArrayStore {
    private LinkedList<T> freeQueue;
    private HashSet<T> inUseSet;

    private ArrayStore() {
      freeQueue = new LinkedList<>();
      inUseSet = new HashSet<>();
    }
  }

  public ArrayPool() {
    arrayMap = new Int2ObjectOpenHashMap<>();
  }

  private int getAdjustedArraySize(int size,
    boolean approximate) {
    if (approximate) {
      int shift =
        32 - Integer
          .numberOfLeadingZeros(size - 1);
      return shift == 31
        ? Constant.MAX_ARRAY_SIZE : 1 << shift;
    } else {
      return Constant.MAX_ARRAY_SIZE < size
        ? Constant.MAX_ARRAY_SIZE : size;
    }
  }

  protected abstract T createNewArray(int size);

  protected abstract int getLength(T array);

  synchronized T getArray(int size,
    boolean approximate) {
    int originSize = size;
    if (originSize <= 0) {
      return null;
    }
    int adjustSize =
      getAdjustedArraySize(originSize,
        approximate);
    if (adjustSize < originSize) {
      return null;
    }
    ArrayStore arrayStore =
      arrayMap.get(adjustSize);
    if (arrayStore == null) {
      arrayStore = new ArrayStore();
      arrayMap.put(adjustSize, arrayStore);
    }
    if (arrayStore.freeQueue.isEmpty()) {
      try {
        T array = createNewArray(adjustSize);
        arrayStore.inUseSet.add(array);
        // LOG
        // .info("Create a new array with original size: "
        // + originSize
        // + ", adjusted size: "
        // + adjustSize
        // + ", with type: "
        // + array.getClass().getName());
        return array;
      } catch (Throwable t) {
        LOG.error(
          "Cannot create array with size "
            + adjustSize
            + ", current total memory: "
            + Runtime.getRuntime().totalMemory()
            + ", current free memory "
            + Runtime.getRuntime().freeMemory(),
          t);
        return null;
      }
    } else {
      T array =
        arrayStore.freeQueue.removeFirst();
      arrayStore.inUseSet.add(array);
      // LOG
      // .info("Get an existing array with adjusted size: "
      // + adjustSize
      // + ", with type "
      // + array.getClass().getName());
      return array;
    }
  }

  synchronized boolean releaseArray(T array) {
    if (array == null) {
      return false;
    }
    int size = getLength(array);
    ArrayStore arrayStore = arrayMap.get(size);
    if (arrayStore == null) {
      // LOG
      // .info("Fail to release an array with size: "
      // + size
      // + ", with type "
      // + array.getClass().getName()
      // + ". no such store.");
      return false;
    } else {
      if (arrayStore.inUseSet.remove(array)) {
        arrayStore.freeQueue.add(array);
        return true;
      } else {
        // LOG
        // .info("Fail to release an array with size: "
        // + size
        // + ", with type "
        // + array.getClass().getName()
        // + ". no such an array.");
        return false;
      }
    }
  }

  synchronized boolean freeArray(T array) {
    int size = getLength(array);
    // LOG.info("Free an array with size: " + size
    // + ", with type "
    // + array.getClass().getName());
    ArrayStore arrayStore = arrayMap.get(size);
    if (arrayStore == null) {
      return false;
    } else {
      return arrayStore.inUseSet.remove(array);
    }
  }

  synchronized void clean() {
    for (ArrayStore store : arrayMap.values()) {
      store.freeQueue.clear();
    }
  }

  synchronized void log() {
    ObjectIterator<Int2ObjectMap.Entry<ArrayStore>> iterator =
      arrayMap.int2ObjectEntrySet()
        .fastIterator();
    while (iterator.hasNext()) {
      Int2ObjectMap.Entry<ArrayStore> entry =
        iterator.next();
      LOG.info(this + ": size="
        + entry.getIntKey() + ", use="
        + entry.getValue().inUseSet.size()
        + ", released="
        + entry.getValue().freeQueue.size());
    }
  }
}
