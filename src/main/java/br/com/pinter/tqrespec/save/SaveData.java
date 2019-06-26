/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

import java.util.ArrayList;
import java.util.Hashtable;

public class SaveData {
    private static SaveData instance = null;
    private Hashtable<Integer, BlockInfo> blockInfo = null;
    private HeaderInfo headerInfo = null;
    private Hashtable<String, ArrayList<Integer>> variableLocation = null;

    private SaveData() {
        variableLocation = new Hashtable<>();
        blockInfo = new Hashtable<>();
    }

    public static SaveData getInstance() {
        if (instance == null) {
            synchronized (SaveData.class) {
                if (instance == null) {
                    instance = new SaveData();
                }
            }
        }
        return instance;
    }

    public Hashtable<Integer, BlockInfo> getBlockInfo() {
        return blockInfo;
    }

    public void setBlockInfo(Hashtable<Integer, BlockInfo> blockInfo) {
        this.blockInfo = blockInfo;
    }

    public HeaderInfo getHeaderInfo() {
        return headerInfo;
    }

    public void setHeaderInfo(HeaderInfo headerInfo) {
        this.headerInfo = headerInfo;
    }

    public Hashtable<String, ArrayList<Integer>> getVariableLocation() {
        return variableLocation;
    }

    public void setVariableLocation(Hashtable<String, ArrayList<Integer>> variableLocation) {
        this.variableLocation = variableLocation;
    }

    public void reset() {
        this.headerInfo = null;
        this.blockInfo = new Hashtable<>();
        this.variableLocation = new Hashtable<>();
    }
}
