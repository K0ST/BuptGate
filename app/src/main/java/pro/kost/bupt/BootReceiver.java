package pro.kost.bupt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import pro.kost.bupt.BuptAuthService;

/**
 * Created by kost on 14/11/25.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg1) {
        context.getApplicationContext().startService(new Intent(context, BuptAuthService.class));
    }

}
