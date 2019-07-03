/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.core;

import com.google.inject.*;
import com.google.inject.Module;
import javafx.fxml.FXMLLoader;

import java.util.ArrayList;
import java.util.List;

public class InjectionContext {
    private Object context;
    private Injector guiceInjector;
    private List<Module> modules;

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
        }

        @Provides
        FXMLLoader producer() {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setControllerFactory(aClass -> getInstance(aClass));
            return fxmlLoader;
        }
    }
}
