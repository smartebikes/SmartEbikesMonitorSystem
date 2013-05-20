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

public class ChangeTrigger {
	public double value;
	public int hold, holdCount;
	private boolean holding;
	public ChangeTrigger(float initValue) {
		init(initValue, 0);
	}
	public ChangeTrigger(float initValue, int holdTime) {
		init(initValue, holdTime);
	}
	
	public void init(float initValue, int holdTime) {
		value = initValue;
		hold = holdTime;
		holdCount = 0;
		holding = false;
	}
	
	boolean newFrame(float newValue) {
		boolean trig = newValue != value;
		value = newValue;
		if (trig) {
			holdCount = 0;
			holding = true;
		}
		if (holding) {
			if (holdCount == hold) {
				trig = true;
				holding = false;
			}else{
				trig = false;
				holdCount++;
			}
		}
		return trig;
	}
}
