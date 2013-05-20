/***
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

package com.commonsware.cwac.wakeful.demo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;
import artsmedia.ebikes.EBikesMonitorService;
import artsmedia.ebikes.ioio.EBikesDoverIOIOService;
import artsmedia.ebikes.ioio.EBikesVeloIOIOService;
import artsmedia.ebikes.phone.batteryInfo;
import artsmedia.ebikes.phone.phoneIdentifier;

import com.commonsware.cwac.wakeful.WakefulIntentService;

/**
 * SEMS app
 * runs in the background, wakes up when motion is detected and starts monitoring
 * @author chris
 *
 */
public class EBikesAccelMonitorAppService extends WakefulIntentService
		implements SensorEventListener {
	private static final double version = 1.4;
	private String TAG = EBikesAccelMonitorAppService.class.getSimpleName();
	private SensorManager sensorManager;
	private float totalAccel;
	private int sensorReadingCount;
	private float oax = 0, oay = 0, oaz = 0;
	private static LinkedList<Float> accelHistory = new LinkedList<Float>();
	private static long statusTS = 0;

	private enum Platforms {
		DOVER, VELOCITE
	};

	/**
	 * Set this according to the bike it will be used with
	 */
	private static final Platforms platform = Platforms.DOVER;

	/**
	 * read in a value from the sdcard, in /sdcard/Download
	 * @param name The name of the value
	 * @return The value
	 */
	double readVal(String name) {
		double val = 0;
		File path = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File file = new File(path, name);
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			val = dis.readDouble();
			dis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i(TAG, "V: " + val);
		return val;
	}

	public EBikesAccelMonitorAppService() {
		super("AppService");

	}

	private boolean dover() {
		return Platforms.DOVER == platform;
	}

	private boolean velocite() {
		return Platforms.VELOCITE == platform;
	}

	/**
	 * Wake up from low power state and maybe do some monitoring if in motion 
	 */
	@Override
	protected void doWakefulWork(Intent intent) {
		try {
			//get battery states
			batteryInfo.update(this.getApplicationContext());
			//post the battery status every so often
			if (System.currentTimeMillis() - statusTS > (1000 * 60 * 180)) {
				Log.i(TAG, "Posting status");
				Log.i(TAG, "Charge: " + batteryInfo.getChargeLevel()
						+ ", source: " + batteryInfo.getPowerSource());
				try {
					postStatus();
					statusTS = System.currentTimeMillis();
				} catch (Exception e) {
					Log.i(TAG, "Error posting status");
					e.printStackTrace();
				}
			}
			Log.i(TAG, "Bat: " + batteryInfo.getChargeLevel());
			
			//listen to the accelerometer for a short time
			sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			sensorManager.registerListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_NORMAL);
			Log.i(TAG, "Awake!");
			totalAccel = 0.f;
			sensorReadingCount = 0;
			// wait while taking acc readings
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Log.i(TAG, "Total accel: " + totalAccel);
			// keep history of accel values
			accelHistory.add(totalAccel);
			if (accelHistory.size() > 2) {
				accelHistory.removeFirst();
			}
			boolean serviceOn = false;

			// switch on location listener if any accels in hist are over the
			// threshold
			for (int i = 0; i < accelHistory.size(); i++) {
				Log.i("AppService", "acc: " + accelHistory.get(i));
				if (accelHistory.get(i) > 8.f) {
					serviceOn = true;
				}
			}

			//start monitoring?
			if (serviceOn) {
				Log.i(TAG, "About to start IOIO");
				startService(new Intent(getIOIOIntentName()));

				Log.i(TAG, "Starting monitor service");
				Intent ebikesMonitorServiceIntent = new Intent(
						EBikesMonitorService.class.getName());
				startService(ebikesMonitorServiceIntent);

			} else {
				stopMonitoring();
			}
			sensorManager.unregisterListener(this);

			Log.i(TAG, "Done, going back to sleep...");
		} catch (Exception e) {
			Log.i(TAG, "EBikes Monitor Exception");
			e.printStackTrace();

		}
	}

	public String getIOIOIntentName() {
		return dover() ? EBikesDoverIOIOService.class.getName()
				: EBikesVeloIOIOService.class.getName();
	}

	public void stopMonitoring() {
		Log.i(TAG, "Stopping IOIO and monitoring services");
		stopService(new Intent(getIOIOIntentName()));
		Intent ebikesMonitorServiceIntent = new Intent(
				EBikesMonitorService.class.getName());
		stopService(ebikesMonitorServiceIntent);
	}

	public void postStatus() throws Exception {
		//post some data to the project server
		URL url;
		HttpURLConnection conn = null;
		try {
			String urlStr;
			urlStr = "http://myserver/st.php?ts="
					+ System.currentTimeMillis() + "&p="
					+ batteryInfo.getPowerSource() + "&c="
					+ batteryInfo.getChargeLevel() + "&id="
					+ phoneIdentifier.getPhoneID();
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

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	/**
	 * log accelerometer data
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float ax = event.values[0];
			float ay = event.values[1];
			float az = event.values[2];
			Log.i(TAG, "Accel " + ax + ", " + ay + "," + az);
			if (sensorReadingCount == 0) {
			} else {
				float diffx = Math.abs(ax - oax);
				float diffy = Math.abs(ay - oay);
				float diffz = Math.abs(az - oaz);
				totalAccel += (diffx + diffy + diffz);

			}
			oax = ax;
			oay = ay;
			oaz = az;
			sensorReadingCount++;
		}

	}
}
