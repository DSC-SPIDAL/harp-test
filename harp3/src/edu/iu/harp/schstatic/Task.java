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

public abstract class Task<I, O> {

  private int taskID;
  private int numTasks;
  private Submitter<I> submitter;

  public abstract O run(I input) throws Exception;

  public int getTaskID() {
    return taskID;
  }

  void setTaskID(int taskID) {
    this.taskID = taskID;
  }

  public int getNumTasks() {
    return this.numTasks;
  }

  void setNumTasks(int numTasks) {
    this.numTasks = numTasks;
  }

  public Submitter<I> getSubmitter() {
    return submitter;
  }

  void setSubmitter(Submitter<I> submitter) {
    this.submitter = submitter;
  }
}