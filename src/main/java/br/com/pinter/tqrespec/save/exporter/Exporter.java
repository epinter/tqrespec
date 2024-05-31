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
import br.com.pinter.tqrespec.save.FileDataMap;
import br.com.pinter.tqrespec.save.VariableInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class Exporter {
    private final File filename;
    private final FileDataMap fileDataMap;
    private final Map<Integer, List<Integer>> children;


    public Exporter(File filename, FileDataMap fileDataMap) {
        this.filename = filename;
        this.fileDataMap = fileDataMap;
        this.children = new HashMap<>();
    }

    public void writeJson() throws IOException {
        Node tree = getTree();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(filename, tree);
    }

    public Node getTree() {
        List<BlockInfo> rootBlocks = fileDataMap.getBlockInfo().values()
                .stream().filter(f -> f.getParentOffset() == -1).sorted(Comparator.comparing(BlockInfo::getStart)).toList();

        for (BlockInfo b : fileDataMap.getBlockInfo().values()) {
            children.computeIfAbsent(b.getParentOffset(), k -> new ArrayList<>());
            children.get(b.getParentOffset()).add(b.getStart());
        }

        Node root;
        if (fileDataMap.getBlockInfo().containsKey(0)) {
            root = new Node(fileDataMap.getBlockInfo().get(0));
            for (VariableInfo v : fileDataMap.getBlockInfo().get(0).getVariables().values()) {
                root.getChildren().add(new Node(v));
            }
        } else {
            root = new Node();
        }


        for (BlockInfo levelZeroBlock : rootBlocks) {
            if (levelZeroBlock.getStart() == 0) {
                continue;
            }
            Node levelZeroNode = new Node(levelZeroBlock);
            levelZeroNode.getChildren().addAll(childNodes(levelZeroBlock));
            root.getChildren().add(levelZeroNode);
        }

        return root;
    }

    private List<Node> childNodes(BlockInfo b) {
        List<Node> ret = new ArrayList<>();
        for (VariableInfo v : b.getVariables().values()) {
            ret.add(new Node(v));
        }

        if (children.containsKey(b.getStart())) {
            for (Integer c : children.get(b.getStart())) {
                BlockInfo currentBlock = fileDataMap.getBlockInfo().get(c);
                Node currentNode = new Node(currentBlock);
                currentNode.getChildren().addAll(childNodes(currentBlock));
                Collections.sort(currentNode.getChildren());
                ret.add(currentNode);

            }
        }
        return ret;
    }

}
