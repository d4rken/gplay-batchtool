package eu.darken.gplaybatchtool.main.core;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import javax.inject.Inject;

import eu.darken.gplaybatchtool.AppComponent;

@AppComponent.Scope
public class ScanController {

    private final Context context;

    @Inject
    public ScanController(Context context) {
        this.context = context;
    }

    public boolean isServiceEnabled() {
        ComponentName comp = new ComponentName(context, ScanService.class);

        String setting = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (setting == null) return false;

        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(setting);

        while (splitter.hasNext()) {
            String componentNameString = splitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

            if (enabledService != null && enabledService.equals(comp)) return true;
        }

        return false;
    }
}
