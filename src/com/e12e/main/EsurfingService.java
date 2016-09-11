package com.e12e.main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.e12e.utils.AddressUtil;
import com.e12e.utils.HttpUtil;
import com.e12e.utils.MD5Util;
import com.e12e.utils.PrintUtil;

public class EsurfingService {
	String username;
	String password;
	String isauto;
	String clientip;
	String nasip;
	String mac;
	String timestamp;
	String secret = "Eshore!@#";
	String iswifi = "4060";

	String md5String;
	String url;

	Properties properties;
	String activeTime;
	String configFile = "config.ini";
	String debugConfig = "debug.dat";

	/**
	 * 初始化客户端信息（isInit为true的话）
	 * @param isInit 是否需要初始化客户端信息
	 * @throws Exception
	 */
	public EsurfingService(boolean isInit) throws Exception {
		if (isInit) {
			properties = new Properties();
			FileInputStream fileInputStream;
			try {
				fileInputStream = new FileInputStream(configFile);
				properties.load(fileInputStream);
			} catch (Exception e1) {
				throw new Exception("获取配置时出现异常！");
			}
			username = properties.getProperty("username");
			password = properties.getProperty("password");

			// 获取是否设置每次自动获取IP地址
			isauto = properties.getProperty("isauto");

			if ("1".equals(isauto)) {
				autoGetInfo();
			} else {
				/*
				 * 手动设置各参数
				 */
				System.out.println("小提示：当前为手动设置客户端参数");
				nasip = properties.getProperty("nasip");
				clientip = properties.getProperty("clientip");
				mac = properties.getProperty("mac");
				activeTime = properties.getProperty("time");
			}

			// 关闭资源
			try {
				fileInputStream.close();
			} catch (IOException e) {
				System.out.println(PrintUtil.printPrefix() + "内部异常！");
			}
		}
	}

	/**
	 * 自动获取客户端信息
	 * @throws Exception 对应的Exception，e.getMessage()即可获得对应的错误信息
	 */
	public void autoGetInfo() throws Exception {
		/*
		 * 自动获取各参数信息
		 */
		System.out.println("小提示：当前为自动设置客户端参数");
		// 自动获取NASIP地址
		try {
			nasip = getNASIP();
		} catch (Exception e) {
			// 抛出异常以终止运行
			throw new Exception(PrintUtil.printPrefix() + "自动获取NASIP时异常！");
		}
		if (nasip == null) {
			// 抛出异常以终止运行
			throw new Exception(PrintUtil.printPrefix() + "自动获取NASIP失败！");
		} else {
			System.out.println(PrintUtil.printPrefix() + "自动获取到NASIP地址：" + nasip);
		}

		// 自动获取当前电脑的IP
		InetAddress ia;
		try {
			ia = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new Exception(PrintUtil.printPrefix() + "获取本机IP地址时出现异常！");
		}
		clientip = ia.getHostAddress();
		System.out.println(PrintUtil.printPrefix() + "自动获取到本机IP地址：" + clientip);

		// 自动获取mac地址
		try {
			mac = AddressUtil.getLocalMac(ia);
		} catch (SocketException e) {
			throw new Exception(PrintUtil.printPrefix() + "获取本机MAC地址时出现异常！");
		}
		System.out.println(PrintUtil.printPrefix() + "自动获取到本机MAC地址：" + mac);

		// 设置默认的维持连接时间为15分钟
		activeTime = "15";
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
		md5String = MD5Util.MD5(clientip + nasip + mac + timestamp + verifyCode + secret);

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
		// 获取上次登录的信息
		Properties properties = new Properties();
		FileInputStream fileInputStream = new FileInputStream(debugConfig);

		properties.load(fileInputStream);
		String lastnasip = properties.getProperty("lastnasip");
		String lastclientip = properties.getProperty("lastclientip");
		String lastmac = properties.getProperty("lastmac");
		fileInputStream.close();

		url = "http://enet.10000.gd.cn:10001/client/logout";
		timestamp = System.currentTimeMillis() + "";
		md5String = MD5Util.MD5(lastclientip + lastnasip + lastmac + timestamp + secret);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("clientip", lastclientip);
		jsonObject.put("nasip", lastnasip);
		jsonObject.put("mac", lastmac);
		jsonObject.put("timestamp", timestamp);
		jsonObject.put("authenticator", md5String);

		String logoutString = HttpUtil.doPost(url, jsonObject.toString());

		return logoutString;
	}

