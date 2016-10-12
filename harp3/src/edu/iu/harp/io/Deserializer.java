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

import java.io.DataInput;
import java.io.IOException;

import org.apache.log4j.Logger;

import edu.iu.harp.resource.ByteArray;

/**
 * Learned from DataInputStream &
 * ByteArrayInputStream
 * 
 * @author zhangbj
 *
 */
public class Deserializer implements DataInput {

  /** Class logger */
  @SuppressWarnings("unused")
  private static final Logger LOG = Logger
    .getLogger(Deserializer.class);

  private byte[] bytes;
  private int len;
  private int pos;

  public Deserializer(ByteArray byteArr) {
    this(byteArr.get(), byteArr.start(), byteArr
      .start() + byteArr.size());
  }

  public Deserializer(byte[] bytes, int pos,
    int len) {
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
  public void readFully(byte[] b)
    throws IOException {
    if ((pos + b.length) > len) {
      throw new IOException("Cannot read.");
    }
    System.arraycopy(bytes, pos, b, 0, b.length);
    pos += b.length;
  }

  @Override
  public void readFully(byte[] b, int off,
    int length) throws IOException {
    if (((pos + length) > len)
      || ((off + length) > b.length)) {
      throw new IOException("Cannot read.");
    }
    System.arraycopy(bytes, pos, b, off, length);
    pos += length;
  }

  @Override
  public int skipBytes(int n) throws IOException {
    pos += n;
    return pos;
  }

  @Override
  public boolean readBoolean() throws IOException {
    if (pos >= len) {
      throw new IOException("Cannot read.");
    }
    byte ch = bytes[pos++];
    if (ch == 1) {
      return true;
    }
    return false;
  }

  @Override
  public byte readByte() throws IOException {
    if (pos >= len) {
      throw new IOException("Cannot read.");
    }
    return bytes[pos++];
  }

  @Override
  public int readUnsignedByte()
    throws IOException {
    int i = readByte();
    return i & 0xff;
  }

  @Override
  public short readShort() throws IOException {
    if ((pos + 2) > len) {
      throw new IOException("Cannot read.");
    }
    return (short) (((bytes[pos++] & 0xff) << 8) | (bytes[pos++] & 0xff));
  }

  @Override
  public int readUnsignedShort()
    throws IOException {
    int s = readShort();
    return s & 0xFFFF;
  }

  @Override
  public char readChar() throws IOException {
    if ((pos + 2) > len) {
      throw new IOException("Cannot read.");
    }
    return (char) (((bytes[pos++] & 0xff) << 8) | (bytes[pos++] & 0xff));
  }

  @Override
  public int readInt() throws IOException {
    if ((pos + 4) > len) {
      throw new IOException("Cannot read.");
    }
    return ((bytes[pos++] & 0xff) << 24)
      | ((bytes[pos++] & 0xff) << 16)
      | ((bytes[pos++] & 0xff) << 8)
      | (bytes[pos++] & 0xff);
  }

  @Override
  public long readLong() throws IOException {
    if ((pos + 8) > len) {
      throw new IOException("Cannot read.");
    }
    return ((bytes[pos++] & 0xffL) << 56)
      | ((bytes[pos++] & 0xffL) << 48)
      | ((bytes[pos++] & 0xffL) << 40)
      | ((bytes[pos++] & 0xffL) << 32)
      | ((bytes[pos++] & 0xffL) << 24)
      | ((bytes[pos++] & 0xffL) << 16)
      | ((bytes[pos++] & 0xffL) << 8)
      | (bytes[pos++] & 0xffL);
  }

  @Override
  public float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  @Override
  public double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  @Override
  public String readLine() throws IOException {
    return readUTF();
  }

  @Override
  public String readUTF() throws IOException {
    int length = readInt();
    if ((pos + length * 2) > len) {
      pos -= 4; // Roll back
      throw new IOException("Cannot read.");
    }
    char[] chars = new char[length];
    for (int i = 0; i < length; i++) {
      chars[i] = readChar();
    }
    return new String(chars);
  }
}
