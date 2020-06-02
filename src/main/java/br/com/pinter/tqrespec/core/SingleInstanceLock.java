/*
 * Copyright (C) 2020 Emerson Pinter - All Rights Reserved
 */

/*    This file is part of TQ Respec.

    TQ Respec is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TQ Respec is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TQ Respec.  If not, see <http://www.gnu.org/licenses/>.
*/

package br.com.pinter.tqrespec.core;

import br.com.pinter.tqrespec.logging.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class SingleInstanceLock {
    private FileChannel fileChannel;
    private FileLock lock;
    private final File lockFile = new File(System.getProperty("java.io.tmpdir"), "tqrespec.pid");
    private static final System.Logger logger = Log.getLogger(SingleInstanceLock.class.getName());

    public SingleInstanceLock() {
        if (lockFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            lockFile.delete();
        }
    }

    public void lock() throws IOException {
        fileChannel = new RandomAccessFile(lockFile, "rw").getChannel();
        lock = fileChannel.tryLock();
        String pid = String.valueOf(ProcessHandle.current().pid());
        fileChannel.write(ByteBuffer.wrap(pid.getBytes()));

        if (lock == null) {
            fileChannel.close();
            throw new IOException("Can't lock file");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::release));
    }

    public void release() {
        if (lock != null) {
            try {
                lock.release();
                fileChannel.close();
                //noinspection ResultOfMethodCallIgnored
                lockFile.delete();
            } catch (IOException e) {
                logger.log(System.Logger.Level.ERROR, "Error deleting lockfile ''{0}''", lockFile.getPath());
            }
        }
    }
}
