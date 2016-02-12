package com.remidi.cvmig1516.remidi_x;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Abbey on 21/01/2016.
 */
public class BootUpReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {

          /***** For start Service  ****/
          Intent myIntent = new Intent(context, LoopService.class);
          context.startService(myIntent);
     }
}
