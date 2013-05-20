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
package artsmedia.ebikes.phone;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * 
 * @author chris
 *
 * Get info on the phone's battery
 */
public class batteryInfo {
	private static double chargeLevel;
	private static int powerSource;
	public static double getChargeLevel() {
		return chargeLevel;
	}
	public static void setChargeLevel(double chargeLevel) {
		batteryInfo.chargeLevel = chargeLevel;
	}
	public static int getPowerSource() {
		return powerSource;
	}
	public static void setPowerSource(int powerSource) {
		batteryInfo.powerSource = powerSource;
	}
	public static void update(Context ctxt) {
		Intent batteryIntent = ctxt.registerReceiver(
				null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int rawlevel = batteryIntent.getIntExtra("level", -1);
		double scale = batteryIntent.getIntExtra("scale", -1);
		powerSource = batteryIntent.getIntExtra("plugged", -1);
		chargeLevel = -1;
		if (rawlevel >= 0 && scale > 0) {
			chargeLevel = rawlevel / scale;
		}
	}
	public static boolean externallyPowered() {
		return powerSource != 0;
	}
	
}
