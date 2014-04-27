package be.kuleuven.cs.chikwadraat.socialfridge.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

import be.kuleuven.cs.chikwadraat.socialfridge.Application;

/**
 * Created by Mattias on 25/04/2014.
 */
public class NetworkUtils {

    public static InetAddress getPublicAddress() {
        assert Application.isDevelopment();
        try {
            // Find address of a network interface
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface current : Collections.list(interfaces)) {
                if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
                Enumeration<InetAddress> addresses = current.getInetAddresses();
                for (InetAddress address : Collections.list(addresses)) {
                    if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                        return address;
                    }
                }
            }
            // Use local host as fallback
            return InetAddress.getLocalHost();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        // Use loop back as fallback
        return InetAddress.getLoopbackAddress();
    }

}
