package uk.ac.lboro.android.apps.Loughborough;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.android.gms.maps.GoogleMap;

import android.os.AsyncTask;
import android.util.Log;

class DownloadTask extends AsyncTask<String, Void, String> {
	
	GoogleMap myMap;

	public DownloadTask(GoogleMap map) {
		myMap = map;
	}

	// Downloading data in non-ui thread
	@Override
	protected String doInBackground(String... url) {

		// For storing data from web service

		String data = "";

		try {
			// Fetching the data from web service
			data = downloadUrl(url[0]);
		} catch (Exception e) {
			Log.d("Background Task", e.toString());
		}
		return data;
	}

	// Executes in UI thread, after the execution of
	// doInBackground()
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

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}
}
