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
	 * ��ʼ���ͻ�����Ϣ��isInitΪtrue�Ļ���
	 * @param isInit �Ƿ���Ҫ��ʼ���ͻ�����Ϣ
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
				throw new Exception("��ȡ����ʱ�����쳣��");
			}
			username = properties.getProperty("username");
			password = properties.getProperty("password");

			// ��ȡ�Ƿ�����ÿ���Զ���ȡIP��ַ
			isauto = properties.getProperty("isauto");

			if ("1".equals(isauto)) {
				autoGetInfo();
			} else {
				/*
				 * �ֶ����ø�����
				 */
				System.out.println("С��ʾ����ǰΪ�ֶ����ÿͻ��˲���");
				nasip = properties.getProperty("nasip");
				clientip = properties.getProperty("clientip");
				mac = properties.getProperty("mac");
				activeTime = properties.getProperty("time");
			}

			// �ر���Դ
			try {
				fileInputStream.close();
			} catch (IOException e) {
				System.out.println(PrintUtil.printPrefix() + "�ڲ��쳣��");
			}
		}
	}

	/**
	 * �Զ���ȡ�ͻ�����Ϣ
	 * @throws Exception ��Ӧ��Exception��e.getMessage()���ɻ�ö�Ӧ�Ĵ�����Ϣ
	 */
	public void autoGetInfo() throws Exception {
		/*
		 * �Զ���ȡ��������Ϣ
		 */
		System.out.println("С��ʾ����ǰΪ�Զ����ÿͻ��˲���");
		// �Զ���ȡNASIP��ַ
		try {
			nasip = getNASIP();
		} catch (Exception e) {
			// �׳��쳣����ֹ����
			throw new Exception(PrintUtil.printPrefix() + "�Զ���ȡNASIPʱ�쳣��");
		}
		if (nasip == null) {
			// �׳��쳣����ֹ����
			throw new Exception(PrintUtil.printPrefix() + "�Զ���ȡNASIPʧ�ܣ�");
		} else {
			System.out.println(PrintUtil.printPrefix() + "�Զ���ȡ��NASIP��ַ��" + nasip);
		}

		// �Զ���ȡ��ǰ���Ե�IP
		InetAddress ia;
		try {
			ia = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new Exception(PrintUtil.printPrefix() + "��ȡ����IP��ַʱ�����쳣��");
		}
		clientip = ia.getHostAddress();
		System.out.println(PrintUtil.printPrefix() + "�Զ���ȡ������IP��ַ��" + clientip);

		// �Զ���ȡmac��ַ
		try {
			mac = AddressUtil.getLocalMac(ia);
		} catch (SocketException e) {
			throw new Exception(PrintUtil.printPrefix() + "��ȡ����MAC��ַʱ�����쳣��");
		}
		System.out.println(PrintUtil.printPrefix() + "�Զ���ȡ������MAC��ַ��" + mac);

		// ����Ĭ�ϵ�ά������ʱ��Ϊ15����
		activeTime = "15";
	}

	/**
	 * �õ���¼ʱ��Ҫ����֤������
	 * 
	 * @return ���ص�¼ʱ��Ҫ����֤������
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
	 * ��¼
	 * 
	 * @param ��֤��
	 * @return ��¼ʱ���������ص�����
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
	 * �ǳ�
	 * 
	 * @return �ǳ�ʱ���������ص�����
	 * @throws Exception
	 */
	public String doLogout() throws Exception {
		// ��ȡ�ϴε�¼����Ϣ
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
	 * ά�����ӵĴ���
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
						System.out.println(PrintUtil.printPrefix() + "ά�����ӳɹ���");
					} else {
						String resinfo = jsonObject.optString("resinfo");
						System.out.println(PrintUtil.printPrefix() + "ά������ʧ�ܣ�" + resinfo);
						reConnect();
					}
				} catch (Exception e) {
					System.out.println(PrintUtil.printPrefix() + "ά������ʱ�����쳣��");
					// ִֹͣ�ж�ʱ����
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
	 * ����ά����������
	 * 
	 * @return ���ؽ��
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
	 * ��ȡproperties�е�ֵ
	 * 
	 * @return properties�ļ���ά������������ʱ����
	 */
	public String getActiveTime() {
		return this.activeTime;
	}

	/**
	 * ���ʰٶ������Ի�ȡNASIP
	 * 
	 * @return ��ȡ����NASIP
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
	 * �޸������ļ�
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
		// ��ԭ�������ݻ������޸�����
		OutputStream outputStream = new FileOutputStream(debugConfig);
		properties.setProperty(key, value);
		properties.store(outputStream, null);

		outputStream.close();
	}

	/**
	 * ��¼�ɹ���ص���¼��¼��Ϣ���ڵǳ�
	 * 
	 * @throws IOException
	 *             properties�ļ������쳣
	 */
	public void doSetNewProperties() throws IOException {
		setNewProperties("lastnasip", nasip);
		setNewProperties("lastclientip", clientip);
		setNewProperties("lastmac", mac);
	}

	/**
	 * ˳������Сͳ�ƣ���Ҫ�����~~
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
			// ��Ҫ����~~~
		}
	}

	public void reConnect() {
		// ����
		System.out.println(PrintUtil.printPrefix() + "������������...");
		for (int i = 0; i < 10; i++) {
			try {
				String verifyCode = new JSONObject(this.getVerifyCodeString()).optString("challenge");
				//��¼ʧ���׳��쳣
				doLogin(verifyCode);
				doActive();
				System.out.println(PrintUtil.printPrefix() + "��"+(i+1)+"���������ӳɹ���");
				break;
			} catch (Exception e1) {
				System.out.println(PrintUtil.printPrefix() + "��"+(i+1)+"����������ʧ�ܣ�");
				System.out.println(PrintUtil.printPrefix() + "10s������...");
				try {
					Thread.sleep(10*1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}

}
