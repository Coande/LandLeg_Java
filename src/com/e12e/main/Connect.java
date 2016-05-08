package com.e12e.main;

import java.io.IOException;

import org.json.JSONObject;

import com.e12e.utils.PrintUtil;
/**
 * 登录主类
 * @author Coande
 *
 */
public class Connect {
	static EsurfingService esurfingService;

	public static void main(String[] args) {
		PrintUtil.printAbout();
		String verifyCodeString = null;
		String verifyCode = null,rescode,resinfo;
		JSONObject jsonObject;
		/*
		 * 初始化 EsurfingService
		 */
		try {
			esurfingService = new EsurfingService(true);
		} catch (Exception e) {
			System.out.println(PrintUtil.printPrefix() + e.getMessage());
			return;
		}

		/*
		 * 获取验证码
		 */
		try {
			verifyCodeString = esurfingService.getVerifyCodeString();
			jsonObject = new JSONObject(verifyCodeString);
			if ("0".equals(jsonObject.opt("rescode"))) {
				verifyCode = (String) jsonObject.opt("challenge");
			} else {
				resinfo = jsonObject.optString("resinfo");
				System.out.println(PrintUtil.printPrefix() + "验证码获取失败：" + resinfo);
				return;
			}
		} catch (Exception e) {
			System.out.println(PrintUtil.printPrefix() + "获取验证码时出现异常！");
			// e.printStackTrace();
			return;
		}

		/*
		 * 连接宽带
		 */
		String loginString;
		try {
			loginString = esurfingService.doLogin(verifyCode);
		} catch (Exception e) {
			System.out.println(PrintUtil.printPrefix() + "登录时出现异常！");
			// e.printStackTrace();
			return;
		}
		
		jsonObject = new JSONObject(loginString);
		rescode =jsonObject.optString("rescode");
		if ("0".equals(rescode)) {
			System.out.println(PrintUtil.printPrefix() + "宽带连接成功！");
			System.out.println("小提示：关闭本窗口若干时间内仍然可以上网");
			System.out.println("小提示：若不关闭本窗口则每隔"
					+ esurfingService.getActiveTime() + "分钟自动维持连接");
			// 记录登录成功的信息用于登出
			try {
				esurfingService.doSetNewProperties();
			} catch (IOException e) {
				System.out.println(PrintUtil.printPrefix() +"记录登录信息时出现异常！");
			}

			// 统计
			esurfingService.analytics();
			// 维持连接
			esurfingService.doActive();
		} else {
			resinfo = jsonObject.optString("resinfo");
			System.out.println(PrintUtil.printPrefix() + "宽带连接失败：" + resinfo);
			return;
		}
		
	}
}
