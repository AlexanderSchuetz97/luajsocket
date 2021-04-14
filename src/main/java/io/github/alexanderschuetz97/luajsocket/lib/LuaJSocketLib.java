//
// Copyright Alexander Schütz, 2021
//
// This file is part of luajsocket.
//
// luajsocket is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// luajsocket is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// A copy of the GNU General Public License should be provided
// in the COPYING file in top level directory of luajsocket.
// If not, see <https://www.gnu.org/licenses/>.
//
package io.github.alexanderschuetz97.luajsocket.lib;

import io.github.alexanderschuetz97.luajsocket.tcp.java.TCPMasterFinalizer;
import io.github.alexanderschuetz97.luajsocket.dns.DNSGetHostnameFunction;
import io.github.alexanderschuetz97.luajsocket.dns.DNSToHostnameFunction;
import io.github.alexanderschuetz97.luajsocket.dns.DNSToIpFunction;
import io.github.alexanderschuetz97.luajsocket.socket.*;
import io.github.alexanderschuetz97.luajsocket.tcp.java.TCPMaster;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.*;
import io.github.alexanderschuetz97.luajsocket.udp.java.UDPMaster;
import io.github.alexanderschuetz97.luajsocket.udp.java.UDPMasterFinalizer;
import io.github.alexanderschuetz97.luajsocket.mime.*;
import io.github.alexanderschuetz97.luajsocket.udp.lua.*;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPMasterUserdata;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static io.github.alexanderschuetz97.luajsocket.util.Util.inputStreamToString;

public class LuaJSocketLib extends TwoArgFunction {

    protected Globals globals;

    protected Executor executor;

    @Override
    public LuaValue call(LuaValue arg1, LuaValue arg2) {
        if (globals != null && globals != arg2) {
            throw new LuaError("Loaded twice with different globals.");
        }
        this.globals = arg2.checkglobals();
        if (this.executor == null) {
            executor = createExecutor();
        }


        LuaValue packageLib = globals.get("package");
        if (!packageLib.istable()) {
            throw new LuaError("PackageLib not loaded");
        }

        LuaValue loaded = packageLib.get("loaded");
        if (!loaded.istable()) {
            throw new LuaError("PackageLib corrupted");
        }

        loaded.set("socket.core", createSocketCore());
        LuaValue socket = createSocket();
        loaded.set("socket", socket);
        loaded.set("ltn12", createLTN12());
        loaded.set("socket.headers", createHeaders());
        loaded.set("socket.url", createURL());
        loaded.set("socket.tp", createTp());
        loaded.set("mime.core", createMimeCore());
        loaded.set("mime", createMime());
        loaded.set("socket.http", createHttp());
        loaded.set("socket.ftp", createFTP());
        loaded.set("socket.smtp", createSMTP());


        return socket;
    }

    private void checkLoaded() {
        if (globals == null) {
            throw new LuaError("Globals not loaded");
        }
    }

    /**
     * ThreadPool used for async java operations. Overwrite to replace this with your own thread pool.
     * This method is only called once. The result is cached.
     */
    protected Executor createExecutor() {
        return Executors.newCachedThreadPool();
    }

    /**
     * Execute async task on the executor. Overwrite if you must, preferable overwrite createExecutor.
     * Task must be executed asynchronously. This method should never block.
     */
    public void execute(Runnable runnable) {
        checkLoaded();
        executor.execute(runnable);
    }

    protected LuaValue createMimeCore() {
        checkLoaded();
        LuaTable mimeTable = new LuaTable();
        mimeTable.set("b64", createFunction(MimeB64Function.class));
        mimeTable.set("dot", createFunction(MimeDotFunction.class));
        mimeTable.set("eol", createFunction(MimeEolFunction.class));
        mimeTable.set("qp", createFunction(MimeQPFunction.class));
        mimeTable.set("wrp", createFunction(MimeWrapFunction.class));
        mimeTable.set("qpwrp", createFunction(MimeQPWrapFunction.class));
        mimeTable.set("unb64", createFunction(MimeUnB64Function.class));
        mimeTable.set("unqp", createFunction(MimeUnQPFunction.class));
        mimeTable.set("core", mimeTable);
        return mimeTable;
    }

