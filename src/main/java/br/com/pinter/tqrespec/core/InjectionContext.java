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

package br.com.pinter.tqrespec.core;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import javafx.fxml.FXMLLoader;

import java.util.ArrayList;
import java.util.List;

public class InjectionContext {
    private final Object context;
    private Injector guiceInjector;
    private final List<Module> modules;

    public InjectionContext(Object context, List<Module> modules) {
        this.context = context;
        this.modules = modules;
    }

    public void initialize() {
        List<Module> modulesList = new ArrayList<>(modules);
        modulesList.add(new FXMLProvider());
        guiceInjector = Guice.createInjector(modulesList.toArray(new Module[0]));
        injectMembers(context);
    }

    public void injectMembers(Object instance) {
        guiceInjector.injectMembers(instance);
    }

    public <T> T getInstance(Class<T> type) {
        return guiceInjector.getInstance(type);
    }

    private class FXMLProvider extends AbstractModule {
        @Override
        protected void configure() {
            //ignored
        }

        @Provides
        FXMLLoader producer() {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setControllerFactory(InjectionContext.this::getInstance);
            return fxmlLoader;
        }
    }
}
