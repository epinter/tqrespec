/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

public enum FileBlockType {
    //generic types
    Unknown,
    Body,
    Multiple,
    //player file types
    PlayerHeader,
    PlayerMain,
    PlayerAttributes,
    PlayerStats,
    PlayerInventory,
    PlayerHotSlot,
    PlayerSkills,
    PlayerUiSkill,
    PlayerLevelPoints
}
