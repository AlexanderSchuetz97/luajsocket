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
package io.github.alexanderschuetz97.luajsocket.tcp.java;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import org.luaj.vm2.LuaError;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Java Master Object for TCP connections.
 */
public class TCPMaster {

    protected final WeakReference<LuaJSocketLib> luaJSocket;

    public TCPMaster(LuaJSocketLib socket) {
        luaJSocket = new WeakReference<>(socket);
    }

    protected final TCPSettings settings = new TCPSettings();

    protected final TCPStats stats = new TCPStats();

    protected TCPClient client;

    protected TCPServer server;

    protected volatile boolean isClosed;

    public TCPSettings getSettings() {
        return settings;
    }

    public TCPStats getStats() {
        return stats;
    }

    public TCPServer getServer() {
        return server;
    }

    public synchronized void setServer(ServerSocket server) throws IOException {
        if (isInitialized()) {
            throw new LuaError("Already initialized");
        }

        getSettings().apply(server);

        this.server = new TCPServer(this, server);
    }

    public TCPClient getClient() {
        return client;
    }

    public synchronized void setClient(Socket client) throws IOException {
        if (isInitialized()) {
            throw new LuaError("Already initialized");
        }

        getSettings().apply(client);

        this.client = new TCPClient(client, this);
    }


    public synchronized void setClient(TCPServer creator, Socket client) throws IOException {
        if (isInitialized()) {
            throw new LuaError("Already initialized");
        }

        getSettings().apply(client);

        this.client = new TCPClient(client, this, creator);
    }

    public boolean isClient() {
        return client != null;
    }

    public boolean isServer() {
        return server != null;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public boolean isOpen() {
        return !isClosed && (client != null || server != null);
    }

    public synchronized void close() throws IOException {
        isClosed = true;
        if (client != null) {
            try {
                client.close();
            }
            finally {
                client = null;
            }
            return;
        }


        if (server != null) {
            server.close();
            server = null;
        }
    }
    //Condition stuff

    protected volatile SelectCondition condition;

    public synchronized void setCondition(SelectCondition condition) {
        if (condition != null && this.condition != condition && this.condition != null) {
            throw new IllegalStateException("Only one condition at a time");
        }
        this.condition = condition;

        //If select called then this also accounts to the querk of needing to be called with a non zero timeout.
        if (this.client != null) {
            this.client.wasCalledWithNonZeroTimeout = true;
        }
    }

    public void notifyReadReady() {
        SelectCondition cond = condition;
        if (cond != null) {
            condition.notifyReadReady(this);
        }
    }

    public void notifyWriteReady() {
        SelectCondition cond = condition;
        if (cond != null) {
            condition.notifyWriteReady(this);
        }
    }

    //Condition stuff end

    /**
     * Delegate method to execute async task.
     */
    protected void execute(Runnable runnable) {
        LuaJSocketLib socket = luaJSocket.get();
        if (socket == null) {
            throw new RuntimeException("LuaJSocket instance was garbage collected.");
        }
        socket.execute(runnable);
    }

    public boolean isInitialized() {
        return isClosed || isClient() || isServer();
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

}
