/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec;

import java.io.Serializable;

public interface IBlockType extends Serializable {
    int getValue();

    String name();
}
