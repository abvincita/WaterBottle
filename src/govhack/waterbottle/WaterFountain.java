package govhack.waterbottle;

import com.google.android.gms.maps.model.LatLng;

public class WaterFountain 
{
	private String id, type, desc, parkName;
	private LatLng position;
	
	public WaterFountain()
	{
		id = "";
		type = "";
		desc = "";
		parkName = "";
		position = new LatLng(0.0, 0.0);
	}
	
	public WaterFountain(String parkName, String id, String type, String desc, LatLng position)
	{
		this.parkName = parkName;
		this.id = id;
		this.type = type;
		this.desc = desc;
		this.position = position;
	}
	
	public String getId()
	{
		return id;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getDesc()
	{
		return desc;
	}
	
	public String getParkName()
	{
		return parkName;
	}
	
	public LatLng getPosition()
	{
		return position;
	}
}
