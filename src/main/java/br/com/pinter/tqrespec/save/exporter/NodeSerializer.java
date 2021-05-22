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

import br.com.pinter.tqrespec.save.VariableInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Collections;

public class NodeSerializer extends JsonSerializer<Node> {
    private void writeField(JsonGenerator gen, VariableInfo v) throws IOException {
        if (v.isInt()) {
            gen.writeNumberField(v.getName(), (Integer) v.getValue());
        } else if (v.isFloat()) {
            gen.writeNumberField(v.getName(), (Float) v.getValue());
        } else if (v.isString() || v.isUid()) {
            gen.writeStringField(v.getName(), v.getValueString());
        } else if (v.isStream()) {
            gen.writeBinaryField(v.getName(), (byte[]) v.getValue());
        }
    }

    @Override
    public void serialize(Node node, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Collections.sort(node.getChildren());

        if (node.getNodeType().equals(Node.Type.BLOCK)) {
            gen.writeStartObject();
            if (node.getBlock() != null) {
                gen.writeFieldName("$metadata");
                serializers.defaultSerializeValue(node.getBlock(), gen);
            }
            for (Node c : node.getChildren()) {
                if (c.getNodeType().equals(Node.Type.BLOCK)) {
                    gen.writeFieldName("$block");
                    serialize(c, gen, serializers);
                } else {
                    writeField(gen, c.getVariable());
                }
            }
            gen.writeEndObject();
        } else {
            writeField(gen, node.getVariable());
        }
    }
}
