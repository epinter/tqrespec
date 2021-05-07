/*
 * Copyright (C) 2021 Emerson Pinter - All Rights Reserved
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


package br.com.pinter.tqrespec.save;

import br.com.pinter.tqrespec.util.CRC32;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class FileWriter {

    public abstract boolean save();

    public abstract int getCrcOffset();

    public abstract boolean isCreateCrc();

    protected abstract FileDataHolder getSaveData();

    protected void writeBuffer(String rootPath, String filename) throws IOException {
        writeBuffer(rootPath, filename, getSaveData().getDataMap(), FileSystems.getDefault());
    }

    protected void writeBuffer(String rootPath, String filename, FileDataMap fileDataMap) throws IOException {
        writeBuffer(rootPath, filename, fileDataMap, FileSystems.getDefault());
    }

    protected void writeBuffer(String rootPath, String filename, FileDataMap fileDataMap, FileSystem fileSystem) throws IOException {
        getSaveData().getBuffer().rewind();

        List<Integer> changedOffsets = new ArrayList<>(fileDataMap.changesKeySet());
        Collections.sort(changedOffsets);

        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();

        for (int offset : changedOffsets) {
            int rawCount = offset - getSaveData().getBuffer().position();
            getSaveData().getBuffer().limit(rawCount +
                    getSaveData().getBuffer().position()
            );

            byte[] buf = new byte[getSaveData().getBuffer().remaining()];
            //copy to buf everything until next change
            getSaveData().getBuffer().get(buf);
            outBuffer.write(buf);
            //restore bytebuffer limit
            getSaveData().getBuffer().limit(getSaveData().getBuffer().capacity());
            //copy changed bytes to output buffer
            byte[] c = fileDataMap.getBytes(offset);
            outBuffer.write(c);
            int previousValueLength = fileDataMap.getValuesLengthIndex().get(offset);
            //skip the number of bytes of original value, to position the cursor at the next variable/block
            getSaveData().getBuffer().position(
                    getSaveData().getBuffer().position() + previousValueLength);
        }

        //copy remaining data to output buffer
        while (true) {
            byte[] buf = new byte[getSaveData().getBuffer().remaining()];
            getSaveData().getBuffer().get(buf);
            outBuffer.write(buf);
            if (buf.length == 0) break;
        }

        ByteBuffer bufferWrapper = ByteBuffer.wrap(outBuffer.toByteArray()).order(ByteOrder.LITTLE_ENDIAN);

        if (isCreateCrc()) {
            bufferWrapper.putInt(getCrcOffset(), 0);
            bufferWrapper.putInt(getCrcOffset(), CRC32.calculate(bufferWrapper));
        }

        Files.write(fileSystem.getPath(rootPath, filename), bufferWrapper.array());
        getSaveData().getBuffer().rewind();
    }

}
