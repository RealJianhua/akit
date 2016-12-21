package wenjh.akit.demo.location.model;

import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;


public class LocationHelper {
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	// flag for when we have co-ords
	private boolean gotLocation = false;
	private LocationManager locationManager;
	private MyLocationListener locationListener;
	private Location mLocation;
	private Context context;
	private Handler uiHandler;

	public LocationHelper(Context context) {
		this.context = context;
		this.uiHandler = new Handler(context.getMainLooper());
	}

	public class MyLocationListener implements LocationListener {

		// called when the location service reports a change in location
		public void onLocationChanged(Location location) {
			if (isBetterLocation(location, mLocation)) {
				mLocation = location;
			}
			
			synchronized (obj) {
				obj.notifyAll();
			}
			
			// change the flag to indicate we now have a location
			gotLocation = true;
			locationManager.removeUpdates(locationListener);

		}

		// called when the provider is disabled
		public void onProviderDisabled(String provider) {
		}

		// called when the provider is enabled
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

	}

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	/***
	 * Stop updates from the Location Service.
	 */
	public void killLocationServices() {
		locationManager.removeUpdates(locationListener);
	}

	/***
	 * Check if a location has been found yet.
	 * 
	 * @return - True if a location has been acquired. False otherwise.
	 */
	public Boolean gotLocation() {
		return gotLocation;
	}
	
	public boolean isLocationServiceEnabled(){
		 boolean result = false;
		 boolean gps_enabled = false,network_enabled = false;
		    if(locationManager==null)
		        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		    try{
		    gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		    }catch(Exception ex){}
		    try{
		    network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		    }catch(Exception ex){}

		   if(!gps_enabled && !network_enabled){
		        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		        dialog.setMessage("Blupe cannot work without location services enabled,Would you like to enable location service?");
		        dialog.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {

		            @Override
		            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
		                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
		                context.startActivity(myIntent);
		                //get gps
		            }
		        });
		        dialog.setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {

		            @Override
		            public void onClick(DialogInterface paramDialogInterface, int paramInt) {}
		        });
		        dialog.show();

		    }else{
		    	result = true;
		    }
		return result;
	}

	public static Object obj = new Object();

	/**
	 * returns a location object, this method will never return null;
	 * 
	 * @return location object for current location
	 */
	public LatLng  getLocation() {

		if (Looper.myLooper() == context.getMainLooper()) {
			throw new IllegalThreadStateException("Can't use this method in main thread");
		}

		uiHandler.post(new Runnable() {

			@Override
			public void run() {

				locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
				// create the location listener
				locationListener = new MyLocationListener();
				// setup a callback for when the GRPS/WiFi gets a lock and we
				// receive data
				if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
				}
				// setup a callback for when the GPS gets a lock and we receive
				// data
				if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
				}

			}
		});

		// wait location
		synchronized (obj) {
			try {
				obj.wait(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		LatLng latLng = null;
		if(mLocation != null) {
			latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude(), mLocation.getAccuracy());
			latLng.setTime(new Date());
			latLng.setProvider(mLocation.getProvider());
		}
		
		return latLng;
	}
	
	public static boolean isLocationAvailable(double lat, double lng) {
		if ((lat == 0.0 && lng == 0.0) || lat < -90 || lat > 90 || lng < -180 || lng > 180) {
			return false;
		}
		return true;
	}
}
