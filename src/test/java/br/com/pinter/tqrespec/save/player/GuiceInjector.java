/*
 * Copyright (C) 2025 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save.player;

import br.com.pinter.tqrespec.core.GuiceModule;
import br.com.pinter.tqrespec.logging.Log;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.Logger.Level.INFO;

public class GuiceInjector {
    private static final System.Logger logger = Log.getLogger(GuiceInjector.class);

    public static Injector init(Object object) throws FileNotFoundException {
        logger.log(INFO, "### Initializing Google Guice for ''{0}''", object.getClass().getSimpleName());

        List<Module> modules = new ArrayList<>() {{
            add(new GuiceModule());
        }};

        Injector injector = Guice.createInjector(modules);

        injector.injectMembers(object);

        logger.log(INFO, "################## Guice injector initialized for ''{0}'' ##################", object.getClass().getSimpleName());
        return injector;
    }
}
