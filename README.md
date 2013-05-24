Smart E-bikes Monitor System (SEMS)
========================

Source code and hardware design for the Smart-EBikes Monitoring System.

This repository accompanies a journal article, 'The Smart E-Bike Monitoring System', which is currently under review.

In short, this repository contains source code and hardware designs for a monitoring system which can autonomously collect location and sensor data describing electric bike usage. The system is in use by the 'Smart E-Bikes' research project at the University of Brighton, see http://www.smart-ebikes.co.uk for more details. The project is funded by Research Council's UK Energy program through the EPSRC. The Smart E-bikes Monitor System (SEMS) is one of the project outcomes and this repository is part of documenting and communicating it.

The system will work as described when used with the Raleigh Dover or Raleigh Velo-cite e-bikes, but will probably require customisation to work with other models. SEMS is designed to be modular, so additional sensors can be added. 

We have open-sourced the monitor system so that it may be used by other e-bike and GPS monitoring projects. 

If you are considering using SEMS, you are welcome to contact us at http://www.smart-ebikes.com/?page_id=36 to discuss it. We would especially be interested in exchanging data with other e-bike related projects.



Structure
========================


There are three folders.

/android_app contains the source code for monitor application.

/hardware contains circuit diagrams for the monitor system

/server_scripts contains php scripts which the android app communicates with.  

Data is stored in a mysql database whose structure is described in /db.sql.





Customisation
========================

There are currently two areas where the system must be customised to a specific bike

(1) Power

The UVLO and LM2567 voltage convertor must be configured to match the voltage range of the bike battery. Currently there are two variations, for the Raleigh Dover (21v-30v) and the Raleigh Velocite (30v-42v).  The Velocite board has higher specified components, and includes a voltage spike supressor to protect the IOIO.  Either of these models may be good starting points for your own bike.

(2) Assistance monitoring

The Dover version works by using by monitoring voltages on the controller connections, while the Velocite board simply listens in on the LIN bus. Again, either of these may be useful, or you may have to create something entirely new; the IOIO should be flexible enough to track assistance in most situations.


