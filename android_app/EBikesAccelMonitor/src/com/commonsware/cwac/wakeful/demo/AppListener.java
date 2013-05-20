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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;
import com.commonsware.cwac.wakeful.WakefulIntentService;

/**
 * configuration for the wakeful intent service timer
 * @author chris
 *
 */
public class AppListener implements WakefulIntentService.AlarmListener {
  private static final int TIMERFREQUENCY = 25000;

public void scheduleAlarms(AlarmManager mgr, PendingIntent pi,
                             Context ctxt) {
    mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime()+1000,
                            TIMERFREQUENCY, pi);
  }

  public void sendWakefulWork(Context ctxt) {
    WakefulIntentService.sendWakefulWork(ctxt, EBikesAccelMonitorAppService.class);
  }

  public long getMaxAge() {
	    return(TIMERFREQUENCY * 2);
  }
}
