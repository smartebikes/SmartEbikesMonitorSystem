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

package artsmedia.ebikes.dsp;

public class basicTrigger {
	private double onThreshold, offThreshold;
	private int holdTime, holdCount;
	private beatPhases phase;

	enum beatPhases {
		WAITING, INBEAT
	}

	public basicTrigger(double d, double e, int hold) {
		onThreshold = d;
		offThreshold = e;
		holdTime = hold;
		reset();
	}

	public void reset() {
		phase = beatPhases.WAITING;
		holdCount = 99999;
	}

	public boolean newFrame(double sig) {
		boolean stateChanged = false;
		holdCount++;
		switch (phase) {
		case WAITING:
			if (holdCount > holdTime) {
				if(sig > onThreshold) {
					stateChanged = true;
					phase = beatPhases.INBEAT;
					holdCount = 0;
				}
			}
			break;
		case INBEAT:
			if(sig < offThreshold){
				stateChanged = true;				
				phase = beatPhases.WAITING;
			}
			break;
		}
		return stateChanged;
	}
	
	public boolean isInBeat() {
		return phase == beatPhases.INBEAT;
	}
}