    protected LuaValue createSMTP() {
        checkLoaded();
        try {
            return globals.load(inputStreamToString(LuaJSocketLib.class.getResourceAsStream("/luasocket/smtp.lua")),"smtp.lua").call();
        } catch (Exception e) {
            throw new LuaError(new RuntimeException("failed to load smtp.lua ",e));
        }
    }

    protected LuaValue createFTP() {
        checkLoaded();
        try {
            return globals.load(inputStreamToString(LuaJSocketLib.class.getResourceAsStream("/luasocket/ftp.lua")),"ftp.lua").call();
        } catch (Exception e) {
            throw new LuaError(new RuntimeException("failed to load ftp.lua ",e));
        }
    }

    protected LuaValue createHttp() {
        checkLoaded();
        try {
            return globals.load(inputStreamToString(LuaJSocketLib.class.getResourceAsStream("/luasocket/http.lua")),"http.lua").call();
        } catch (Exception e) {
            throw new LuaError(new RuntimeException("failed to load http.lua ",e));
        }
    }

    protected LuaValue createTp() {
        checkLoaded();
        try {
            return globals.load(inputStreamToString(LuaJSocketLib.class.getResourceAsStream("/luasocket/tp.lua")),"tp.lua").call();
        } catch (Exception e) {
            throw new LuaError(new RuntimeException("failed to load tp.lua ",e));
        }
    }

    protected LuaValue createMime() {
        checkLoaded();
        try {
            return globals.load(inputStreamToString(LuaJSocketLib.class.getResourceAsStream("/luasocket/mime.lua")),"mime.lua").call();
        } catch (Exception e) {
            throw new LuaError(new RuntimeException("failed to load mime.lua ",e));
        }
    }

    protected LuaValue createLTN12() {
        checkLoaded();
        try {
            return globals.load(inputStreamToString(LuaJSocketLib.class.getResourceAsStream("/luasocket/ltn12.lua")),"ltn12.lua").call();
        } catch (Exception e) {
            throw new LuaError(new RuntimeException("failed to load ltn12.lua ",e));
        }
    }

    protected LuaValue createHeaders() {
        checkLoaded();
        try {
            return globals.load(inputStreamToString(LuaJSocketLib.class.getResourceAsStream("/luasocket/headers.lua")),"headers.lua").call();
        } catch (Exception e) {
            throw new LuaError(new RuntimeException("failed to load headers.lua ",e));
        }
    }

    protected LuaValue createURL() {
        checkLoaded();
        try {
            return globals.load(inputStreamToString(LuaJSocketLib.class.getResourceAsStream("/luasocket/url.lua")),"url.lua").call();
        } catch (Exception e) {
            throw new LuaError(new RuntimeException("failed to load url.lua ",e));
        }
    }

    protected LuaValue createSocketCore() {
        checkLoaded();
        LuaTable socketTable = new LuaTable();
        socketTable.set("dns", createDNS());
        LuaValue tcp = createFunction(TCPFunction.class);
        //Java really does not care if your socket is ipv4 or ipv6...
        socketTable.set("tcp", tcp);
        socketTable.set("tcp4", tcp);
        socketTable.set("tcp6", tcp);

        socketTable.set("udp", createFunction(UDPFunction.class));

        socketTable.set("bind", createFunction(BindFunction.class));
        socketTable.set("connect", createFunction(ConnectFunction.class));
        socketTable.set("newtry", createFunction(NewTryFunction.class));
        socketTable.set("protect", createFunction(ProtectFunction.class));
        socketTable.set("select", createFunction(SelectFunction.class));
        socketTable.set("sink", createFunction(SinkFunction.class));
        socketTable.set("skip", createFunction(SkipFunction.class));
        socketTable.set("sleep", createFunction(SleepFunction.class));
        socketTable.set("gettime", createFunction(GetTimeFunction.class));
        socketTable.set("core", socketTable);
        return socketTable;
    }

