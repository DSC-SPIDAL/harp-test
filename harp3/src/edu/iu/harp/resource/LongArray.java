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

import java.io.DataOutput;
import java.io.IOException;

import edu.iu.harp.io.DataType;

public final class LongArray extends
  Array<long[]> {

  public LongArray(long[] arr, int start, int size) {
    super(arr, start, size);
  }

  @Override
  public int getNumEnocdeBytes() {
    return size * 8 + 5;
  }

  @Override
  public void encode(DataOutput out)
    throws IOException {
    out.writeByte(DataType.LONG_ARRAY);
    int len = start + size;
    out.writeInt(size);
    for (int i = start; i < len; i++) {
      out.writeLong(array[i]);
    }
  }

  public static LongArray create(int len,
    boolean approximate) {
    if (len > 0) {
      long[] longs =
        ResourcePool.get().getLongsPool()
          .getArray(len, approximate);
      if (longs != null) {
        return new LongArray(longs, 0, len);
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  @Override
  public void release() {
    ResourcePool.get().getLongsPool()
      .releaseArray(array);
    this.reset();
  }

  @Override
  public void free() {
    ResourcePool.get().getLongsPool()
      .freeArray(array);
    this.reset();
  }
}
