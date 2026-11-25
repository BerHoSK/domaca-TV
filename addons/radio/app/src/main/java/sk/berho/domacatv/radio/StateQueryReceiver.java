package sk.berho.domacatv.radio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Launcher sa pýta, čo práve hrá: odpovieme aktuálnym stavom. */
public final class StateQueryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        RadioService.announce(context);
    }
}
