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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * 
 * @author chris
 *
 * A service for location listening
 */
public class EBikesMonitorService extends Service {

	private final class APIStub extends EBikesDataAPI.Stub {
		@Override
		public float getUpdate() throws RemoteException {
			return 0;
		}

		public boolean isBikeMoving() {
			return false;
		}

		@Override
		public void addListener(EBikesDataListener listener)
				throws RemoteException {
			synchronized (listeners) {
				listeners.add(listener);
			}
		}

		@Override
		public void removeListener(EBikesDataListener listener)
				throws RemoteException {
			synchronized (listeners) {
				listeners.remove(listener);
			}

		}
	}

	private static final String TAG = EBikesMonitorService.class
			.getSimpleName();
	private List<EBikesDataListener> listeners = new ArrayList<EBikesDataListener>();

	private EBikesDataAPI.Stub apiEndPoint = new APIStub();

	private Timer timer;

	private TimerTask updateTask = new TimerTask() {
		@Override
		public void run() {
			Log.i(TAG, "Ebikes monitor timer task");
			synchronized (listeners) {
				for (EBikesDataListener listener : listeners) {
					try {
						listener.handleUpdated();
					} catch (RemoteException e) {
						Log.w(TAG, "Failed to notify listener " + listener, e);
					}
				}
			}
		}
	};
	//private boolean bikeIsMoving = false;
	private LocationManager locationManager;
	private EBikesLocationListener loc;

	@Override
	public IBinder onBind(Intent intent) {
		if (EBikesMonitorService.class.getName().equals(intent.getAction())) {
			Log.d(TAG, "Bound by intent " + intent);
			return apiEndPoint;
		} else {
			return null;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service creating");
		
		//start monitoring GPS signal
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		loc = new EBikesLocationListener();
		loc.init();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				1, loc);

		timer = new Timer("EBikesMonitorTimer");
		timer.schedule(updateTask, 500L, 3000L);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "Service destroying");
		loc.exit();
		locationManager.removeUpdates(loc);

		timer.cancel();
		timer = null;
		super.onDestroy();
	}


}
