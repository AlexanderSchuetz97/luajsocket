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

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

/**
 * Ring buffer for TCP connections that implements the luasocket timeout mechanics..
 */
public class RingBuffer {

    private final byte[] buffer = new byte[4096];

    private volatile int readPos = 0;
    private volatile int writePos = 0;

    private volatile long totalWriteCount = 0;

    private volatile boolean eof;
    private volatile IOException exc;

    private volatile boolean readerBlocked;

    private final Object waitForReadableBytes = new Object();
    private final Object waitForWriteableBytes = new Object();
    private final Object waitForReader = new Object();
    private final Object readMutex = new Object();
    private final Object writeMutex = new Object();

    protected int availableToRead() {
        int rp = readPos;
        int wp = writePos;
        if (rp > wp) {
            return (buffer.length-rp)+wp;
        }

        if (wp > rp) {
            return wp-rp;
        }

        return 0;
    }

    protected int availableToWrite() {
        int rp = readPos;
        int wp = writePos;

        if (rp > wp) {
            return (rp-wp)-1;
        }

        if (wp > rp) {
            return ((buffer.length-wp)+rp)-1;
        }

        return buffer.length-1;
    }

    public boolean canWrite() {
        return eof || availableToWrite() > 0;
    }

    public boolean canRead() {
        return eof || availableToRead() > 0;
    }

    protected int waitForBytesToRead(int timeout) {
        int av = availableToRead();
        if (av > 0) {
            return av;
        }

        try {
        synchronized (waitForReadableBytes) {
            av = availableToRead();
            if (av > 0) {
                return av;
            }

            if (eof) {
                return -1;
            }

            if (timeout == 0) {
                return 0;
            }

            synchronized (waitForReader) {
                readerBlocked = true;
                waitForReader.notifyAll();
            }

            try {
                if (timeout > 0) {
                    waitForReadableBytes.wait(timeout);
                } else {
                    waitForReadableBytes.wait();
                }
            } catch (InterruptedException e) {
                err(new InterruptedIOException());
                return -1;
            }

            av = availableToRead();
            if (av > 0) {
                return av;
            }

            return eof ? -1 : 0;
        }
        } finally {
            synchronized (waitForReader) {
                readerBlocked = false;
                waitForReader.notifyAll();
            }
        }
    }

    protected int waitForBytesToWrite(int timeout) {
        if (eof) {
            return -1;
        }

        int av = availableToWrite();
        if (av > 0) {
            return av;
        }

        synchronized (waitForWriteableBytes) {
            if (eof) {
                return -1;
            }

            av = availableToWrite();
            if (av > 0) {
                return av;
            }



            if (timeout == 0) {
                return 0;
            }

            try {
                if (timeout > 0) {
                    waitForWriteableBytes.wait(timeout);
                } else {
                    waitForWriteableBytes.wait();
                }
            } catch (InterruptedException e) {
                err(new InterruptedIOException());
                return -1;
            }

            if (eof) {
                return -1;
            }

            return availableToWrite();
        }
    }

    public boolean waitForEmpty(int timeout) {
        synchronized (writeMutex) {
            if (availableToRead() <= 0) {
                return true;
            }

            if (timeout == 0) {
                return false;
            }

            synchronized (waitForWriteableBytes) {
                 if (availableToRead() <= 0) {
                    return true;
                 }

                 if (eof) {
                     return false;
                 }

                    try {
                        if (timeout > 0) {
                            waitForReadableBytes.wait(timeout);
                        } else {
                            waitForReadableBytes.wait();
                        }
                    } catch (InterruptedException e) {

                    }
                }

            return availableToRead() <= 0;
        }
    }

    /**
     * Waits until a reader is blocked, waiting for the writer.
     * This operation blocks the writer. (it should only be called by the writer...)
     */
    public boolean waitForReaderBlocked(int timeout) {
        if (timeout == 0) {
            return readerBlocked;
        }

        synchronized (writeMutex) {
            synchronized (waitForReader) {
                if (readerBlocked) {
                    return true;
                }

                if (eof) {
                    return false;
                }

                try {
                    if (timeout > 0) {
                        waitForReader.wait(timeout);
                    } else {
                        waitForReader.wait();
                    }
                } catch (InterruptedException e) {

                }
            }

            return readerBlocked;
        }
    }

