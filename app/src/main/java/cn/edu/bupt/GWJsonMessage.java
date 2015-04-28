package cn.edu.bupt;

public class GWJsonMessage{
	public String date = null,
			serverDate = null;
	public boolean outmessage = false;
	public Note note = null;
	
	@Override
	public String toString() {
		return "{date: " + date + ", " +
				"note: " + note + ", " +
				"outmessage: " + outmessage + ", " +
				"serverDate: " + serverDate + "}";
	}
	
	public class Note{
		public String leftmoeny = null,
				overdate = null,
				service = null,
				status = null,
				welcome = null;
		public int onlinestate = 0;

		@Override
		public String toString() {
			return "{leftmoeny: " + leftmoeny + ", " +
					"onlinestate: " + onlinestate + ", " +
					"overdate: " + overdate + ", " +
					"service: " + service + ", " +
					"status: " + status + ", " +
					"welcome: " + welcome + "}";
		}
	}

}
