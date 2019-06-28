/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.tqdata;

import br.com.pinter.tqdatabase.Text;

import javax.inject.Singleton;
import java.io.FileNotFoundException;

@Singleton
public class Txt {
    private Text text;

    public void initialize() {
        try {
            if(text == null) {
                text = new Text(GameInfo.getInstance().getGamePath() + "/Text");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading text resource.");
        }
    }

    public String getString(String str) {
        initialize();
        return text.getString(str);
    }

    public void preload() {
        initialize();
        text.preload();
    }
}
