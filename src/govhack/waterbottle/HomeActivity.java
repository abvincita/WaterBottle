package govhack.waterbottle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class HomeActivity extends FragmentActivity implements
LocationListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener 
{
	private static final LatLng DEFAULT_LOCATION = new LatLng(-27.498037,153.017823);
	public static final String CHOOSE_BOTTLE_SETTING = "CHOOSE_BOTTLE";
	public static final int NUM_PAGES = 4;
	public static TextView chooseBottleTitle;
	public static ImageView waterBottle;
	public static ImageView blueBG;
	private final Context CONTEXT = this;
	
	private boolean bottleFull = false;
	private AlarmManager am;
	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private GoogleMap mMap;
	private SupportMapFragment mapFrag;
	private LocationClient mLocationClient;
	private Location mCurrentLocation;
	private ArrayList<WaterFountain> fountainList = new ArrayList<WaterFountain>();
	private ArrayList<Marker> fountainMarkers = new ArrayList<Marker>();
	
	// Handle to SharedPreferences for this app
	SharedPreferences settings;
    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		Intent receivedIntent = getIntent();
        boolean fromNotif = receivedIntent.getBooleanExtra("FromNotification", false);
        
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        unsetAlarm();

        if(fromNotif)
        {
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);

        	builder.setMessage("Find the nearest water fountain, drink and hold the bottle icon to refill!" + "\n\n" + "Do you want to continue receiving water notifications?").setTitle("Running out of water?");
        	builder.setIcon(R.drawable.holo_icon);
        	
        	builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	               setAlarm();
        	           }
        	       });
        	builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	               unsetAlarm();
        	           }
        	       });

        	// Create the AlertDialog
        	builder.create().show();
        }
        else
        	setAlarm();
        
		mLocationClient = new LocationClient(this, this, this);
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		boolean chooseBottle = settings.getBoolean(CHOOSE_BOTTLE_SETTING, true);
		
		chooseBottleTitle = (TextView) findViewById(R.id.tutorial_title);
		waterBottle = (ImageView) findViewById(R.id.water_bottle);
		blueBG = (ImageView)findViewById(R.id.blue_bg);
		
		if (chooseBottle)
			showChooseBottleDialog();
		else
			chooseBottleTitle.setVisibility(View.GONE);
		
		mapInit();
		
		waterBottle.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				if(!bottleFull)
					increaseWater();
				
				return true;
			}
		});
		
		decreaseWater();
	}
	
	public void increaseWater()
	{	
		bottleFull = true;
		AnimatorSet blueUp = (AnimatorSet) AnimatorInflater.loadAnimator(CONTEXT, R.animator.blue_swing);

		ObjectAnimator oa = ObjectAnimator.ofFloat(blueBG, "y", 5);
		oa.setDuration(5000);
		oa.start();
//		blueUp.setTarget(blueBG);
//		blueUp.start();
	}
	
	public void decreaseWater()
	{
		LayoutParams param = (LayoutParams) blueBG.getLayoutParams();
		param.topMargin = 240;
		//param.setMargins(0, 200, 20, 0);
		Log.d("BLUEBOX", "Height " + param.height + " Width " + param.width);
		
	}
	
	public void setAlarm()
	{
		Intent intent = new Intent(this, MyReceiver.class);
		  PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
		    intent, PendingIntent.FLAG_CANCEL_CURRENT);
		  
		  long firstTime = System.currentTimeMillis();
	        firstTime += (30 * 60 * 1000);
		  
		  am.setRepeating(AlarmManager.RTC_WAKEUP, firstTime,
		    (30 * 60 * 1000), pendingIntent);
	}
	
	public void unsetAlarm() 
	{
		Intent intent = new Intent(this, MyReceiver.class);
		  PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
		    intent, PendingIntent.FLAG_CANCEL_CURRENT);

	    am.cancel(pendingIntent);
	} 
	
	public void showChooseBottleDialog() 
	{
		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager(), mPager);
		mPager.setAdapter(mPagerAdapter);
	}
	
	private class TutorialPagerAdapter extends FragmentStatePagerAdapter 
	{
		private ViewPager pagerView;

		public TutorialPagerAdapter(FragmentManager fm, ViewPager parent) 
		{
			super(fm);
			pagerView = parent;
		}

		@Override
		public Fragment getItem(int position) 
		{
			return ChooseBottleFragment.create(position, pagerView);
		}

		@Override
		public int getCount()
		{
			return NUM_PAGES;
		}
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }
	
	@Override
    protected void onResume() {
        super.onResume();

        
    }
	
	@Override
	protected void onStop() {
	    // Disconnecting the client invalidates it.
	    mLocationClient.disconnect();
	    super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	public void mapInit() 
	{
		LatLng center = DEFAULT_LOCATION;

		mapFrag = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);

		mMap = mapFrag.getMap();

		while (mMap == null) {
			// The application is still unable to load the map.
		}
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));
		mMap.setMyLocationEnabled(true);
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {

        mCurrentLocation = mLocationClient.getLastLocation();
        LatLng myLoc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());   
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLoc, (float) 17.0), 2000, null);
        
        AssetManager am = getAssets();
		try
		{
			InputStream is = am.open("dataset_drinking_fountain_taps3.csv");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			
			String line = reader.readLine();
	        while((line = reader.readLine()) != null)
	        {
	             String[] attributes = line.split(",");
	             double lat = Double.parseDouble(attributes[12]);
	             double lng = Double.parseDouble(attributes[11]);
	             LatLng pos = new LatLng(lat, lng);
	             WaterFountain fountain = new WaterFountain(attributes[3], attributes[6], attributes[7], attributes[8], pos);
	             fountainList.add(fountain);
	             
	             Marker m = mMap.addMarker(new MarkerOptions()
					.position(pos)
					.title(fountain.getDesc())
					.snippet(fountain.getParkName())
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.water_mark_small)));
	             
	             fountainMarkers.add(m);
	        }
			
		    reader.close();
		    is.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		Marker closestMarker = fountainMarkers.get(0);
		Double closestDistance = haversianDistance(myLoc, closestMarker.getPosition());
		
		for(Marker marker : fountainMarkers)
		{
			closestDistance = haversianDistance(myLoc, closestMarker.getPosition());
			Double newDistance = haversianDistance(myLoc, marker.getPosition());
			
			if(newDistance < closestDistance)
			{
				closestMarker = marker;
				closestDistance = newDistance;
			}
		}
		
		closestMarker.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.water_mark)));
		
		TextView distanceText = (TextView) findViewById(R.id.distance_text); 
		distanceText.setText(closestDistance.intValue() + "m");
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	private Double haversianDistance(LatLng center, LatLng point)
	{
		//earth radius in meter
		final int R = 6371000;
		
		double lat1 = center.latitude;
		double lat2 = point.latitude;
		double lng1 = center.longitude;
		double lng2 = point.longitude;
		
		Double latDistance = toRad(lat2 - lat1);
        Double lonDistance = toRad(lng2 - lng1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + 
                   Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * 
                   Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Double distance = R * c;
        
        return distance;
	}
	
	private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }

}