package com.nightscout.app.drivers.USB;

import java.io.IOException;

import javax.usb.UsbConst;
import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbInterfacePolicy;
import javax.usb.UsbNotActiveException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

public class DexcomG4USBDriver extends AbstractUSBDriver {
	public static final int VENDOR_ID = 8867;
	public static final int PRODUCT_ID = 71;
	public static final int DEVICE_CLASS = 2;
	public static final int DEVICE_SUBCLASS = 0;
	public static final int PROTOCOL = 0;

	private UsbInterface controlInterface;
	private UsbInterface dataInterface;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected boolean powerManagementEnabled = false;
	protected boolean forceClaimDevices = false;

	public DexcomG4USBDriver(UsbDevice device) {
		super(device);
	}

	public DexcomG4USBDriver(UsbDevice device, boolean powerManagementEnabled, boolean forceClaimDevices) {
		super(device);
		this.powerManagementEnabled = powerManagementEnabled;
		this.forceClaimDevices = forceClaimDevices;
	}

	public static DexcomG4USBDriver getDexcomG4Driver() {
		UsbDevice usbDevice = AbstractUSBDriver.findUsbDevice(VENDOR_ID, PRODUCT_ID, DEVICE_CLASS, DEVICE_SUBCLASS,
				PROTOCOL);
		if (usbDevice != null) {
			// The Dexcom CGM receiver is currently plugged into the system and
			// recognized
			return new DexcomG4USBDriver(usbDevice);
		}
		return null;
	}

	@Override
	public void open() throws IOException {
		if (connected) {
			logger.warn("driver.open() called when a connection has already been made");
			return;
		}

		if (powerManagementEnabled) {
			USBPower.powerOn();
		}

		logger.debug("claiming interfaces, count=" + device.getActiveUsbConfigurationNumber() + " force claim="
				+ this.forceClaimDevices);

		// TODO: determine if we even need to claim the control interface for
		// the Dexcom G4/G5
		controlInterface = this.getFirstControlInterface();
		// mControlInterface = (UsbInterface)
		// configuration.getUsbInterfaces().get(0);
		logger.debug("Control iface=" + controlInterface);
		// class should be USB_CLASS_COMM
		if(controlInterface.isClaimed())
			logger.warn("Control interface is already claimed, indicating an error occurred on a previous close()");

		try {
			controlInterface.claim((this.forceClaimDevices) ? ForceClaimPolicy : DoNotForceClaimPolicy);
		} catch (UsbNotActiveException | UsbException e) {
			logger.error("An error occurred during the claim operation of the control interface", e);
			e.printStackTrace();
		} catch(UsbDisconnectedException e) {
			logger.warn("The USB device appears to be unplugged.");
			throw new IOException("The USB device appears to be unplugged.");
		}
		
	
		dataInterface = this.getFirstDataInterface();
		logger.debug("data iface=" + dataInterface);
		// class should be USB_CLASS_CDC_DATA
		if(dataInterface.isClaimed())
			logger.warn("Control interface is already claimed, indicating an error occurred on a previous close()");
		try {
			dataInterface.claim((this.forceClaimDevices) ? ForceClaimPolicy : DoNotForceClaimPolicy);
		} catch (UsbNotActiveException | UsbException e) {
			logger.error("An error occurred during the claim operation of the data interface", e);
			e.printStackTrace();
		} catch(UsbDisconnectedException e) {
			logger.warn("The USB device appears to be unplugged.");
			throw new IOException("The USB device appears to be unplugged.");
		}

		readEndpoint = getFirstEndpointByDirection(dataInterface, UsbConst.ENDPOINT_DIRECTION_IN);
		logger.debug("Read endpoint direction: " + readEndpoint.getDirection());
		writeEndpoint = getFirstEndpointByDirection(dataInterface, UsbConst.ENDPOINT_DIRECTION_OUT);
		logger.debug("Write endpoint direction: " + writeEndpoint.getDirection());
		connected = true;
		logger.info("Dexcom G4/G5 device connected successfully");
	}