	/**
	 * 维持连接的处理
	 */
	public void doActive() {

		int activeTimeInt = Integer.parseInt(activeTime);

		TimerTask timerTask = new TimerTask() {
			String activeString;
			JSONObject jsonObject;
			String rescode;

			@Override
			public void run() {
				try {
					activeString = EsurfingService.this.keepConnection();
					jsonObject = new JSONObject(activeString);
					rescode = (String) jsonObject.opt("rescode");
					if ("0".equals(rescode)) {
						System.out.println(PrintUtil.printPrefix() + "维持连接成功！");
					} else {
						String resinfo = jsonObject.optString("resinfo");
						System.out.println(PrintUtil.printPrefix() + "维持连接失败：" + resinfo);
						reConnect();
					}
				} catch (Exception e) {
					System.out.println(PrintUtil.printPrefix() + "维持连接时出现异常！");
					// 停止执行定时任务
					this.cancel();
					reConnect();
					return;
				}
			}
		};

		Timer timer = new Timer();
		timer.schedule(timerTask, activeTimeInt * 60000, activeTimeInt * 60000);
		// timer.schedule(timerTask, 2000, 2000);
	}

	/**
	 * 发送维持连接请求
	 * 
	 * @return 返回结果
	 * @throws Exception
	 */
	private String keepConnection() throws Exception {
		timestamp = System.currentTimeMillis() + "";
		md5String = MD5Util.MD5(clientip + nasip + mac + timestamp + secret);
		url = "http://enet.10000.gd.cn:8001/hbservice/client/active";

		String param = "username=" + username + "&clientip=" + clientip + "&nasip=" + nasip + "&mac=" + mac + "&timestamp=" + timestamp + "&authenticator="
				+ md5String;
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
	 * 
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
	 * 
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	private void setNewProperties(String key, String value) throws IOException {
		Properties properties = new Properties();
		InputStream inputStream = new FileInputStream(debugConfig);
		properties.load(inputStream);
		inputStream.close();
		// 在原来的数据基础上修改数据
		OutputStream outputStream = new FileOutputStream(debugConfig);
		properties.setProperty(key, value);
		properties.store(outputStream, null);

		outputStream.close();
	}

	/**
	 * 登录成功后回调记录登录信息用于登出
	 * 
	 * @throws IOException
	 *             properties文件操作异常
	 */
	public void doSetNewProperties() throws IOException {
		setNewProperties("lastnasip", nasip);
		setNewProperties("lastclientip", clientip);
		setNewProperties("lastmac", mac);
	}

	/**
	 * 顺便做个小统计，不要介意哈~~
	 */
	public void analytics() {
		try {
			String resString = HttpUtil.doGet("http://ip.taobao.com/service/getIpInfo.php", "ip=myip");
			JSONObject jsonObject2 = new JSONObject(resString);
			int code = jsonObject2.optInt("code");
			String city = null;
			if (code == 0) {
				JSONObject jsonObject3 = (JSONObject) jsonObject2.opt("data");
				city = (String) jsonObject3.opt("city");
				city = URLEncoder.encode(city, "utf-8");
				String param = "uid=" + MD5Util.MD5(AddressUtil.getLocalMac(InetAddress.getLocalHost())) + "&city=" + city + "&type=1";
				HttpUtil.doGet("http://s2.e12e.com:8080/Analytics/", param);
			}
		} catch (Exception e) {
			// 不要报错~~~
		}
	}

	public void reConnect() {
		// 重连
		System.out.println(PrintUtil.printPrefix() + "尝试重新连接...");
		for (int i = 0; i < 10; i++) {
			try {
				String verifyCode = new JSONObject(this.getVerifyCodeString()).optString("challenge");
				//登录失败抛出异常
				doLogin(verifyCode);
				doActive();
				System.out.println(PrintUtil.printPrefix() + "第"+(i+1)+"次重新连接成功！");
				break;
			} catch (Exception e1) {
				System.out.println(PrintUtil.printPrefix() + "第"+(i+1)+"次重新连接失败！");
				System.out.println(PrintUtil.printPrefix() + "10s后重连...");
				try {
					Thread.sleep(10*1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}

}
