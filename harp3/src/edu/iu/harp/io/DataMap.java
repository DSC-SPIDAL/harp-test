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

package edu.iu.harp.io;

import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class DataMap {

  /** Class logger */
  @SuppressWarnings("unused")
  private static final Logger LOG = Logger
    .getLogger(DataMap.class);

  private ConcurrentMap<String, ConcurrentMap<String, BlockingQueue<Data>>> dataMap;
  private final int initialCapacity =
    Constant.NUM_THREADS;

  public DataMap() {
    dataMap =
      new ConcurrentHashMap<>(initialCapacity);
  }

  private BlockingQueue<Data>
    createOperationDataQueue(String contextName,
      String operationName) {
    ConcurrentMap<String, BlockingQueue<Data>> opDataMap =
      dataMap.get(contextName);
    if (opDataMap == null) {
      opDataMap =
        new ConcurrentHashMap<>(initialCapacity);
      ConcurrentMap<String, BlockingQueue<Data>> oldOpDataMap =
        dataMap.putIfAbsent(contextName,
          opDataMap);
      if (oldOpDataMap != null) {
        opDataMap = oldOpDataMap;
      }
    }
    BlockingQueue<Data> opDataQueue =
      opDataMap.get(operationName);
    if (opDataQueue == null) {
      opDataQueue = new LinkedBlockingQueue<>();
      BlockingQueue<Data> oldOpDataQueue =
        opDataMap.putIfAbsent(operationName,
          opDataQueue);
      if (oldOpDataQueue != null) {
        opDataQueue = oldOpDataQueue;
      }
    }
    return opDataQueue;
  }

  public Data waitAndGetData(String contextName,
    String operationName, long maxWaitTime)
    throws InterruptedException {
    BlockingQueue<Data> opDataQueue =
      createOperationDataQueue(contextName,
        operationName);
    return opDataQueue.poll(maxWaitTime,
      TimeUnit.SECONDS);
  }

  public void putData(Data data) {
    BlockingQueue<Data> opDataQueue =
      createOperationDataQueue(
        data.getContextName(),
        data.getOperationName());
    opDataQueue.add(data);
  }

  public void cleanOperationData(
    String contextName, String operationName) {
    ConcurrentMap<String, BlockingQueue<Data>> opDataMap =
      dataMap.get(contextName);
    if (opDataMap != null) {
      BlockingQueue<Data> opDataQueue =
        opDataMap.remove(operationName);
      if (opDataQueue != null) {
        for (Data data : opDataQueue) {
          data.release();
        }
        opDataQueue.clear();
      }
    }
  }

  /**
   * Context is the execution context of the
   * operation. Invoke this when the context is
   * done.
   * 
   */
  public void cleanData(String contextName) {
    ConcurrentMap<String, BlockingQueue<Data>> opDataMap =
      dataMap.remove(contextName);
    if (opDataMap != null) {
      for (Entry<String, BlockingQueue<Data>> entry : opDataMap
        .entrySet()) {
        BlockingQueue<Data> opDataQueue =
          entry.getValue();
        if (opDataQueue != null) {
          for (Data data : opDataQueue) {
            data.release();
          }
          opDataQueue.clear();
        }
      }
    }
  }

  /**
   * If failures happen, the old contexts should
   * all be trashed.
   */
  public void clean() {
    ConcurrentMap<String, ConcurrentMap<String, BlockingQueue<Data>>> tmpDataMap =
      null;
    synchronized (this) {
      tmpDataMap = dataMap;
      dataMap =
        new ConcurrentHashMap<>(initialCapacity);
    }
    for (Entry<String, ConcurrentMap<String, BlockingQueue<Data>>> entry : tmpDataMap
      .entrySet()) {
      cleanData(entry.getKey());
    }
    dataMap.clear();
  }
}
