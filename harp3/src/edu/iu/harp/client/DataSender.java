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

package edu.iu.harp.client;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import edu.iu.harp.io.Connection;
import edu.iu.harp.io.Data;
import edu.iu.harp.io.DataStatus;
import edu.iu.harp.io.IOUtil;
import edu.iu.harp.io.Serializer;
import edu.iu.harp.resource.ByteArray;
import edu.iu.harp.worker.Workers;

/**
 * Different from DataReceiver, we don't release
 * the encoded bytes in failures.
 * 
 */
public class DataSender extends Sender {
  /** Class logger */
  @SuppressWarnings("unused")
  private static final Logger LOG = Logger
    .getLogger(DataSender.class);

  public DataSender(Data data, int destWorkerID,
    Workers workers, byte command) {
    super(data, destWorkerID, workers, command);
  }

  public DataSender(Data data, String host,
    int port, byte command) {
    super(data, host, port, command);
  }

  @Override
  protected void handleData(
    final Connection conn, final Data data)
    throws Exception {
    // Get head size and body size
    int headArrSize = getHeadSize(data);
    ByteArray opArray =
      getOPByteArray(headArrSize);
    if (opArray == null) {
      throw new IOException(
        "Cannot get op array.");
    }
    try {
      sendDataBytes(conn, opArray, data);
    } catch (IOException e) {
      throw e;
    } finally {
      opArray.release();
    }
  }

  protected int getHeadSize(Data data) {
    return data.getHeadArray().size();
  }

  protected ByteArray getOPByteArray(
    int headArrSize) {
    ByteArray opArray = ByteArray.create(4, true);
    if (opArray != null) {
      try {
        Serializer serializer =
          new Serializer(opArray);
        serializer.writeInt(headArrSize);
        return opArray;
      } catch (Exception e) {
        opArray.release();
        return null;
      }
    } else {
      return null;
    }
  }

  protected void sendDataBytes(Connection conn,
    final ByteArray opArray, final Data data)
    throws IOException {
    // Get op bytes and size
    OutputStream out = conn.getOutputStream();
    byte[] opBytes = opArray.get();
    int opArrSize = opArray.size();
    // Get head size and body size
    ByteArray headArray = data.getHeadArray();
    byte[] headBytes = headArray.get();
    int headArrSize = headArray.size();
    try {
      out.write(getCommand());
      IOUtil
        .sendBytes(out, opBytes, 0, opArrSize);
      out.flush();
      // Send head bytes
      if (headArrSize > 0) {
        IOUtil.sendBytes(out, headBytes, 0,
          headArrSize);
      }
      sendBodyBytes(out, data);
    } catch (IOException e) {
      throw e;
    }
  }

  private void sendBodyBytes(
    final OutputStream out, final Data data)
    throws IOException {
    // Send content data, check the array size
    // first. Sending or receiving null array is
    // allowed
    DataStatus bodyStatus = data.getBodyStatus();
    if (bodyStatus == DataStatus.ENCODED_ARRAY_DECODED
      || bodyStatus == DataStatus.ENCODED_ARRAY
      || bodyStatus == DataStatus.ENCODED_ARRAY_DECODE_FAILED) {
      ByteArray bodyArray = data.getBodyArray();
      IOUtil.sendBytes(out, bodyArray.get(),
        bodyArray.start(), bodyArray.size());
    }
  }
}
