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

import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import artsmedia.ebikes.network.AssistanceReport;
import artsmedia.ebikes.phone.phoneIdentifier;

public class EBikesVeloIOIOService extends IOIOService {
	private static final String TAG = EBikesVeloIOIOService.class
			.getSimpleName();
	private String phoneID;

	public static boolean started = false;

	public EBikesVeloIOIOService() {
		phoneID = phoneIdentifier.getPhoneID();
		Log.i(TAG, "IOIO Create");
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new BaseIOIOLooper() {
			private Uart uart;
			private InputStream inStream;
			private ArrayDeque<Integer> q;
			private List<ModeChangeEvent> postList = new ArrayList<ModeChangeEvent>();

			final int seqEcoMode[] = { 85, 1, 0, 2, 3, 0, 0 };
			final int seqMidMode[] = { 85, 1, 0, 2, 3, 1, 1 };
			final int seqHighMode[] = { 85, 1, 0, 2, 3, 2, 2 };
			private int lastMode = -1;
			long ts = 0;
			long postTs = 0;

			public boolean isMatch(ArrayDeque<Integer> q, int[] seq) {
				boolean res = true;
				Object arr[] = q.toArray();
				for (int i = 0; i < q.size(); i++) {
					if ((Integer) arr[i] != seq[i]) {
						res = false;
						break;
					}
				}
				return res;
			}

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
				postTs = System.currentTimeMillis();
				q = new ArrayDeque<Integer>(7);
				for (int i = 0; i < 7; i++) {
					q.add(0);
				}
				Log.i(TAG, "Connecting to IOIO\n");
				uart = ioio_.openUart(6, IOIO.INVALID_PIN, 9600,
						Uart.Parity.NONE, Uart.StopBits.ONE);
				inStream = uart.getInputStream();
				Log.i(TAG, "UART Open");

			}
			
			@Override
			public void disconnected() {
				postCache();
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}				
				uart.close();
				super.disconnected();			}

			/**
			 * Called repetitively while the IOIO is connected.
			 * 
			 * @throws ConnectionLostException
			 *             When IOIO conopenUartnection is lost.
			 * 
			 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
			 */
			@Override
			public void loop() throws ConnectionLostException {
				int av = 0;
				try {
					av = inStream.available();
					if (av == 0) {
						if (System.currentTimeMillis() - ts > 10000) {
							reportMode(0);
						}
					}
					// String msg = "";
					for (int i = 0; i < av; i++) {
						ts = System.currentTimeMillis();
						int b = inStream.read();
						q.add(b);
						// msg = msg + b + ",";
						if (q.size() > 7) {
							q.poll();
						}

						int mode = -1;
						if (isMatch(q, seqEcoMode)) {
							mode = 1;
						} else if (isMatch(q, seqMidMode)) {
							mode = 2;
						} else if (isMatch(q, seqHighMode)) {
							mode = 3;
						}
						reportMode(mode);
					}
					// if(msg != "") {
					// Log.i(TAG, "Serial: " + msg);
					// }
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			public void reportMode(int mode) {
				if (mode != -1) {
					if (mode != lastMode) {
						lastMode = mode;
						postList.add(new ModeChangeEvent(mode, System.currentTimeMillis()));
						
					}
					postCache();

				}
			}

			public void postCache() {
				if (System.currentTimeMillis() - postTs > 35200) {
					Iterator<ModeChangeEvent> it = postList.iterator();
					while(it.hasNext()) {
						AssistanceReport.postAssistanceStatus(it.next(), phoneID);	
					}
					postList.clear();
					postTs = System.currentTimeMillis();
				}
			}
		};
	}

	@Override
	public void onStart(Intent intent, int startId) {

		if (started) {
			Log.i(TAG, "Service already on");
		} else {
			Log.i(TAG, "Starting IOIO");
			super.onStart(intent, startId);
			if (intent != null && intent.getAction() != null
					&& intent.getAction().equals("stop")) {
				stopSelf();
				started = false;
			} else {
			}
			Log.i(TAG, "IOIO Started");
			started = true;

		}
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "Velo IOIO Service onDestroy");
		started = false;
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}