package eu.darken.gplaybatchtool;

import android.app.Service;

import dagger.Binds;
import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.android.ServiceKey;
import dagger.multibindings.IntoMap;
import eu.darken.gplaybatchtool.main.core.ScanService;
import eu.darken.gplaybatchtool.main.core.ScanServiceComponent;

@Module(subcomponents = {ScanServiceComponent.class})
abstract class ServiceBinderModule {

    @Binds
    @IntoMap
    @ServiceKey(ScanService.class)
    abstract AndroidInjector.Factory<? extends Service> scanservice(ScanServiceComponent.Builder impl);
}