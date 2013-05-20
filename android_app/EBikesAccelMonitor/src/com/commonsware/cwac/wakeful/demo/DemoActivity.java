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

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
import com.commonsware.cwac.wakeful.WakefulIntentService;

/**
 * this activity launches the SEMS monitor in the background and then stops
 * @author chris
 *
 */
public class DemoActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    WakefulIntentService.scheduleAlarms(new AppListener(),
                                        this, true);
    
    Toast.makeText(this, "Accel Alarms active!",
                   Toast.LENGTH_LONG).show();
    
    finish();
  }
}
