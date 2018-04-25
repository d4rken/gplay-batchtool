package eu.darken.gplaybatchtool.main.ui.intro;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.Nullable;

import java.util.Locale;

import javax.inject.Inject;

import eu.darken.gplaybatchtool.main.core.ScanController;
import eu.darken.mvpbakery.base.Presenter;
import eu.darken.mvpbakery.injection.ComponentPresenter;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@IntroComponent.Scope
public class IntroPresenter extends ComponentPresenter<IntroPresenter.View, IntroComponent> {

    private final Context context;
    private final PackageManager packageManager;
    private final ScanController controller;
    private boolean gplayInstalled;
    private boolean englishLanguage;
    private boolean serviceEnabled;

    @Inject
    IntroPresenter(Context context, PackageManager packageManager, ScanController controller) {
        this.context = context;
        this.packageManager = packageManager;
        this.controller = controller;
    }

    @Override
    public void onBindChange(@Nullable View view) {
        super.onBindChange(view);
        if (getView() == null) return;
        Single
                .create((SingleOnSubscribe<Boolean>) e -> {
                    try {
                        packageManager.getPackageInfo("com.android.vending", 0);
                        e.onSuccess(true);
                    } catch (PackageManager.NameNotFoundException ignore) {
                        e.onSuccess(false);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(gplayInstalled -> IntroPresenter.this.gplayInstalled = gplayInstalled)
                .subscribe(exists -> onView(v -> v.updateGPlayInstalled(exists)));
        Single
                .create((SingleOnSubscribe<Boolean>) e -> e.onSuccess(Locale.getDefault().getLanguage().equals("en")))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(english -> IntroPresenter.this.englishLanguage = english)
                .subscribe(exists -> onView(v -> v.updateEnglishLanguage(exists)));
        Single
                .create((SingleOnSubscribe<Boolean>) e -> e.onSuccess(controller.isServiceEnabled()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(serviceEnabled -> IntroPresenter.this.serviceEnabled = serviceEnabled)
                .subscribe(exists -> onView(v -> v.updateAccessibilityState(exists)));
    }

    void onShowAccessibilityPage() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    interface View extends Presenter.View {

        void updateGPlayInstalled(boolean gplayInstalled);

        void updateEnglishLanguage(boolean englishLanguage);

        void updateAccessibilityState(boolean serviceEnabled);
    }
}
