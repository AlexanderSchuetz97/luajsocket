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
package io.github.alexanderschuetz97.luajsocket.tcp.java;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPSettings {

    private Boolean keepAlive;

    private Boolean lingerOn;

    private Integer lingerTime;

    private Boolean reuseAddress;

    private Boolean noDelay;

    private int singleTimeout = -1;

    private int totalTimeout = -1;

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setSoLinger(boolean lingerOn, int lingerTime) {
        this.lingerOn = lingerOn;
        this.lingerTime = lingerTime;
    }

    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    public void setNoDelay(boolean noDelay) {
        this.noDelay = noDelay;
    }

    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public Boolean getLingerOn() {
        return lingerOn;
    }

    public Integer getLingerTime() {
        return lingerTime;
    }

    public Boolean getReuseAddress() {
        return reuseAddress;
    }

    public Boolean getNoDelay() {
        return noDelay;
    }

    public int getSingleTimeout() {
        return singleTimeout;
    }

    public int getTotalTimeout() {
        return totalTimeout;
    }

    public int getMinTimeout() {
        if (singleTimeout >= 0 && totalTimeout >= 0) {
            return Math.min(singleTimeout, totalTimeout);
        }

        if (singleTimeout >= 0) {
            return singleTimeout;
        }

        return totalTimeout;
    }

    public void setSingleTimeout(int timeout) {
        this.singleTimeout = timeout;
    }


    public void setTotalTimeout(int timeout) {
        this.totalTimeout = timeout;
    }

    public void apply(Socket socket) throws IOException {
        if (noDelay != null) {
            socket.setTcpNoDelay(noDelay);
        }

        if (lingerOn != null) {
            socket.setSoLinger(lingerOn, lingerTime);
        }

        if (keepAlive != null) {
            socket.setKeepAlive(keepAlive);
        }
    }

    public void apply(ServerSocket serverSocket) throws IOException {
        if (reuseAddress != null) {
            serverSocket.setReuseAddress(reuseAddress);
        }
    }

    public void copyTo(TCPSettings target) {
        target.keepAlive = keepAlive;
        target.lingerTime = lingerTime;
        target.lingerOn = lingerOn;
        target.noDelay = noDelay;
        target.singleTimeout = singleTimeout;
        target.totalTimeout = totalTimeout;
        target.reuseAddress = reuseAddress;
    }

}
