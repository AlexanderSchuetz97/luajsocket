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
import org.luaj.vm2.Varargs;

import static io.github.alexanderschuetz97.luajsocket.util.Util.CRLF_BYTES;

/**
 * This method does not care about chopping UTF-8 characters in half! Neither does the original wrp of luasocket!
 * example: calling wrp(1,'ä') results in 0xC3|CRLF|0xA4
 * (Hint ä in utf-8 is C3 A4)
 *
 *  This function follows the standard concatenation pattern for stream processing in luasocket.
 *  This is explained in {@link MimeB64Function}
 */
public class MimeWrapFunction extends AbstractLuaJSocketFunction {

    public MimeWrapFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    public Varargs invoke(Varargs args) {
        int n = args.checkint(1);
        if (args.isnoneornil(2)) {
            return varargsOf(NIL, valueOf(n));
        }
        if (n < 0) {
            n = 0;
        }

        LuaString str = args.checkstring(2);

        if (str.m_length == 0) {
            return varargsOf(str, valueOf(n));
        }

        int maxlenFL = args.optint(3, 76);
        int maxlen = maxlenFL;
        //Pretty sure this is unintended behavior of luasocket, the second return value gets negative with this...
        if (maxlen <= 0) {
            //The actual maximum in this case.
            maxlen = 1;
        }

        ByteArrayOutputStreamWithBufferAccess baos = new ByteArrayOutputStreamWithBufferAccess(str.m_length+16);
        for (int i = 0; i < str.m_length; i++) {
            byte b = str.m_bytes[i+str.m_offset];
            if (b == '\r') {
                continue;
            }
            baos.write(b);
        }

        byte[] cbuf = baos.toByteArray();
        baos.reset();

        int copied = 0;
        int tc = Math.min(n, cbuf.length);
        for (int i = 0; i < tc; i++) {
            byte b = cbuf[i];
            copied++;
            if (b == '\r') {
                continue;
            }
            if (b == '\n') {
                copied = i+1;
                break;
            }
            baos.write(b);
        }

        if (copied == cbuf.length) {
            return varargsOf(LuaString.valueUsing(baos.getBuffer(), 0, baos.size()), valueOf(n-copied));
        }

        outer: while(copied < cbuf.length) {
            baos.write(CRLF_BYTES);
            tc = Math.min(cbuf.length-copied, maxlen);
            int off = copied;
            for (int i = 0; i < tc; i++) {
                byte b = cbuf[off+i];
                if (b == '\r') {
                    continue;
                }
                if (b == '\n') {
                    copied +=i+1;
                    if (copied == cbuf.length) {
                        baos.write(CRLF_BYTES);
                        break outer;
                    }
                    continue outer;
                }

                baos.write(b);

            }
            copied+=tc;
        }

        return varargsOf(LuaString.valueUsing(baos.getBuffer(), 0, baos.size()), valueOf(maxlenFL-tc));
    }
}
