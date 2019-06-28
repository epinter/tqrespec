/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.core;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.util.ResourceBundle;

public class ResourceBundleProducer {
    @Produces
    @FxmlResourceBundle("")
    public ResourceBundle produces(InjectionPoint injectionPoint) {
        String name = injectionPoint.getAnnotated().getAnnotation(FxmlResourceBundle.class).value();
        return ResourceBundle.getBundle(name);
    }

}
