/*
 * Copyright (C) 2020 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.tqdata;

import java.util.Arrays;
import java.util.Optional;

public enum InstallType {
    UNKNOWN(0),
    WINDOWS(1),
    STEAM(2),
    GOG(3),
    MICROSOFT_STORE(4),
    LEGACY_DISC(5),
    ALTERNATIVE_STEAM_API(6),
    MANUAL(100)
    ;

    private int value;

    InstallType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static InstallType fromValue(int version) {
        Optional<InstallType> first = Arrays.stream(InstallType.values()).filter(f -> f.value() == version).findFirst();
        if(first.isEmpty()) {
            throw new EnumConstantNotPresentException(InstallType.class,String.valueOf(version));
        }
        return first.get();
    }

}
