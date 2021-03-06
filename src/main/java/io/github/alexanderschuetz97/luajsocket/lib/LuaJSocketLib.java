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
package io.github.alexanderschuetz97.luajsocket.lib;

import io.github.alexanderschuetz97.luajsocket.dns.DNSGetHostnameFunction;
import io.github.alexanderschuetz97.luajsocket.dns.DNSToHostnameFunction;
import io.github.alexanderschuetz97.luajsocket.dns.DNSToIpFunction;
import io.github.alexanderschuetz97.luajsocket.mime.MimeB64Function;
import io.github.alexanderschuetz97.luajsocket.mime.MimeDotFunction;
import io.github.alexanderschuetz97.luajsocket.mime.MimeEolFunction;
import io.github.alexanderschuetz97.luajsocket.mime.MimeQPFunction;
import io.github.alexanderschuetz97.luajsocket.mime.MimeQPWrapFunction;
import io.github.alexanderschuetz97.luajsocket.mime.MimeUnB64Function;
import io.github.alexanderschuetz97.luajsocket.mime.MimeUnQPFunction;
import io.github.alexanderschuetz97.luajsocket.mime.MimeWrapFunction;
import io.github.alexanderschuetz97.luajsocket.socket.AbstractLuaJSocketFunction;
import io.github.alexanderschuetz97.luajsocket.socket.BindFunction;
import io.github.alexanderschuetz97.luajsocket.socket.ConnectFunction;
import io.github.alexanderschuetz97.luajsocket.socket.GetTimeFunction;
import io.github.alexanderschuetz97.luajsocket.socket.NewTryFunction;
import io.github.alexanderschuetz97.luajsocket.socket.ProtectFunction;
import io.github.alexanderschuetz97.luajsocket.socket.SelectFunction;
import io.github.alexanderschuetz97.luajsocket.socket.SinkFunction;
import io.github.alexanderschuetz97.luajsocket.socket.SkipFunction;
import io.github.alexanderschuetz97.luajsocket.socket.SleepFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.java.TCPMaster;
import io.github.alexanderschuetz97.luajsocket.tcp.java.TCPMasterFinalizer;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPAcceptFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPBindFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPCloseFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPConnectFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPGetPeerNameFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPGetSockNameFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPGetStatsFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPListenFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPMasterUserdata;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPReceiveFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPSendFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPSetOptionFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPSetStatsFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPSetTimeoutFunction;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPShutdownFunction;
import io.github.alexanderschuetz97.luajsocket.udp.java.UDPMaster;
import io.github.alexanderschuetz97.luajsocket.udp.java.UDPMasterFinalizer;
import io.github.alexanderschuetz97.luajsocket.udp.lua.UDPCloseFunction;
import io.github.alexanderschuetz97.luajsocket.udp.lua.UDPFunction;
import io.github.alexanderschuetz97.luajsocket.udp.lua.UDPGetPeerNameFunction;
import io.github.alexanderschuetz97.luajsocket.udp.lua.UDPGetSockNameFunction;
import io.github.alexanderschuetz97.luajsocket.udp.lua.UDPMasterUserdata;
import io.github.alexanderschuetz97.luajsocket.udp.lua.UDPReceiveFromFunction;
import io.github.alexanderschuetz97.luajsocket.udp.lua.UDPReceiveFunction;
import io.github.alexanderschuetz97.luajsocket.udp.lua.UDPSendFunction;
import io.github.alexanderschuetz97.luajsocket.udp.lua.UDPSendToFunction;
import io.github.alexanderschuetz97.luajsocket.udp.lua.UDPSetOptionFunction;
import io.github.alexanderschuetz97.luajsocket.udp.lua.UDPSetPeerNameFunction;
import io.github.alexanderschuetz97.luajsocket.udp.lua.UDPSetSockNameFunction;
import io.github.alexanderschuetz97.luajsocket.udp.lua.UDPSetTimeoutFunction;
import io.github.alexanderschuetz97.luajsocket.util.ScriptLoader;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Library that implements socket, socket.core, ltn12, socket.headers, socket.url, socket.tp, mime, mime.core, socket.http, socket.ftp, socket.smtp modules.
 * Load it by calling Globals.load(new LuaJSocketLib()).
 * This library requires PackageLib to be loaded and Globals.compiler to be set.
 */
