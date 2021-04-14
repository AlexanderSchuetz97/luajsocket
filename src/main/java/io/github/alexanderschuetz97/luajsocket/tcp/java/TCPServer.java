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
package io.github.alexanderschuetz97.luajsocket.tcp.java;


import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPMasterUserdata;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents a TCP server instance in TCPMaster.
 */
public class TCPServer {

    private final TCPMaster master;

    private final ServerSocket server;

    private final AcceptWorker worker = new AcceptWorker();

    public TCPServer(TCPMaster master, ServerSocket socket) {
        this.master = master;
        this.server = socket;
        master.execute(worker);
    }

    public TCPMaster getMaster() {
        return master;
    }

    private LinkedBlockingQueue<Object> socketTransferQueue = new LinkedBlockingQueue<>(1);

    public void close() {
        try {
            server.close();
        } catch (IOException e) {

        }

       worker.interrupt();
    }

    public ServerSocket getSocket() {
        return server;
    }

    public synchronized boolean acceptReady() {
        if (server.isClosed()) {
            return true;
        }

        return !socketTransferQueue.isEmpty();
    }

    public synchronized TCPMasterUserdata accept(int timeout) throws TimeoutException, IOException {
        if (server.isClosed()) {
            throw new IOException("closed");
        }
        Object socket = null;
        try {
            if (timeout == 0) {
                socket = socketTransferQueue.poll();
            } else if (timeout < 0) {
                socket = socketTransferQueue.take();
            } else {
                socket = socketTransferQueue.poll(timeout, TimeUnit.MILLISECONDS);
            }

            if (socket == null) {
                throw new TimeoutException();
            }
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }

        if (socket instanceof IOException) {
            throw (IOException) socket;
        }

        Socket theSock = (Socket) socket;


        TCPMasterUserdata tcpMasterUserdata = (TCPMasterUserdata) getMaster().luaJSocket.get().createTCP();
        TCPMaster tcpMaster = tcpMasterUserdata.getMaster();
        master.getSettings().copyTo(tcpMaster.getSettings());
        tcpMaster.setClient(theSock);
        return tcpMasterUserdata;
    }

    private class AcceptWorker implements Runnable {

        volatile boolean started = false;
        volatile Thread workerThread;

        @Override
        public void run() {
            synchronized (this) {
                workerThread = Thread.currentThread();
                started = true;
                this.notifyAll();
            }
            try {
                while(!server.isClosed()) {
                    Object socket;
                    try {
                        socket = server.accept();
                    } catch (IOException e) {
                        socket = e;
                    }


                    try {
                        socketTransferQueue.put(socket);
                    } catch (InterruptedException e) {
                        try {
                            server.close();
                        } catch (IOException ioException) {
                            //DC.
                        }
                        return;
                    }
                }
            } finally {
                synchronized (this) {
                    workerThread = null;
                    Thread.interrupted();
                }
            }
        }

        public synchronized void waitForStart() {
            while (!started) {
                try {
                    this.wait();
                } catch (InterruptedException e) {

                }
            }
        }

        public synchronized void interrupt() {
            if (workerThread != null) {
                workerThread.interrupt();
            }
        }
    }
}
