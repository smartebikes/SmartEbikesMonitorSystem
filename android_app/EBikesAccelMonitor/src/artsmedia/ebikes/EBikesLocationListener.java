/*
This file is part of the Smart E-bikes Monitor System
© Copyright 2012, Frauke Behrendt and Chris Kiefer.
This software is freely available for non-commercial use under the terms of the 
GNU General Public License as published by the Free Software Foundation, either 
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package artsmedia.ebikes;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.DeflaterOutputStream;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import artsmedia.ebikes.phone.phoneIdentifier;

public class EBikesLocationListener implements LocationListener {
	private static String TAG = EBikesLocationListener.class.getSimpleName();
	File log;
	FileWriter logw;
	private String phoneID;
	private String data;
	private long sendTS;

	public void init() {
		Log.i(TAG, "Location manager starting");

		phoneID = phoneIdentifier.getPhoneID();
		data = "";
		sendTS = System.currentTimeMillis();

	}

	public void exit() {
		postDataCache();
		Log.i(TAG, "Location manager stopping");
	}

	public void onLocationChanged(Location loc) {
		Log.i(TAG, "Loc:" + loc.getLongitude() + "," + loc.getLatitude());
		try {
			postLocation(loc.getLongitude(), loc.getLatitude(),
					loc.getAltitude(), loc.getAccuracy(), phoneID,
					System.currentTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void postLocation(double lon, double lat, double alt, double acc,
			String phoneID, long ts) throws Exception {
		// add entry to accumulating data
		data = data + ts + "," + lon + "," + lat + "," + acc + "," + alt + ","
				+ phoneID + "|";
		//send periodically
		if (System.currentTimeMillis() - sendTS > 30000) {
			postDataCache();
		}
	}

	/**
	 * zip up the data (to save transmission time and power) and send it
	 */
	public void postDataCache() {
		// send data
		URL url;
		HttpURLConnection conn = null;
		try {
			Log.i(TAG, "Sending " + data);
			url = new URL("http://myserver/locz.php");
			System.setProperty("http.keepAlive", "false");
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-encoding", "deflate");
			conn.setRequestProperty("Content-type", "application/octet-stream");
			conn.setRequestProperty("Connection", "close");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			DeflaterOutputStream dos = new DeflaterOutputStream(
					conn.getOutputStream());
			dos.write(data.getBytes());
			dos.flush();
			dos.close();
			Log.i(TAG, "Data sent");
			InputStream in = new BufferedInputStream(conn.getInputStream());
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				total.append(line);
				Log.i(TAG, line);
			}
			Log.i(TAG, total.toString());
			data = "";
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			conn.disconnect();
		}
		sendTS = System.currentTimeMillis();
	}

	public void onProviderDisabled(String arg0) {
		Log.i(TAG, "gps disabled");
	}

	public void onProviderEnabled(String arg0) {
		Log.i(TAG, "gps enabled");
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		Log.i(TAG, arg0);
	}

}