/*
 * Copyright (C) 2025 Emerson Pinter - All Rights Reserved
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

package br.com.pinter.tqrespec.gui;

import br.com.pinter.tqrespec.save.SaveLocation;
import br.com.pinter.tqrespec.tqdata.PlayerCharacterFile;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.Locale;

public class CharacterListCell extends ListCell<PlayerCharacterFile> {
    private final HBox container;
    private final Label characterName;
    private final Label characterType;

    public CharacterListCell() {
        super();
        characterName = new Label();
        characterType = new Label();
        HBox containerName = new HBox(characterName);
        HBox containerType = new HBox(characterType);
        container = new HBox(containerName, containerType);
        HBox.setHgrow(containerName, Priority.ALWAYS);
        characterName.getStyleClass().add("character-combobox-name");
        characterType.getStyleClass().add("character-combobox-type");
    }

    @Override
    public void updateItem(PlayerCharacterFile character, boolean empty) {
        super.updateItem(character, empty);
        if (empty) {
            setGraphic(null);
            characterName.setText(null);
            characterType.setText(null);
        } else if (character != null) {
            characterName.setText(character.getPlayerName());
            characterType.setText(null);
            String typeTag = switch (character.getLocation()) {
                case USER -> ResourceHelper.getMessage("characters.store.USER");
                case EXTERNAL -> ResourceHelper.getMessage("characters.store.EXTERNAL");
                default -> null;
            };

            if (typeTag == null) {
                characterType.setText(null);
                setTooltip(null);
            } else {
                characterType.setText(String.format("[%s]", typeTag).toUpperCase(Locale.ROOT));
                setTooltip(new UIUtils().simpleTooltip(typeTag));
            }
            setGraphic(container);
        }
    }
}
