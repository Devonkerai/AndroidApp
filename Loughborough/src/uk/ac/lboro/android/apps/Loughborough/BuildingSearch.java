package uk.ac.lboro.android.apps.Loughborough;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.lboro.android.apps.Loughborough.R;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

public class BuildingSearch extends Activity {

	AutoCompleteTextView autoCompTextBuildings;
	EditText editTextRoomCode;
	String buildingName, roomCode, currentLat, currentLng;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Make the activity fullscreen - Make sure you set content view after making it full screen.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.building_search);

		Log.d("Devon", "Log message in buildingsearch.java");
				
		setUpAutoCompleteTextViews();
		editTextRoomCode = (EditText) findViewById(R.id.editTextRoomCode);
	}

	private void setUpAutoCompleteTextViews() {

		autoCompTextBuildings = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewBuildingName);

		// Build string array of buildings from mybuildings list
		List<String> buildingNames = new ArrayList<String>();
		for (Buildings bldg : Menu.myBuildings) {
			String bname = bldg.getBuildingName();
			buildingNames.add(bname);
		}

		// Create the adapter and set it to the AutoCompleteTextView
		ArrayAdapter<String> adapterBuildingName = new ArrayAdapter<String>(
				this, android.R.layout.simple_list_item_1, buildingNames);
		autoCompTextBuildings.setAdapter(adapterBuildingName);
		autoCompTextBuildings.setThreshold(1);
	}

	public void onClick_GetDirections(View v) {

		// Launches the 'Other' activity and displays the directions.

		buildingName = autoCompTextBuildings.getText().toString();
		roomCode = editTextRoomCode.getText().toString().toUpperCase();
		Location mylocation = Menu.getLocation();
		
		Buildings bn = getBuildingFromNameOrRoomCode(buildingName, roomCode);
		
		String showDirections = "Yes";

		if (bn == null) {

			Toast.makeText(this, "Cannot find location via building name or room code.", Toast.LENGTH_LONG).show();
			Log.d("Devon", "Cannot find building name in list.");
			return;
		}
		
		if (mylocation == null) {
			Toast.makeText(this, "Cannot retrieve current location, please wait and then try again.", Toast.LENGTH_LONG).show();
			return;
		}
		
		currentLat = String.valueOf(mylocation.getLatitude());
		currentLng = String.valueOf(mylocation.getLongitude());
		
		Log.d("Devon", "Current Lat: " + currentLat + " Current Lng: " + currentLng);
		
		if (currentLat == "0.0" && currentLng == "0.0") {
			
			Toast.makeText(this, "Cannot find current location.", Toast.LENGTH_LONG).show();
			Log.d("Devon", "Cannot find current location.");
			return;
		}
		
		Log.d("Devon", "Building name retrieved from textview: " + buildingName);
		Log.d("Devon", "Room code retrieved from textview: " + roomCode);
		Log.d("Devon", "Current Lat: " + currentLat + " Current Lng: " + currentLng);
		Log.d("Devon", "ShowDirections: " + showDirections);
		
		Intent i = new Intent("uk.ac.lboro.android.apps.Loughborough.NAVIGATION");

		i.putExtra("LAT", bn.getLatitude()); // Building name will be blank (not null) if field is not in.
		i.putExtra("LNG", bn.getLongitude()); // Room code will be blank (not null) if field is not filled in.
		i.putExtra("CURRENTLAT", currentLat);
		i.putExtra("CURRENTLNG", currentLng);
		i.putExtra("showDirections", showDirections);
		startActivity(i);
	}

	private Buildings getBuildingFromNameOrRoomCode(String bname, String rcode) {
		
		Buildings bn = null;

		if (!bname.isEmpty()) {

			bn = findBuildingFromName(bname);
			return bn;
		}

		else if (!rcode.isEmpty()) {

			bn = findBuildingFromRoomCode(rcode);
			return bn;
		}
		return null;
	}
	
	private Buildings findBuildingFromName (String name)
	{	
		//Log.d("Devon", "Building Name is: " + name);
		
		for(Buildings bldg : Menu.myBuildings)
		{
			String bname = bldg.getBuildingName();
			
			//Log.d("Devon", "bname in findLocationFromName is: " + bname);
			
			if(bname.equalsIgnoreCase(name))
				return bldg;
		}
		
		return null;
	}
	
	private Buildings findBuildingFromRoomCode (String name) {
		
		Log.d("Devon", "Room code entered is: " + name);
		String roomCStart = null;
		
		// Only 4 building codes contains letters and numbers.
		if (name.contains("LP1") || name.contains("LP2") || name.contains("HD3") || name.contains("HD7")) {
			
			roomCStart = name.substring(0,3);
		}
		
		else {
			// Regex to split the room code and only use letters to find within the XML list.
			Pattern pat = Pattern.compile("^[A-Z]*");
			Matcher match = pat.matcher(name);
			while (match.find()) {
				
				roomCStart = match.group();
			}
		}
		
		for(Buildings bldg : Menu.myBuildings)
		{
			String[] rcode = bldg.getRoomCodes();
			for (String str : rcode) {
				
				if(str.equals(roomCStart)) {
					Log.d("Devon", "Found room code: " + roomCStart);
					return bldg;
				}	
			}
		}
		return null;
	}

	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent i = new Intent("uk.ac.lboro.android.apps.Loughborough.MENU");
		startActivity(i);
	}

}
