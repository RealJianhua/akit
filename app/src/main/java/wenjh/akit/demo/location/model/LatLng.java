package wenjh.akit.demo.location.model;

import java.util.Date;

public class LatLng {
	private double latitude = -1;
	private double longitude = -1;
	private float accuracy = -1;
	private Date timeDate = null;
	private String provider = null;
	
	public LatLng() {
	}
	
	public LatLng(double lat, double lng, float acc) {
		this.latitude = lat;
		this.longitude = lng;
		this.accuracy = acc;
	}

	public double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}
	
	public String getStaticMapUri() {
		return "staticmap://?lat="+latitude+"&lng="+longitude;
	}

	public Date getTime() {
		return timeDate;
	}
	
	public long getTimestamp() {
		return timeDate != null ? timeDate.getTime() : 0;
	}
	
	public void setTime(Date timeDate) {
		this.timeDate = timeDate;
	}
	
	public String getProvider() {
		return provider;
	}
	
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	@Override
	public String toString() {
		return "LngLat [latitude=" + latitude + ", longitude=" + longitude + ", accuracy=" + accuracy + ",timeDate="+timeDate+"]";
	}
}
