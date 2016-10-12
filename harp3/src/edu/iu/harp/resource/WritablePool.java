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

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.IdentityHashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;

/**
 * Create and manage writable objects
 * 
 */
public class WritablePool {
  /** Class logger */
  private static final Logger LOG = Logger
    .getLogger(WritablePool.class);

  /** A map between ref, and writable objects */
  private Object2ObjectOpenHashMap<Class<? extends Writable>, WritableStore> writableMap;

  private class WritableStore {
    private IdentityHashMap<Writable, Object> inUseMap;
    private LinkedList<Writable> freeQueue;

    private WritableStore() {
      inUseMap = new IdentityHashMap<>();
      freeQueue = new LinkedList<>();
    }
  }

  // Dummy value to associate with an Object in
  // the backing Map
  private static final Object PRESENT =
    new Object();

  public WritablePool() {
    writableMap =
      new Object2ObjectOpenHashMap<>();
  }

  synchronized <W extends Writable> W
    getWritable(Class<W> clazz) {
    WritableStore writableStore =
      writableMap.get(clazz);
    if (writableStore == null) {
      writableStore = new WritableStore();
      writableMap.put(clazz, writableStore);
    }
    if (!writableStore.freeQueue.isEmpty()) {
      Writable obj =
        writableStore.freeQueue.removeFirst();
      writableStore.inUseMap.put(obj, PRESENT);
      // LOG.info("Get existing object "
      // + className + ".");
      return (W) obj;
    } else {
      // LOG.info("Create a new object " +
      // className + ".");
      W obj = Writable.newInstance(clazz);
      if (obj != null) {
        writableStore.inUseMap.put(obj, PRESENT);
      }
      return obj;
    }
  }

  synchronized <W extends Writable> boolean
    releaseWritable(W obj) {
    if (obj == null) {
      return false;
    }
    WritableStore writableStore =
      writableMap.get(obj.getClass());
    if (writableStore == null) {
      return false;
    }
    Object rmObject =
      writableStore.inUseMap.remove(obj);
    if (rmObject == null) {
      return false;
    } else {
      obj.clear();
      writableStore.freeQueue.add(obj);
      // LOG.info("Release object "
      // + obj.getClass().getName() + ".");
      return true;
    }
  }

  public synchronized <W extends Writable>
    boolean freeWritable(W obj) {
    if (obj == null) {
      return false;
    }
    WritableStore writableStore =
      writableMap.get(obj.getClass());
    if (writableStore == null) {
      return false;
    }
    Object rmObject =
      writableStore.inUseMap.remove(obj);
    if (rmObject == null) {
      return false;
    } else {
      obj.clear();
      return true;
    }
  }

  public synchronized void clean() {
    for (WritableStore store : writableMap
      .values()) {
      store.freeQueue.clear();
    }
  }

  synchronized void log() {
    ObjectIterator<Object2ObjectMap.Entry<Class<? extends Writable>, WritableStore>> iterator =
      writableMap.object2ObjectEntrySet()
        .fastIterator();
    while (iterator.hasNext()) {
      Object2ObjectMap.Entry<Class<? extends Writable>, WritableStore> entry =
        iterator.next();
      LOG.info("class: " + entry.getKey()
        + ", use="
        + entry.getValue().inUseMap.size()
        + ", released="
        + entry.getValue().freeQueue.size());
    }
  }
}
