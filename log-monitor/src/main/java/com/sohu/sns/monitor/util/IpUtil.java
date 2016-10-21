/**
 * 
 */
package com.sohu.sns.monitor.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author zhouhe
 * @since 下午3:04:24
 */
public class IpUtil {
	private static final String LAN_IP_REGEX = "(10[.]\\d{1,3}[.]\\d{1,3}[.]\\d{1,3})|" +
			"(172[.]((1[6-9])|(2d)|(3[01]))[.]\\d{1,3}[.]\\d{1,3})|" + "(192[.]168[.]\\d{1,3}[.]\\d{1,3})";
	private static volatile InetAddress LOCAL_ADDRESS = null;
	  
    /** 
     * 遍历本地网卡，返回内网IP。 
     *  
     * @return 本地网卡IP 
     */  
    public static InetAddress getLocalAddress() {  
        if (LOCAL_ADDRESS != null) {  
            return LOCAL_ADDRESS;  
        }  
        InetAddress localAddress = getLocalAddress0();  
        LOCAL_ADDRESS = localAddress;  
        return localAddress;  
    }  
      
    private static InetAddress getLocalAddress0() {  
        
        try {  
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();  
            while (interfaces.hasMoreElements()) {  
                NetworkInterface network = interfaces.nextElement();  
                Enumeration<InetAddress> addresses = network.getInetAddresses();  
                while (addresses != null && addresses.hasMoreElements()) {  
                    InetAddress address = addresses.nextElement();  
                    if (address != null && !address.isLoopbackAddress()) {  
  
                        String name = address.getHostAddress();  
                        if (name != null && name.matches(LAN_IP_REGEX)) {  
                            return address;  
                        }  
                    }  
                }  
            }  
        } catch (SocketException e) {  
            throw new RuntimeException(e);  
        }  
        throw new RuntimeException("Could not get local host ip address!");  
    }
}
