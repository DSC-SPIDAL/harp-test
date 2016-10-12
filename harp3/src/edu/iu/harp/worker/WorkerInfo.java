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

public class WorkerInfo {

  private final int id;
  /** Hostname or IP */
  private final String node;
  private final int port;
  private final int rack;

  public WorkerInfo(int id, String node,
    int port, int rack) {
    this.id = id;
    this.node = node;
    this.port = port;
    this.rack = rack;
  }

  public int getID() {
    return id;
  }

  public String getNode() {
    return node;
  }

  public int getPort() {
    return port;
  }

  public int getRack() {
    return rack;
  }
}
