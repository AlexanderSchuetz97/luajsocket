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
package io.github.alexanderschuetz97.luajsocket.mime;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import io.github.alexanderschuetz97.luajsocket.socket.AbstractLuaJSocketFunction;
import io.github.alexanderschuetz97.luajsocket.util.ByteArrayOutputStreamWithBufferAccess;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import static io.github.alexanderschuetz97.luajsocket.util.Util.*;

/**
 * Decodes a base64 string.
 * This function follows the standard concatenation pattern for stream processing in luasocket.
 * This is explained in {@link MimeB64Function}
 */
public class MimeUnB64Function extends AbstractLuaJSocketFunction {

    public MimeUnB64Function(LuaJSocketLib env) {
        super(env);
    }

    public byte[] decode(byte[] bytes) {
        int groups = bytes.length / 4;
        int targetLength = groups*3;
        int missing = 0;
        if (bytes[bytes.length-2] == '=') {
            missing = 2;
        } else if (bytes[bytes.length-1] == '=') {
            missing = 1;
        }

        targetLength-=missing;
        if (missing != 0) {
            groups--;
        }

        byte[] target = new byte[targetLength];
        int srcIndex = 0;
        int targetIndex = 0;
        for (int i = 0; i < groups; i++) {
             int b1 = B64TABLE_REVERSE[unsignedByte(bytes[srcIndex++])];
             int b2 = B64TABLE_REVERSE[unsignedByte(bytes[srcIndex++])];
             int b3 = B64TABLE_REVERSE[unsignedByte(bytes[srcIndex++])];
             int b4 = B64TABLE_REVERSE[unsignedByte(bytes[srcIndex++])];
            target[targetIndex++] = (byte) ((b1 << 2) | (b2 >> 4));
            target[targetIndex++] = (byte) ((b2 << 4) | (b3 >> 2));
            target[targetIndex++] = (byte) ((b3 << 6) | b4);
        }

        if (missing != 0) {
            int ch0 = B64TABLE_REVERSE[unsignedByte(bytes[srcIndex++])];
            int ch1 = B64TABLE_REVERSE[unsignedByte(bytes[srcIndex++])];
            target[targetIndex++] = (byte) ((ch0 << 2) | (ch1 >> 4));

            if (missing == 1) {
                int ch2 = B64TABLE_REVERSE[unsignedByte(bytes[srcIndex])];
                target[targetIndex] = (byte) ((ch1 << 4) | (ch2 >> 2));
            }
        }

        return target;
    }


    @Override
    public Varargs invoke(Varargs args) {
        if (args.isnoneornil(1)) {
            return NIL_NIL;
        }

        LuaString lstr = args.checkstring(1);
        LuaValue lv2 = args.arg(2).tostring();
        if (lv2.isnil()) {
            ByteArrayOutputStreamWithBufferAccess baos = new ByteArrayOutputStreamWithBufferAccess();
            for (int i = 0; i < lstr.m_length; i++) {
                byte b = lstr.m_bytes[i+lstr.m_offset];
                int l = unsignedByte(b);
                if (b == '=' && i+2 >= lstr.m_length) {
                    baos.write('=');
                    continue;
                }
                if (l >= B64TABLE_REVERSE.length || B64TABLE_REVERSE[l] == null) {
                    return NIL_NIL;
                }
                baos.write(b);
            }
            byte[] b = baos.toByteArray();
            if (b.length % 4 != 0) {
                return NIL_NIL;
            }

            return varargsOf(LuaString.valueUsing(decode(b)), NIL);
        }
        LuaString lstr2 = lv2.checkstring();

        ByteArrayOutputStreamWithBufferAccess baos = new ByteArrayOutputStreamWithBufferAccess();

        int totalLen = lstr2.length() + lstr.length();
        int idx = 0;
        for (int i = 0; i < lstr.m_length; i++) {
            byte b = lstr.m_bytes[i+lstr.m_offset];
            int l = unsignedByte(b);
            if (b == '=' && idx+2 >= totalLen) {
                idx++;
                baos.write('=');
                continue;
            }
            if (l >= B64TABLE_REVERSE.length || B64TABLE_REVERSE[l] == null) {
                return NIL_NIL;
            }
            idx++;
            baos.write(b);
        }

        for (int i = 0; i < lstr2.m_length; i++) {
            byte b = lstr2.m_bytes[i+lstr2.m_offset];
            int l = unsignedByte(b);
            if (b == '=' && idx+2 >= totalLen) {
                idx++;
                baos.write('=');
                continue;
            }
            if (l >= B64TABLE_REVERSE.length || B64TABLE_REVERSE[l] == null) {
                return NIL_NIL;
            }
            idx++;
            baos.write(b);
        }

        byte[] b = baos.toByteArray();

        int mod = totalLen %4;
        if (mod == 0) {
            return varargsOf(LuaString.valueUsing(decode(b)), EMPTYSTRING);
        }

        byte[] b2 = new byte[b.length-mod];
        byte[] b3 = new byte[mod];
        System.arraycopy(b, 0, b2, 0,b2.length);
        System.arraycopy(b, b2.length, b3, 0, b3.length);
        return varargsOf(LuaString.valueUsing(decode(b2)), LuaString.valueUsing(b3));
    }
}
