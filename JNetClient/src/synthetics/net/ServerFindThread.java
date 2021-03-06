package synthetics.net;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import synthetics.Main;

public class ServerFindThread implements Runnable {

	public static String ownIP;
	public static String rawIP;
	InetAddress current_addr;
	public static boolean gotServer = false;
	
	public void run() {
		try {
			current_addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) { e.printStackTrace();}
		System.out.println("Server Location Started");
		ownIP = current_addr.getHostAddress();
		System.out.println("Client IP: [" + ownIP + "]");
		System.out.println("[Scanning...]");
		rawIP = stripIP(ownIP);
		loopDetect(rawIP);
	}
	
	public static String stripIP(String mainIP) {
		String[] octets = mainIP.split("\\.");
		String output = octets[0] + "." + octets[1] + ".";
		return output;
	}

	public static void loopDetect(String incIP) {
		while(Main.hostIp == null || Main.hostPort == 0) {
			for(int i=0; i<=255; i++) {
				String firstProcessed = incIP + i + ".";
				for(int j=0; j<=255; j++) {
					System.out.println(firstProcessed + j);
					try {
						Socket detectSocket = new Socket();
						SocketAddress detectAddress = new InetSocketAddress(firstProcessed+j, 1800);
						detectSocket.connect(detectAddress, 5);
						detectSocket.close();
						System.out.println("Server Found: [" + firstProcessed + j + "]");
						Main.hostIp = incIP+i;
						Main.hostPort = 1800;
						gotServer = true;
						break;
					} catch (IOException e) {}
				}
			}
		}
	}
}
