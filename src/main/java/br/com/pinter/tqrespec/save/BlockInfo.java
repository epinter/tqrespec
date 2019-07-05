/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

/*    This file is part of TQ Respec.

    TQ Respec is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TQ Respec is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TQ Respec.  If not, see <http://www.gnu.org/licenses/>.
*/

package br.com.pinter.tqrespec.save;

import java.io.Serializable;
import java.util.Hashtable;

@SuppressWarnings("unused")
public class BlockInfo implements Serializable {
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

    Hashtable<String, VariableInfo> getVariables() {
        return variables;
    }

    void setVariables(Hashtable<String, VariableInfo> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return "BlockInfo{" +
                "tag='" + tag + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", size=" + size +
                ", variables=" + variables +
                '}';
    }
}
