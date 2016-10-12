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

public class Input<I> {

  private I object;
  private boolean pause;
  private boolean stop;

  public Input(I object, boolean pause,
    boolean stop) {
    this.object = object;
    this.pause = pause;
    this.stop = stop;
  }

  public I getInput() {
    return object;
  }

  public boolean isPause() {
    return pause;
  }

  public boolean isStop() {
    return stop;
  }
}
