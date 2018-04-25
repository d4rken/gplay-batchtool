/*
 * Project Root Validator
 *
 * @link https://github.com/d4rken/rootvalidator
 * @license https://github.com/d4rken/rootvalidator/blob/master/LICENSE GPLv3
 */

package eu.darken.gplaybatchtool.main.ui.intro;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import eu.darken.gplaybatchtool.R;
import eu.darken.mvpbakery.base.MVPBakery;
import eu.darken.mvpbakery.base.ViewModelRetainer;
import eu.darken.mvpbakery.injection.InjectedPresenter;
import eu.darken.mvpbakery.injection.PresenterInjectionCallback;
import timber.log.Timber;


public class IntroFragment extends Fragment implements IntroPresenter.View {

    @BindView(R.id.req_gplayinstalled) TextView reqGplay;
    @BindView(R.id.req_language) TextView reqEnglish;
    @BindView(R.id.req_serviceenabled) TextView reqService;

    @Inject IntroPresenter presenter;

    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.intro_fragment_layout, container, false);
        unbinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        MVPBakery.<IntroPresenter.View, IntroPresenter>builder()
                .presenterFactory(new InjectedPresenter<>(this))
                .presenterRetainer(new ViewModelRetainer<>(this))
                .addPresenterCallback(new PresenterInjectionCallback<>(this))
                .attach(this);
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        reqService.setOnClickListener(view -> presenter.onShowAccessibilityPage());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.validator_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                StringBuilder sb = new StringBuilder();
                try {
                    sb.append(String.format("Version: %s\n", getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName));
                } catch (PackageManager.NameNotFoundException e) {
                    Timber.e(e);
                }
                sb.append("This app is open-source.");

                new AlertDialog.Builder(getContext())
                        .setMessage(sb.toString())
                        .setPositiveButton("GitHub", (dialogInterface, i) -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://github.com/d4rken/gplay-batchtool"));
                            startActivity(intent);
                        })
                        .setNeutralButton("Twitter", (dialogInterface, i) -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://twitter.com/d4rken"));
                            startActivity(intent);
                        })
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void updateGPlayInstalled(boolean gplayInstalled) {
        reqGplay.setBackgroundColor(gplayInstalled ? Color.GREEN : Color.YELLOW);
        reqGplay.setText(gplayInstalled ?
                "Google Play is installed." :
                "Google Play is not installed, what do you want this app for?"
        );
    }

    @Override
    public void updateEnglishLanguage(boolean englishLanguage) {
        reqEnglish.setBackgroundColor(englishLanguage ? Color.GREEN : Color.YELLOW);
        reqEnglish.setText(englishLanguage ?
                "English is set as default app language. Nice." :
                "Change the default app language to english so this app knows what to click."
        );
    }

    @Override
    public void updateAccessibilityState(boolean serviceEnabled) {
        reqService.setBackgroundColor(serviceEnabled ? Color.GREEN : Color.YELLOW);
        reqService.setText(serviceEnabled ?
                "The service is enabled and its controls should be visible now." :
                "Enable the accessibility service by clicking this text!"
        );
    }
}
