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

import edu.iu.harp.keyval.Key;

public class StringKey extends Key {

  private String str;

  public StringKey() {
  }

  public StringKey(String str) {
    this.str = str;
  }

  public void setStringKey(String str) {
    this.str = str;
  }

  public String getStringKey() {
    return this.str;
  }

  @Override
  public boolean equals(Object object) {
    if (object.getClass().equals(StringKey.class)) {
      StringKey strKey = (StringKey) object;
      return str.equals(strKey.getStringKey());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return str.hashCode();
  }

  @Override
  public void write(DataOutput out)
    throws IOException {
    out.writeUTF(str);
  }

  @Override
  public void read(DataInput in)
    throws IOException {
    this.str = in.readUTF();
  }

  @Override
  public int getNumWriteBytes() {
    if (str == null) {
      return 0;
    } else {
      return str.length() * 2 + 4;
    }
  }

  @Override
  public void clear() {
    this.str = null;
  }
}
