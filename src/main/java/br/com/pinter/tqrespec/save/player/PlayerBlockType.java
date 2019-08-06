/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save.player;

import br.com.pinter.tqrespec.save.FileBlockType;

public class PlayerBlockType extends FileBlockType {
    static FileBlockType PLAYER_HEADER = new FileBlockType(0, "PLAYER_HEADER");
    static FileBlockType PLAYER_MAIN = new FileBlockType(1, "PLAYER_MAIN");
    static FileBlockType PLAYER_ATTRIBUTES = new FileBlockType(2, "PLAYER_ATTRIBUTES");
    static FileBlockType PLAYER_STATS = new FileBlockType(3, "PLAYER_STATS");
    static FileBlockType PLAYER_INVENTORY = new FileBlockType(5, "PLAYER_INVENTORY");
    static FileBlockType PLAYER_ITEM = new FileBlockType(6, "PLAYER_ITEM");
    static FileBlockType PLAYER_EQUIPMENT = new FileBlockType(7, "PLAYER_EQUIPMENT");
    static FileBlockType PLAYER_HOT_SLOT = new FileBlockType(8, "PLAYER_HOT_SLOT");
    static FileBlockType PLAYER_SKILLS = new FileBlockType(9, "PLAYER_SKILLS");
    static FileBlockType PLAYER_UI_SKILL = new FileBlockType(10, "PLAYER_UI_SKILL");
    static FileBlockType PLAYER_LEVEL_POINTS = new FileBlockType(11, "PLAYER_LEVEL_POINTS");

    public PlayerBlockType(int value, String name) {
        super(value, name);
    }
}
