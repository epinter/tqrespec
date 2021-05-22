/*
 * Copyright (C) 2021 Emerson Pinter - All Rights Reserved
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

package br.com.pinter.tqrespec.save.exporter;

import br.com.pinter.tqrespec.save.BlockInfo;
import br.com.pinter.tqrespec.save.VariableInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonSerialize(using = NodeSerializer.class)
public class Node implements Comparable<Node> {
    private int offset;
    private final List<Node> children = new ArrayList<>();
    private BlockInfo block;
    private VariableInfo variable;
    private final Node.Type nodeType;

    @Override
    public int compareTo(Node o) {
        return Integer.compare(offset, o.offset);
    }

    public enum Type {
        BLOCK,
        VAR
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return offset == node.offset && Objects.equals(children, node.children) && Objects.equals(block, node.block) && Objects.equals(variable, node.variable) && nodeType == node.nodeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, children, block, variable, nodeType);
    }

    public Node() {
        nodeType = Type.BLOCK;
    }

    public Node(BlockInfo block) {
        this.block = block;
        offset = block.getStart();
        nodeType = Type.BLOCK;
    }

    public Node(VariableInfo variable) {
        this.variable = variable;
        offset = variable.getKeyOffset();
        nodeType = Type.VAR;
    }

    public List<Node> getChildren() {
        return children;
    }

    public Type getNodeType() {
        return nodeType;
    }

    public BlockInfo getBlock() {
        return block;
    }

    public VariableInfo getVariable() {
        return variable;
    }
}
