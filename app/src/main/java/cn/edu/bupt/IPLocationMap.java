package cn.edu.bupt;

import android.text.TextUtils;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class IPLocationMap {
	
	private static Map<String, String> map = getLocalIPLocationMap();
	
	public static Map<String, String> getLocalIPLocationMap(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("101", "教一");
		map.put("102", "教二");
		map.put("103", "教三");
		map.put("104", "教四");
		map.put("105", "主教");
		map.put("106", "教六");
		map.put("107", "明光楼");
		map.put("108", "新科研楼");
		map.put("109", "新科研楼");
		map.put("110", "创新大本营 学十楼北地下室 综合服务楼");
		map.put("201", "学一");
		map.put("202", "学二");
		map.put("203", "学三");
		map.put("204", "学四");
		map.put("205", "学五");
		map.put("206", "学六");
		map.put("207", "学七");
		map.put("208", "学八");
		map.put("209", "学九");
		map.put("210", "学十");
		map.put("211", "学十一");
		map.put("212", "学十二");
		map.put("213", "学十三");
		map.put("214", "学十四");
		map.put("215", "学二十九");
		return map;
	}
	
	public static String getLocation(String ip){
        if (TextUtils.isEmpty(ip) || ip.length() < 7)
            return "未知位置";
		String location = map.get(ip.subSequence(3, 6));
		if(ip.startsWith("10.8.")) location = "无线";
		if(location == null) location = "未知位置";
		return location;
	}
}
