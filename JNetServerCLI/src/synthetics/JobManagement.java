package synthetics;
import java.math.BigInteger;
import java.util.ArrayList;

import synthetics.net.Client;

public class JobManagement implements Runnable {
	@SuppressWarnings("rawtypes")
	public static volatile ArrayList incomplete = new ArrayList();
	@SuppressWarnings("rawtypes")
	public static volatile ArrayList requests = new ArrayList();
	@SuppressWarnings("rawtypes")
	public static ArrayList toRemove = new ArrayList();
	public static BigInteger startCrackNumber = BigInteger.valueOf(0);
	private static Client current;
	//public static Client c;
	private float clientRate;
	private float totalRate;
	private long range;
	private BigInteger[] storage = new BigInteger[2];
	private boolean cracking = false;
	public JobManagement() {

	}
	@SuppressWarnings("unchecked")
	public void run() {
		while (true) {
			try {
				double clientPercent = 0;
				if (cracking != Startup.cracking) {
					if (!Startup.cracking) {
						requests.clear();
						incomplete.clear();
					}
					cracking = Startup.cracking;
				}
				Thread.sleep(25);
				if (requests.size() != 0) {
					for (Object s : requests) {
						toRemove.add(s);
						current = (Client)s;
						clientRate = current.clientHashRate;
						totalRate = Startup.getHashRate();
						if (incomplete.size() == 0) {
							//if (current.average != -1) {
							//	clientRate = current.average;
							//}
							int conSize = Startup.clients.size();
							
							//DEPRECATED SERVER-SIDE ZERO CHECK
							if(clientRate == 0 || totalRate == 0) {
								totalRate = (float) (2400 * conSize);
								clientRate = 2400;
							}
							if(clientRate == 0) {
								System.out.println("CAUGHT ZERO!");
							}
							
							double shouldRate = (totalRate / conSize);
							clientPercent = clientRate / shouldRate;
							range = (long) (clientPercent * 10350000);
							System.out.println("========================");
							//if(totalRate == 2300) {
							//	System.out.println("Total Rate: ARTIFICIAL");
							//} else {
							//	System.out.println("Total Rate: " + totalRate);
							//}
							System.out.println(Startup.clients.size() + " Client(s)");
							//System.out.println("Should Be: " + shouldRate);
							//System.out.println("Total :" + totalRate);
							//System.out.println("Client: " + clientRate);
							//System.out.println("Client Actually: " + clientPercent * 100 + "%");
							System.out.println("Range: " + range);
							System.out.println("Rate: " + totalRate);
							current.bounds[0] = startCrackNumber;
							current.bounds[1] = startCrackNumber.add(BigInteger.valueOf(range));
							startCrackNumber = current.bounds[1];
							current.hasBlock = true;
						} else {
							storage = (BigInteger[]) incomplete.get(0);
							current.bounds[0] = storage[0];
							current.bounds[1] = storage[1];
							current.hasBlock = true;
							incomplete.remove(0);
							System.out.println("Recycled rejected block with range of " + storage[1].subtract(storage[0]));
						}
					}
					for (Object s : toRemove) {
						requests.remove(s);
					}
					toRemove.clear();
				}
			} catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	public void resetStart() {
		startCrackNumber = BigInteger.valueOf(0);
		clientRate = 0;
		totalRate = 0;
	}
}
