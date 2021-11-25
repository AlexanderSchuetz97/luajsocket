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

import java.util.concurrent.TimeUnit;

public class TCPStats {

    private volatile long ageTSP = System.currentTimeMillis();

    private volatile long bytesSent = 0;

    private volatile long bytesReceived = 0;

    public long getBytesReceived() {
        return bytesReceived;
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public long getAgeInSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()-ageTSP);
    }

    public void setBytesSent(long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public void setBytesReceived(long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public synchronized void setAgeInSeconds(int seconds) {
        if (seconds <= 0) {
            ageTSP = System.currentTimeMillis();
            return;
        }

        ageTSP = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(seconds);
    }

    public synchronized void addBytesSent(long bytesSent) {
        this.bytesSent += bytesSent;
    }
    public synchronized void addBytesReceived(long bytesSent) {
        this.bytesReceived += bytesSent;
    }


}
