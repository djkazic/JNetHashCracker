package synthetics;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;

import synthetics.net.Client;
import synthetics.net.Listener;

public class Startup {
	@SuppressWarnings("rawtypes")
	public static ArrayList clients = new ArrayList();
	@SuppressWarnings("rawtypes")
	public static ArrayList clientMutexes = new ArrayList();
	public static Listener l;
	public static Client c;
	@SuppressWarnings("rawtypes")
	public static ArrayList listeners = new ArrayList();
	@SuppressWarnings("rawtypes")
	public static ArrayList listenerClasses = new ArrayList();
	public static long initiateTime = System.currentTimeMillis();
	public static boolean cracking = false;
	public static String currentHashString = "";
	public static boolean crackNumbers = false;
	public static boolean crackUpperCase = false;
	public static boolean crackSpecial = false;
	static Scanner in = new Scanner(System.in);
	public static int threads = 0;
	public static char[] array;
	public static JobManagement jb;
	public static String status;
	public static boolean devMode;
	public static boolean killAll = false;
	public static String killID = null;
	public static boolean killSwitch = false;
	public static Client killTarget;
	
	public static String genSequenceFromBigInt(BigInteger num) {
		StringBuilder sb = new StringBuilder();
		while(num.compareTo(BigInteger.ZERO) == 1) {
			int tempNum = num.mod(BigInteger.valueOf(array.length)).intValue();
			if (tempNum == 0) {
				tempNum += 1;
			}
			sb.append(tempNum);
			num = num.divide(BigInteger.valueOf(array.length));
		}
		return sb.reverse().toString();
	}
	public static String getLowest() {
		BigInteger lowest = BigInteger.valueOf(-1);
		for (int i = 0; i < clients.size(); i++) {
			c = (Client)clients.get(i);
			if (c.isConnected()) {
				if (lowest.compareTo(BigInteger.valueOf(-1)) == 0) {
					lowest = c.clientLast;
				}
				if (c.clientLast.compareTo(lowest) == -1) {
					lowest = c.clientLast;
				}
			}
		}
		return genSequenceFromBigInt(lowest);
	}
	
	public static byte[] dehexify(String hexString) {
		if (hexString.length()%2 == 1)
			throw new IllegalArgumentException("Invalid length");       
		int len = hexString.length()/2;
		byte[] bytes = new byte[len];
		for (int i=0; i<len; i++) {
			int index = i*2;
			bytes[i] = (byte)Integer.parseInt(hexString.substring(index, index+2), 16);
		}
		return bytes;
	}
	
