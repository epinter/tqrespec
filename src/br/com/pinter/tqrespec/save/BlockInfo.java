/*
 * Copyright (C) 2018 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

import java.util.Hashtable;

public class BlockInfo {
    private String tag = null;
    private int start = -1;
    private int end = -1;
    private int size = -1;
    private Hashtable<String, VariableInfo> variables = null;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Hashtable<String, VariableInfo> getVariables() {
        return variables;
    }

    public void setVariables(Hashtable<String, VariableInfo> variables) {
        this.variables = variables;
    }
}