    public boolean waitForEmptyBufferAndBlockedReader(int singleTimeout, int totalTimeout) {
        synchronized (writeMutex) {
            if (totalTimeout == 0) {
                singleTimeout = 0;
            }

            if (singleTimeout == -1) {
                singleTimeout = totalTimeout;
            }

            long start = System.currentTimeMillis();
            if (!waitForEmpty(singleTimeout)) {
                return false;
            }


            int timeout2 = singleTimeout;
            if (totalTimeout > 0) {
                int left = (int) (totalTimeout-(Math.max(0,System.currentTimeMillis()-start)));
                if (left < 0) {
                    timeout2 = 0;
                } else {
                    timeout2 = Math.min(singleTimeout, left);
                }
            }

            return waitForReaderBlocked(timeout2);
        }
    }

    public void readLine(OutputStream output, int singleTimeout, int totalTimeout) throws IOException, TimeoutException {
       synchronized (readMutex) {
           long start = System.currentTimeMillis();

           if (totalTimeout == 0) {
               singleTimeout = 0;
           }

           do {
               int av = waitForBytesToRead(singleTimeout);
               if (av < 0) {
                   if (exc != null) {
                       throw exc;
                   } else {
                       throw new EOFException();
                   }
               }

               if (av == 0) {
                   throw new TimeoutException();
               }

               while (av > 0) {
                   byte b = buffer[readPos];
                   int inc = readPos + 1;
                   if (inc >= buffer.length) {
                       inc = 0;
                   }
                   readPos = inc;

                   av--;
                   if (b == '\r') {
                       continue;
                   }
                   if (b == '\n') {
                       synchronized (waitForWriteableBytes) {
                           waitForWriteableBytes.notifyAll();
                       }
                       return;
                   }
                   output.write(b);
               }

               synchronized (waitForWriteableBytes) {
                   waitForWriteableBytes.notifyAll();
               }
           } while (totalTimeout <= 0 || System.currentTimeMillis() - start < totalTimeout);

           throw new TimeoutException();
       }
    }

    public void readAll(OutputStream output, int singleTimeout, int totalTimeout) throws IOException, TimeoutException {
        synchronized (readMutex) {
            long start = System.currentTimeMillis();
            if (totalTimeout == 0) {
                singleTimeout = 0;
            }

            do {
                int av = waitForBytesToRead(singleTimeout);
                if (av < 0) {
                    if (exc != null) {
                        throw exc;
                    } else {
                        return;
                    }
                }

                if (av == 0) {
                    throw new TimeoutException();
                }

                if (readPos+av <= buffer.length) {
                    output.write(buffer, readPos, av);
                    readPos+=av;
                    synchronized (waitForWriteableBytes) {
                        waitForWriteableBytes.notifyAll();
                    }
                    continue;
                }
                int firstRead = buffer.length-readPos;
                output.write(buffer, readPos, firstRead);
                int sr = av-firstRead;
                if (sr > 0) {
                    output.write(buffer, 0, sr);
                }
                readPos = sr;
                synchronized (waitForWriteableBytes) {
                    waitForWriteableBytes.notifyAll();
                }

            } while (totalTimeout <= 0 || System.currentTimeMillis() - start < totalTimeout);

            throw new TimeoutException();
        }
    }

    public int readBytes(OutputStream output, int timeout) throws IOException, TimeoutException {
        synchronized (readMutex) {
            int av = waitForBytesToRead(timeout);
            if (av < 0) {
                if (exc != null) {
                    throw exc;
                } else {
                    return -1;
                }
            }

            if (av == 0) {
                throw new TimeoutException();
            }


            if (readPos+av < buffer.length) {
                output.write(buffer, readPos, av);
                readPos+=av;

                synchronized (waitForWriteableBytes) {
                    waitForWriteableBytes.notifyAll();
                }

                return av;
            }

            int firstRead = buffer.length-readPos;
            output.write(buffer, readPos, firstRead);
            int sr = av-firstRead;
            if (sr > 0) {
                output.write(buffer, 0, sr);
            }
            readPos = sr;
            synchronized (waitForWriteableBytes) {
                waitForWriteableBytes.notifyAll();
            }

            return av;
        }
    }

