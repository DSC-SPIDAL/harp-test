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

package edu.iu.harp.resource;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import edu.iu.harp.io.DataType;

public abstract class Writable extends Simple {

  @Override
  public final int getNumEnocdeBytes() {
    return 1 + this.getClass().getName().length()
      * 2 + 4 + getNumWriteBytes();
  }

  @Override
  public final void encode(DataOutput out)
    throws IOException {
    out.writeByte(DataType.WRITABLE);
    out.writeUTF(this.getClass().getName());
    this.write(out);
  }

  public final static <W extends Writable> W
    newInstance(Class<W> clazz) {
    try {
      Constructor<W> constructor =
        clazz.getConstructor();
      return constructor.newInstance();
    } catch (InstantiationException |
      IllegalAccessException |
      IllegalArgumentException |
      InvocationTargetException |
      NoSuchMethodException | SecurityException e) {
      return null;
    }
  }

  public final static <W extends Writable> W
    create(Class<W> clazz) {
    return ResourcePool.get().getWritablePool()
      .getWritable(clazz);
  }

  public final static <W extends Writable>
    Class<W> forClass(String className) {
    try {
      return (Class<W>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  @Override
  public final void release() {
    ResourcePool.get().getWritablePool()
      .releaseWritable(this);
  }
  
  @Override
  public final void free() {
    ResourcePool.get().getWritablePool()
      .freeWritable(this);
  }

  public abstract void write(DataOutput out)
    throws IOException;

  public abstract void read(DataInput in)
    throws IOException;

  public abstract void clear();

  public abstract int getNumWriteBytes();
}