package eu.darken.gplaybatchtool;

import timber.log.Timber;

public class Sleep {
    public static void ms(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Timber.e(e);
        }
    }
}
