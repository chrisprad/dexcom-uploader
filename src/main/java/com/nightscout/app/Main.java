package com.nightscout.app;

import java.io.IOException;
import java.util.List;

import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usb4java.LibUsb;

import com.nightscout.app.drivers.LinuxUploaderDevice;
import com.nightscout.app.drivers.USB.DexcomG4USBDriver;
import com.nightscout.app.preferences.LinuxNightscoutPreferences;
import com.nightscout.app.upload.Uploader;
import com.nightscout.core.dexcom.Utils;
import com.nightscout.core.drivers.AbstractUploaderDevice;
import com.nightscout.core.drivers.DeviceTransportFactory;
import com.nightscout.core.drivers.DexcomG4;
import com.nightscout.core.model.DownloadResults;
import com.nightscout.core.model.DownloadStatus;
import com.nightscout.core.model.SensorGlucoseValueEntry;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

		logger.info("Nightscout Dexcom Uploader");

		AbstractUploaderDevice uploaderDevice = LinuxUploaderDevice.getUploaderDevice();
		DexcomG4USBDriver usbDevice = null;

		long nextUpload = 300000;
		LinuxNightscoutPreferences prefs;

		try {
			prefs = new LinuxNightscoutPreferences("config.properties");
		} catch (IOException e1) {
			logger.error("An error occurred while reading the config.properties file", e1);
			return;
		}
		Uploader uploader = new Uploader(prefs);
		logger.debug("Initializing LibUSB");
		LibUsb.init(null);
		// TODO: check override flag from Preferences
		if (LibUsb.hasCapability(LibUsb.CAP_SUPPORTS_DETACH_KERNEL_DRIVER) || prefs.isForceInitialDetach()) {
			logger.info("Attempting to detach the ACM kernel driver");
			while (true) {
				try {
					DexcomG4USBDriver.DetachACMDriver();
					break;
				} catch (UsbDisconnectedException ex) {
					logger.info("The Dexcom G4/G5 device does not appear to be connected. Trying again in 60 seconds");
					Sleep(60000);
				} catch (UsbException ex) {
					logger.error("An error occurred while detaching the USB driver");
					ex.printStackTrace();
				}
			}
		}
		DexcomG4 device = null;
		while (true) {
			if (device == null) { // disconnected
				usbDevice = (DexcomG4USBDriver) DeviceTransportFactory.getDeviceTransportByName("DexcomG4USBDriver");

				if (usbDevice == null) {
					System.out.println(
							"The Dexcom G4/G5 device appears to be unplugged. Please ensure that the device is connected.");
					Sleep(60000);
					continue;
				}
				device = new DexcomG4(usbDevice, prefs, uploaderDevice);
			}

			try {
				logger.debug("Starting download run...");
				device.setNumOfPages(1);
				DownloadResults res = device.download();
				if (res.getDownloadStatus() == DownloadStatus.SUCCESS) {
					nextUpload = res.getNextUploadTime();
					if (nextUpload < 0)
						nextUpload = 30000; //missed reading? try again in 30 seconds
					else
						nextUpload += 2000; //push forward 2 seconds to ensure the next reading comes in
					logger.debug("The download run completed with a status of {}.",
							res.getDownload().download_status.toString());
					if (!res.getDownload().sgv.isEmpty()) {
						List<SensorGlucoseValueEntry> sgvs = res.getDownload().sgv;
						if (sgvs != null && sgvs.size() > 0) {
							SensorGlucoseValueEntry latestSgv = sgvs.get(sgvs.size() - 1);
							logger.info("The most recent sgv is {} mg/dl at {}", latestSgv.sgv_mgdl,
									Utils.receiverTimeToDate(latestSgv.disp_timestamp_sec).toString());
						}
					}
					logger.info("The next run will occur in {} seconds", nextUpload / 1000);
					uploader.upload(res, 1);
				}
			} catch (Exception ex) {
				// assume a device communication error occurred
				device = null;
			} finally {
				try {
					if (usbDevice.isConnected())
						usbDevice.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Sleep(nextUpload);

		}
	}

	static void Sleep(long sleepMS) {
		try {
			Thread.sleep(sleepMS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
