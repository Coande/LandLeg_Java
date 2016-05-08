package com.e12e.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

public class HttpUtil {
	static String debugConfig = "debug.dat";

	public static String doGet(String url, String param) throws IOException {
		String result = "";
		BufferedReader in = null;

		String urlNameString = url + "?" + param;

		URL realUrl = new URL(urlNameString);
		// �򿪺�URL֮�������
		URLConnection connection = realUrl.openConnection();
		// ����ʵ�ʵ�����
		connection.connect();

		// ���� BufferedReader����������ȡURL����Ӧ
		in = new BufferedReader(new InputStreamReader(
				connection.getInputStream(), "utf-8"));
		String line;
		while ((line = in.readLine()) != null) {
			result += line;
		}

		in.close();

		return result;
	}

	public static String doPost(String url, String param) throws Exception {
		Properties properties = new Properties();
		FileInputStream fileInputStream = new FileInputStream(debugConfig);
		properties.load(fileInputStream);
		String isdebug = properties.getProperty("isdebug");
		//��ȡproperties�ļ��鿴�Ƿ���debug
		if ("1".equals(isdebug)) {
			System.out.println(PrintUtil.printPrefix() + "Post param= " + param);
		}

		URL realUrl = new URL(url);
		URLConnection conn = realUrl.openConnection();

		// ����POST�������������������
		conn.setDoOutput(true);
		conn.setDoInput(true);

		// ��ȡURLConnection�����Ӧ�������
		PrintWriter out = new PrintWriter(conn.getOutputStream());

		// �����������
		out.print(param);

		// flush������Ļ���
		out.flush();

		// ����BufferedReader����������ȡURL����Ӧ
		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream(), "utf-8"));

		String line = "", result = "";

		while ((line = in.readLine()) != null) {
			result += line;
		}
		
		//��ȡproperties�ļ��鿴�Ƿ���debug
		if ("1".equals(isdebug)) {
			System.out.println(PrintUtil.printPrefix() + "Post result= " + result);
		}
		
		in.close();
		out.close();
		fileInputStream.close();
		return result;
	}

	public static String getRedirectUrl(String urlString) throws Exception {
		HttpURLConnection conn = (HttpURLConnection) new URL(urlString)
				.openConnection();
		conn.setInstanceFollowRedirects(false);
		return conn.getHeaderField("Location");
	}

}
