package eu.darken.gplaybatchtool;

import android.content.Context;
import android.content.pm.PackageManager;

import dagger.Module;
import dagger.Provides;


@Module
class AndroidModule {
    private final App app;

    AndroidModule(App app) {this.app = app;}

    @Provides
    @AppComponent.Scope
    Context context() {
        return app.getApplicationContext();
    }

    @Provides
    @AppComponent.Scope
    PackageManager packageManager() {
        return app.getPackageManager();
    }

}
