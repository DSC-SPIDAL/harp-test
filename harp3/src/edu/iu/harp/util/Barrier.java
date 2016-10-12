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

package edu.iu.harp.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import edu.iu.harp.resource.Writable;

public class Barrier extends Writable {

  private boolean status;

  public Barrier() {
    status = false;
  }

  public void setStatus(boolean st) {
    status = st;
  }

  public boolean getStatus() {
    return status;
  }

  @Override
  public void write(DataOutput out)
    throws IOException {
    out.writeBoolean(status);
  }

  @Override
  public void read(DataInput in)
    throws IOException {
    status = in.readBoolean();
  }

  @Override
  public int getNumWriteBytes() {
    return 1;
  }

  @Override
  public void clear() {
  }
}
