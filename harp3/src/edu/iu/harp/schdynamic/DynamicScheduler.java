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

package edu.iu.harp.schdynamic;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DynamicScheduler<I, O, T extends Task<I, O>> {

  protected static final Log LOG = LogFactory
    .getLog(DynamicScheduler.class);

  private final BlockingDeque<Input<I>> inputQueue;
  private final BlockingQueue<Output<O>> outputQueue;

  private Thread[] threads;
  private int inputCount;
  private int outputCount;
  private int errorCount;
  private boolean isRunning;
  private boolean isPausing;
  private final List<TaskMonitor<I, O, T>> taskMonitors;
  private final List<T> tasks;
  private final int numTaskMonitors;
  private final Semaphore barrier1;

  public DynamicScheduler(List<T> tasks) {
    inputQueue = new LinkedBlockingDeque<>();
    outputQueue = new LinkedBlockingQueue<>();
    threads = null;
    inputCount = 0;
    outputCount = 0;
    errorCount = 0;
    isRunning = false;
    isPausing = false;
    barrier1 = new Semaphore(0);
    numTaskMonitors = tasks.size();
    this.tasks = tasks;
    taskMonitors = new ObjectArrayList<>();
    for (T task : tasks) {
      taskMonitors.add(new TaskMonitor<>(
        inputQueue, outputQueue, task, barrier1));
    }
  }

  public List<T> getTasks() {
    return tasks;
  }

  public synchronized void submit(I input) {
    if (input != null) {
      inputQueue.add(new Input<I>(input, false,
        false));
      if (isRunning) {
        inputCount++;
      }
    }
  }

  public synchronized void submitAll(
    Collection<I> inputs) {
    for (I input : inputs) {
      inputQueue.add(new Input<I>(input, false,
        false));
    }
    if (isRunning) {
      inputCount += inputs.size();
    }
  }

  public synchronized void submitAll(I[] inputs) {
    // Submit inputs
    int submitCount = 0;
    for (I input : inputs) {
      if (input != null) {
        inputQueue.add(new Input<I>(input, false,
          false));
        submitCount++;
      }
    }
    if (isRunning) {
      inputCount += submitCount;
    }
  }

  public synchronized void start() {
    // Start monitor threads, wait for inputs
    if (!isRunning) {
      isRunning = true;
      inputCount += inputQueue.size();
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
      for (int i = 0; i < numTaskMonitors; i++) {
        inputQueue.addLast(new Input<I>(null,
          true, false));
      }
      ComputeUtil.acquire(barrier1,
        numTaskMonitors);
      inputCount -= inputQueue.size();
    }
  }

  public synchronized void pauseNow() {
    if (isRunning && !isPausing) {
      isRunning = false;
      isPausing = true;
      for (int i = 0; i < numTaskMonitors; i++) {
        inputQueue.addFirst(new Input<I>(null,
          true, false));
      }
      ComputeUtil.acquire(barrier1,
        numTaskMonitors);
      inputCount -= inputQueue.size();
    }
  }

  public synchronized void cleanInputQueue() {
    if (isPausing || !isRunning) {
      inputQueue.clear();
    }
  }

  public synchronized void stop() {
    // Stop submission
    // Send stop signal to the queue
    // based on the number of tasks
    if (isPausing) {
      start();
    }
    if (isRunning) {
      isRunning = false;
      for (int i = 0; i < numTaskMonitors; i++) {
        inputQueue.addLast(new Input<I>(null,
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
  public synchronized O waitForOutput() {
    // If no output is available, wait for one
    if (hasNext()) {
      boolean isFailed = false;
      Output<O> output = null;
      do {
        try {
          output = outputQueue.take();
          isFailed = false;
        } catch (Exception e) {
          output = null;
          isFailed = true;
          LOG.error("Error when waiting output",
            e);
        }
      } while (isFailed);
      outputCount++;
      if (output.isError()) {
        errorCount++;
      }
      return output.getOutput();
    } else {
      return null;
    }
  }

  public synchronized boolean hasOutput() {
    return hasNext();
  }

  private boolean hasNext() {
    return inputCount > outputCount;
  }

  public synchronized boolean hasError() {
    int count = errorCount;
    errorCount = 0;
    return count > 0;
  }
}
