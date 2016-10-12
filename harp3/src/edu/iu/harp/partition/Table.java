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

package edu.iu.harp.partition;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import edu.iu.harp.resource.Simple;

/**
 * The abstraction of distributed dataset
 * 
 * @author zhangbj
 *
 * @param <P>
 */
public class Table<P extends Simple> {

  private final int tableID;
  private final Int2ObjectOpenHashMap<Partition<P>> partitions;
  private final PartitionCombiner<P> combiner;

  /**
   * Constructor.
   * 
   * @param tableID
   *          a table is assigned with an ID which
   *          is convenient for reference. Any ID
   *          is allowed.
   * @param combiner
   *          the combiner used for partitions
   */
  public Table(int tableID,
    PartitionCombiner<P> combiner) {
    this.tableID = tableID;
    this.partitions =
      new Int2ObjectOpenHashMap<>();
    this.combiner = combiner;
  }

  public int getTableID() {
    return tableID;
  }

  public PartitionCombiner<P> getCombiner() {
    return this.combiner;
  }

  public final int getNumPartitions() {
    return partitions.size();
  }

  public final IntSet getPartitionIDs() {
    return partitions.keySet();
  }

  public final ObjectCollection<Partition<P>>
    getPartitions() {
    return partitions.values();
  }

  /**
   * Add a partition into a table
   * 
   * @param partition
   * @return
   */
  public final PartitionStatus addPartition(
    Partition<P> partition) {
    if (partition == null) {
      return PartitionStatus.ADD_FAILED;
    }
    Partition<P> curPartition =
      this.partitions.get(partition.id());
    if (curPartition == null) {
      return insertPartition(partition);
    } else {
      return combiner.combine(curPartition.get(),
        partition.get());
    }
  }

  protected final PartitionStatus
    insertPartition(Partition<P> partition) {
    partitions.put(partition.id(), partition);
    return PartitionStatus.ADDED;
  }

  public final Partition<P> getPartition(
    int partitionID) {
    return partitions.get(partitionID);
  }

  public final Partition<P> removePartition(
    int partitionID) {
    return partitions.remove(partitionID);
  }

  public final boolean isEmpty() {
    return partitions.isEmpty();
  }

  public final void release() {
    for (Partition<P> partition : partitions
      .values()) {
      partition.release();
    }
    partitions.clear();
  }

  public final void free() {
    for (Partition<P> partition : partitions
      .values()) {
      partition.free();
    }
    partitions.clear();
  }
}
