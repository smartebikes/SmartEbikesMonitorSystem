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

package artsmedia.ebikes.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;
import artsmedia.ebikes.ioio.ModeChangeEvent;

/**
 * for posting assistance status to the server, used by both bikes
 * @author chris
 *
 */
public class AssistanceReport {
	
	private static final String TAG = AssistanceReport.class.getSimpleName();

	public static void postAssistanceStatus(final ModeChangeEvent modeChangeEvent, final String monitorID) {
		new Thread(new Runnable() {
			public void run() {
				
				//send data
				URL url;
				HttpURLConnection conn = null;
				try {
					String urlStr;
					urlStr = "http://myserver/as.php?ts="
							+ modeChangeEvent.when + "&p=" + monitorID + "&l="
							+ modeChangeEvent.mode;
					url = new URL(urlStr);
					System.setProperty("http.keepAlive", "false");
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setDoOutput(true);
					conn.setConnectTimeout(5000);
					conn.setReadTimeout(5000);
					conn.setRequestProperty("Connection", "close");
					conn.connect();
					InputStream in = new BufferedInputStream(
							conn.getInputStream());
					BufferedReader r = new BufferedReader(
							new InputStreamReader(in));
					StringBuilder total = new StringBuilder();
					String line;
					while ((line = r.readLine()) != null) {
						total.append(line);
						Log.i(TAG, line);
					}
					Log.i(TAG, total.toString());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					conn.disconnect();
				}
				
			}
		}).start();
	}

}
