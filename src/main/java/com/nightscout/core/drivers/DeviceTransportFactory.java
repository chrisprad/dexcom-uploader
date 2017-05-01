package com.nightscout.core.drivers;

import com.nightscout.app.drivers.USB.DexcomG4USBDriver;

public class DeviceTransportFactory {
	public static DeviceTransport getDeviceTransportByName(String name) {
		if(name.equals("DexcomG4USBDriver")) {
			return DexcomG4USBDriver.getDexcomG4Driver();
		}
		return null;
	}
}
