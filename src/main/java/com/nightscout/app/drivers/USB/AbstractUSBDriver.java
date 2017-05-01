package com.nightscout.app.drivers.USB;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotClaimedException;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbPipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.nightscout.core.drivers.DeviceTransport;

public abstract class AbstractUSBDriver implements DeviceTransport {
	
	protected final UsbDevice device;
    protected UsbEndpoint readEndpoint;
    protected UsbEndpoint writeEndpoint;

    protected final Object readBufferLock = new Object();
    protected final Object writeBufferLock = new Object();
    
    protected boolean connected = false;

    public static final int DEFAULT_READ_BUFFER_SIZE = 16 * 1024;
    public static final int DEFAULT_WRITE_BUFFER_SIZE = 16 * 1024;
    
    /**
     * Internal read buffer.  Guarded by {@link #readBufferLock}.
     */
    protected byte[] readBuffer;

    /**
     * Internal write buffer.  Guarded by {@link #writeBufferLock}.
     */
    protected byte[] writeBuffer;
    
    private final Logger logger = LoggerFactory.getLogger(AbstractUSBDriver.class);
    
    public AbstractUSBDriver(UsbDevice device) {
		super();
		this.device = device;
		readBuffer = new byte[DEFAULT_READ_BUFFER_SIZE];
		
	}
    
	@Override
    public byte[] read(int size, int timeoutMillis) throws IOException {
        byte[] data = new byte[size];
        int readSize = read(data, timeoutMillis);
        return Arrays.copyOfRange(data, 0, readSize);
    }
	
    @Override
    public int read(byte[] dest, int timeoutMillis) throws IOException {
        int numBytesRead = 0;
        synchronized (readBufferLock) {
            UsbPipe inPipe = readEndpoint.getUsbPipe();
            try {
				inPipe.open();
			} catch (UsbNotActiveException | UsbNotClaimedException | UsbDisconnectedException | UsbException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}
            try {
				numBytesRead = inPipe.syncSubmit(readBuffer);
			} catch (UsbNotActiveException | UsbNotOpenException | IllegalArgumentException | UsbDisconnectedException
					| UsbException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            finally
            {
            	try {
					inPipe.close();
				} catch (UsbNotActiveException | UsbNotOpenException | UsbDisconnectedException | UsbException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            
            System.arraycopy(readBuffer, 0, dest, 0, numBytesRead);
        }
        return numBytesRead;
    }

    @Override
    public int write(byte[] src, int timeoutMillis) throws IOException {

        	int amtWritten = 0;
        	UsbPipe outPipe = writeEndpoint.getUsbPipe();
        	try {
				outPipe.open();
			} catch (UsbNotActiveException | UsbNotClaimedException | UsbDisconnectedException | UsbException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	synchronized (writeBufferLock) {
        		try {
					amtWritten = outPipe.syncSubmit(src);
				} catch (UsbNotActiveException | UsbNotOpenException | IllegalArgumentException
						| UsbDisconnectedException | UsbException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		finally {
        			try {
						outPipe.close();
					} catch (UsbNotActiveException | UsbNotOpenException | UsbDisconnectedException | UsbException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        	}
        	
        	logger.debug("Wrote amt=" + amtWritten);
            return amtWritten;
    }
    protected UsbInterface getFirstControlInterface() {
    	return getFirstControlInterface(device.getActiveUsbConfiguration());
    }
    protected UsbInterface getFirstControlInterface(UsbConfiguration configuration) {
    	return getFirstInterfaceByType(configuration,(byte)0x02);
    }
    protected UsbInterface getFirstDataInterface() {
    	return getFirstDataInterface(device.getActiveUsbConfiguration());
    }
    protected UsbInterface getFirstDataInterface(UsbConfiguration configuration) {
    	return getFirstInterfaceByType(configuration,(byte)0x0A);
    }
    protected UsbInterface getFirstInterfaceByType(UsbConfiguration configuration, byte endpointType) {
    	for(UsbInterface iface : Lists
				.newArrayList(Iterables.filter(configuration.getUsbInterfaces(), UsbInterface.class)))
    		if(iface.getUsbInterfaceDescriptor().bInterfaceClass() == endpointType)
    			return iface;
    	return null;
    }
    protected UsbEndpoint getFirstEndpointByDirection(UsbInterface usbInterface, byte direction) {
    	for(UsbEndpoint endpoint : Lists.newArrayList(Iterables.filter(usbInterface.getUsbEndpoints(), UsbEndpoint.class)))
    			if(endpoint.getDirection() == direction)
    				return endpoint;
    	return null;
    }
    public static UsbDevice findUsbDevice(int vendorId, int productId, int deviceClass, 
    		int subClass, int protocol) {
        //find and iterate the root USB hub of the system
        UsbHub rootHub = null;
		try {
			rootHub = UsbHostManager.getUsbServices().getRootUsbHub();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UsbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return findUsbDevice(rootHub, vendorId, productId, deviceClass, subClass, protocol);
    }

	public static UsbDevice findUsbDevice(UsbHub usbHub, int vendorId, int productId, int deviceClass, int subClass,
			int protocol) {

		List<UsbDevice> deviceList = Lists
				.newArrayList(Iterables.filter(usbHub.getAttachedUsbDevices(), UsbDevice.class));

		for (UsbDevice device : (List<UsbDevice>) deviceList) {
			if (device.isUsbHub()) {
				UsbDevice hubDevice = findUsbDevice((UsbHub) device, vendorId, productId, deviceClass, subClass,
						protocol);
				if (hubDevice != null)
					return hubDevice;
				else
					continue;
			} else {
				UsbDeviceDescriptor deviceDescriptor = device.getUsbDeviceDescriptor();
				if (deviceDescriptor.idVendor() == vendorId && deviceDescriptor.idProduct() == productId
						&& deviceDescriptor.bDeviceClass() == deviceClass
						&& deviceDescriptor.bDeviceSubClass() == subClass
						&& deviceDescriptor.bDeviceProtocol() == protocol) {
					return device;
				}
			}
		}
		// Device not found
		return null;
	}
}
