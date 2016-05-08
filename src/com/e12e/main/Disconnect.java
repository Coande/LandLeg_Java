package com.e12e.main;

import org.json.JSONObject;

import com.e12e.utils.PrintUtil;

/**
 * �ǳ�����
 * @author Coande
 *
 */
public class Disconnect {

	public static void main(String[] args) {
		PrintUtil.printAbout();
		EsurfingService esurfingService = null;
		String logoutString = null, rescode, resinfo;
		JSONObject jsonObject;
		
		try {
			esurfingService = new EsurfingService(false);
		} catch (Exception e) {
			System.out.println(PrintUtil.printPrefix() + "�ڲ�����");
			return;
		}
		try {
			logoutString = esurfingService.doLogout();
		} catch (Exception e) {
			System.out.println(PrintUtil.printPrefix() + "�ǳ�ʱ�����쳣��");
			// e.printStackTrace();
			return;
		}
		jsonObject = new JSONObject(logoutString);
		rescode = (String) jsonObject.opt("rescode");
		if ("0".equals(rescode)) {
			System.out.println(PrintUtil.printPrefix() + "����Ͽ��ɹ���");
		} else {
			resinfo = jsonObject.optString("resinfo");
			System.out.println(PrintUtil.printPrefix() + "����Ͽ�ʧ�ܣ�" + resinfo);
		}
	}

}
