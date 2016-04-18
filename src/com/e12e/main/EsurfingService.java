package com.e12e.main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import org.json.JSONObject;

import com.e12e.utils.AddressUtil;
import com.e12e.utils.HttpUtil;
import com.e12e.utils.MD5Util;
import com.e12e.utils.PrintPrefix;

public class EsurfingService {
	String username;
	String clientip;
	String nasip;
	String mac;
	String timestamp;
	String secret = "Eshore!@#";
	String iswifi = "4060";

	String password;
	String md5String;
	String url;

	Properties properties;
	String activeTime;
	String configFile="config.txt";

	public EsurfingService() throws IOException {
		properties = new Properties();
		FileInputStream fileInputStream = new FileInputStream(
				configFile);
		properties.load(fileInputStream);
		username = properties.getProperty("username");

		clientip = properties.getProperty("clientip");
		InetAddress ia = null;
		if ("".equals(clientip)) {
			ia = InetAddress.getLocalHost();
			clientip = ia.getHostAddress();
			setNewProperties("clientip", clientip);
			System.out.println(PrintPrefix.print()+"自动获取到本机IP地址："+clientip);
		}
		
		nasip = properties.getProperty("nasip");
		if("".equals(nasip)){
			try {
				nasip=getNASIP();
				if(nasip!=null){
					setNewProperties("nasip", nasip);
					System.out.println(PrintPrefix.print()+"自动获取到NASIP地址："+nasip);
				}else {
					System.out.println(PrintPrefix.print()+"自动获取到NASIP失败！");
				}
				
			} catch (Exception e) {
				System.out.println(PrintPrefix.print()+"自动获取NASIP地址出现异常！");
				e.printStackTrace();
			}
		}
		
		mac = properties.getProperty("mac");
		if ("".equals(mac)) {
			ia = InetAddress.getLocalHost();
			mac = AddressUtil.getLocalMac(ia);
			setNewProperties("mac", mac);
			System.out.println(PrintPrefix.print()+"自动获取到本机MAC地址："+mac);
		}

		password = properties.getProperty("password");

		activeTime = properties.getProperty("time");

		// 关闭资源
		fileInputStream.close();
	}

	/**
	 * 得到登录时需要的验证码数据
	 * 
	 * @return 返回登录时需要的验证码数据
	 * @throws Exception
	 */
	public String getVerifyCodeString() throws Exception {
		url = "http://enet.10000.gd.cn:10001/client/challenge";
		timestamp = System.currentTimeMillis() + "";
		md5String = MD5Util.MD5(clientip + nasip + mac + timestamp + secret);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("username", username);
		jsonObject.put("clientip", clientip);
		jsonObject.put("nasip", nasip);
		jsonObject.put("mac", mac);
		jsonObject.put("iswifi", iswifi);
		jsonObject.put("timestamp", timestamp);
		jsonObject.put("authenticator", md5String);

		String verifyCodeString = HttpUtil.doPost(url, jsonObject.toString());
		return verifyCodeString;
	}

	/**
	 * 登录
	 * 
	 * @param 验证码
	 * @return 登录时服务器返回的数据
	 * @throws Exception
	 */
	public String doLogin(String verifyCode) throws Exception {
		url = "http://enet.10000.gd.cn:10001/client/login";
		timestamp = System.currentTimeMillis() + "";
		md5String = MD5Util.MD5(clientip + nasip + mac + timestamp + verifyCode
				+ secret);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("username", username);
		jsonObject.put("password", password);
		jsonObject.put("verificationcode", "");
		jsonObject.put("clientip", clientip);
		jsonObject.put("nasip", nasip);
		jsonObject.put("mac", mac);
		jsonObject.put("iswifi", iswifi);
		jsonObject.put("timestamp", timestamp);
		jsonObject.put("authenticator", md5String);

		String loginString = HttpUtil.doPost(url, jsonObject.toString());

		return loginString;
	}

	/**
	 * 登出
	 * 
	 * @return 登出时服务器返回的数据
	 * @throws Exception
	 */
	public String doLogout() throws Exception {
		url = "http://enet.10000.gd.cn:10001/client/logout";
		timestamp = System.currentTimeMillis() + "";
		md5String = MD5Util.MD5(clientip + nasip + mac + timestamp + secret);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("clientip", clientip);
		jsonObject.put("nasip", nasip);
		jsonObject.put("mac", mac);
		jsonObject.put("timestamp", timestamp);
		jsonObject.put("authenticator", md5String);

		String logoutString = HttpUtil.doPost(url, jsonObject.toString());

		return logoutString;
	}

	/**
	 * 发送维持连接请求
	 * 
	 * @return 返回结果
	 * @throws Exception
	 */
	public String doActive() throws Exception {
		timestamp = System.currentTimeMillis() + "";
		md5String = MD5Util.MD5(clientip + nasip + mac + timestamp + secret);
		url = "http://enet.10000.gd.cn:8001/hbservice/client/active";

		// 不知道为啥timestamp为null时才能通过~
		String param = "username=" + username + "&clientip=" + clientip
				+ "&nasip=" + nasip + "&mac=" + mac + "&timestamp=" + timestamp
				+ "&authenticator=" + md5String;
		String activeString = HttpUtil.doGet(url, param);
		return activeString;
	}

	/**
	 * 获取properties中的值
	 * 
	 * @return properties文件中维持连接请求发送时间间隔
	 */
	public String getActiveTime() {
		return this.activeTime;
	}
	
	
	/**
	 * 访问百度来测试获取NASIP
	 * @return 获取当地NASIP
	 * @throws Exception
	 */
	public String getNASIP() throws Exception {
		String nasip = null;
		String location = HttpUtil.getRedirectUrl("http://www.baidu.com");
		if (location != null) {
			URL url = new URL(location);
			String queryString = url.getQuery();
			if (queryString != null) {
				String[] queryStringArr = queryString.split("&");
				HashMap<String, String> map = new HashMap<String, String>();
				for (String query : queryStringArr) {
					String[] querStringArry2 = query.split("=");
					map.put(querStringArry2[0], querStringArry2[1]);
				}
				nasip = map.get("wlanacip");
			}
		}
		return nasip;
	}
	
	/**
	 * 修改配置文件
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void setNewProperties(String key,String value) throws IOException{
		Properties properties=new Properties();
		InputStream inputStream=new FileInputStream(configFile);
		properties.load(inputStream);
		inputStream.close();
		//在原来的数据基础上修改数据
		OutputStream outputStream = new FileOutputStream(configFile); 
		properties.setProperty(key, value);
		properties.store(outputStream,"");
		 outputStream.close();  
	}

}
