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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class FileWriter {

    public abstract boolean save();

    public abstract int getCrcOffset();

    public abstract boolean isCreateCrc();

    protected abstract FileDataHolder getSaveData();

    protected void writeBuffer(String filename) throws IOException {
        this.writeBuffer(filename, getSaveData().getDataMap());
    }

    protected void writeBuffer(String filename, FileDataMap fileDataMap) throws IOException {
        getSaveData().getBuffer().rewind();

        List<Integer> changedOffsets = new ArrayList<>(fileDataMap.changesKeySet());
        Collections.sort(changedOffsets);

        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();

        File out = new File(filename);
        try (FileChannel outChannel = new FileOutputStream(out).getChannel()) {

            for (int offset : changedOffsets) {
                int rawCount = offset - getSaveData().getBuffer().position();
                getSaveData().getBuffer().limit(rawCount +
                        getSaveData().getBuffer().position()
                );

                byte[] buf = new byte[getSaveData().getBuffer().remaining()];
                getSaveData().getBuffer().get(buf);
                outBuffer.write(buf);
                getSaveData().getBuffer().limit(getSaveData().getBuffer().capacity());
                byte[] c = fileDataMap.getBytes(offset);
                outBuffer.write(c);
                int previousValueLength = fileDataMap.getValuesLengthIndex().get(offset);
                getSaveData().getBuffer().position(
                        getSaveData().getBuffer().position() + previousValueLength);
            }

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
            bufferWrapper.rewind();

            while (true) {
                if (outChannel.write(bufferWrapper) <= 0) break;
            }

            getSaveData().getBuffer().rewind();
            outChannel.force(false);
        }

    }

}
