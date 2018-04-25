package eu.darken.gplaybatchtool;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import dagger.Component;
import dagger.MembersInjector;


@AppComponent.Scope
@Component(modules = {
        AndroidModule.class,
        AppModule.class,
        ActivityBinderModule.class,
        ServiceBinderModule.class
})
public interface AppComponent extends MembersInjector<App> {
    void inject(App app);

    @Component.Builder
    interface Builder {
        Builder androidModule(AndroidModule module);

        AppComponent build();
    }

    @Documented
    @javax.inject.Scope
    @Retention(RetentionPolicy.RUNTIME)
    @interface Scope {
    }
}
