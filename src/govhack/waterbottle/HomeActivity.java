package govhack.waterbottle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
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
import android.preference.PreferenceManager;
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
import android.widget.TextView;

public class HomeActivity extends FragmentActivity implements
LocationListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener 
{
	private static final LatLng DEFAULT_LOCATION = new LatLng(-27.498037,153.017823);
	private static final int UPDATE_INTERVAL = 10000;
	private static final int FASTEST_INTERVAL = 5000;
	public static final String CHOOSE_BOTTLE_SETTING = "CHOOSE_BOTTLE";
	public static final String LAST_OPEN_TIME = "LAST_OPEN_TIME";
	public static final String BOTTLE_CHOICE = "BOTTLE_CHOICE";
	public static final int NUM_PAGES = 4;
	public static TextView chooseBottleTitle;
	public static ImageView waterBottle;
	public static ImageView blueBG;
	public static Context baseContext;
	
	private static boolean bottleFull = false;
	private AlarmManager am;
	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private GoogleMap mMap;
	private SupportMapFragment mapFrag;
	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;
	private Location mCurrentLocation;
	private Marker currentNearestMarker;
	private ArrayList<ParkItem> fountainList = new ArrayList<ParkItem>();
	private ArrayList<Marker> fountainMarkers, toiletMarkers, fitnessMarkers;
	private APILoader apiLoader;
	
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
		mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		boolean chooseBottle = settings.getBoolean(CHOOSE_BOTTLE_SETTING, true);
		int bottleChoice = settings.getInt(BOTTLE_CHOICE, 0);
		
		chooseBottleTitle = (TextView) findViewById(R.id.tutorial_title);
		waterBottle = (ImageView) findViewById(R.id.water_bottle);
		blueBG = (ImageView) findViewById(R.id.blue_bg);
		
		baseContext = getBaseContext();
		
		if (chooseBottle)
		{
			showChooseBottleDialog();
		}
		else
		{
			chooseBottleTitle.setVisibility(View.GONE);

			long lastOpenTime = settings.getLong(LAST_OPEN_TIME, (long) 0.0);
			long currentTime = System.currentTimeMillis();
			long diff = currentTime - lastOpenTime;
			
			if(diff < (7.5 * 60 * 1000))
			{
				bottleFull = true;
				switch(bottleChoice)
				{
					case 1:
						waterBottle.setImageResource(R.drawable.circle1);
	            		break;
	            	case 2:
	            		waterBottle.setImageResource(R.drawable.circle2);
	            		break;
	            	case 3:
	            		waterBottle.setImageResource(R.drawable.circle3);
	            		break;
	            	case 4:
	            		waterBottle.setImageResource(R.drawable.circle4);
	            		break;
	            	default:
	            		break;
				}
			}
			else if(diff >= (7.5 * 60 * 1000) && diff < (15 * 60 * 1000))
			{
				bottleFull = false;
				switch(bottleChoice)
				{
					case 1:
						waterBottle.setImageResource(R.drawable.waterbottle_31);
	            		break;
	            	case 2:
	            		waterBottle.setImageResource(R.drawable.waterbottle_32);
	            		break;
	            	case 3:
	            		waterBottle.setImageResource(R.drawable.waterbottle_33);
	            		break;
	            	case 4:
	            		waterBottle.setImageResource(R.drawable.waterbottle_34);
	            		break;
	            	default:
	            		break;
				}
			}
			else if(diff >= (15 * 60 * 1000) && diff < (22.5 * 60 * 1000))
			{
				bottleFull = false;
				switch(bottleChoice)
				{
					case 1:
						waterBottle.setImageResource(R.drawable.waterbottle_27);
	            		break;
	            	case 2:
	            		waterBottle.setImageResource(R.drawable.waterbottle_28);
	            		break;
	            	case 3:
	            		waterBottle.setImageResource(R.drawable.waterbottle_29);
	            		break;
	            	case 4:
	            		waterBottle.setImageResource(R.drawable.waterbottle_30);
	            		break;
	            	default:
	            		break;
				}
			}
			else if(diff >= (22.5 * 60 * 1000) && diff < (30 * 60 * 1000))
			{
				bottleFull = false;
				switch(bottleChoice)
				{
					case 1:
						waterBottle.setImageResource(R.drawable.waterbottle_23);
	            		break;
	            	case 2:
	            		waterBottle.setImageResource(R.drawable.waterbottle_24);
	            		break;
	            	case 3:
	            		waterBottle.setImageResource(R.drawable.waterbottle_25);
	            		break;
	            	case 4:
	            		waterBottle.setImageResource(R.drawable.waterbottle_26);
	            		break;
	            	default:
	            		break;
				}
			}
			else
			{
				bottleFull = false;
				switch(bottleChoice)
				{
					case 1:
						waterBottle.setImageResource(R.drawable.holo1);
	            		break;
	            	case 2:
	            		waterBottle.setImageResource(R.drawable.holo2);
	            		break;
	            	case 3:
	            		waterBottle.setImageResource(R.drawable.holo3);
	            		break;
	            	case 4:
	            		waterBottle.setImageResource(R.drawable.holo4);
	            		break;
	            	default:
	            		break;
				}
			}
			
		}
		
		mapInit();
		
		waterBottle.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				
				if(!bottleFull)
					increaseWater(0);
				
				return true;
			}
		});
	}
	
	public static void increaseWater(long delay)
	{	
		bottleFull = true;

		ObjectAnimator oa = ObjectAnimator.ofFloat(blueBG, "y", 7);
		oa.setDuration(5000);
		oa.setStartDelay(delay);
		oa.start();
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(baseContext);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putLong(LAST_OPEN_TIME, System.currentTimeMillis());
	    editor.commit();
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
        
        if(fountainMarkers == null)
        {
        	fountainMarkers = new ArrayList<Marker>();
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
		             ParkItem fountain = new ParkItem(attributes[3], attributes[6], attributes[7], attributes[8], pos);
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
			
			currentNearestMarker = closestMarker;
			closestMarker.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.water_mark)));
			
			TextView distanceText = (TextView) findViewById(R.id.distance_text); 
			distanceText.setText(closestDistance.intValue() + "m");
			
//			toiletMarkers = new ArrayList<Marker>();
//			apiLoader = new APILoader(mMap, toiletMarkers);
//			apiLoader.requestToilets();
        }
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Log.d("ONLOCATIONCHANGED", "inside");
		
		mCurrentLocation = location;
		LatLng myLoc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()); 
		
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
		
		if(!currentNearestMarker.getPosition().equals(closestMarker.getPosition()))
		{
			closestMarker.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.water_mark)));
			currentNearestMarker.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.water_mark_small)));
			
			TextView distanceText = (TextView) findViewById(R.id.distance_text); 
			distanceText.setText(closestDistance.intValue() + "m");
		}
		
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
