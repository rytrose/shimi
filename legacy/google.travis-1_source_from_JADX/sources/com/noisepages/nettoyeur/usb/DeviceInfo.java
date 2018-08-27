package com.noisepages.nettoyeur.usb;

import android.hardware.usb.UsbDevice;
import java.io.IOException;
import java.util.Scanner;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class DeviceInfo {
    private final String product;
    private final String vendor;

    public static DeviceInfo retrieveDeviceInfo(UsbDevice device) {
        DeviceInfo info = null;
        try {
            info = retrieveDeviceInfo(device.getVendorId(), device.getProductId());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return info;
    }

    public DeviceInfo(UsbDevice device) {
        this(asFourDigitHex(device.getVendorId()), asFourDigitHex(device.getProductId()));
    }

    private DeviceInfo(String vendor, String product) {
        this.vendor = vendor;
        this.product = product;
    }

    public String getVendor() {
        return this.vendor;
    }

    public String getProduct() {
        return this.product;
    }

    private static String asFourDigitHex(int id) {
        return Integer.toHexString(65536 | id).substring(1);
    }

    private static DeviceInfo retrieveDeviceInfo(int vendorId, int productId) throws ClientProtocolException, IOException {
        String vendorHex = asFourDigitHex(vendorId);
        String productHex = asFourDigitHex(productId);
        String url = "http://usb-ids.gowdy.us/read/UD/" + vendorHex;
        String vendorName = getName(url);
        String productName = getName(new StringBuilder(String.valueOf(url)).append("/").append(productHex).toString());
        if (vendorName == null || productName == null) {
            return null;
        }
        return new DeviceInfo(vendorName, productName);
    }

    private static String getName(String url) throws ClientProtocolException, IOException {
        Scanner scanner = new Scanner(new DefaultHttpClient().execute(new HttpGet(url)).getEntity().getContent());
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            int start = line.indexOf("Name:") + 6;
            if (start > 5) {
                int end = line.indexOf("<", start);
                if (end > start) {
                    return line.substring(start, end);
                }
            }
        }
        return null;
    }

    public String toString() {
        return this.vendor + ":" + this.product;
    }

    public int hashCode() {
        return (this.vendor.hashCode() * 31) + this.product.hashCode();
    }

    public boolean equals(Object o) {
        return (o instanceof DeviceInfo) && ((DeviceInfo) o).vendor.equals(this.vendor) && ((DeviceInfo) o).product.equals(this.product);
    }
}
