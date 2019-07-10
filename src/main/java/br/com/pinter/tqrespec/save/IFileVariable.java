/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

public interface IFileVariable {
    String var();

    VariableType type();

    FileBlockType location();
}
