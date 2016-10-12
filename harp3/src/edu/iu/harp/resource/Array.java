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

/**
 * The abstract class of arrays
 * 
 * @author zhangbj
 *
 * @param <T>
 */
abstract class Array<T> extends Simple {
  /** Array */
  protected T array = null;
  /** Start position in the array data */
  protected int start = -1;
  /** Default is -1 */
  protected int size = -1;

  public Array(T arr, int start, int size) {
    this.array = arr;
    this.start = start;
    this.size = size;
  }

  /**
   * Get the array body.
   * 
   * @return the array
   */
  public T get() {
    return array;
  }

  /**
   * The start index of the array
   * 
   * @return start index
   */
  public int start() {
    return start;
  }

  /**
   * The size of the array.
   * 
   * @return array size
   */
  public int size() {
    return size;
  }

  protected void reset() {
    array = null;
    start = -1;
    size = -1;
  }
}
