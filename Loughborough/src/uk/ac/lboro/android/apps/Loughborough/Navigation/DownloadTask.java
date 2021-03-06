package uk.ac.lboro.android.apps.Loughborough.Navigation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.android.gms.maps.GoogleMap;

import android.os.AsyncTask;
import android.util.Log;

// The following code is based on code found here:
// http://wptrafficanalyzer.in/blog/drawing-driving-route-directions-between-two-locations-using-google-directions-in-google-map-android-api-v2/
class DownloadTask extends AsyncTask<String, Void, String> {
	
	GoogleMap myMap;

	public DownloadTask(GoogleMap map) {
		myMap = map;
	}

	// Downloading data in non-UI thread
	@Override
	protected String doInBackground(String... url) {

		// For storing data from web service
		String data = "";

		try {
			
			// Fetching the data from web service
			data = downloadUrl(url[0]);
		} catch (Exception e) {
			Log.d("Devon", "Background Task: " + e.toString());
		}
		return data;
	}

	// Executes in UI thread, after the execution of doInBackground()
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);

		ParserTask parserTask = new ParserTask(myMap);

		// Invokes the thread for parsing the JSON data
		parserTask.execute(result);
	}

	// Downloads JSON data from a URL
	private String downloadUrl(String strUrl) throws IOException {

		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;

		try {
			URL url = new URL(strUrl);

			// Creating an HTTP connection to communicate with URL
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to URL
			urlConnection.connect();

			// Reading data from the URL
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			
			while ((line = br.readLine()) != null)
				sb.append(line);

			data = sb.toString();
			br.close();

		} catch (Exception e) {
			
			Log.d("Devon", "Exception while downloading url: " + e.toString());
		} finally {
			
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}
}
