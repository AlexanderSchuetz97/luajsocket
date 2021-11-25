//
// Copyright Alexander Schütz, 2021
//
// This file is part of luajsocket.
//
// luajsocket is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// luajsocket is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// A copy of the GNU Lesser General Public License should be provided
// in the COPYING & COPYING.LESSER files in top level directory of luajsocket.
// If not, see <https://www.gnu.org/licenses/>.
//
package io.github.alexanderschuetz97.luajsocket.util;

import java.io.ByteArrayOutputStream;

/**
 * Sometimes, it is really not necessary to copy out the buffer from a byte array output stream.
 * This util class does just that. You can get the internal buffer without needing to copy.
 * Also removes an unneeded exception from one of the write methods.
 */
public class ByteArrayOutputStreamWithBufferAccess extends ByteArrayOutputStream {
    public ByteArrayOutputStreamWithBufferAccess() {
        super();
    }

    public ByteArrayOutputStreamWithBufferAccess(int len) {
        super(len);
    }

    /**
     * Gets the current internal buffer without copying it. offset is always 0, length can be fetched by calling size().
     */
    public byte[] getBuffer() {
        return buf;
    }

    //remove unneeded exception.
    public void write(byte b[]) {
        write(b, 0, b.length);
    }



}
