Radio apps
==========
rosalilastudio.com
github.com/rosalila/radioapp

Usage:

1. Link the pheeliks visualisation project
-----------------------------------------
*   Clone it:		git clone git://github.com/felixpalmer/android-visualizer.git
*	Import it:		File > Import > Android > Existing Android Code > Root Directory > Browse > [Select the cloned project] > Import
*	Add it:			Project > Properties > Android > Library > Add > [Select the cloned project]

2. Change the radio url
-----------------------
In MainActivity.java remplace the variable RADIO_STREAM_LINK just like this:
*   private static final Uri RADIO_STREAM_LINK = Uri.parse("[Link here]");
Some working URLS:
*   Deejay:			http://giss.tv:8001/deejayonline.mp3
*   Radio Luz:		http://74.222.5.162:9980/
*   Musiquera:		http://s9.voscast.com:7584/
*   Stereo Mass:	rtsp://4.30.20.151:1935/stereomass/mp3:stereomass

3. Linking to Admob
-------------------
ToDo

4. Editing the Imges
--------------------
Go to res/ and edit the following folders
*	drawable
*	drawable-hdpi
*	drawable-ldpi
*	drawable-mdpi
*	drawable-xhdpi

5. Editing the Layout
---------------------
Go to res/ and edit the following folders
*	layout/main_activity.xml
*	layout-large/main_activity.xml
*	layout-small/main_activity.xml

6. Create the following elements
--------------------------------
*	Money
*	Money
*	More money
