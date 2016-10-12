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

package edu.iu.harp.io;

import java.io.DataOutput;
import java.io.IOException;

import org.apache.log4j.Logger;

import edu.iu.harp.resource.ByteArray;

public class Serializer implements DataOutput {

  /** Class logger */
  @SuppressWarnings("unused")
  private static final Logger LOG = Logger
    .getLogger(Serializer.class);

  private byte[] bytes;
  private int pos;
  private int len;

  public Serializer(ByteArray byteArr) {
    this(byteArr.get(), byteArr.start(), byteArr
      .start() + byteArr.size());
  }

  /**
   * Serialize on bytes which starts from bytes[0]
   * to bytes[len - 1], with pos between 0 ~ len -
   * 1
   * 
   * @param bytes
   * @param len
   * @param pos
   */
  public Serializer(byte[] bytes, int pos, int len) {
    this.bytes = bytes;
    this.pos = pos;
    this.len = len;
  }

  public int getPos() {
    return this.pos;
  }

  public int getLength() {
    return this.len;
  }

  @Override
  public void write(int b) throws IOException {
    b = b & 0xFF;
    writeByte(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    if ((pos + b.length) > len) {
      throw new IOException("Cannot write.");
    }
    System.arraycopy(b, 0, bytes, pos, b.length);
    pos += b.length;
  }

  @Override
  public void
    write(byte[] b, int off, int length)
      throws IOException {
    if (((pos + length) > len)
      || ((off + length) > b.length)) {
      throw new IOException("Cannot write.");
    }
    System.arraycopy(b, off, bytes, pos, length);
    pos += length;
  }

  @Override
  public void writeBoolean(boolean v)
    throws IOException {
    if (pos >= len) {
      throw new IOException("Cannot write.");
    }
    if (v) {
      bytes[pos++] = 1;
    } else {
      bytes[pos++] = 0;
    }
  }

  @Override
  public void writeByte(int v) throws IOException {
    if (pos >= len) {
      throw new IOException("Cannot write.");
    }
    bytes[pos++] = (byte) (v & 0xFF);
  }

  @Override
  public void writeShort(int v)
    throws IOException {
    if ((pos + 2) > len) {
      throw new IOException("Cannot write.");
    }
    bytes[pos++] = (byte) ((v >>> 8) & 0xFF);
    bytes[pos++] = (byte) ((v >>> 0) & 0xFF);
  }

  @Override
  public void writeChar(int v) throws IOException {
    if ((pos + 2) > len) {
      throw new IOException("Cannot write.");
    }
    bytes[pos++] = (byte) ((v >>> 8) & 0xFF);
    bytes[pos++] = (byte) ((v >>> 0) & 0xFF);
  }

  @Override
  public void writeInt(int v) throws IOException {
    if ((pos + 4) > len) {
      throw new IOException("Cannot write.");
    }
    bytes[pos++] = (byte) ((v >>> 24) & 0xFF);
    bytes[pos++] = (byte) ((v >>> 16) & 0xFF);
    bytes[pos++] = (byte) ((v >>> 8) & 0xFF);
    bytes[pos++] = (byte) ((v >>> 0) & 0xFF);
  }

  @Override
  public void writeLong(long v)
    throws IOException {
    if ((pos + 8) > len) {
      throw new IOException("Cannot write long.");
    }
    bytes[pos++] = (byte) ((v >>> 56) & 0xffL);
    bytes[pos++] = (byte) ((v >>> 48) & 0xffL);
    bytes[pos++] = (byte) ((v >>> 40) & 0xffL);
    bytes[pos++] = (byte) ((v >>> 32) & 0xffL);
    bytes[pos++] = (byte) ((v >>> 24) & 0xffL);
    bytes[pos++] = (byte) ((v >>> 16) & 0xffL);
    bytes[pos++] = (byte) ((v >>> 8) & 0xffL);
    bytes[pos++] = (byte) ((v >>> 0) & 0xffL);
  }

  @Override
  public void writeFloat(float v)
    throws IOException {
    writeInt(Float.floatToIntBits(v));
  }

  @Override
  public void writeDouble(double v)
    throws IOException {
    writeLong(Double.doubleToLongBits(v));
  }

  @Override
  public void writeBytes(String s)
    throws IOException {
    writeChars(s);
  }

  @Override
  public void writeChars(String s)
    throws IOException {
    int length = s.length();
    if ((pos + 4 + length * 2) > len) {
      throw new IOException("Cannot write.");
    }
    writeInt(length);
    for (int i = 0; i < length; i++) {
      writeChar(s.charAt(i));
    }
  }

  @Override
  public void writeUTF(String s)
    throws IOException {
    writeChars(s);
  }
}
