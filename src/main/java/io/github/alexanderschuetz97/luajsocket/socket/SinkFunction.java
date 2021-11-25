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
package io.github.alexanderschuetz97.luajsocket.socket;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import io.github.alexanderschuetz97.luajsocket.tcp.java.TCPMaster;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPMasterUserdata;
import io.github.alexanderschuetz97.luajsocket.util.ByteArrayOutputStreamWithBufferAccess;
import io.github.alexanderschuetz97.luajsocket.util.Util;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This function creates ltn12 sinks for sockets.
 * This can be useful for processing data from a ltn12 stream.
 */
public class SinkFunction extends AbstractLuaJSocketFunction {

    public SinkFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    public Varargs invoke(Varargs args) {
        String mode = args.checkjstring(1);
        LuaValue socket = args.arg(2);
        TCPMaster master = null;
        if (socket instanceof TCPMasterUserdata) {
            master = ((TCPMasterUserdata) socket).getMaster();
        }

        if (master == null) {
            return new ErrSink(valueOf("downstream is not a socket. got " + socket.typename()));
        }

        switch (mode) {
            case "http-chunked":
                return new SinkChunked(master);
            case "close-when-done":
                return new SinkClose(master);
            case "keep-open":
                return new SinkOpen(master);
            default:
                return new ErrSink(valueOf("invalid sink mode " + mode));
        }
    }

    private static class ErrSink extends VarArgFunction {

        private final LuaString err;

        private ErrSink(LuaString err) {
            this.err = err;
        }

        @Override
        public Varargs invoke(Varargs varargs) {
            return varargsOf(NIL, err);
        }
    }

    private static class SinkClose extends VarArgFunction {

        final TCPMaster downstream;

        private SinkClose(TCPMaster downstream) {
            this.downstream = downstream;
        }

        @Override
        public Varargs invoke(Varargs args) {
            if (downstream.isClosed()) {
                return varargsOf(NIL, valueOf("closed"));
            }

            if (!downstream.isClient()) {
                return varargsOf(NIL, valueOf("not a client socket"));
            }

            if (args.isnil(1)) {
                return closeIt(downstream);
            }

            LuaValue payload = args.arg1().tostring();
            if (payload.isnil()) {
                closeIt(downstream);
                return varargsOf(NIL, valueOf("chunk is not a string is " + payload.typename()));
            }

            LuaString payloadStr = payload.checkstring();
            int timeout = downstream.getSettings().getSingleTimeout();
            int totalTimeout = downstream.getSettings().getTotalTimeout();
            try {
                if (downstream.getClient().write(payloadStr.m_bytes, payloadStr.m_offset, payloadStr.m_length, timeout, totalTimeout) != payloadStr.m_length) {
                    closeIt(downstream);
                    return varargsOf(NIL, valueOf("timeout"));
                }
            } catch (Exception e) {
                closeIt(downstream);
                return varargsOf(NIL, Util.stringToLuaString(e.getMessage()));
            }

            return ONE;
        }
    }

    private static class SinkOpen extends VarArgFunction {

        final TCPMaster downstream;

        private SinkOpen(TCPMaster downstream) {
            this.downstream = downstream;
        }

        @Override
        public Varargs invoke(Varargs args) {
            if (downstream.isClosed()) {
                return varargsOf(NIL, valueOf("closed"));
            }

            if (!downstream.isClient()) {
                return varargsOf(NIL, valueOf("not a client socket"));
            }

            if (args.isnil(1)) {
                return ONE;
            }

            LuaValue payload = args.arg1().tostring();
            if (payload.isnil()) {
                return varargsOf(NIL, valueOf("chunk is not a string is " + payload.typename()));
            }

            LuaString payloadStr = payload.checkstring();
            int timeout = downstream.getSettings().getSingleTimeout();
            int totalTimeout = downstream.getSettings().getTotalTimeout();
            try {
                int written = downstream.getClient().write(payloadStr.m_bytes, payloadStr.m_offset, payloadStr.m_length, timeout, totalTimeout);
                if (written < payloadStr.m_length) {
                    return varargsOf(NIL, valueOf("timeout"), LuaString.valueUsing(payloadStr.m_bytes, payloadStr.m_offset+written, payloadStr.m_length-written));
                }
            } catch (Exception e) {
                return varargsOf(NIL, Util.stringToLuaString(e.getMessage()));
            }

            return ONE;
        }
    }


    private static class SinkChunked extends VarArgFunction {

        private static final byte[] END = new byte[]{'0','\r','\n','\r','\n'};

        final TCPMaster downstream;

        private SinkChunked(TCPMaster downstream) {
            this.downstream = downstream;
        }

        @Override
        public Varargs invoke(Varargs args) {
            if (downstream.isClosed()) {
                return varargsOf(NIL, valueOf("closed"));
            }

            if (!downstream.isClient()) {
                return varargsOf(NIL, valueOf("not a client socket"));
            }

            int timeout = downstream.getSettings().getSingleTimeout();
            int totalTimeout = downstream.getSettings().getTotalTimeout();


            if (args.isnil(1)) {
                try {
                    if (downstream.getClient().write(END, timeout, totalTimeout) != END.length) {
                        return varargsOf(NIL, valueOf("timeout"));
                    }
                } catch (Exception e){
                    closeIt(downstream);
                    return varargsOf(NIL, Util.stringToLuaString(e.getMessage()));
                }

                return closeIt(downstream);
            }

            LuaValue payload = args.arg1().tostring();
            if (payload.isnil()) {
                closeIt(downstream);
                return varargsOf(NIL, valueOf("chunk is not a string is " + payload.typename()));
            }


            LuaString payloadStr = payload.checkstring();
            ByteArrayOutputStreamWithBufferAccess baos = new ByteArrayOutputStreamWithBufferAccess();
            try {
                baos.write(String.valueOf(payloadStr.m_length).getBytes(StandardCharsets.UTF_8));
                baos.write('\r');
                baos.write('\n');
                baos.write(payloadStr.m_bytes);
                baos.write('\r');
                baos.write('\n');
                if (downstream.getClient().write(baos.getBuffer(), 0, baos.size()) != baos.size()) {
                    closeIt(downstream);
                    return varargsOf(NIL, valueOf("timeout"));
                }
            } catch (Exception e) {
                closeIt(downstream);
                return varargsOf(NIL, Util.stringToLuaString(e.getMessage()));
            }

            return ONE;
        }
    }

    public static Varargs closeIt(TCPMaster master) {
        try {
            master.close();
        } catch (IOException e) {
            return varargsOf(NIL, Util.stringToLuaString(e.getMessage()));
        }

        return ONE;
    }
}
