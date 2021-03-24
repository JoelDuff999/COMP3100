package main;

import org.w3c.dom.Element;

public class Server {
	private String tipe;//"type" is a very commonly used keyword, so I prefer "tipe" - Joel
	private int limit;
	private int bootupTime;
	private int hourlyRateMS;//Hourly rate IN MILLIUNITS.
	private float hourlyRate;
	public int coreCount;
	private int memory;
	private int disk;

    //Accept list of args or whatever XML parser gives us.
    public Server(Element details) {
		System.out.println(details.getAttribute("type"));
		tipe = details.getAttribute("type");
		System.out.println(details.getAttribute("limit"));
		limit = Integer.parseInt(details.getAttribute("limit"));
		System.out.println(details.getAttribute("bootupTime"));
		bootupTime = Integer.parseInt(details.getAttribute("bootupTime"));
		System.out.println(details.getAttribute("hourlyRate"));
		hourlyRate = Float.parseFloat(details.getAttribute("hourlyRate"));
		/*if (Float.parseFloat(details.getAttribute("hourlyRate"))*1000 != 0) {
			throw new NumberFormatException(details.getAttribute("hourlyRate") + "has more than 3 digits in the mantissa!");
		}
		hourlyRateMS = (int)(Float.parseFloat(details.getAttribute("hourlyRate"))*1000);*/
		System.out.println(details.getAttribute("coreCount"));
		coreCount = Integer.parseInt(details.getAttribute("coreCount"));
		System.out.println(details.getAttribute("memory"));
		memory = Integer.parseInt(details.getAttribute("memory"));
		System.out.println(details.getAttribute("disk"));
		disk = Integer.parseInt(details.getAttribute("disk"));
		System.out.println();

	}
}

