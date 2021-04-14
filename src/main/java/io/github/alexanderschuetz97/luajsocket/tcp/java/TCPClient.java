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

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Represents a TCP client instance in TCPMaster.
 */
public class TCPClient {

    private final TCPMaster master;

    private final TCPServer creator;

    private final Socket socket;

    private final OutputStream socketOutputStream;

    private final InputStream socketInputStream;

    private final RingBuffer readFromSocketBuffer = new RingBuffer();

    private final RingBuffer writeToSocketBuffer = new RingBuffer();

    private final WriteJob writeJob = new WriteJob();

    private final ReadJob readJob = new ReadJob();

    private final Object writeMutex = new Object();

    private volatile boolean writeErrorPassedOn = false;

    public TCPClient(Socket socket, TCPMaster master) throws IOException {
        this(socket, master, null);
    }

    public TCPClient(Socket socket, TCPMaster master, TCPServer creator) throws IOException {
        this.master = master;
        this.socket = socket;
        this.creator = creator;
        this.socketOutputStream = socket.getOutputStream();
        this.socketInputStream = socket.getInputStream();
        master.execute(readJob);
        master.execute(writeJob);
    }

    public TCPMaster getMaster() {
        return master;
    }

    public void close() throws IOException {
        if (!socket.isClosed()) {
            synchronized (writeMutex) {
                writeToSocketBuffer.eof();
            }
            writeJob.awaitEOF();
        }

        socket.close();

        if (!writeErrorPassedOn && writeToSocketBuffer.getError() != null) {
            writeErrorPassedOn = true;
            throw writeToSocketBuffer.getError();
        }

    }

    public Socket getSocket() {
        return socket;
    }

    private void incrementReadBytes(long amt) {
        master.getStats().addBytesReceived(amt);
        if (creator != null) {
            creator.getMaster().getStats().addBytesReceived(amt);
        }
    }

    private void incrementWrittenBytes(long amt) {
        master.getStats().addBytesSent(amt);
        if (creator != null) {
            creator.getMaster().getStats().addBytesSent(amt);
        }
    }

    /**
     * Querk from luasocket. If read was never called with a non zero timeout then it always pretends to not have data...
     * Unfortunately mobdebug.lua relies on this querk to work properly so we have to emulate it.
     */
    private boolean wasCalledWithNonZeroTimeout = false;

    public void readLine(ByteArrayOutputStream output, int singleTimeout, int totalTimeout) throws IOException, TimeoutException {
        if (singleTimeout != 0) {
            wasCalledWithNonZeroTimeout = true;
        }
        if (singleTimeout == 0 && !wasCalledWithNonZeroTimeout) {
            throw new TimeoutException();
        }

        int presize = output.size();

        try {
            readFromSocketBuffer.readLine(output, singleTimeout, totalTimeout);
        } finally {
            incrementReadBytes(output.size()-presize);
        }
    }

    public void readAll(ByteArrayOutputStream output, int singleTimeout, int totalTimeout) throws IOException, TimeoutException {
        if (singleTimeout != 0) {
            wasCalledWithNonZeroTimeout = true;
        }
        if (singleTimeout == 0 && !wasCalledWithNonZeroTimeout) {
            throw new TimeoutException();
        }

        int presize = output.size();

        try {
            readFromSocketBuffer.readAll(output, singleTimeout, totalTimeout);
        } finally {
            incrementReadBytes(output.size()-presize);
        }
    }


    public void readBytes(ByteArrayOutputStream output, int count, int singleTimeout, int totalTimeout) throws IOException, TimeoutException {
        if (singleTimeout != 0) {
            wasCalledWithNonZeroTimeout = true;
        }
        if (singleTimeout == 0 && !wasCalledWithNonZeroTimeout) {
            throw new TimeoutException();
        }

        int presize = output.size();

        try {
            readFromSocketBuffer.readBytes(output, count, singleTimeout, totalTimeout);
        } finally {
            incrementReadBytes(output.size()-presize);
        }
    }

    public boolean readReady() {
        return readFromSocketBuffer.canRead();
    }

    public boolean writeReady() {
        return writeToSocketBuffer.canWrite();
    }

    public int write(byte[] buffer, int off, int len, int singleTimeout, int totalTimeout) throws IOException {
        synchronized (writeMutex) {

            long presize = writeToSocketBuffer.getTotalWriteCount();
            try {
                return writeToSocketBuffer.write(buffer, off, len, singleTimeout, totalTimeout);
            } catch(Exception exc) {
                writeErrorPassedOn = true;
                throw exc;
            } finally {
                incrementWrittenBytes(writeToSocketBuffer.getTotalWriteCount() - presize);
            }
        }
    }

    public int write(byte[] bytes, int timeout, int totalTimeout) throws IOException {
        return write(bytes, 0, bytes.length, timeout, totalTimeout);
    }

    public void shutdownInput() throws IOException {
        socket.shutdownInput();
    }

    public void shutdownOutput() throws IOException {
        synchronized (writeMutex) {
            writeToSocketBuffer.eof();
        }
        //Wait for any async data not yet sent....
        writeJob.awaitEOF();
    }

    class WriteJob implements Runnable {
        volatile boolean done = false;

        @Override
        public void run() {
            try {
                while((writeToSocketBuffer.readBytes(socketOutputStream,-1) != -1)) {
                    socketOutputStream.flush();
                    master.notifyWriteReady();
                }
            } catch (IOException e) {
                writeToSocketBuffer.err(e);
            } catch (TimeoutException e) {
                //cant occur.
            }
            finally {
                synchronized (this) {
                    done = true;
                    this.notifyAll();
                }
                master.notifyWriteReady();
            }
        }

        void awaitEOF() {
            synchronized (this) {
                while (!done) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        //DC.
                    }
                }
            }
        }
    }

    class ReadJob implements Runnable {

        @Override
        public void run() {
            byte[] buf = new byte[512];
            int i = 0;
            while(i != -1) {
                try {
                    i = socketInputStream.read(buf);
                } catch (IOException e) {
                    readFromSocketBuffer.err(e);
                    return;
                }
                if (i > 0) {
                    try {
                        readFromSocketBuffer.write(buf, 0, i, -1, -1);
                    } catch (IOException e) {
                        return;
                    }
                }
            }

            readFromSocketBuffer.eof();
        }
    }


}
