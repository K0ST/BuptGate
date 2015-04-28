package pro.kost.bupt.network;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import pro.kost.bupt.Utils;

public class SimpleLogin {
    private List<String> cookies;
    private HttpURLConnection conn;
    String url = "http://gw.bupt.edu.cn/a11.htm";
    String suffix = "/a11.htm";
    public static String ok = "http://10.3.8.211";
    public static String ok2 = "http://10.4.1.2";
    public void login(final String name,final String password){

        new Thread(new Runnable() {
            public void run() {
                try {
                    Response response = Jsoup.connect("http://www.baidu.com").execute();
                    if (!response.url().toString().equals("http://www.baidu.com"))
                        ok = response.url().toString();
                    SimpleLogin http = new SimpleLogin();
                    CookieHandler.setDefault(new CookieManager());
                    url = ok + suffix;
                    System.out.println("url = " + url);
                    String page = http.GetPageContent(url);
                    String postParams = http.getFormParams(page, name, password);
                    http.sendPost(url, postParams);
                    String result = http.GetPageContent(ok);
                    decodeResult(result);
                }catch (Exception e) {
                    e.printStackTrace();
                    if (mAuthFinished != null)
                        mAuthFinished.onFinished(false,0, 0, 0);
                }
            }
        }).start();

    }
	public void checkWifi() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int count = 0;
				while (count < 10) {
					try {
						boolean value = false;
						Response response = Jsoup.connect("http://www.baidu.com").execute();
						 if (response.url().toString().equals("http://www.baidu.com")  && mOnWifiChecked != null) {
							mOnWifiChecked.onChecked(false);
							return;
						 }else if (response.url().toString().startsWith("http://10.") && mOnWifiChecked != null)  {
							mOnWifiChecked.onChecked(true);
							return;
						}
						count++;
					}catch (Exception e) {
						count++;
					}
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (mOnWifiChecked != null)
					mOnWifiChecked.onChecked(false);
			}
		}).start();
	}
	public void checkLogin() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
//                    Response response = Jsoup.connect("http://www.baidu.com").execute();
//                    if (!response.url().toString().equals("http://www.baidu.com"))
//                        ok = response.url().toString();
					SimpleLogin http = new SimpleLogin();
					String result = http.GetPageContent(ok);
					decodeResult(result);
				} catch(IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
                    e.printStackTrace();
                }
            }
		}).start();
	}
	public void logout() {
		
		final String logout = "http://" + ok.replace("http://", "").replace("/", "") + "/F.htm";
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					SimpleLogin http = new SimpleLogin();
					String result = http.GetPageContent(logout);
					decodeLogout(result);
				} catch(Exception e) {
					if (mOnLogout!=null)
                        mOnLogout.onLogout(false);
				}
			}
		}).start();
	}
	private void decodeLogout(String content) {
		if (mOnLogout == null)
			return;
		if (content.contains("Logout successfully")) {
			mOnLogout.onLogout(true);
		} else {
			mOnLogout.onLogout(false);
		}
	}
	private void decodeResult(String content) {
		String time = getString("time",content);
		String flow = getString("flow",content);
		String fee = getString("fee",content);
		long timeNum = 0;
		long flowNum = 0;
		double feeNum = 0;

		boolean success = true;
		if (time != null) {
			timeNum = Long.parseLong(time);
		} else {
			success = false;
		}
		if (flow != null) {
			flowNum = Long.parseLong(flow);
		} else 
			success = false;
		if (fee != null) {
			feeNum = Long.parseLong(fee);
		} else 
			success = false;
		if (mAuthFinished != null)
			mAuthFinished.onFinished(success,timeNum, flowNum, feeNum/10000);
	}
	private String getString(String key,String content) {
		int start = content.indexOf(key + "='") + key.length() + 2;
		int end = content.indexOf("'", start + 1);
		
		String result = content.substring(start, end);
		result = result.replace(" ", "");

		return result;
	}
	private void sendPost(String url, String postParams) throws Exception {

		URL obj = new URL(url);
		conn = (HttpURLConnection) obj.openConnection();

		// Acts like a browser
		conn.setUseCaches(false);
		conn.setConnectTimeout(3 * 1000);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Host", ok.replace("http://", ""));
		//conn.setRequestProperty("User-Agent", Utils.USER_AGENT);
		conn.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language","zh-CN,zh;q=0.8,en;q=0.6,ja;q=0.4");
		if (this.cookies != null)
			for (String cookie : this.cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Referer", ok);
		conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length",Integer.toString(postParams.length()));
		conn.setDoOutput(true);
		conn.setDoInput(true);

		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
	}

	private String GetPageContent(String url) throws Exception {
		URL obj = new URL(url);
		conn = (HttpURLConnection) obj.openConnection();

		// default is GET
		conn.setConnectTimeout(3 * 1000);
		conn.setRequestMethod("GET");
		conn.setUseCaches(false);
		// act like a browser
		//conn.setRequestProperty("User-Agent", Utils.USER_AGENT);
		conn.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language","zh-CN,zh;q=0.8,en;q=0.6,ja;q=0.4");
		if (cookies != null) {
			for (String cookie : this.cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		setCookies(conn.getHeaderFields().get("Set-Cookie"));
		return response.toString();
	}


    public String getFormParams(String html, String username, String password)
            throws UnsupportedEncodingException {
        System.out.println("Extracting form's data...");
        Document doc = Jsoup.parse(html);

        Elements elements = doc.getElementsByTag("input");
        List<String> paramList = new ArrayList<String>();
        for (Element element : elements) {
            String key = element.attr("name");
            String value = "";
            if (key.equals("DDDDD"))
                value = username;
            else if (key.equals("upass"))
                value = password;
            paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
        }
        StringBuilder result = new StringBuilder();
        for (String param : paramList) {
            if (result.length() == 0) {
                result.append(param);
            } else {
                result.append("&" + param);
            }
        }
        return result.toString();
    }
	public String getFormParamsOld(String html, String username, String password)
			throws UnsupportedEncodingException {
		System.out.println("Extracting form's data...");
		Document doc = Jsoup.parse(html);

		Elements loginform = doc.getElementsByAttributeValue("name", "f1");
		Elements inputElements = null;
		for (Element element : loginform) {
			inputElements = element.getElementsByTag("input");
			if (inputElements != null) {
				System.out.println("*****find them!!!!");
				break;
			}
		}
		if (inputElements == null) {
			System.out.println("*****find failed!!!!!");
			return "";
		}
		List<String> paramList = new ArrayList<String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");

			System.out.println("------------current key = " + key);
			if (key.equals("DDDDD"))
				value = username;
			else if (key.equals("upass"))
				value = password;
			paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
		}

		StringBuilder result = new StringBuilder();
		for (String param : paramList) {
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		return result.toString();
	}

	public List<String> getCookies() {
		return cookies;
	}

	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
	}
	
	private OnAuthFinished mAuthFinished = null;
	private OnLogout mOnLogout = null;
	private OnWifiChecked mOnWifiChecked = null;
	public void setOnAuthFinished(OnAuthFinished onAuthFinished) {
		this.mAuthFinished = onAuthFinished;
	}
	public void setOnLogout(OnLogout onLogout) {
		this.mOnLogout = onLogout;
	}
	public void setOnChecked(OnWifiChecked checked) {
		this.mOnWifiChecked = checked;
	}
	public interface OnAuthFinished {
		void onFinished(boolean success, long time, long bytes, double fee);
	}
	public interface OnLogout {
		void onLogout(boolean success);
	}
	public interface OnWifiChecked {
		void onChecked(boolean is);
	}
}