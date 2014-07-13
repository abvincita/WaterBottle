package govhack.waterbottle;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class APILoader implements JSONRequest.NetworkListener 
{
	private LoadingListener loadingListener= null;
	private boolean isLoading;
	private String result;
	private GoogleMap mMap;
	private State state;
	private ArrayList<ParkItem> parkItems = new ArrayList<ParkItem>();
	private ArrayList<Marker> markers = new ArrayList<Marker>();
	
	private enum State {
		TOILET, FITNESS
	}

	public APILoader(GoogleMap map, ArrayList<Marker> markers) 
	{
		mMap = map;
		this.markers = markers;
		
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
		JSONRequest request = new JSONRequest();
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
		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);

		isLoading = true;
		if (loadingListener != null) {
			loadingListener.onStateChange(isLoading);
		}
	}

	@Override
	public void networkRequestCompleted(String result) {
		this.result = result;

		Object obj = JSONValue.parse(result);
		try 
		{
			JSONArray array = (JSONArray)((JSONObject)obj).get("data");
			
			for (int i = 0; i < array.size(); i++) 
			{
				JSONObject obj2 = (JSONObject)array.get(i);
				Log.d("RESULT=", obj2.toJSONString());
				
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
}
