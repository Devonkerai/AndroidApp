package uk.ac.lboro.android.apps.Loughborough.Ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.lboro.android.apps.Loughborough.R;
import uk.ac.lboro.android.apps.Loughborough.Buildings.Buildings;
import uk.ac.lboro.android.apps.Loughborough.Other.Features;
import uk.ac.lboro.android.apps.Loughborough.Staff.Staff;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
	
	String classes[] = {"Navigation.Navigation", "Buildings.BuildingSearch", "Learn", "WebView Caspa", "WebView Timetable",
						"WebView Main Website", "WebView Lsu Website", "WebView Bus Travel Info", "WebView Email",
						"WebView News", "WebView Events", "Library", "Staff.StaffSearch", "WebView PC Lab Availability", "Other.SafetyToolbox"};
	
	// Arrays to store the data parsed from the XML files.
	public static List<Buildings> myBuildings = new ArrayList<Buildings>();
	public static List<Staff> myStaff = new ArrayList<Staff>();
	public static List<Features> myFeatures = new ArrayList<Features>();
	
	// Creates a dictionary of feature name to website link from the 'myFeatures' array.
	Map<String, String> myDictionary = new HashMap<String, String>();
	
	// Variables to get current location
	static Location mylocation;
	LocationManager lm;
	LocationListener myListener;
	
	GridView gridview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		// Makes it fullscreen - Make sure you set content view after making it full screen.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.grid_menu);
		
		// Gets current location of phone to determine route directions.
	    getCurrentLocation(); 
	    
	    // Only parses the XML files if the arrays are not already populated.
	    if (myFeatures.isEmpty())
	    	FeaturesXML(); // Reading XML file containing Loughborough features (Learn, Caspa, Timetable, etc).
	    
	    if (myBuildings.isEmpty())
	    	BuildingsXML(); // Reading XML file containing Loughborough building names.
	    
	    if (myStaff.isEmpty())
	    	StaffSearchXML(); // Reading XML file containing Loughborough EESE staff names.
	    
	    extractFeaturesInfo();
		
	    // Sets up the menu layout.
	    gridview = (GridView) findViewById(R.id.gridview);
	    gridview.setAdapter(new ImageAdapter(this));	
		
	    // Loads the activity depending on what icon was pressed.
	    gridview.setOnItemClickListener(new OnItemClickListener() {
	    	
	        @Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

	            String appActivity = classes[position];
	    		
	    		try {
	    			
	    			// If Learn is clicked, load in Google Chrome browser.
	    			if (appActivity.equalsIgnoreCase("Learn")) {
	    				Log.d("Devon", "Loading LEARN in Chrome.");
	    				
	    				String weblink = myDictionary.get(appActivity);
	    				Log.d("Devon", "Activity weblink: " + weblink);
	    				loadinChrome(weblink);
	    			}
	    			
	    			// If Library is clicked, load in Google Chrome browser.
	    			else if (appActivity.equalsIgnoreCase("Library")) {
	    				Log.d("Devon", "Loading Library in Chrome.");
	    				
	    				String weblink = myDictionary.get(appActivity);
	    				Log.d("Devon", "Activity weblink: " + weblink);
	    				loadinChrome(weblink);
	    			}
	    			
	    			// If any other webview activity is clicked, load in a webview within the app.
	    			else if (appActivity.startsWith("WebView")) {
	    				Log.d("Devon", "Loading activity in a normal webview.");
	    				
	    				Log.d("Devon", "Activity: " + appActivity);
	    				
	    				String featname = appActivity.substring(8);
	    				Log.d("Devon", "featname: " + featname);
	    				
	    				String weblink = myDictionary.get(featname);
	    				Log.d("Devon", "Activity weblink: " + weblink);
	    				
	    				loadNormalWebview(appActivity, featname, weblink);
	    			}
	    			
	    			// For all activities that aren't webviews.
	    			else {
	    			
			    		Class ourClass = Class.forName("uk.ac.lboro.android.apps.Loughborough." + appActivity);
			    		Log.d("Devon", "Class being loaded is: uk.ac.lboro.android.apps.Loughborough." + appActivity);
			    		
			    		Intent ourIntent = new Intent(Menu.this, ourClass);
			    		startActivity(ourIntent);
	    			}
	    		}
	    		
	    		catch (ClassNotFoundException e) {
	    			
	    			e.printStackTrace();
	    		}
	        }
	    });
	}

	// If you press the menu button on older smartphone devices, an additional button appears.
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		
		super.onCreateOptionsMenu(menu);
		MenuInflater menuInflate = getMenuInflater();
		menuInflate.inflate(R.menu.additional_info, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		int id = item.getItemId();
		
		if (id == R.id.aboutUs) {

			aboutUsInfo();
		}
		
		return false;
	}
	
	private void extractFeaturesInfo() {
		
		// Putting features in a dictionary.
		for (Features feat: myFeatures) {
			String fname = feat.getFeatureName();
			String flink = feat.getWebsiteLink();
			myDictionary.put(fname,flink);
		}
		
		// Log.d("Devon", "Feature myDictionary: " + myDictionary);
	}
	
	// Loads a website externally in the Google Chrome browser.
	private void loadinChrome(String urlString) {

		Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(urlString));
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setPackage("com.android.chrome");
		
		try {
			
		    startActivity(i);
		} catch (ActivityNotFoundException e) {
			
		    // Chrome browser probably not installed so allow the user to choose another instead.
		    i.setPackage(null);
		    startActivity(i);
		}
	}
	
	// Loads the webviews in an activity within the app.
	private void loadNormalWebview(String appActivity, String featname, String weblink) {
		
		Intent i = new Intent("uk.ac.lboro.android.apps.Loughborough.Other.NORMALWEBVIEW");
		i.putExtra("FeatName", featname);
		i.putExtra("WebLink", weblink);
		startActivity(i);
	}
	
	// Loads the features (Learn, Caspa, etc) via an XML file.
	// The following code is based on code found here: http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
	private void FeaturesXML() {
		
		try {

			// Take xml file via input stream from the assets folder.
			InputStream inputStream = getApplicationContext().getAssets().open("features.xml");

			DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuildFactory.newDocumentBuilder();
			Document document = docBuilder.parse(inputStream);

			document.getDocumentElement().normalize();
			  
			// Log.d("Devon", "Root element: " + document.getDocumentElement().getNodeName());
			  
			NodeList nodeList = document.getElementsByTagName("feature");

			for (int i = 0; i < nodeList.getLength(); i++) {
				  
				Node node = nodeList.item(i);
			  
				// Log.d("Devon", "Current Element: " + nNode.getNodeName());
			  
				if (node.getNodeType() == Node.ELEMENT_NODE) {
			  
					Element element = (Element) node;

					// Log.d("Devon","Id : " + eElement.getAttribute("id"));
					String FeatureName = element.getElementsByTagName("name").item(0).getTextContent();
					// Log.d("Devon", "Feature Name: " + FeatureName);
					
					String WebLink = element.getElementsByTagName("weblink").item(0).getTextContent();
					// Log.d("Devon", "Weblink: " + WebLink);
					
					// Creates an object
					Features features = new Features(FeatureName, WebLink);
					myFeatures.add(features);
					
					//Log.d("Devon", "Added feature: " + features.getFeatureName());
				}
			}
		}

		catch (Exception e) {
			Log.d("Devon", "Parsing Features XML file failed. Check the log.");
			e.printStackTrace();
		}
	}
	
	// Loads the data about all the buildings via an XML file.
	// The following code is based on code found here: http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
	private void BuildingsXML() {

		try {
			
			// Take xml file via input stream from the assets folder.
			InputStream inputStream = getApplicationContext().getAssets().open("buildings.xml");
			
			DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuildFactory.newDocumentBuilder();
			Document document = docBuilder.parse(inputStream);

			document.getDocumentElement().normalize();
			  
			// Log.d("Devon", "Root element: " + document.getDocumentElement().getNodeName());
			  
			NodeList nodeList = document.getElementsByTagName("building");
			  
			for (int i = 0; i < nodeList.getLength(); i++) {
			  
				Node node = nodeList.item(i);
			  
				// Log.d("Devon", "Current Element: " + nNode.getNodeName());
			  
				if (node.getNodeType() == Node.ELEMENT_NODE) {
			  
					Element element = (Element) node;
			  
					// Log.d("Devon","Id : " + eElement.getAttribute("id"));
					String BuildingName = element.getElementsByTagName("name").item(0).getTextContent();
					// Log.d("Devon", "Building Name: " + BuildingName);
					String BuildingLat = element.getElementsByTagName("latitude").item(0).getTextContent();
					// Log.d("Devon", "Latitude: " + BuildingLat);
					String BuildingLng = element.getElementsByTagName("longitude").item(0).getTextContent();
					// Log.d("Devon", "Longitude: " + BuildingLng);
					String RoomCodes = element.getElementsByTagName("roomcodes").item(0).getTextContent();
					// Log.d("Devon", "Room codes: " + RoomCodes);
					
					// Creates an object
					Buildings building = new Buildings(BuildingName, RoomCodes, BuildingLat, BuildingLng);
					myBuildings.add(building);
					// Log.d("Devon", "Added building object called: " + building.getBuildingName());
				}
			}
		}
		
		catch (Exception e) {
				Log.d("Devon", "Parsing Building XML file failed. Check the log.");
				e.printStackTrace();
			    }
	}	
	
	// Loads the staff data via an XML file.
	// The following code is based on code found here: http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
	private void StaffSearchXML() {

		try {

			// Take xml file via input stream from the assets folder.
			InputStream inputStream = getApplicationContext().getAssets().open("staff.xml");

			DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuildFactory.newDocumentBuilder();
			Document document = docBuilder.parse(inputStream);

			document.getDocumentElement().normalize();
			  
			// Log.d("Devon", "Root element: " + document.getDocumentElement().getNodeName());
			  
			NodeList nodeList = document.getElementsByTagName("person");

			for (int i = 0; i < nodeList.getLength(); i++) {
				  
				Node node = nodeList.item(i);
			  
				// Log.d("Devon", "Current Element: " + nNode.getNodeName());
			  
				if (node.getNodeType() == Node.ELEMENT_NODE) {
			  
					Element element = (Element) node;

					// Log.d("Devon","Id : " + eElement.getAttribute("id"));
					String PersonName = element.getElementsByTagName("name").item(0).getTextContent();
					//Log.d("Devon", "Person Name: " + PersonName);
					
					String Dept = element.getElementsByTagName("department").item(0).getTextContent();
					//Log.d("Devon", "Department: " + Dept);
					
					String Email = element.getElementsByTagName("email").item(0).getTextContent();
					//Log.d("Devon", "Email: " + Email);
					
					String Extension = element.getElementsByTagName("extension").item(0).getTextContent();
					//Log.d("Devon", "Extension: " + Extension);
					
					// Creates an object
					Staff staff = new Staff(PersonName, Dept, Email, Extension);
					myStaff.add(staff);
					
					//Log.d("Devon", "Added staff: " + staff.getStaffName());
				}
			}
		}

		catch (Exception e) {
			Log.d("Devon", "Parsing Staff XML file failed. Check the log.");
			e.printStackTrace();
		}
	}

	// Retrieves current location of device.
	public void getCurrentLocation() {

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		myListener = new LocationListener() {
			
			@Override
			public void onLocationChanged(Location location) {

				mylocation = location;
				Log.d("Devon", "Location Listener retrieved: " + location);
				
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
		
		// Finds location using Wifi or GPS
		if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
			
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myListener);
			Log.d ("Devon", "Location listener has been activated via Network (Wifi).");
		}
		else if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
			
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myListener);
			Log.d ("Devon", "Location listener has been activated via GPS.");
		}
		else {
			
			Log.d ("Devon", "Cannot retrieve current location, please enable location and re-start the app.");
			Toast.makeText(this, "Cannot retrieve current location, please enable location and restart the app.", Toast.LENGTH_LONG).show();
		}
	}
	
	public static Location getLocation() {
		
		return mylocation;
	}
	
	public void onClick_AboutUs(View v) {

		aboutUsInfo();
	}
	
	private void aboutUsInfo() {
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		 
		// set title
		alertDialogBuilder.setTitle("About Us");
		
		String message = "This is an app to help students with their day to day life at Loughborough University." +
				" This was created as a final year project by Devon Kerai." +
				" If you have any queries, please email me at: d.kerai-11@student.lboro.ac.uk";
		
		// set dialog message
		alertDialogBuilder.setMessage(message).setCancelable(false)
			
		.setPositiveButton("OK",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Close the dialog box
				dialog.cancel();
				}
			});
 
			AlertDialog alertDialog = alertDialogBuilder.create();
 
			alertDialog.show();
	}
	
	// Moves the activity to the back of the stack so if the user was to press the back button, the application would be exited.
	// Prevents having the need to press back multiple times to reverse back through the stack to exit the app.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        moveTaskToBack(true);
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	// Exits the app when the back button is pressed.
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
}