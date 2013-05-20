<?php
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

error_reporting(E_ALL);
$con = mysql_connect("localhost", "dbname", "password");
if (!$con) {
	die('Could not connect:' . mysql_error());
}
mysql_select_db("ebikesdatabasename", $con);
$q = "insert into checkin(ts, powersource, chargelevel, phoneID) VALUES(".$_REQUEST['ts'].",".$_REQUEST['p'].",".$_REQUEST['c'].",".$_REQUEST['id'].");";
echo $q;
mysql_query($q);
mysql_close($con);
?>
Yeah...!
