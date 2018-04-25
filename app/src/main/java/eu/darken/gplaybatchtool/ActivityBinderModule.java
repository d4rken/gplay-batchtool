package eu.darken.gplaybatchtool;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;
import eu.darken.gplaybatchtool.main.ui.MainActivity;
import eu.darken.gplaybatchtool.main.ui.MainActivityComponent;

@Module(subcomponents = {
        MainActivityComponent.class
})
abstract class ActivityBinderModule {

    @Binds
    @IntoMap
    @ActivityKey(MainActivity.class)
    abstract AndroidInjector.Factory<? extends Activity> main(MainActivityComponent.Builder impl);

}