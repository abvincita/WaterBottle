package govhack.waterbottle;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
 
public class MyReceiver extends BroadcastReceiver
{
      
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	NotificationManager nm = (NotificationManager) context
    		    .getSystemService(Context.NOTIFICATION_SERVICE);
    		  
    		  Intent resultIntent = new Intent(context, HomeActivity.class);
    		  resultIntent = resultIntent.putExtra("FromNotification", true);
    		  
    		  PendingIntent resultPendingIntent =
    				    PendingIntent.getActivity(
    				    context,
    				    0,
    				    resultIntent,
    				    PendingIntent.FLAG_ONE_SHOT
    				);
    		  
    		  
    		  Notification notif = new Notification.Builder(context)
    	      .setContentTitle("Running out of water?")
    	      .setContentText("Find the nearest water fountain!")
    	      .setSmallIcon(R.drawable.holo_icon)
    	      .setContentIntent(resultPendingIntent)
    	      .setVibrate(new long[] { 1000, 1000 })
    	      .build();
    		  nm.notify(1, notif);
        
    }   
}
