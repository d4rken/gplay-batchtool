package eu.darken.gplaybatchtool;

import android.app.Activity;
import android.app.Application;
import android.app.Service;

import javax.inject.Inject;

import eu.darken.mvpbakery.injection.ComponentSource;
import eu.darken.mvpbakery.injection.ManualInjector;
import eu.darken.mvpbakery.injection.activity.HasManualActivityInjector;
import eu.darken.mvpbakery.injection.service.HasManualServiceInjector;
import timber.log.Timber;


public class App extends Application implements HasManualActivityInjector, HasManualServiceInjector {

    @Inject AppComponent appComponent;
    @Inject ComponentSource<Activity> activityInjector;
    @Inject ComponentSource<Service> serviceInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());
        DaggerAppComponent.builder()
                .androidModule(new AndroidModule(this))
                .build()
                .injectMembers(this);
    }

    @Override
    public ManualInjector<Activity> activityInjector() {
        return activityInjector;
    }

    @Override
    public ManualInjector<Service> serviceInjector() {
        return serviceInjector;
    }
}