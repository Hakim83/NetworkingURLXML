package course.examples.networking.urlxml;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NetworkingURLXMLActivity extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Get your own user name at http://www.geonames.org/login
		final String USER_NAME = "aporter";
		final String URL = "http://api.geonames.org/earthquakes?north=44.1&south=-9.9&east=-22.4&west=55.2&username="
				+ USER_NAME;
		new GetResponseTask().execute(URL);
	}

	public class GetResponseTask extends AsyncTask<String, Void, List<String>> {

		private static final String MAGNITUDE_TAG = "magnitude";
		private static final String LONGITUDE_TAG = "lng";
		private static final String LATITUDE_TAG = "lat";
		private String mLat, mLng, mMag;
		private boolean mIsParsingLat, mIsParsingLng, mIsParsingMag;
		private final List<String> mResults = new ArrayList<String>();

		@Override
		protected List<String> doInBackground(String... strings) {
			//this part only return raw response from url!

			try {
				InputStream rawStream = GetHttpResponse(strings[0]);
				if (rawStream != null) {
					return ParseXML(rawStream);
				}
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<String> result) {
			if (result == null) {
				Toast.makeText(getApplicationContext(), "No Connection!", Toast.LENGTH_SHORT).show();
			} else {
				setListAdapter(new ArrayAdapter<String>(
						NetworkingURLXMLActivity.this,
						R.layout.list_item, result));
			}

		}

		protected InputStream GetHttpResponse(String urlText) {
			InputStream rawStream = null;

			try {
				URL url = new URL(urlText);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.connect();

				rawStream = connection.getInputStream();

			} catch (IOException e) {
				e.printStackTrace();
			}
			return rawStream;
		}

		// parse raw JSON data into strings list
		private List<String> ParseXML(InputStream rawStream) throws XmlPullParserException, IOException {
			// Create the Pull Parser
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xpp = factory.newPullParser();

			// Set the Parser's input to be the XML document in the HTTP Response
			xpp.setInput(rawStream, null);

			// Get the first Parser event and start iterating over the XML document
			int eventType = xpp.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {

				if (eventType == XmlPullParser.START_TAG) {
					startTag(xpp.getName());
				} else if (eventType == XmlPullParser.END_TAG) {
					endTag(xpp.getName());
				} else if (eventType == XmlPullParser.TEXT) {
					text(xpp.getText());
				}
				eventType = xpp.next();
			}
			return mResults;
		}

		public void startTag(String localName) {
			if (localName.equals(LATITUDE_TAG)) {
				mIsParsingLat = true;
			} else if (localName.equals(LONGITUDE_TAG)) {
				mIsParsingLng = true;
			} else if (localName.equals(MAGNITUDE_TAG)) {
				mIsParsingMag = true;
			}
		}

		public void text(String text) {
			if (mIsParsingLat) {
				mLat = text.trim();
			} else if (mIsParsingLng) {
				mLng = text.trim();
			} else if (mIsParsingMag) {
				mMag = text.trim();
			}
		}

		public void endTag(String localName) {
			if (localName.equals(LATITUDE_TAG)) {
				mIsParsingLat = false;
			} else if (localName.equals(LONGITUDE_TAG)) {
				mIsParsingLng = false;
			} else if (localName.equals(MAGNITUDE_TAG)) {
				mIsParsingMag = false;
			} else if (localName.equals("earthquake")) {
				mResults.add(MAGNITUDE_TAG + ":" + mMag + "," + LATITUDE_TAG + ":"
						+ mLat + "," + LONGITUDE_TAG + ":" + mLng);
				mLat = null;
				mLng = null;
				mMag = null;
			}
		}
	}
}