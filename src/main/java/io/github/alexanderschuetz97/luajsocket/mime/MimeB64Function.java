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
// in the COPYING file in top level directory of luajsocket.
// If not, see <https://www.gnu.org/licenses/>.
//
package io.github.alexanderschuetz97.luajsocket.mime;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import io.github.alexanderschuetz97.luajsocket.socket.AbstractLuaJSocketFunction;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.Varargs;

import static io.github.alexanderschuetz97.luajsocket.util.Util.BASE64_TABLE;

/**
 * This function does some very weird base 64 stuff.
 * It takes 2 Inputs. If the second is nil then just base64 the first and thats it.
 * If the second input is not nil It will concatenate them and return the base 64 of the substring of that concatenation
 * but only of the up those characters where a base 64 representation without creating '=' can be found.
 * (As in up until the point where the character index is greater than the last index which is dividable by 3 for base 64).
 * The characters which are not encoded are returned as is in a second result.
 *
 * The algorithm with 2 inputs is thus:
 * 1. given "ABCDEF", "A"
 * 2. concat to "ABCDEFA"
 * 3. determine lenght of  "ABCDEFA" -> 7
 * 4. calculate x=l-(l%3) -> x=6 for l = 7
 * 5. take substring of "ABCDEFA" length x -> "ABCDEF"
 * 6. Base 64 "ABCDEF"
 * 7. take subsgring of "ABCDEFA" starting at x for the rest of the string -> "A"
 * 8. return result from 6. and 7.
 *
 * The benefit of this is that the result of 6 never has "==" or "=" in it.
 * This is useful when processing a stream of unknown length.
 */
public class MimeB64Function extends AbstractLuaJSocketFunction {

    public MimeB64Function(LuaJSocketLib env) {
        super(env);
    }




    private static byte[] b64simple(byte[] buf, int off, int len) {
        int targetLen = len;
        int mod = targetLen%3;
        if (mod != 0) {
            targetLen+=3-mod;
        }

        targetLen /=3;
        int groups = mod == 0 ? targetLen : targetLen-1;
        targetLen *=4;
        byte[] target = new byte[targetLen];

        int srcCursor = off;
        int targetCursor = 0;
        for (int g = 0; g < groups; g++) {
            int byte0 = buf[srcCursor++] & 0xff;
            int byte1 = buf[srcCursor++] & 0xff;
            int byte2 = buf[srcCursor++] & 0xff;
            target[targetCursor++] = BASE64_TABLE[(byte0 >> 2)];
            target[targetCursor++] = BASE64_TABLE[(byte0 << 4)&0x3f | (byte1 >> 4)];
            target[targetCursor++] = BASE64_TABLE[(byte1 << 2)&0x3f | (byte2 >> 6)];
            target[targetCursor++] = BASE64_TABLE[byte2 & 0x3f];
        }

        if (mod == 0) {
            return target;
        }

        int byte0 = buf[srcCursor++] & 0xff;
        target[targetCursor++] = BASE64_TABLE[(byte0 >> 2)];
        if (mod == 1) {
            target[targetCursor++] = BASE64_TABLE[((byte0 << 4) & 0x3f)];
            target[targetCursor++] = '=';
            target[targetCursor] = '=';
            return target;
        }

        int byte1 = buf[srcCursor] & 0xff;
        target[targetCursor++] = BASE64_TABLE[((byte0 << 4)&0x3f | (byte1 >> 4))];
        target[targetCursor++] = BASE64_TABLE[((byte1 << 2)&0x3f)];
        target[targetCursor] = '=';
        return target;
    }

    private static byte[][] b64advanced(byte[] buf1, int off1, int len1, byte[] buf2, int off2, int len2) {
        int targetLen = len1+len2;
        int mod = targetLen%3;
        if (mod != 0) {
            targetLen-=mod;
        }

        targetLen /=3;
        int groups = targetLen;
        targetLen *=4;
        byte[] target = new byte[targetLen];

        int srcCursor = 0;
        int targetCursor = 0;
        for (int g = 0; g < groups; g++) {
            int byte0 = (srcCursor >= len1 ? buf2[(srcCursor++)-len1+off2] : buf1[(srcCursor++)+off1]) & 0xff;
            int byte1 = (srcCursor >= len1 ? buf2[(srcCursor++)-len1+off2] : buf1[(srcCursor++)+off1]) & 0xff;
            int byte2 = (srcCursor >= len1 ? buf2[(srcCursor++)-len1+off2] : buf1[(srcCursor++)+off1]) & 0xff;
            target[targetCursor++] = BASE64_TABLE[(byte0 >> 2)];
            target[targetCursor++] = BASE64_TABLE[((byte0 << 4)&0x3f | (byte1 >> 4))];
            target[targetCursor++] = BASE64_TABLE[ ((byte1 << 2)&0x3f | (byte2 >> 6))];
            target[targetCursor++] = BASE64_TABLE[ (byte2 & 0x3f)];
        }

        if (mod == 0) {
            return new byte[][]{target, null};
        }

        byte[] left = new byte[mod];
        for (int i = 0; i < mod; i++) {
            left[i] = (srcCursor >= len1 ? buf2[(srcCursor++)-len1+off2] : buf1[(srcCursor++)+off1]);
        }

        return new byte[][]{target, left};
    }

    @Override
    public Varargs invoke(Varargs args) {
        LuaString arg1 = args.arg1().checkstring(1);
        LuaString arg2 = args.arg(2).optstring(EMPTYSTRING);
        if (arg2.m_length == 0) {
            return valueOf(b64simple(arg1.m_bytes, arg1.m_offset, arg1.m_length));
        }

        byte[][] res = b64advanced(arg1.m_bytes, arg1.m_offset, arg1.m_length, arg2.m_bytes, arg2.m_offset, arg2.m_length);
        if (res[1] == null) {
            return LuaString.valueUsing(res[0]);
        }

        return varargsOf(LuaString.valueUsing(res[0]), LuaString.valueUsing(res[1]));
    }
}
