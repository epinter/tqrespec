/*
 * Copyright (C) 2017 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec;

import br.com.pinter.tqrespec.save.PlayerData;
import br.com.pinter.tqrespec.save.PlayerWriter;

public class Main {
    public static void main(String args[]) throws Exception {
        System.out.println(String.format("Windows version %s", GameInfo.getInstance().getWindowsVersion()));
        System.out.println(GameInfo.getInstance().getGamePath());
        System.out.println(GameInfo.getInstance().getSavePath());


        PlayerData.getInstance().loadPlayerData("test");
        System.err.println("playerName="+PlayerData.getInstance().getString("myPlayerName"));
        System.err.println("playerTexture="+PlayerData.getInstance().getString("playerTexture"));
        System.err.println("life="+PlayerData.getInstance().getFloat("life"));
        System.err.println("mana="+PlayerData.getInstance().getFloat("mana"));
        System.err.println("str="+PlayerData.getInstance().getFloat("str"));
        System.err.println("int="+PlayerData.getInstance().getFloat("int"));
        System.err.println("dex="+PlayerData.getInstance().getFloat("dex"));

        PlayerData.getInstance().setFloat("str",231);
        PlayerData.getInstance().setString("myPlayerName","Haruspec",true);
        PlayerData.getInstance().setString("playerTexture","XPack\\Items\\dyes\\ninja.tex");
        new PlayerWriter().writeBuffer("D:\\dev\\save\\_test\\Player-out.chr");
    }
}
