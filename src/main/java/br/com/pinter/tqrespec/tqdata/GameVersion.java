/*
 * Copyright (C) 2020 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.tqdata;

import java.util.Arrays;
import java.util.Optional;

public enum GameVersion {
    UNKNOWN(0),
    TQ(1),
    TQIT(2),
    TQAE(3);

    private final int value;

    GameVersion(int value) {
        this.value = value;
    }

    public static GameVersion fromValue(int version) {
        Optional<GameVersion> first = Arrays.stream(GameVersion.values()).filter(f -> f.value() == version).findFirst();
        if(first.isEmpty()) {
            throw new EnumConstantNotPresentException(GameVersion.class,String.valueOf(version));
        }
        return first.get();
    }

    public int value() {
        return value;
    }
}
