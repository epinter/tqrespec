/*
 * Copyright (C) 2021 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.gui;

import br.com.pinter.tqrespec.save.Platform;

import java.util.Arrays;

public enum CopyTarget {
    WINDOWS,
    MOBILE,
    BACKUP;

    public Platform getPlatform() {
        return Arrays.stream(Platform.values()).filter(p -> p.name().equals(name())).findFirst().orElse(null);
    }
}
