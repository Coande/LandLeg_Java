package com.e12e.main;

import org.json.JSONObject;

import com.e12e.utils.PrintUtil;

/**
 * 登出主类
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
			System.out.println(PrintUtil.printPrefix() + "内部错误！");
			return;
		}
		try {
			logoutString = esurfingService.doLogout();
		} catch (Exception e) {
			System.out.println(PrintUtil.printPrefix() + "登出时出现异常！");
			// e.printStackTrace();
			return;
		}
		jsonObject = new JSONObject(logoutString);
		rescode = (String) jsonObject.opt("rescode");
		if ("0".equals(rescode)) {
			System.out.println(PrintUtil.printPrefix() + "宽带断开成功！");
		} else {
			resinfo = jsonObject.optString("resinfo");
			System.out.println(PrintUtil.printPrefix() + "宽带断开失败：" + resinfo);
		}
	}

}
