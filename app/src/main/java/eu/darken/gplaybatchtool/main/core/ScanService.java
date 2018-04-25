package eu.darken.gplaybatchtool.main.core;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.NoSuchElementException;

import eu.darken.gplaybatchtool.App;
import eu.darken.gplaybatchtool.R;
import eu.darken.gplaybatchtool.Sleep;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ScanService extends AccessibilityService {

    private Disposable disposable = Disposables.disposed();
    private Handler uiThread;
    private Crawler.NodeSource rootNode;
    private TextView progressText;
    private View startButton;

    @Override
    public void onCreate() {
        ((App) getApplication()).serviceInjector().inject(this);
        super.onCreate();
        uiThread = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Timber.d("onAccessibilityEvent(event=%s)", event);
    }

    @Override
    public void onInterrupt() {
        Timber.d("onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        Timber.d("onServiceConnected");
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        FrameLayout controlLayout = new FrameLayout(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.view_accessibility_control, controlLayout);
        wm.addView(controlLayout, lp);


        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);

        startButton = controlLayout.findViewById(R.id.start);
        startButton.setOnClickListener(v -> onStartClicked());
        controlLayout.findViewById(R.id.cancel).setOnClickListener(v -> onCancelCLicked());
        controlLayout.findViewById(R.id.exit).setOnClickListener(v -> onExitClicked());
        progressText = controlLayout.findViewById(R.id.progress);
        updateProgress("Idle");
    }

    void updateProgress(String string) {
        Timber.i("Updating progress: %s", string);
        uiThread.post(() -> progressText.setText(string));
    }

    void onStartClicked() {
        startButton.setVisibility(View.GONE);
        disposable = Completable
                .create(emitter -> {
                    rootNode = Crawler.NodeSource.from(ScanService.this);

                    updateProgress("Opening Google Play");
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.vending");
                    if (launchIntent == null) throw new IllegalStateException("Google Play not installed");
                    startActivity(launchIntent);

                    navigateToList();
                    removeEntries();
                })
                .doOnComplete(() -> updateProgress("Completed"))
                .doOnDispose(() -> {
                    Timber.d("Diposed");
                    uiThread.removeCallbacks(null);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> uiThread.post(() -> startButton.setVisibility(View.VISIBLE)))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    void onCancelCLicked() {
        updateProgress("Canceling...");
        disposable.dispose();
        updateProgress("Terminated");
    }

    void onExitClicked() {
        updateProgress("Exiting...");
        disposable.dispose();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            disableSelf();
        } else {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    void navigateToList() {
        Crawler.on(rootNode)
                .filter(node -> "Navigate up".equals(node.getContentDescription()))
                .findFirst()
                .doOnSubscribe(d -> updateProgress("Moving to root screen"))
                .subscribe((node, throwable) -> {
                    if (node != null) node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    else updateProgress("We already were on the root screen of Google Play");
                });

        Crawler.on(rootNode)
                .filter(node -> "Show navigation drawer".equals(node.getContentDescription()))
                .findFirst()
                .doOnSubscribe(d -> updateProgress("Opening drawer"))
                .subscribe((node, throwable) -> {
                    if (node != null) node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    else updateProgress("Drawer already open?");
                });

        Crawler.on(rootNode)
                .filter(node -> "My apps & games".equals(node.getText()))
                .findFirst()
                .doOnSubscribe(d -> updateProgress("Opening 'My apps & games'"))
                .subscribe((node, throwable) -> {
                    if (node != null) node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    else updateProgress("Can't find 'My apps & games'?");
                });

        Crawler.on(rootNode)
                .filter(node -> "LIBRARY".equals(node.getText()))
                .findFirst()
                .doOnSubscribe(d -> updateProgress("Opening 'Library' tab"))
                .subscribe((node, throwable) -> {
                    if (node != null) node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    else updateProgress("Can't find 'Library' tab");
                });
    }

    void removeEntries() {
        updateProgress("Waiting for list to load...");
        Sleep.ms(1000);
        while (!disposable.isDisposed()) {
            Crawler.on(rootNode)
                    .startDelay(100)
                    .timeout(2000)
                    .filter(node -> "Remove".equals(node.getContentDescription()) && node.getClassName().equals(ImageView.class.getName()))
                    .findFirst()
                    .doOnSubscribe(d -> updateProgress("Clicking 'X' (Remove)"))
                    .subscribe((node, throwable) -> {
                        if (node != null) {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        } else {
                            updateProgress("No app found.");
                        }
                    });
            Crawler.on(rootNode)
                    .startDelay(100)
                    .timeout(2000)
                    .filter(node -> "OK".equals(node.getText()) && node.getClassName().equals(Button.class.getName()))
                    .findFirst()
                    .doOnSubscribe(d -> updateProgress("Confirming removal..."))
                    .subscribe((node, throwable) -> {
                        if (node != null) node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        else updateProgress("Can't find 'OK' button...");
                    });

            while (true) {
                try {
                    Crawler.on(rootNode)
                            .timeout(75)
                            .startDelay(250)
                            .filter(node -> "Removing...".equals(node.getText()) && node.getClassName().equals(TextView.class.getName()))
                            .findFirst()
                            .doOnSubscribe(d -> updateProgress("Waiting for list to update..."))
                            .blockingGet();
                } catch (NoSuchElementException e) {
                    break;
                }
            }

            Crawler.on(rootNode)
                    .timeout(75)
                    .startDelay(250)
                    .filter(node -> "No Activity".equals(node.getText()) && node.getClassName().equals(TextView.class.getName()))
                    .findFirst()
                    .doOnSubscribe(d -> updateProgress("Checking if we cleared the list..."))
                    .subscribe((node, throwable) -> {
                        if (node != null) {
                            updateProgress("List cleared.");
                            disposable.dispose();
                        }
                    });
        }
    }
}

