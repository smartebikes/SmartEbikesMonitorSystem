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

import java.util.Arrays;


public class RunningStats {
	
	double[] win;
	int size;
	int idx;
	
	public RunningStats(int _size, double initval) {
		size = _size;
		win = new double[size];
		idx=0;
		for(int i=0; i < size; i++)
			win[i] = initval;
	}
	
	void newFrame(double val) {
		win[idx % size] = val;
		idx++;
	}
	
	double min() {
		double lowest=Double.MAX_VALUE;
		for(int i=0; i < size; i++) {
			if(win[i] < lowest) {
				lowest = win[i];
			}
		}
		return lowest;
	}
	
	double max() {
		double highest = Double.MIN_VALUE;
		for(int i=0; i < size; i++) {
			if(win[i] > highest) {
				highest = win[i];
			}
		}
		return highest;
	}
	
	double median() {
		double med=0;
		double[] sortedWin = win.clone();
		Arrays.sort(sortedWin);
		if (size % 2 == 1) {
			med = sortedWin[size/2];
		}else{
			med = (sortedWin[size/2] + sortedWin[(size/2) -1]) / 2.0f;
		}
		return med;
	}
	
	double mean() {
		double sum = 0;
		for(int i=0; i < size; i++) {
			sum += win[i];
		}
		return sum / size;
	}
}
