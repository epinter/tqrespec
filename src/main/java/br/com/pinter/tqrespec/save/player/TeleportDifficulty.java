/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save.player;

import br.com.pinter.tqrespec.save.VariableInfo;

import java.util.List;

public class TeleportDifficulty {
    private int difficulty;
    private int size;
    private int offset;
    private List<VariableInfo> teleportList;

    public TeleportDifficulty(int difficulty, int size, int offset, List<VariableInfo> teleportList) {
        this.difficulty = difficulty;
        this.size = size;
        this.offset = offset;
        this.teleportList = teleportList;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getSize() {
        return size;
    }

    public int getOffset() {
        return offset;
    }

    public List<VariableInfo> getTeleportList() {
        return teleportList;
    }

    @Override
    public String toString() {
        return "TeleportDifficulty{" +
                "difficulty=" + difficulty +
                ", size=" + size +
                ", offset=" + offset +
                ", teleportList=" + teleportList +
                '}';
    }
}
