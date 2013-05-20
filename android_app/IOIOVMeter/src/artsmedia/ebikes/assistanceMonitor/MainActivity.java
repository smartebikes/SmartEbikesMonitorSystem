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
package artsmedia.ebikes.assistanceMonitor;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



/**
 *
 * @author chris
 * 
 * This app is used for configuring assistance monitoring on the dover e-bike.  Use it to calibrate the program for each assistance setting. It stores these values
 * on the sd-card for use by the SEMS system 
 * 
 * inputs from the control system are read on inputs 43 and 44 on the IOIO
 * 
 */
public class MainActivity extends IOIOActivity {

	private TextView a43, a44, diffTV, statusTV, cl1TV, cl2TV, cl3TV;
	private Button bLow, bMid, bHigh;
	private AnalogInput in43, in44;
	private RunningStats ma43, ma44, maDiff;
	private ChangeTrigger statusTrig;
	private double class1, class2, class3;
	private double[] dists;

	public MainActivity() {
		int maVal = 15;
		ma43 = new RunningStats(maVal, 0.0f);
		ma44 = new RunningStats(maVal, 0.0f);
		maDiff = new RunningStats(maVal, 0.0f);
		statusTrig = new ChangeTrigger(-1000, 20);
		class1 = 0;
		class2 = 0;
		class3 = 0;
		dists = new double[3];
	}

	/**
	 * write values to the sdcard
	 * @param name The name of the value to store
	 * @param val The value
	 */
	void writeVal(String name, double val) {
		//make sure there's a folder called /sdcard/Download
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File file = new File(path, name);
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
			dos.writeDouble(val);
			dos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * read values from the sdcard
	 * @param name The name of te value to store
	 * @return the value of the parameter
	 */
	double readVal(String name) {
		double val=0;
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
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
		return val;
	}

	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		a43 = (TextView) findViewById(R.id.a43);
		a44 = (TextView) findViewById(R.id.a44);
		diffTV = (TextView) findViewById(R.id.diffVal);
		statusTV = (TextView) findViewById(R.id.status);
		//make three button. Tap these to calibrate each assistance level
		bLow = (Button) findViewById(R.id.button1);
		bMid = (Button) findViewById(R.id.button2);
		bHigh = (Button) findViewById(R.id.high);
		cl1TV = (TextView) findViewById(R.id.cl1);
		cl2TV = (TextView) findViewById(R.id.cl2);
		cl3TV = (TextView) findViewById(R.id.cl3);

		class1 = readVal("low");
		class2 = readVal("mid");
		class3 = readVal("high");
		cl1TV.setText("" + class1);
		cl2TV.setText("" + class2);
		cl3TV.setText("" + class3);
		
		bLow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				class1 = maDiff.median();
				cl1TV.setText("" + class1);
				writeVal("low", class1);
			}
		});
		bMid.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				class2 = maDiff.median();
				cl2TV.setText("" + class2);
				writeVal("mid", class2);
			}
		});
		bHigh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				class3 = maDiff.median();
				cl3TV.setText("" + class3);
				writeVal("high", class3);}
		});
	}

	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			//open the analogue inputs
			in43 = ioio_.openAnalogInput(43);
			in44 = ioio_.openAnalogInput(44);
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException {
			double v, sm, v1, v2;
			try {
				//read the input
				v = in43.read();
				//smooth with a median filter
				ma43.newFrame(v);
				sm = ma43.median();
				v1 = sm;

				setText(a43, "43: " + sm + ", " + v);

				v = in44.read();
				ma44.newFrame(v);
				sm = ma44.median();
				v2 = sm;
				setText(a44, "44: " + sm + ", " + v);

				maDiff.newFrame((v2 - v1));
				double diffSm = maDiff.median();

				setText(diffTV, "Diff: " + diffSm + "\n, " + (v2 - v1));
				int status = 0;
				if (v1 < 0.3 && v2 < 0.3) {
					// off
				} else {
					//estimate the assistance level by looking at the distance between the current value and the three calibrated values
					// get dists
					double val = maDiff.median();
					dists[0] = Math.abs(class1 - val);
					dists[1] = Math.abs(class2 - val);
					dists[2] = Math.abs(class3 - val);
					int idx = 0;
					double maxVal = 99999;
					for (int i = 0; i < 3; i++) {
						if (dists[i] <= maxVal) {
							idx = i;
							maxVal = dists[i];
						}
					}
					status = idx + 1;
				}
				
				//if the status has changed then display the value
				if (statusTrig.newFrame(status)) {
					setText(statusTV, "Status: " + status);
				}

			} catch (InterruptedException e1) {
				Log.i("IOIO", "error while reading IOIO");
				e1.printStackTrace();
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}

	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	/**
	 * Show some text in a textview outside of the IOIO thread
	 * @param tv
	 * @param str
	 */
	private void setText(final TextView tv, final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tv.setText(str);
			}
		});
	}
}