    protected LuaValue createSocket() {
        checkLoaded();
        try {
            return globals.load(inputStreamToString(LuaJSocketLib.class.getResourceAsStream("/luasocket/socket.lua")),"socket.lua").call();
        } catch (Exception e) {
            throw new LuaError(new RuntimeException("failed to load socket.lua ",e));
        }
    }

    protected LuaValue createDNS() {
        checkLoaded();
        LuaTable dnsTable = new LuaTable();
        dnsTable.set("gethostname", createFunction(DNSGetHostnameFunction.class));
        dnsTable.set("tohostname", createFunction(DNSToHostnameFunction.class));
        dnsTable.set("toip", createFunction(DNSToIpFunction.class));
        return dnsTable;
    }


    public LuaValue createUDP() {
        checkLoaded();
        UDPMasterUserdata master = new UDPMasterUserdata(new UDPMaster());
        new UDPMasterFinalizer(master);

        master.setFunction("close", createFunction(UDPCloseFunction.class));
        master.setFunction("getpeername", createFunction(UDPGetPeerNameFunction.class));
        master.setFunction("getsockname", createFunction(UDPGetSockNameFunction.class));
        master.setFunction("receive", createFunction(UDPReceiveFunction.class));
        master.setFunction("receivefrom", createFunction(UDPReceiveFromFunction.class));
        master.setFunction("send", createFunction(UDPSendFunction.class));
        master.setFunction("sendto", createFunction(UDPSendToFunction.class));
        master.setFunction("setpeername", createFunction(UDPSetPeerNameFunction.class));
        master.setFunction("setsockname", createFunction(UDPSetSockNameFunction.class));
        master.setFunction("setoption", createFunction(UDPSetOptionFunction.class));
        master.setFunction("settimeout", createFunction(UDPSetTimeoutFunction.class));

        return master;
    }

    public LuaValue createTCP() {
        checkLoaded();
        TCPMasterUserdata master = new TCPMasterUserdata(new TCPMaster(this));

        //This will self register on the reference queue.
        new TCPMasterFinalizer(master);

        master.setFunction("accept", createFunction(TCPAcceptFunction.class));
        master.setFunction("bind", createFunction(TCPBindFunction.class));
        master.setFunction("close", createFunction(TCPCloseFunction.class));
        master.setFunction("connect", createFunction(TCPConnectFunction.class));
        master.setFunction("getpeername", createFunction(TCPGetPeerNameFunction.class));
        master.setFunction("getsockname", createFunction(TCPGetSockNameFunction.class));
        master.setFunction("getstats", createFunction(TCPGetStatsFunction.class));
        master.setFunction("listen", createFunction(TCPListenFunction.class));
        master.setFunction("receive", createFunction(TCPReceiveFunction.class));
        master.setFunction("send", createFunction(TCPSendFunction.class));
        master.setFunction("setoption", createFunction(TCPSetOptionFunction.class));
        master.setFunction("setstats", createFunction(TCPSetStatsFunction.class));
        master.setFunction("settimeout", createFunction(TCPSetTimeoutFunction.class));
        master.setFunction("shutdown", createFunction(TCPShutdownFunction.class));
        return master;
    }

    /**
     * Overwrite this method in a subclass to overwrite functionality. This can be useful to fix bugs i did not spot
     * or "change" functionality in case that change better suits your needs.
     * The class acts as an identifier what functionality is being created, you are free to return whatever you
     * want but it should be a function otherwise it may break.
     */
    public LuaValue createFunction(Class<? extends AbstractLuaJSocketFunction> func) {
        checkLoaded();
        try {
            return func.getConstructor(LuaJSocketLib.class).newInstance(this);
        } catch (Exception e) {
            throw new LuaError(e);
        }
    }



}
