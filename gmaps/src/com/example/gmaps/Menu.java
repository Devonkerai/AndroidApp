package com.example.gmaps;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.gmaps.R;
import com.example.gmaps.R.id;
import com.example.gmaps.R.layout;
import com.example.gmaps.R.menu;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;


public class Menu extends Activity {

	// Has to have the EXACT same name as the corresponding classes.
	String classes[] = {"Gmaps", "BuildingSearch", "Learn", "Caspa", "Timetable",
						"MainWebsite", "LsuWebsite", "BusTravelInfo", "Email",
						"News", "Events", "Library", "StaffSearch", "PcLabs", "SafetyToolbox"};
	
	public static List<Buildings> myBuildings = new ArrayList<Buildings>();
	public static List<Lecturers> myLecturer = new ArrayList<Lecturers>();
	
	// Variables to get current location
	static Location mylocation;
	LocationManager lm;
	LocationListener myListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		// Makes it fullscreen - Make sure you set content view after making it full screen.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.grid_menu);
		
	    GridView gridview = (GridView) findViewById(R.id.gridview);
	    gridview.setAdapter(new ImageAdapter(this));	
		
	    gridview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

	            String appActivity = classes[position];
	    		
	    		try {
	    			
	    			if (appActivity == "Learn") {
	    				Log.d("Devon", "Loading LEARN in Chrome");
	    				String urlString ="http://learn.lboro.ac.uk/my";
	    				loadinChrome(urlString);
	    			}
	    			
	    			else if (appActivity == "Library") {
	    				Log.d("Devon", "Loading Library in Chrome");
	    				String urlString ="http://lb-primo.hosted.exlibrisgroup.com/primo_library/libweb/action/search.do";
	    				loadinChrome(urlString);
	    			}
	    			
	    			else {
	    			
		    		Class ourClass = Class.forName("com.example.gmaps." + appActivity);
		    		Log.d("Devon", "Class is: com.example.gmaps." + appActivity);
		    		
		    		Intent ourIntent = new Intent(Menu.this, ourClass);
		    		startActivity(ourIntent);
	    			}
	    		}
	    		
	    		catch (ClassNotFoundException e) {
	    			
	    			e.printStackTrace();
	    		}
	        }
	    });
	    
		BuildingsXML(); // Reading XML file containing Loughborough building names.
		StaffSearchXML(); // Reading XML file containing Loughborough EESE staff names.
		
		getCurrentLocation(); // Gets current location of phone to determine route directions.
	}
	
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		
		super.onCreateOptionsMenu(menu);
		MenuInflater blowUp = getMenuInflater();
		blowUp.inflate(R.menu.additional_info, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		
		case R.id.aboutUs:
			Intent i = new Intent("com.example.gmaps.ABOUTUS");
			startActivity(i);
			break;
			
		case R.id.exit:
			finish();
			break;
		}
		
		return false;
	}
	
	private void loadinChrome(String urlString) {

		Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(urlString));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setPackage("com.android.chrome");
		
		try {
		    startActivity(intent);
		} catch (ActivityNotFoundException ex) {
			
		    // Chrome browser presumably not installed so allow user to choose instead
		    intent.setPackage(null);
		    startActivity(intent);
		}
	}
	
	// Loads the data about all the buildings from an XML file.
	private void BuildingsXML() {

		try {
			
			// Take xml file via input stream from the assets folder.
			InputStream in_s = getApplicationContext().getAssets().open("lborofull.xml");
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(in_s);


			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work

			doc.getDocumentElement().normalize();
			  
			//Log.d("Devon", "Root element: " + doc.getDocumentElement().getNodeName());
			  
			NodeList nList = doc.getElementsByTagName("building");
			  
			for (int i = 0; i < nList.getLength(); i++) {
			  
				Node nNode = nList.item(i);
			  
				//Log.d("Devon", "Current Element: " + nNode.getNodeName());
			  
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			  
					Element eElement = (Element) nNode;
			  
					// Log.d("Devon","Id : " + eElement.getAttribute("id"));
					String BuildingName = eElement.getElementsByTagName("name").item(0).getTextContent();
					//Log.d("Devon", "Building Name: " + BuildingName);
					String BuildingLat = eElement.getElementsByTagName("latitude").item(0).getTextContent();
					//Log.d("Devon", "Latitude: " + BuildingLat);
					String BuildingLng = eElement.getElementsByTagName("longitude").item(0).getTextContent();
					//Log.d("Devon", "Longitude: " + BuildingLng);
					String RoomCodes = eElement.getElementsByTagName("roomcodes").item(0).getTextContent();
					//Log.d("Devon", "Room codes: " + RoomCodes);
					
					// Creates an object
					Buildings building = new Buildings(BuildingName, RoomCodes, BuildingLat, BuildingLng);
					myBuildings.add(building);
					//Log.d("Devon", "Added building object called: " + building.getBuildingName());
				}
			}
		}
		
		catch (Exception e) {
				Log.d("Devon", "In the log");
				e.printStackTrace();
			    }
	}	
	
	private void StaffSearchXML() {

		try {

			// Take xml file via input stream from the assets folder.
			InputStream in_s = getApplicationContext().getAssets().open("staffsearch.xml");
			

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(in_s);

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work

			doc.getDocumentElement().normalize();

			//Log.d("Devon", "Root element: " + doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName("person");

			for (int i = 0; i < nList.getLength(); i++) {

				Node nNode = nList.item(i);

				//Log.d("Devon", "Current Element: " + nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					// Log.d("Devon","Id : " + eElement.getAttribute("id"));
					String PersonName = eElement.getElementsByTagName("name").item(0).getTextContent();
					//Log.d("Devon", "Person Name: " + PersonName);
					
					String Dept = eElement.getElementsByTagName("department").item(0).getTextContent();
					//Log.d("Devon", "Department: " + Dept);
					
					String Email = eElement.getElementsByTagName("email").item(0).getTextContent();
					//Log.d("Devon", "Email: " + Email);
					
					String Extension = eElement.getElementsByTagName("extension").item(0).getTextContent();
					//Log.d("Devon", "Extension: " + Extension);
					
					// Creates an object
					Lecturers lecturers = new Lecturers(PersonName, Dept, Email, Extension);
					myLecturer.add(lecturers);
					
					//Log.d("Devon", "Added lecturer: " + lecturers.getLecturerName());
				}
			}
		}

		catch (Exception e) {
			Log.d("Devon", "In the log");
			e.printStackTrace();
		}
	}

	public void getCurrentLocation() {

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		myListener = new LocationListener() {
			
			public void onLocationChanged(Location location) {

				mylocation = location;
				Log.d("Devon", "Location Listener retrieved: " + location);
				
				// Write something here to stop listening for the current location.
				lm.removeUpdates(myListener);
				Log.d ("Devon", "Location listener has been de-activated.");
				
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {			
			}

			@Override
			public void onProviderDisabled(String provider) {		
			}
		};
		
		// Update this to use the best provider to find location.
		if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
			
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myListener);
			Log.d ("Devon", "Location listener has been activated via Network.");
			Log.d ("Devon", "Location (via Network) is: " + mylocation);
		}
		else if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
			
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myListener);
			Log.d ("Devon", "Location listener has been activated via GPS.");
			Log.d ("Devon", "Location (via GPS) is: " + mylocation);
		}
		else {
			
			Log.d ("Devon", "Cannot find location. Please enable Wifi or GPS.");
			Toast.makeText(this, "Cannot find location. Please enable Wifi or GPS and then try again.", Toast.LENGTH_LONG).show();
		}
        
	}
	
	public static Location getLocation() {
		
		return mylocation;
	}

	
}