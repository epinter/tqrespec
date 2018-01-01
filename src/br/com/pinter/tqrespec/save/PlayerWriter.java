/*
 * Copyright (C) 2018 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

import br.com.pinter.tqrespec.GameInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class PlayerWriter
{
    public PlayerWriter() {
    }

    private String getPlayerDirectory(String playerName) {
        return String.format("%s\\_%s\\Player.chr", GameInfo.getInstance().getSaveDataMainPath(),playerName);
    }

    public void writeBuffer() {
        String filename=this.getPlayerDirectory(PlayerData.getInstance().getPlayerName());

        //writeBuffer(filename);
    }

    public void writeBuffer(String filename) throws IOException {
        PlayerData.getInstance().getBuffer().flip();
        List<Integer> changedOffsets = new ArrayList<>(PlayerData.getInstance().getChanges().keySet());
        Collections.sort(changedOffsets);

        File out = new File(filename);
        FileChannel outChannel = new FileOutputStream(out).getChannel();

        for (int offset: changedOffsets) {
//            System.err.println("K == "+offset);
            int rawCount = offset - PlayerData.getInstance().getBuffer().position();
//            System.err.println("C == "+rawCount);
            PlayerData.getInstance().getBuffer().limit(rawCount+
                            PlayerData.getInstance().getBuffer().position()
                    );
            outChannel.write(PlayerData.getInstance().getBuffer());
            PlayerData.getInstance().getBuffer().limit(PlayerData.getInstance().getBuffer().capacity());
            byte c[] = PlayerData.getInstance().getChanges().get(offset);
            outChannel.write(ByteBuffer.wrap(c));
            int previousValueLength = PlayerData.getInstance().getValuesLengthIndex().get(offset);
//            System.err.println("P == "+previousValueLength);
            PlayerData.getInstance().getBuffer().position(
                    PlayerData.getInstance().getBuffer().position()+previousValueLength);
        }

        while(true) {
            if(outChannel.write(PlayerData.getInstance().getBuffer()) <= 0) break;
        }
        outChannel.force(false);
        outChannel.close();

    }
}
