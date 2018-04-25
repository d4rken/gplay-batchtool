package eu.darken.gplaybatchtool.main.core;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import eu.darken.gplaybatchtool.Sleep;
import io.reactivex.Single;
import timber.log.Timber;

public class Crawler {

    private final NodeSource nodeSource;
    private final NodeTest nodeTest;
    private final long waitForAndSleep;
    private final long timeout;
    private final long startDelay;

    Crawler(Builder builder) {
        nodeSource = builder.nodeSource;
        nodeTest = builder.nodeTest;
        waitForAndSleep = builder.sleep;
        timeout = builder.timeout;
        startDelay = builder.startDelay;
    }

    List<AccessibilityNodeInfo> findAll() {
        List<AccessibilityNodeInfo> result;
        Sleep.ms(startDelay);
        long start = System.currentTimeMillis();
        while ((result = doFindAll()).isEmpty()) {
            if (waitForAndSleep > 0) Sleep.ms(waitForAndSleep);
            else break;
            if (timeout != 0 && System.currentTimeMillis() - start > timeout) {
                break;
            }
        }
        return result;
    }

    private List<AccessibilityNodeInfo> doFindAll() {
        List<AccessibilityNodeInfo> targets = new ArrayList<>();
        Queue<AccessibilityNodeInfo> queue = new LinkedBlockingQueue<>();
        queue.add(nodeSource.getNode());
        while (!queue.isEmpty()) {
            AccessibilityNodeInfo current = queue.poll();
            Timber.v("Crawling: %s", current);
            if (nodeTest.onTest(current)) {
                targets.add(current);
            }
            for (int i = 0; i < current.getChildCount(); i++) {
                queue.add(current.getChild(i));
            }
        }
        return targets;
    }

    public static Builder on(NodeSource source) {
        return new Builder(source);
    }

    public interface NodeTest {
        boolean onTest(AccessibilityNodeInfo node);
    }

    public interface NodeSource {
        AccessibilityNodeInfo getNode();

        static NodeSource from(AccessibilityService service) {
            return () -> {
                AccessibilityNodeInfo root;
                while (true) {
                    Sleep.ms(50);
                    root = service.getRootInActiveWindow();
                    if (root == null) continue;
                    if (root.getPackageName() == null) continue;
                    if (!root.getPackageName().equals("com.android.vending")) continue;
                    break;
                }
                return root;
            };
        }
    }

    public static class Builder {
        final NodeSource nodeSource;
        NodeTest nodeTest;
        private long startDelay = 1000;
        private long sleep = 50;
        private long timeout = 5000;

        public Builder(NodeSource nodeSource) {
            this.nodeSource = nodeSource;
        }

        public Builder filter(NodeTest nodeTest) {
            this.nodeTest = nodeTest;
            return this;
        }

        public Builder startDelay(long milliseconds) {
            this.startDelay = milliseconds;
            return this;
        }

        public Builder sleep(long milliseconds) {
            this.sleep = milliseconds;
            return this;
        }

        public Builder timeout(long milliseconds) {
            this.timeout = milliseconds;
            return this;
        }

        public Single<List<AccessibilityNodeInfo>> findAll() {
            return Single.create(emitter -> {
                final List<AccessibilityNodeInfo> nodes = new Crawler(Builder.this).findAll();
                emitter.onSuccess(nodes);
            });
        }

        public Single<AccessibilityNodeInfo> findFirst() {
            return findAll().map(nodes -> {
                if (nodes.isEmpty()) throw new NoSuchElementException();
                return nodes.get(0);
            });
        }
    }


}
