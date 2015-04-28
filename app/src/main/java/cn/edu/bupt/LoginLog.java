package cn.edu.bupt;

import java.util.List;

public class LoginLog {
	
	// summary logs of this day or month
	public double upload = 0.0,
			download = 0.0,
			total = 0.0,
			fees = 0.0;
	public Integer minutes = 0;
	
	// detail logs of each login & logout
	public List<LogItem> logs = null;
	
	@Override
	public String toString() {
		return "{upload: " + upload + ", " +
				"download: " + download + ", " +
				"total: " + total + ", " +
				"fees: " + fees + ", " +
				"minutes: " + minutes + "}\n" +
				logs.toString();
	}
	
	public class LogItem{
		public String loginTime = null,
				logoutTime = null,
				ip = null;
		public double upload = 0.0,
				download = 0.0,
				total = 0.0,
				fees = 0.0;
		public Integer minutes = 0;
		
		@Override
		public String toString() {
			return "{loginTime: " + loginTime + ", " +
					"logoutTime: " + logoutTime + ", " +
					"minutes: " + minutes + ", " +
					"total: " + total + ", " +
					"fees: " + fees + ", " +
					"upload: " + upload + ", " +
					"download: " + download + ", " +
					"ip: " + ip + "}\n";
		}
	}
	
	public enum LogType{
		DAY,
		MONTH
	}
}