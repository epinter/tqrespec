/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

public enum FileBlockType {
    //generic types
    UNKNOWN,
    BODY,
    MULTIPLE,
    //player file types
    PLAYER_HEADER,
    PLAYER_MAIN,
    PLAYER_ATTRIBUTES,
    PLAYER_STATS,
    PLAYER_INVENTORY,
    PLAYER_HOT_SLOT,
    PLAYER_SKILLS,
    PLAYER_UI_SKILL,
    PLAYER_LEVEL_POINTS
}
