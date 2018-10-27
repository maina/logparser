package com.wallethub.logparser;

public class Main {

	public static void main(String[] args) {
		try {
			String strStartDate = null;
			String duration = null;// can take only "hourly", "daily"
			String threshold = null;// can be an integer.
			String accesslog = null;
			if (args != null && args.length > 0) {

				for (int i = 0; i < args.length; i++) {
					System.out.println(" args " + args[i]);
					if (args[i].toLowerCase().contains("startdate")) {
						strStartDate = args[i].substring(args[i].indexOf("=") + 1);
					} else if (args[i].toLowerCase().contains("duration")) {
						duration = args[i].substring(args[i].indexOf("=") + 1);
					} else if (args[i].toLowerCase().contains("threshold")) {
						threshold = args[i].substring(args[i].indexOf("=") + 1);
					} else if (args[i].toLowerCase().contains("accesslog")) {
						accesslog = args[i].substring(args[i].indexOf("=") + 1);
					}
				}

			} else {
				System.out.println("No Arguments passed");
			}

			FileReader.readFile(strStartDate, duration, threshold, accesslog);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
