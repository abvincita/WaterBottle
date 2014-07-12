package govhack.waterbottle;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimeAlarm extends BroadcastReceiver {

	 NotificationManager nm;

	 @Override
	 public void onReceive(Context context, Intent intent) {
	  nm = (NotificationManager) context
	    .getSystemService(Context.NOTIFICATION_SERVICE);
	  
	  Intent resultIntent = new Intent(context, HomeActivity.class);
	  PendingIntent resultPendingIntent =
			    PendingIntent.getActivity(
			    context,
			    0,
			    resultIntent,
			    PendingIntent.FLAG_UPDATE_CURRENT
			);
	  
	  
	  Notification notif = new Notification.Builder(context)
      .setContentTitle("New mail from " + "Andrea")
      .setContentText("GovHack")
      .setSmallIcon(R.drawable.water_mark)
      .setContentIntent(resultPendingIntent)
      .build();
	  nm.notify(1, notif);
	 }
	}