	@Override
	public void close() throws IOException {
		if (!connected) {
			logger.warn("driver.close() called when the connection has already been closed.");
			return;
		}
		if (controlInterface != null)
			try {
				controlInterface.release();
				controlInterface = null;
			} catch (UsbNotActiveException | UsbException e) {
				logger.error("An error occurred during the release of the claim release of the control interface", e);
				e.printStackTrace();
			} catch(UsbDisconnectedException e) {
				logger.warn("The USB device appears to be unplugged.");
				throw new IOException("The USB device appears to be unplugged.");
			}
		if (dataInterface != null)
			try {
				dataInterface.release();
				dataInterface = null;
			} catch (UsbNotActiveException | UsbException e) {
				logger.error("An error occurred during the release of the claim release of the data interface", e);
				e.printStackTrace();
			} catch(UsbDisconnectedException e) {
				logger.warn("The USB device appears to be unplugged.");
				throw new IOException("The USB device appears to be unplugged.");
			}
		if (powerManagementEnabled) {
			USBPower.powerOff();
		}
		connected = false;
		logger.info("Dexcom G4/G5 device disconnected");
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	public boolean isPowerManagementEnabled() {
		return powerManagementEnabled;
	}

	public void setPowerManagementEnabled(boolean powerManagementEnabled) {
		this.powerManagementEnabled = powerManagementEnabled;
	}

	public boolean isForceClaimDevices() {
		return forceClaimDevices;
	}

	public void setForceClaimDevices(boolean claimDevices) {
		this.forceClaimDevices = claimDevices;
	}

	public static void DetachACMDriver() throws UsbException {
		// find and open the Dexcom G4/G5 device
		int result = LibUsb.init(null);
		if (result != LibUsb.SUCCESS)
			throw new LibUsbException("Unable to initialize libusb.", result);
		Device dexcomDevice = libUSBfindDevice((short) VENDOR_ID, (short) PRODUCT_ID);
		if (dexcomDevice == null)
			throw new UsbDisconnectedException();

		final DeviceHandle handle = new DeviceHandle();
		int openResult = LibUsb.open(dexcomDevice, handle);
		if (openResult < 0) {
			throw new LibUsbException("An error occurred while attempting to open the device using LibUSB", openResult);
		} else {
			// Detach the ACM driver, if it's active
			// 0 is the control interface
			int kernelDriverActive = LibUsb.kernelDriverActive(handle, 0);
			if (kernelDriverActive == LibUsb.ERROR_NO_DEVICE) {
				LibUsb.close(handle);
				throw new LibUsbException(
						"An error occurred while attempting to query the kernel driver from the device using LibUSB",
						kernelDriverActive);
			}
			if (kernelDriverActive == 1) {
				int kernelDriverDetached = LibUsb.detachKernelDriver(handle, 0);
				if (kernelDriverDetached < 0) {
					LibUsb.close(handle);
					throw new LibUsbException(
							"An error occurred while attempting to detach the kernel driver from the device using LibUSB",
							kernelDriverDetached);
				}
			}
			LibUsb.close(handle);
		}
	}

	private static Device libUSBfindDevice(short vendorId, short productId) {
		// Read the USB device list
		DeviceList list = new DeviceList();
		int result = LibUsb.getDeviceList(null, list);
		if (result < 0)
			throw new LibUsbException("Unable to get device list", result);

		try {
			// Iterate over all devices and scan for the right one
			for (Device device : list) {
				DeviceDescriptor descriptor = new DeviceDescriptor();
				result = LibUsb.getDeviceDescriptor(device, descriptor);
				if (result != LibUsb.SUCCESS)
					throw new LibUsbException("Unable to read device descriptor", result);
				if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId)
					return device;
			}
		} finally {
			// Ensure the allocated device list is freed
			LibUsb.freeDeviceList(list, true);
		}

		// Device not found
		return null;
	}

	protected final UsbInterfacePolicy ForceClaimPolicy = new UsbInterfacePolicy() {
		@Override
		public boolean forceClaim(UsbInterface usbInterface) {
			return true;
		}
	};
	protected final UsbInterfacePolicy DoNotForceClaimPolicy = new UsbInterfacePolicy() {
		@Override
		public boolean forceClaim(UsbInterface usbInterface) {
			return false;
		}
	};
}
