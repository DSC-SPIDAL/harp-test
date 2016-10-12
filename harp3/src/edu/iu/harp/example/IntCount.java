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

package edu.iu.harp.example;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import edu.iu.harp.keyval.Value;

public class IntCount extends Value {

  private int val;
  private int count;

  public IntCount() {
  }

  public IntCount(int v, int c) {
    this.val = v;
    this.count = c;
  }

  public void setInt(int val) {
    this.val = val;
  }

  public int getInt() {
    return this.val;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  @Override
  public void write(DataOutput out)
    throws IOException {
    out.writeInt(val);
    out.writeInt(count);
  }

  @Override
  public void read(DataInput in)
    throws IOException {
    this.val = in.readInt();
    this.count = in.readInt();
  }

  @Override
  public int getNumWriteBytes() {
    return 8;
  }

  @Override
  public void clear() {
  }
}
