package cn.edu.bupt;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cn.edu.bupt.LoginLog.LogItem;
import cn.edu.bupt.LoginLog.LogType;

import com.google.gson.Gson;

public class GWSelf {
	private Map<String, String> cookies = null;
	private Map<String, String> ipMap = null;
	private GWJsonMessage info = null;
	
	public void login(String account, String password) throws IOException{
		String url = null, body = null, checkcode = null;
		Map<String, String> data = new HashMap<String, String>();
		cookies = new HashMap<String, String>();
		Response res = null;
		
		url = "http://gwself.bupt.edu.cn/nav_login";
		res = Jsoup.connect(url).method(Method.GET).execute();
		body = res.body();
		cookies = res.cookies();
		checkcode = getCheckCode(body);
		
		url = "http://gwself.bupt.edu.cn/RandomCodeAction.action?" +
				"randomNum=" + Math.random();
		URL target = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) target.openConnection();
		conn.setRequestProperty("Cookie", convertMapToString(cookies));
		conn.getInputStream().close();
		conn.disconnect();

		url = "http://gwself.bupt.edu.cn/LoginAction.action";
		data.put("account", account);
		data.put("password", md5(password));
		data.put("code", "");
		data.put("checkcode", checkcode);
		data.put("Submit", "登 录");
		res = Jsoup.connect(url).cookies(cookies)
				.method(Method.POST).data(data).execute();
		info = getInformation();
	}
    public static String md5(String s)
    {
        MessageDigest digest;
        try
        {
            digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes(),0,s.length());
            String hash = new BigInteger(1, digest.digest()).toString(16);
            return hash;
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return "";
    }
	private String getCheckCode(String html){
		String ans = null, prefix = "checkcode=\"", postfix = "\";";
		ans = html.substring(html.indexOf(prefix) + prefix.length());
		ans = ans.substring(0, ans.indexOf(postfix));
		return ans;
	}
	
	private String convertMapToString(Map<String, String> cookies)
			throws UnsupportedEncodingException{
		String ans = "";
		for(String key: cookies.keySet()){
			ans += "&" + key + "="
					+ URLEncoder.encode(cookies.get(key), "UTF-8");
		}
		ans = ans.substring(1);
		return ans;
	}
	
	public GWJsonMessage getInformation() throws IOException{
		String url = "http://gwself.bupt.edu.cn/refreshaccount?t="
				+ Math.random();
		info = new Gson().fromJson(getJson(url, cookies), GWJsonMessage.class);
		return info;
	}
	
	private String getJson(String url, Map<String, String> cookies)
			throws IOException{
		URL target = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) target.openConnection();
		conn.setRequestProperty("Cookie", convertMapToString(cookies));
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(conn.getInputStream()));
		String line = null;
		StringBuilder response = new StringBuilder();
		while((line = reader.readLine()) != null){
			response.append(line);
		}
		reader.close();
		conn.disconnect();
		return response.toString();
	}
	
	public List<String> getOnlineIps() throws IOException{
		String url = "http://gwself.bupt.edu.cn/nav_offLine";
		Document doc = Jsoup.connect(url).cookies(cookies).get();
		Elements elements = doc.select("tbody > tr");
		ipMap = new HashMap<String, String>();
		for(Element e: elements){
			String ip = e.child(0).ownText().replaceAll("\u00a0", ""),
					fldsessionid = e.child(3).ownText();
			ipMap.put(ip, fldsessionid);
		}
		return new ArrayList<String>(ipMap.keySet());
	}
	
	public GWJsonMessage forceOffline(String ip) throws IOException{
		String url = "http://gwself.bupt.edu.cn/tooffline?t="
				+ Math.random() + "&fldsessionid=" + ipMap.remove(ip);
		return new Gson().fromJson(getJson(url, cookies), GWJsonMessage.class);
	}
	
	public LoginLog getLoginLog(LogType type) throws IOException{
		String url = "http://gwself.bupt.edu.cn/UserLoginLogAction.action";
		Map<String, String> data = new HashMap<String, String>();
		Response res = null;
		data.put("type", type == LogType.DAY ? "1": "2");
		data.put("startDate", info.serverDate.substring(0, 8) + "01");
		data.put("endDate", info.serverDate);
		res = Jsoup.connect(url).cookies(cookies)
				.method(Method.POST).data(data).execute();
		return parseLogDocument(res.parse());
	}
	
	/**
	 * 
	 * @param startDate
	 * for example: 2014-01-01
	 * @param endDate
	 * for example: 2014-12-21
	 * @return
	 * @throws IOException
	 */
	public LoginLog getLoginLog(String startDate, String endDate)
			throws IOException{
		Response res = null;
		String url = "http://gwself.bupt.edu.cn/UserLoginLogAction.action";
		Map<String, String> data = new HashMap<String, String>();
		data.put("type", "4");
		data.put("startDate", startDate);
		data.put("endDate", endDate);
		res = Jsoup.connect(url).cookies(cookies)
				.method(Method.POST).data(data).execute();
		return parseLogDocument(res.parse());
	}
	
	private LoginLog parseLogDocument(Document doc){
		LoginLog log = new LoginLog();
		Elements data = null;
		data = doc.select("table.table2 font");
        if (data == null || data.isEmpty() || TextUtils.isEmpty(data.get(0).ownText()))
            return log;
		log.download = Double.valueOf(data.get(1).ownText());
		log.total = Double.valueOf(data.get(2).ownText());
		log.fees = Double.valueOf(data.get(3).ownText());
		log.minutes = Integer.valueOf(data.get(4).ownText());
		data = doc.select("table#example > tbody > tr");
		log.logs = new ArrayList<LogItem>();
		for (Element e: data){
			LogItem item = log.new LogItem();
			item.loginTime = e.child(0).ownText();
			item.logoutTime = e.child(1).ownText();
			item.minutes = Integer.valueOf(e.child(2).ownText().trim());
			item.total = Double.valueOf(e.child(3).ownText().trim());
			item.fees = Double.valueOf(e.child(4).ownText().trim());
			item.upload = Double.valueOf(e.child(5).ownText().trim());
			item.download = Double.valueOf(e.child(6).ownText().trim());
			item.ip = e.child(7).ownText();
			log.logs.add(item);
		}
		return log;
	}
	
	public static void main(String[] args) {
		try {
			GWSelf gw = new GWSelf();
			gw.login("xxx", "yyy");
			System.out.println(gw.getInformation());
			List<String> ips = gw.getOnlineIps();
			System.out.println(ips);
//			System.out.println(gw.forceOffline(ips.get(0)));
//			System.out.println(gw.getLoginLog(LogType.DAY));
//			System.out.println(gw.getLoginLog(LogType.MONTH));
			LoginLog log = gw.getLoginLog("2014-01-01", "2014-12-21");
			for(LogItem item: log.logs)
				System.out.println(IPLocationMap.getLocation(item.ip));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
