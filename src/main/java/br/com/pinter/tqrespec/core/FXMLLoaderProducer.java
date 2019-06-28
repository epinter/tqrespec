/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.core;

import javafx.fxml.FXMLLoader;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.util.ResourceBundle;

public class FXMLLoaderProducer {
    @Inject
    Instance<Object> instance;

    @Produces
    @FxmlLoaderLocation("")
    @FxmlResourceBundle("")
    public FXMLLoader produceLoader(InjectionPoint injectionPoint) {
        FxmlLoaderLocation locationAnn = injectionPoint.getAnnotated().getAnnotation(FxmlLoaderLocation.class);
        FxmlResourceBundle bundleAnn = injectionPoint.getAnnotated().getAnnotation(FxmlResourceBundle.class);

        FXMLLoader fxmlLoader = new FXMLLoader();
        if (locationAnn != null && !locationAnn.value().isEmpty()) {
            fxmlLoader.setLocation(getClass().getResource(locationAnn.value()));
        }
        if (bundleAnn != null && !bundleAnn.value().isEmpty()) {
            fxmlLoader.setResources(ResourceBundle.getBundle(bundleAnn.value()));
        }
        fxmlLoader.setControllerFactory(objects -> instance.select(objects).get());
        return fxmlLoader;
    }
}
