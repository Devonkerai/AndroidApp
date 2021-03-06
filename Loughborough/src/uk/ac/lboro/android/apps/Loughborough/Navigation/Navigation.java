package uk.ac.lboro.android.apps.Loughborough.Navigation;

import uk.ac.lboro.android.apps.Loughborough.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class Navigation extends Activity {

	private final LatLng LOCATION_UNI = new LatLng(52.765997, -1.234043); // University co-ordinates
	
	TextView textView;
	
	GoogleMap map;
	CameraUpdate update;
	Location myLocation;
	
	int zoomLevel;
	String textViewName, lat, lng, currentLat, currentLng, showDirections;
	
	ConnectivityManager conMan;
	NetworkInfo netInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		// Make the activity fullscreen - Make sure you set content view after making full screen.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.navigation);
		
		
		initialiseVariables();
		initialiseMap();
		initialiseDirections();
	}
	
	// Initialises variables
	private void initialiseVariables() {
		
		conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		textView = (TextView) findViewById(R.id.textViewTitle);
		
		// Sets up heading of the activity
		textViewName = "Navigation";
		textView.setText(textViewName);
		textView.setTextSize(16);
		textView.setTextColor(Color.parseColor("#C70066"));
		textView.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
		
		// Check status of network
		netInfo = conMan.getActiveNetworkInfo();
		Log.d("Devon", "netInfo: " + netInfo);
	}
	
	// Initialises map
	private void initialiseMap() {
	
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		map.getUiSettings().setZoomControlsEnabled(false);
		map.setMyLocationEnabled(true);
		update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNI, 17); // Shows these co-ords as default when the map is loaded.
		map.animateCamera(update);
		
		if (netInfo == null)
		{
			Toast.makeText(this, "You do not have access to the internet. Please enable wifi or mobile data and try again.", Toast.LENGTH_LONG).show();
			Log.d("Devon", "No access to the internet.");
			return;
		}
	}
	
	// Checks to see if the the Navigation activity has been called from another activity and whether the route needs to be displayed onto the map.
	private void initialiseDirections() {
		
		// Retrieving current location co-ords and building co-ords to show route direction.
		Intent i = getIntent();
		lat = i.getStringExtra("LAT");
		lng = i.getStringExtra("LNG");
		currentLat = i.getStringExtra("CURRENTLAT");
		currentLng = i.getStringExtra("CURRENTLNG");
		showDirections = i.getStringExtra("showDirections");
		
		if (showDirections != null) {
			
			Log.d("Devon", "Showing route directions");
			showDirections(lat, lng, currentLat, currentLng);
			update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNI, 14);
			map.animateCamera(update);
		}
	}
	
	// Pressing the 'Uni' button.
	public void onClick_Uni(View v) {

		update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNI, 17);
		map.clear(); // Clears all overlays from the map.
		map.animateCamera(update);	
	}
	
	// Pressing the 'Find Me' button
	public void onClick_FindMe(View v) {
		
		zoomLevel = 16;
		getMyLocation(zoomLevel);
	}
	
	// Shows current location on the map
	private void getMyLocation(int zoomLevel) {
		
		myLocation = map.getMyLocation();
		
		if (myLocation == null ) {
			
			Toast.makeText(this, "Your location is not available. Please enable location and try again.", Toast.LENGTH_LONG).show();
		} else {
			
			update = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), zoomLevel);
			map.animateCamera(update);
		}
	}
	
	// Pressing the 'Building Search' button launches the Building Search activity.
	public void onClick_BuildingSearch(View v) {

		Toast.makeText(this, "Launching Building Search", Toast.LENGTH_SHORT).show();
		Intent i = new Intent("uk.ac.lboro.android.apps.Loughborough.Buildings.BUILDINGSEARCH");
		startActivity(i);
	}
	
	// Displays route on the map
	// The following code is based on code found here:
	// http://wptrafficanalyzer.in/blog/drawing-driving-route-directions-between-two-locations-using-google-directions-in-google-map-android-api-v2/
	public void showDirections(String lat, String lng, String currentLat, String currentLng) {
			
		// May need to convert the lat, lng etc to doubles and then put it into the array.
		// Converts the Strings of the co-ordinates into a Double so a new LatLng co-ordinates can be made.
		LatLng origin = new LatLng(Double.parseDouble(currentLat), Double.parseDouble(currentLng));
		LatLng dest = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

		// Getting URL to send to the Google Directions API
	    String url = getDirectionsUrl(origin, dest);
	
	    DownloadTask downloadTask = new DownloadTask(map);
	
	    // Start downloading JSON data from the Google Directions API
	    Log.d("Devon", "URL: " + url);
	    if (!url.isEmpty())
	    	downloadTask.execute(url);
	    else
	    	Toast.makeText(this, "Your location is not available", Toast.LENGTH_LONG).show();
	    	Log.d("Devon", "URL: " + url);
	}
	
	// The following code is based on code found here:
	// http://wptrafficanalyzer.in/blog/drawing-driving-route-directions-between-two-locations-using-google-directions-in-google-map-android-api-v2/
    private String getDirectionsUrl(LatLng origin, LatLng dest){
     	 
        // Output format
        String output = "json";
        
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
 
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
 
        // Sensor enabled
        String walkingmode = "mode=walking&units=metric";
 
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + walkingmode;
 
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        
        // Log.d("Devon", "URL: " + url);
        return url;
    }
    
    // Goes back to the menu screen when the back button is pressed.
    @Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent i = new Intent("uk.ac.lboro.android.apps.Loughborough.Ui.MENU");
		startActivity(i);
	}
}
