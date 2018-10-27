package com.wallethub.logparser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.wallethub.logparser.model.AccessLog;

public class FileReader {
	static DateFormat yyyymmddhhmmssSSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	static DateFormat yyyymmddhhmmss = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");

	public static void readFile(String strStartDate, String duration, String strThreshold, String path) throws Exception {
		// Get file from resources folder
		InputStream fileInputStream = null;
		BufferedReader reader = null;
		try {
			if (path == null || path.isEmpty()) {
				System.out.println("Invalid file path.");
				return;
			}
			if (!DbHelper.checkifFileAlreadyProcessed(path)) { // read only files not read before based on file path

				fileInputStream = new FileInputStream(path);
				reader = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));
				List<AccessLog> logs = new ArrayList<AccessLog>();
				while (true) {
					String line = reader.readLine();
					// System.out.println("Processing line " + line);
					if (line == null) {
						break;
					}
					AccessLog log = processFields(line);
					log.setSrcFile(path);
					logs.add(log);
				}

				DbHelper.saveAccessLogs(logs);
			} else {
				System.out.println("File already processed.");
			}
			filterData(strStartDate, duration, strThreshold);
		} finally {

			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
	}

	private static void filterData(String strStartDate, String duration, String strThreshold) throws Exception {
		Date startDate = yyyymmddhhmmss.parse(strStartDate);
		Date endDate = null;
		Integer threshold = Integer.valueOf(strThreshold);
		if (duration.equalsIgnoreCase("hourly")) {
			Calendar cal = Calendar.getInstance(); // creates calendar
			cal.setTime(startDate); // sets calendar time/date
			cal.add(Calendar.HOUR_OF_DAY, 1); // adds one hour
			endDate = cal.getTime(); //
		} else if (duration.equalsIgnoreCase("daily")) {
			Calendar cal = Calendar.getInstance(); // creates calendar
			cal.setTime(startDate); // sets calendar time/date
			cal.add(Calendar.DATE, 1); // adds one hour
			endDate = cal.getTime(); //
		}

		List<Object[]> logs = DbHelper.selectLogsBetweenDates(yyyymmddhhmmss.format(startDate), yyyymmddhhmmss.format(endDate), threshold);
		Iterator it = logs.iterator();
		while (it.hasNext()) {
			Object[] line = (Object[]) it.next();
			System.out.println("IP " + line[0] + " Count " + line[1]);

		}

	}

	private static AccessLog processFields(String line) throws Exception {
		String[] fields = line.split("\\|");
		AccessLog log = new AccessLog();
		for (int i = 0; i < fields.length; i++) {
			// System.out.println("Processing field " + fields[i]);
			switch (i) {
			case 0:
				log.setLogDate(yyyymmddhhmmssSSS.parse(fields[0]));
				break;
			case 1:
				log.setIp(fields[1]);
				break;
			case 2:
				log.setRequest(fields[2]);
				break;
			case 3:
				log.setStatus(Integer.valueOf(fields[3]));
				break;
			case 4:
				log.setUserAgent(fields[4]);
				break;

			default:
			}

		}
		return log;
	}

}
