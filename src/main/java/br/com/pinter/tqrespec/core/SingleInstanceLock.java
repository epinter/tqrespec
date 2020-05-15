/*
 * Copyright (C) 2020 Emerson Pinter - All Rights Reserved
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
    private final File lockFile = new File(System.getProperty("java.io.tmpdir"),"tqrespec.pid");
    private static final System.Logger logger = Log.getLogger(SingleInstanceLock.class.getName());

    public SingleInstanceLock() {
        if(lockFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            lockFile.delete();
        }
    }

    public void lock() throws IOException {
        fileChannel = new RandomAccessFile(lockFile,"rw").getChannel();
        lock = fileChannel.tryLock();
        String pid = String.valueOf(ProcessHandle.current().pid());
        fileChannel.write(ByteBuffer.wrap(pid.getBytes()));

        if(lock == null) {
            fileChannel.close();
            throw new IOException("Can't lock file");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::release));
    }

    public void release() {
        if(lock!=null) {
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