public class LuaJSocketLib extends TwoArgFunction {

    protected Globals globals;

    protected static Executor DEFAULT_EXECUTOR;

    protected Executor executor;

    @Override
    public LuaValue call(LuaValue arg1, LuaValue arg2) {
        if (globals != null && globals != arg2) {
            throw new LuaError("Loaded twice with different globals.");
        }
        this.globals = arg2.checkglobals();
        if (this.executor == null) {
            executor = getExecutor();
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

    /**
     * Utility method that checks if the library was loaded properly by calling Globals.load().
     */
    protected void checkLoaded() {
        if (globals == null) {
            throw new LuaError("Globals not loaded");
        }
    }



    /**
     * ThreadPool used for async java operations. Overwrite to replace this with your own thread pool.
     * This method is only called once per instance. The result is cached.
     */
    protected Executor getExecutor() {
        synchronized (LuaJSocketLib.class) {
            if (DEFAULT_EXECUTOR == null) {
                DEFAULT_EXECUTOR = Executors.newCachedThreadPool();
            }
        }

        return DEFAULT_EXECUTOR;
    }

    /**
     * Execute async task on the executor. Overwrite if you must, preferable overwrite createExecutor.
     * Task must be executed asynchronously. This method should never block.
     */
    public void execute(Runnable runnable) {
        checkLoaded();
        executor.execute(runnable);
    }


    /**
     * The version of LuaSocket we are trying to "emulate"
     */
    protected static final LuaString VERSION = LuaString.valueOf("LuaSocket 3.0");

    protected LuaValue getVersion() {
        return VERSION;
    }

    /**
     * Create the core mime functions. luasocket implements them in c.
     */
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

    /**
     * load the smtp.lua from luasocket.
     */
    protected LuaValue createSMTP() {
        return loadLuaScript("smtp.lua");
    }

    /**
     * load the ftp.lua from luasocket.
     */
    protected LuaValue createFTP() {
        return loadLuaScript("ftp.lua");
    }

    /**
     * load the http.lua from luasocket.
     */
    protected LuaValue createHttp() {
        return loadLuaScript("http.lua");
    }

    /**
     * load the tp.lua from luasocket.
     */
    protected LuaValue createTp() {
        return loadLuaScript("tp.lua");
    }

    /**
     * load the mime.lua from luasocket.
     */
    protected LuaValue createMime() {
        return loadLuaScript("mime.lua");
    }

    /**
     * load the headers.lua from luasocket.
     */
    protected LuaValue createHeaders() {
        return loadLuaScript("headers.lua");
    }

    protected LuaValue createURL() {
        return loadLuaScript("url.lua");
    }

    protected LuaValue createSocket() {
        return loadLuaScript("socket.lua");
    }

    protected LuaValue createLTN12() {
        return loadLuaScript("ltn12.lua");
    }



    protected LuaValue createSocketCore() {
        checkLoaded();
        LuaTable socketTable = new LuaTable();

        //Various misc constants that luasocket sets.
        //Applications may need them there are here just in case.
        socketTable.set("_VERSION", getVersion());
        socketTable.set("_SETSIZE", 1024);
        socketTable.set("_DATAGRAMSIZE", 8192);
        socketTable.set("_LUAJ", TRUE);

        socketTable.set("dns", createDNS());
        LuaValue tcp = createFunction(TCPFunction.class);
        //Java really does not care if your socket is ipv4 or ipv6...
        socketTable.set("tcp", tcp);
        socketTable.set("tcp4", tcp);
        socketTable.set("tcp6", tcp);

        socketTable.set("udp", createFunction(UDPFunction.class));
        socketTable.set("udp4", createFunction(UDPFunction.class));
        socketTable.set("udp6", createFunction(UDPFunction.class));

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


    /**
     * Method to overwrite to load the luasocket lua scripts from another location.
     * The default implementation will cache the prototypes/luajc class output (if luac is the compiler in the globals).
     *
     * If no loader/compiler is present in the globals then luac is used.
     * @param aScript
     * @return
     */
    protected LuaValue loadLuaScript(String aScript) {
        checkLoaded();
        return ScriptLoader.instance().load(globals, aScript).call();
    }



}
