/*
 * Copyright (C) 2026 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

import com.google.common.collect.ImmutableListMultimap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileDataMapTest {
    @Test
    void should_DecreaseValueWhenVariableHasNoPendingChange() {
        FileDataMap dataMap = new FileDataMap();
        VariableInfo variable = VariableInfo.builder()
                .name("teleportUIDsSize")
                .keyOffset(100)
                .valOffset(120)
                .blockOffset(10)
                .variableType(VariableType.INTEGER)
                .value(3)
                .build();

        BlockInfo blockInfo = new BlockInfo();
        blockInfo.setStart(10);
        blockInfo.setVariables(ImmutableListMultimap.of(variable.getName(), variable));
        dataMap.getBlockInfo().put(blockInfo.getStart(), blockInfo);

        dataMap.decrementInt(variable);

        assertEquals(2, dataMap.getInt(variable));
    }
}