	@SuppressWarnings({ "unchecked" })
	public static void main (String[] args) throws InterruptedException {
		out("[JN]: J-Net Distributed MD5 Hash Cracker");
		out("> Initializing...");
		out("> Type help for information");
		jb = new JobManagement();
		(new Thread(jb)).start();
		(new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						if (cracking) {
							int total = 0;
							for (int i = 0; i < clients.size(); i ++) {
								c = (Client)clients.get(i);
								if (c.isConnected()) {
									total += c.clientHashRate;
								}
							}
							if (total != 0) {
								//System.out.println("Speed: " + total + "KH/s Sequence: " + getLowest());
							}
						}
					} catch (Exception e) {  }
					try { Thread.sleep(1000); } catch (Exception e) {  }
				}
			}
		})).start();
		while (true) {
			String command = in.nextLine();
			int numberNow;
			String[] split = command.split(" ");
			if (split[0].equals("list")) {
				if (Startup.getConnectedNumbers() == 0) {
					out("[JN]: There are no connected clients");
				} else {
					for (int i = 0; i < Startup.clients.size(); i ++) {
						Startup.c = (Client)Startup.clients.get(i);
						if (Startup.c.isConnected()) {
							out("     [" + Startup.c.clientID + "] Client " + Startup.c.cs.getInetAddress().getHostAddress() + " " + Startup.c.clientHashRate + "kH/s" + " || Hashing: " + Startup.c.isCracking);
						}
					}
				}
			} else if (split[0].equals("listener")) {
				split = command.split(" ");
				try {
					if (split[1].equals("add")) {
						try {
							numberNow = Integer.parseInt(split[2]);
							if (!listeners.contains(numberNow)) {
								listeners.add(numberNow);
								out("[JN]: Added listener " + numberNow);
								addListener(numberNow);
							} else {
								out("[JN]: Listener " + numberNow + " already exists");
							}
						} catch (Exception e2) { out("Please use a number as an argument"); }
					} else if (split[1].equals("del")) {
						try {
							numberNow = Integer.parseInt(split[2]);
							if (!listeners.contains(numberNow)) {
								out("[JN]: Listener " + numberNow + " does not exist");
							} else {
								Startup.listeners.remove((Object)numberNow);
								out("[JN]: Deleted listener " + numberNow);
								Startup.delListener(numberNow);
							}
						} catch (Exception e2) { e2.printStackTrace(); out("[JN]: Please use a number as an argument"); }
					} else if (split[1].equals("list")) {
						if (Startup.listeners.size() == 0) {
							System.out.println("There are no listeners");
						} else {
							for (int i = 0; i < Startup.listeners.size(); i ++) {
								out(Startup.listeners.get(i) + "");
							}
						}
					} else {
						out("[JN]: Accepted arguments for listener (add|del|list) number");
					}
				} catch (Exception e2) { out("[JN]: Accepted arguments for listener (add|del|list) number"); }
			} else if(split[0].equals("killall")) {
				while(clients.size() > 0) {
					killAll = true;
				}
				killAll = false;
			} else if(split[0].equals("kill")) {
				split = command.split(" ");
				try {
					Startup.killID = split[1];
					while(Startup.killTarget == null) {
						Startup.killSwitch = true;
					}
					Startup.killSwitch = false;
				} catch (Exception noID) { out("[JN]: The correct format for the kill command is kill (ID)");}
			} else if(split[0].equals("devmode")) {
				split = command.split(" ");
				try{
						Startup.devMode = true;
				} catch (Exception dMode) { out("[JN]: Dev mode has no arguments"); }
			} else if (split[0].equals("rate")) {
				out(Startup.getHashRate() + "kH/s");
			} else if (split[0].equals("stop")) {
				Startup.stopCracking();
			} else if (split[0].equals("crack")) {
				try {
					if (!Startup.cracking) {
						Startup.currentHashString = split[1];
						Startup.array = Startup.comblist;
					}
					jb.resetStart();
					Startup.startCracking();
				} catch (Exception e2) {
					out("[JN]: crack (hash)");
				}
			}
			else if (split[0].equals("status")) {
				if (Startup.cracking) {
					out("[JN]: Solving " + " @" + Startup.getHashRate() + "kH/s for " + (Float.valueOf(System.currentTimeMillis() - Startup.initiateTime)/1000F) + "sec");
				} else {
					out("[JN]: Not solving a hash");
				}
			}
			else if (split[0].equals("help")) {
				out("[JN]:  ==============[J-Net Commands]===============");
				out("> listener (add|del|list) number - adds a port to listen");
				out("> rate - retrieves hash rate (kH/s)");
				out("> crack (hash) (start length)");
				out("> list - lists connected clients");
				out("> stop - stops cracking");
				out("> status - gives a status on the current crack");
			} else {
				out("[JN]: Unknown command. Try help");
			}
		}
	}
		

	public static int getConnectedNumbers() {
		int num = 0;
		for (int i = 0; i < clients.size(); i ++) {
			c = (Client)clients.get(i);
			if (c.isConnected()) {
				num += 1;
			}
		}
		return num;
	}
	
	public static void stopCracking() {
		if (cracking) {
			cracking = false;
			out("[JN]: Stopped cracking");
		} else {
			out("[JN]: The hash solver is not cracking");
		}
		jb.resetStart();
		resetHashRate();
	}
	
	public static void startCracking() {
		if (cracking) {
			out("[JN]: Already solving a hash");
		} else {
			initiateTime = System.currentTimeMillis();
			cracking = true;
			out("[JN]: Attempting to solve hash:");
			out("       [" + currentHashString + "]");
		}
	}
	
	public static float getHashRate() {
		float i = 0;
		for (int i2 = 0; i2 < clients.size(); i2 ++) {
			c = (Client)clients.get(i2);
			if (c.connected) {
				i += c.clientHashRate;
			}
		}
		return i;
	}
	
	public static void resetHashRate() {
		for(int i=0; i < clients.size(); i++) {
			c = (Client)clients.get(i);
			c.clientHashRate = 0;
		}
	}
	
	public static void delListener(int inumber) {
		try {
			for (int i = 0; i < listenerClasses.size(); i ++) {
				l = (Listener)listenerClasses.get(i);
				if (l.port == inumber) {
					l.stop();
					l.ss.close();
					listenerClasses.remove(i);
					for (int i2 = 0; i2 < listeners.size(); i2 ++) {
						if (l.port == (Integer)listeners.get(i2)) {
							listeners.remove(i2);
							break;
						}
					}
					break;
				}
			}
		} catch (Exception e) {  }
	}
	public static void success(String hash) {
		out("[JN] Solved hash as " + hash);
		out("        Time: " + (System.currentTimeMillis() - initiateTime) + "ms");
		stopCracking();
	}
	@SuppressWarnings("unchecked")
	public static void addListener(int inumber) {
		l = new Listener(inumber);
		listenerClasses.add(l);
		Thread t = new Thread(l);
		t.start();
	}
	
	public static void out(String output) {
		System.out.println(output);
	}
	
	public static String toHex(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
	public static char[] comblist = new char[] {'a','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','1','2','3','4','5','6','7','8','9','0','!','�','$','%',',','.','?','@','#'};
	public static String getStatus() {
		if(cracking)
		{
			status = "CRACKING";
		} else
		{
			status = "IDLE";
		}
		return status;
	}
}