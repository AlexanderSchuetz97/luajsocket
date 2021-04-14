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
package io.github.alexanderschuetz97.luajsocket.udp.java;

import org.luaj.vm2.LuaError;

import java.io.IOException;
import java.net.*;

/**
 * Java Master Object for UDP connections.
 */
public class UDPMaster {

    private final DatagramSocket socket;
    private final DatagramPacket buffer = new DatagramPacket(new byte[0xffff], 0xffff );
    private InetSocketAddress connectedAddress;

    public UDPMaster() {
        try {
            this.socket = new DatagramSocket();
            this.socket.setReuseAddress(true);
        } catch (SocketException e) {
            throw new LuaError(e);
        }
    }

    public synchronized void setTimeout(int timeout) throws IOException {
        if (timeout == 0) {
            timeout = 1;
        }

        if (timeout < 0) {
            timeout = 0;
        }

        socket.setSoTimeout(timeout);
    }

    public synchronized void connect(String address, int port) throws IOException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getByName(address), port);
        this.socket.connect(inetSocketAddress);
        this.connectedAddress = inetSocketAddress;

    }

    public synchronized void send(byte[] payload, int off, int len) throws IOException {
        if (connectedAddress == null) {
            throw new IOException("Not connected!");
        }
        DatagramPacket packet = new DatagramPacket(payload, off, len, connectedAddress);
        socket.send(packet);
    }

    public synchronized void sendTo(byte[] payload, int off, int len, String address, int port) throws IOException{
        InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getByName(address), port);
        boolean dc = false;
        if (connectedAddress != null && !connectedAddress.equals(inetSocketAddress)) {
            //unlucky
            socket.disconnect();
            dc = true;
        }

        socket.send(new DatagramPacket(payload, off, len, inetSocketAddress));
        if (dc) {
            socket.connect(connectedAddress);
        }
    }

    public synchronized byte[] receive() throws IOException {
        socket.receive(buffer);
        int l = buffer.getLength();
        byte[] copy = new byte[l];
        System.arraycopy(buffer.getData(), buffer.getOffset(), copy, 0, l);
        return copy;
    }

    public synchronized ReceiveFromResult receiveFrom() throws IOException {
        socket.receive(buffer);
        int l = buffer.getLength();
        byte[] copy = new byte[l];
        System.arraycopy(buffer.getData(), buffer.getOffset(), copy, 0, l);
        return new ReceiveFromResult(new InetSocketAddress(buffer.getAddress(), buffer.getPort()), copy);
    }

    public synchronized void disconnect() {
        socket.disconnect();
    }

    public synchronized void setBroadcast(boolean flag) throws IOException {
        socket.setBroadcast(flag);
    }

    public synchronized void bind(String address, int port) throws IOException {
        if ("*".equals(address)) {
            address = "0.0.0.0";
        }

        socket.bind(new InetSocketAddress(InetAddress.getByName(address), port));
    }

    public InetSocketAddress getConnectedAddress() {
        return connectedAddress;
    }

    public InetSocketAddress getLocalAddress() {
        if (!socket.isBound()) {
            return null;
        }

        return (InetSocketAddress) socket.getLocalSocketAddress();
    }

    public synchronized void close() {
        socket.close();
    }

    public static class ReceiveFromResult {
        final InetSocketAddress address;
        final byte[] data;

        ReceiveFromResult(InetSocketAddress address, byte[] data) {
            this.address = address;
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }

        public InetSocketAddress getAddress() {
            return address;
        }
    }
}
