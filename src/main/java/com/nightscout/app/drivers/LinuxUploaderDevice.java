package com.nightscout.app.drivers;


import com.nightscout.core.drivers.AbstractUploaderDevice;

public class LinuxUploaderDevice extends AbstractUploaderDevice {

	@Override
	public int getBatteryLevel() {
		// TODO Auto-generated method stub
		return 100;
	}

	public static AbstractUploaderDevice getUploaderDevice() {
		// TODO Auto-generated method stub
		return new LinuxUploaderDevice();
	}
}
