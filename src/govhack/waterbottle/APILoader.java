package govhack.waterbottle;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class APILoader implements JSONRequest.NetworkListener 
{
	private LoadingListener loadingListener= null;
	private boolean isLoading;
	private GoogleMap mMap;
	private State state;
	private ArrayList<ParkItem> parkItems = new ArrayList<ParkItem>();
	private ArrayList<Marker> markers = new ArrayList<Marker>();
	private LatLng myLoc;
	private TextView distanceText;
	private JSONRequest request;

	public APILoader(GoogleMap map, ArrayList<Marker> markers, LatLng myLoc, TextView distanceText) 
	{
		mMap = map;
		this.markers = markers;
		this.myLoc = myLoc;
		this.distanceText = distanceText;
		
		isLoading = false;
		if (loadingListener != null) {
			loadingListener.onStateChange(isLoading);
		}
	}

	public void requestToilets() 
	{
		state = State.TOILET;
		String urlString = "http://118.138.242.136/parks/index.php?useAPI=true&ITEM_TYPE=TOILET&limit=5000";
		Log.d("urlString: ", urlString);
		request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);

		isLoading = true;
		if (loadingListener != null) {
			loadingListener.onStateChange(isLoading);
		}
	}
	
	public void requestFitness() 
	{
		state = State.FITNESS;
		String urlString = "http://118.138.242.136/parks/index.php?useAPI=true&ITEM_TYPE=FITNESS&limit=5000";
		Log.d("urlString: ", urlString);
		request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);

		isLoading = true;
		if (loadingListener != null) {
			loadingListener.onStateChange(isLoading);
		}
	}
	
	public void cancelLoadingAPI()
	{
		request.cancel(true);
	}

	@Override
	public void networkRequestCompleted(String result) 
	{
		Object obj = JSONValue.parse(result);
		try 
		{
			JSONArray array = (JSONArray)((JSONObject)obj).get("data");
			
			for (int i = 0; i < array.size(); i++) 
			{
				JSONObject obj2 = (JSONObject)array.get(i);
				
				String parkName = (String) obj2.get("PARK_NAME");
				String id = (String) obj2.get("ITEM_ID");
				String type = (String) obj2.get("ITEM_TYPE");
				String desc = (String) obj2.get("DESCRIPTION");
				String lng = (String) obj2.get("LONGITUDE");
				String lat = (String) obj2.get("LATITUDE");
				
				LatLng pos = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
				ParkItem item = new ParkItem(parkName, id, type, desc, pos);
				
				parkItems.add(item);
				
				if(state == State.TOILET)
				{
					Marker m = mMap.addMarker(new MarkerOptions()
					.position(pos)
					.title(desc)
					.snippet(parkName)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_geo_border)));
					
					markers.add(m);
				}
				else
				{
					Marker m = mMap.addMarker(new MarkerOptions()
					.position(pos)
					.title(desc)
					.snippet(parkName)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ferry_geo_border)));
					
					markers.add(m);
				}
			} 
			
			getClosestMarker(markers);
		}
		catch (Exception e)
		{
			//No data were found or there was some network error
		}
	}

	/**
	* @return if waiting for response from server
	**/
	public boolean isLoading() {
		return isLoading;
	}

	public void registerListener (LoadingListener listener) {
		loadingListener = listener;
	}
	
	private void getClosestMarker(ArrayList<Marker> markerList)
	{
		Marker closestMarker = markerList.get(0);
		Double closestDistance = HomeActivity.haversianDistance(myLoc, closestMarker.getPosition());
		
		for(Marker marker : markerList)
		{
			closestDistance = HomeActivity.haversianDistance(myLoc, closestMarker.getPosition());
			Double newDistance = HomeActivity.haversianDistance(myLoc, marker.getPosition());
			
			if(newDistance < closestDistance)
			{
				closestMarker = marker;
				closestDistance = newDistance;
			}
		}
		
		distanceText.setText(closestDistance.intValue() + "m");
	}
}
