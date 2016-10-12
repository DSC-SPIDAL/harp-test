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

package edu.iu.harp.schstatic;

import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.iu.harp.schdynamic.ComputeUtil;
import edu.iu.harp.schdynamic.Input;

public class StaticScheduler<I, O, T extends Task<I, O>> {
  protected static final Log LOG = LogFactory
    .getLog(StaticScheduler.class);

  private Thread[] threads;
  private boolean isRunning;
  private boolean isPausing;
  private final TaskMonitor<I, O, T>[] taskMonitors;
  private final int numTaskMonitors;
  private final Submitter<I> submitter;
  private final Semaphore barrier1;

  @SuppressWarnings("unchecked")
  public StaticScheduler(List<T> tasks) {
    threads = null;
    barrier1 = new Semaphore(0);
    numTaskMonitors = tasks.size();
    taskMonitors =
      new TaskMonitor[numTaskMonitors];
    submitter = new Submitter<>(taskMonitors);
    int i = 0;
    for (T task : tasks) {
      taskMonitors[i] =
        new TaskMonitor<>(i, task, submitter,
          numTaskMonitors, barrier1);
      i++;
    }
  }

  public T getTask(int taskID) {
    return taskMonitors[taskID].getTask();
  }

  public synchronized void submit(int taskID,
    I input) {
    submitter.submit(taskID, input);
  }

  public synchronized void start() {
    if (!isRunning) {
      isRunning = true;
      if (isPausing) {
        isPausing = false;
        for (TaskMonitor<I, O, T> monitor : taskMonitors) {
          monitor.release();
        }
      } else {
        threads = new Thread[numTaskMonitors];
        int i = 0;
        for (TaskMonitor<I, O, T> monitor : taskMonitors) {
          threads[i] = new Thread(monitor);
          threads[i].start();
          i++;
        }
      }
    }
  }

  public synchronized void pause() {
    if (isRunning && !isPausing) {
      isRunning = false;
      isPausing = true;
      for (TaskMonitor<I, O, T> taskMonitor : taskMonitors) {
        taskMonitor.submit(new Input<I>(null,
          true, false));
      }
      ComputeUtil.acquire(barrier1,
        numTaskMonitors);
    }
  }

  public synchronized void cleanInputQueue() {
    if (isPausing || !isRunning) {
      for (TaskMonitor<I, O, T> taskMonitor : taskMonitors) {
        taskMonitor.cleanInputQueue();
      }
    }
  }

  public synchronized void stop() {
    if (isPausing) {
      start();
    }
    if (isRunning) {
      isRunning = false;
      for (TaskMonitor<I, O, T> taskMonitor : taskMonitors) {
        taskMonitor.submit(new Input<I>(null,
          false, true));
      }
      for (int i = 0; i < numTaskMonitors; i++) {
        ComputeUtil.joinThread(threads[i]);
      }
      threads = null;
    }
  }

  /**
   * Invoke as while(hasOutput()) {
   * waitForOutput(); }
   * 
   * @return
   */
  public O waitForOutput(int taskID) {
    if (taskID < taskMonitors.length) {
      return taskMonitors[taskID].waitForOutput();
    } else {
      return null;
    }
  }

  public boolean hasOutput(int taskID) {
    if (taskID < taskMonitors.length) {
      return taskMonitors[taskID].hasOutput();
    } else {
      return false;
    }
  }

  public boolean hasError(int taskID) {
    if (taskID < taskMonitors.length) {
      return taskMonitors[taskID].hasError();
    } else {
      return false;
    }
  }
}
