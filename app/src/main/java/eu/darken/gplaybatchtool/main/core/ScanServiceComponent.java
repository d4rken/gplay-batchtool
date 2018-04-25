package eu.darken.gplaybatchtool.main.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import dagger.Subcomponent;
import eu.darken.mvpbakery.injection.service.ServiceComponent;


@ScanServiceComponent.Scope
@Subcomponent()
public interface ScanServiceComponent extends ServiceComponent<ScanService> {

    @Subcomponent.Builder
    abstract class Builder extends ServiceComponent.Builder<ScanService, ScanServiceComponent> {

    }

    @javax.inject.Scope
    @Retention(RetentionPolicy.RUNTIME)
    @interface Scope {}
}
