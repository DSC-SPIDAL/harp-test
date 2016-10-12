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

package edu.iu.harp.worker;

import java.io.BufferedReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.iu.harp.io.Constant;

/**
 * Workers: 0... Self, Next, ... Max Master/Slave
 * Master: 0 Slave: else
 * 
 * Not modifiable from outside
 */
public class Workers extends Nodes {

  /** Map between worker id and worker info */
  private final Map<Integer, WorkerInfo> workerInfos;
  /** Map between rack id and worker id */
  private final Map<Integer, List<Integer>> rackWorkers;
  /** Worker ID of the current worker */
  private final int selfID;
  /** Master info (communication coordinator) */
  private final int masterID;
  private final WorkerInfo masterInfo;
  /** Max worker ID */
  private final int maxID;
  /** Min worker ID */
  private final int minID;
  /** Middle worker ID */
  private final int middleID;
  /** Worker ID of the next worker */
  private final int nextID;
  private final int initCapacity =
    Constant.NUM_THREADS;

  public Workers() throws Exception {
    // Get workers,
    // but self is not a member of workers
    // Then next is pointed to Worker 0 (master)
    this(Constant.UNKNOWN_WORKER_ID);
  }

  public Workers(int selfID) throws Exception {
    this(null, selfID);
  }

  public Workers(BufferedReader reader, int selfid)
    throws Exception {
    super(reader);
    int workerPortBase =
      Constant.DEFAULT_WORKER_POART_BASE;
    workerInfos =
      new ConcurrentHashMap<>(initCapacity);
    rackWorkers =
      new ConcurrentHashMap<>(initCapacity);
    Map<Integer, List<String>> nodes =
      this.getNodes();
    // Load based on the order in node file.
    int workerID = -1;
    for (int rackID : getRackList()) {
      List<Integer> workerIDs =
        new LinkedList<>();
      rackWorkers.put(rackID, workerIDs);
      for (String node : nodes.get(rackID)) {
        System.out.println("node: " + node);
        // Generate next worker ID
        workerID++;
        // Port: workerPortBase + workerID
        workerInfos.put(workerID, new WorkerInfo(
          workerID, node, workerPortBase
            + workerID, rackID));
        workerIDs.add(workerID);
      }
    }
    selfID = selfid;
    masterID = 0;
    masterInfo = workerInfos.get(masterID);
    minID = 0;
    maxID = workerID;
    middleID = workerID / 2;
    // Set next worker ID
    if (selfID >= 0 && selfID < maxID) {
      nextID = selfID + 1;
    } else {
      nextID = 0;
    }
  }

  public int getNumWorkers() {
    return workerInfos.size();
  }

  public boolean isTheOnlyWorker() {
    return workerInfos.size() <= 1;
  }

  public int getMasterID() {
    return this.masterID;
  }

  public boolean isMaster() {
    return selfID == masterID;
  }

  public WorkerInfo getMasterInfo() {
    return masterInfo;
  }

  public int getSelfID() {
    return selfID;
  }

  public boolean isSelfInWorker() {
    if (selfID >= 0 && selfID <= maxID) {
      return true;
    } else {
      return false;
    }
  }

  public WorkerInfo getSelfInfo() {
    return workerInfos.get(selfID);
  }

  public boolean isMax() {
    return selfID == maxID;
  }

  public int getMinID() {
    return minID;
  }

  public int getMiddleID() {
    return middleID;
  }

  public int getMaxID() {
    return maxID;
  }

  public int getNextID() {
    return nextID;
  }

  public WorkerInfo getNextInfo() {
    return workerInfos.get(nextID);
  }

  public WorkerInfo getWorkerInfo(int workerID) {
    return workerInfos.get(workerID);
  }

  public WorkerInfoList getWorkerInfoList() {
    return new WorkerInfoList();
  }

  public class WorkerInfoList implements
    Iterable<WorkerInfo> {

    @Override
    public Iterator<WorkerInfo> iterator() {
      return new WorkerInfoIterator();
    }
  }

  public class WorkerInfoIterator implements
    Iterator<WorkerInfo> {
    protected int workerID = -1;

    @Override
    public boolean hasNext() {
      if ((workerID + 1) <= getMaxID()) {
        return true;
      }
      return false;
    }

    @Override
    public WorkerInfo next() {
      workerID = workerID + 1;
      return workerInfos.get(workerID);
    }

    @Override
    public void remove() {
    }
  }
}
