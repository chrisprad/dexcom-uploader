dexcom-uploader
====================
This project is a port of the android-uploader (https://github.com/nightscout/android-uploader) Nightscout project with the purpose of delivering CGM data from Dexcom G4/G5 receivers using any system that supports Java (and libUSB).

The goal of the project is to allow stable, reliable delivery of CGM data over PCs and micro devices such as the raspberry pi.

## Status
The project is currently working on Linux (Raspberry Pi Zero W).

## TODO
- [x] Working Linux version
- [ ] Working Windows version using zadig driver
- [ ] Working Mac OSX version (No mac PCs available)


## Example
Here's a recent example from the debug.log of my current system 
```
2017.May.03 23:39:28.199 [dexcom-uploader] INFO - Nightscout Dexcom Uploader
2017.May.03 23:39:28.987 [dexcom-uploader] DEBUG - Initializing LibUSB
2017.May.03 23:39:30.162 [dexcom-uploader] DEBUG - Starting download run...
2017.May.03 23:39:30.172 [dexcom-uploader] DEBUG - claiming interfaces, count=1 force claim=false
2017.May.03 23:39:30.205 [dexcom-uploader] DEBUG - Control iface=USB interface 00
2017.May.03 23:39:30.219 [dexcom-uploader] DEBUG - data iface=USB interface 01
2017.May.03 23:39:30.223 [dexcom-uploader] DEBUG - Read endpoint direction: -128
2017.May.03 23:39:30.225 [dexcom-uploader] DEBUG - Write endpoint direction: 0
2017.May.03 23:39:30.227 [dexcom-uploader] INFO - Dexcom G4/G5 device connected successfully
2017.May.03 23:39:30.256 [dexcom-uploader] DEBUG - Reading EGV page range...
2017.May.03 23:39:30.366 [dexcom-uploader] DEBUG - Read 14 byte(s) complete.
2017.May.03 23:39:30.532 [dexcom-uploader] DEBUG - Reading 1 EGV page(s)...
2017.May.03 23:39:30.535 [dexcom-uploader] DEBUG - Reading #0 EGV pages (page number 4493)
2017.May.03 23:39:30.572 [dexcom-uploader] DEBUG - Read 534 byte(s) complete.
2017.May.03 23:39:32.816 [dexcom-uploader] DEBUG - Read complete of EGV pages.
2017.May.03 23:39:32.818 [dexcom-uploader] DEBUG - Reading Meter page...
2017.May.03 23:39:32.986 [dexcom-uploader] DEBUG - Read 534 byte(s) complete.
2017.May.03 23:39:33.635 [dexcom-uploader] DEBUG - Reading Sensor page range...
2017.May.03 23:39:33.761 [dexcom-uploader] DEBUG - Reading 1 Sensor page(s)...
2017.May.03 23:39:33.763 [dexcom-uploader] DEBUG - Read complete of Sensor pages.
2017.May.03 23:39:33.765 [dexcom-uploader] DEBUG - Reading Cal Records page range...
2017.May.03 23:39:33.899 [dexcom-uploader] DEBUG - Calibration range is -1, this is probably a Dexcom G5 device
2017.May.03 23:39:33.901 [dexcom-uploader] DEBUG - Reading system time...
2017.May.03 23:39:34.032 [dexcom-uploader] DEBUG - Reading display time offset...
2017.May.03 23:39:34.162 [dexcom-uploader] DEBUG - Reading system time...
2017.May.03 23:39:34.417 [dexcom-uploader] INFO - Dexcom G4/G5 device disconnected
2017.May.03 23:39:35.094 [dexcom-uploader] DEBUG - The download run completed with a status of SUCCESS.
2017.May.03 23:39:35.100 [dexcom-uploader] INFO - The most recent sgv is **114 mg/dl** at Wed May 03 23:35:56 EDT 2017
2017.May.03 23:39:35.105 [dexcom-uploader] INFO - The next run will occur in 203 seconds
```