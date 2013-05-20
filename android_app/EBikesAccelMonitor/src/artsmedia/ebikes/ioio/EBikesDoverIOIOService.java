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
package artsmedia.ebikes.ioio;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import artsmedia.ebikes.dsp.ChangeTrigger;
import artsmedia.ebikes.dsp.RunningStats;
import artsmedia.ebikes.network.AssistanceReport;
import artsmedia.ebikes.phone.phoneIdentifier;

public class EBikesDoverIOIOService extends IOIOService {
	private static final String TAG = EBikesDoverIOIOService.class.getSimpleName();
	private RunningStats ma43, ma44, maDiff;
	private ChangeTrigger statusTrig;
	private double class1, class2, class3;
	private double[] dists;
	private String phoneID;


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
		Log.i(TAG, "V: " + val);
		return val;
	}

	public EBikesDoverIOIOService() {
		class1 = readVal("low");
		class2 = readVal("mid");
		class3 = readVal("high");
		Log.i(TAG, "cl1: " + class1);
		Log.i(TAG, "cl2: " + class2);
		Log.i(TAG, "cl3: " + class3);
		dists = new double[3];
		int maVal = 15;
		ma43 = new RunningStats(maVal, 0.0f);
		ma44 = new RunningStats(maVal, 0.0f);
		maDiff = new RunningStats(maVal, 0.0f);
		statusTrig = new ChangeTrigger(-1000, 20);
		phoneID = phoneIdentifier.getPhoneID();
		Log.i(TAG, "IOIO Create");
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new BaseIOIOLooper() {
			private AnalogInput in43, in44;

			@Override
			protected void setup() throws ConnectionLostException,
					InterruptedException {
//				led_ = ioio_.openDigitalOutput(IOIO.LED_PIN);
				in43 = ioio_.openAnalogInput(43);
				in44 = ioio_.openAnalogInput(44);
			}

			@Override
			public void loop() throws ConnectionLostException,
					InterruptedException {
				//led_.write(ledState);
				//ledState = !ledState;
				double v, sm, v1, v2;
				try {
					v = in43.read();
					ma43.newFrame(v);
					sm = ma43.median();
					v1 = sm;

					v = in44.read();
					ma44.newFrame(v);
					sm = ma44.median();
					v2 = sm;

					maDiff.newFrame((v2 - v1));
					int status = 0;
					if (v1 < 0.4 && v2 < 0.4) {
						// off
					} else {
						// get dists
						double val = maDiff.median();
						Log.i(TAG, "Diff: " + val);
						dists[0] = Math.abs(class1 - val);
						dists[1] = Math.abs(class2 - val);
						dists[2] = Math.abs(class3 - val);
						int idx = 0;
						double minVal = Double.MAX_VALUE;
						for (int i = 0; i < 3; i++) {
							if (dists[i] < minVal) {
								idx = i;
								minVal = dists[i];
							}
						}
						status = idx + 1;
					}

					if (statusTrig.newFrame(status)) {
						AssistanceReport.postAssistanceStatus(new ModeChangeEvent(status, System.currentTimeMillis()), phoneID);
					}

				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					Log.i("IOIO", "error while reading IOIO");
					e1.printStackTrace();
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}

		};
	}

	@Override
	public void onStart(Intent intent, int startId) {
		
		Log.i(TAG, "Starting IOIO");
		super.onStart(intent, startId);
		if (intent != null && intent.getAction() != null
				&& intent.getAction().equals("stop")) {
			stopSelf();
		} else {
		}
		Log.i(TAG, "IOIO Started");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