    public void readBytes(OutputStream output, int count, int singleTimeout, int totalTimeout) throws IOException, TimeoutException {
        if (count <= 0) {
            return;
        }

        synchronized (readMutex) {
            long start = System.currentTimeMillis();
            if (totalTimeout == 0) {
                singleTimeout = 0;
            }

            do {
                if (count == 0) {
                    return;
                }

                int av = waitForBytesToRead(singleTimeout);
                if (av < 0) {
                    if (exc != null) {
                        throw exc;
                    } else {
                        throw new EOFException();
                    }
                }

                if (av == 0) {
                    throw new TimeoutException();
                }

                av = Math.min(av, count);
                count-=av;

                if (readPos+av < buffer.length) {
                    output.write(buffer, readPos, av);
                    readPos+=av;
                    synchronized (waitForWriteableBytes) {
                        waitForWriteableBytes.notifyAll();
                    }
                    continue;
                }
                int firstRead = buffer.length-readPos;
                output.write(buffer, readPos, firstRead);
                int sr = av-firstRead;
                if (sr > 0) {
                    output.write(buffer, 0, sr);
                }
                readPos = sr;
                synchronized (waitForWriteableBytes) {
                    waitForWriteableBytes.notifyAll();
                }

            } while (totalTimeout <= 0 || System.currentTimeMillis() - start < totalTimeout);

            if (count == 0) {
                return;
            }

            throw new TimeoutException();
        }
    }


    public long getTotalWriteCount() {
        return totalWriteCount;
    }

    public int write(byte[] wb, int wbstart, int count, int singleTimeout, int totalTimeout) throws IOException {
        if (count < 0) {
            return 0;
        }
        int written = 0;
        synchronized (writeMutex) {
            long start = System.currentTimeMillis();
            if (totalTimeout == 0) {
                singleTimeout = 0;
            }

            do {
                int leftToWrite = count-written;
                if (leftToWrite == 0) {
                    return written;
                }

                int av = waitForBytesToWrite(singleTimeout);
                if (av < 0) {
                    if (exc != null) {
                        totalWriteCount+=written;
                        throw exc;
                    } else {
                        totalWriteCount+=written;
                        throw new EOFException();
                    }
                }

                if (av == 0) {
                    totalWriteCount+=written;
                    return written;
                }

                av = Math.min(av, leftToWrite);

                if (writePos+av < buffer.length) {
                    System.arraycopy(wb, wbstart+written, buffer, writePos,av);
                    written+=av;
                    writePos+=av;
                    synchronized (waitForReadableBytes) {
                        waitForReadableBytes.notifyAll();
                    }
                    continue;
                }
                int firstRead = buffer.length-writePos;
                System.arraycopy(wb, wbstart+written, buffer, writePos, firstRead);
                written+=firstRead;
                int sr = av-firstRead;
                if (sr > 0) {
                    System.arraycopy(wb, wbstart + written, buffer, 0, sr);
                    written += sr;
                }
                writePos = sr;
                synchronized (waitForReadableBytes) {
                    waitForReadableBytes.notifyAll();
                }

            } while (totalTimeout <= 0 || System.currentTimeMillis() - start < totalTimeout);

            totalWriteCount+=written;
            return written;
        }
    }

    public void eof() {
        eof = true;
        synchronized (waitForReadableBytes) {
            waitForReadableBytes.notifyAll();
        }
        synchronized (waitForWriteableBytes) {
            waitForWriteableBytes.notifyAll();
        }
        synchronized (waitForReader) {
            waitForReader.notifyAll();
        }
    }

    public boolean isEof() {
        return eof;
    }

    public IOException getError() {
        return exc;
    }

    public void err(IOException exc) {
        eof = true;
        this.exc = exc;
        synchronized (waitForReadableBytes) {
            waitForReadableBytes.notifyAll();
        }
        synchronized (waitForWriteableBytes) {
            waitForWriteableBytes.notifyAll();
        }
        synchronized (waitForReader) {
            waitForReader.notifyAll();
        }
    }
}
