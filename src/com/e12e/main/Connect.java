package com.e12e.main;

import java.io.IOException;

import org.json.JSONObject;

import com.e12e.utils.PrintUtil;
/**
 * ��¼����
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
		 * ��ʼ�� EsurfingService
		 */
		try {
			esurfingService = new EsurfingService(true);
		} catch (Exception e) {
			System.out.println(PrintUtil.printPrefix() + e.getMessage());
			return;
		}

		/*
		 * ��ȡ��֤��
		 */
		try {
			verifyCodeString = esurfingService.getVerifyCodeString();
			jsonObject = new JSONObject(verifyCodeString);
			if ("0".equals(jsonObject.opt("rescode"))) {
				verifyCode = (String) jsonObject.opt("challenge");
			} else {
				resinfo = jsonObject.optString("resinfo");
				System.out.println(PrintUtil.printPrefix() + "��֤���ȡʧ�ܣ�" + resinfo);
				return;
			}
		} catch (Exception e) {
			System.out.println(PrintUtil.printPrefix() + "��ȡ��֤��ʱ�����쳣��");
			// e.printStackTrace();
			return;
		}

		/*
		 * ���ӿ��
		 */
		String loginString;
		try {
			loginString = esurfingService.doLogin(verifyCode);
		} catch (Exception e) {
			System.out.println(PrintUtil.printPrefix() + "��¼ʱ�����쳣��");
			// e.printStackTrace();
			return;
		}
		
		jsonObject = new JSONObject(loginString);
		rescode =jsonObject.optString("rescode");
		if ("0".equals(rescode)) {
			System.out.println(PrintUtil.printPrefix() + "������ӳɹ���");
			System.out.println("С��ʾ���رձ���������ʱ������Ȼ��������");
			System.out.println("С��ʾ�������رձ�������ÿ��"
					+ esurfingService.getActiveTime() + "�����Զ�ά������");
			// ��¼��¼�ɹ�����Ϣ���ڵǳ�
			try {
				esurfingService.doSetNewProperties();
			} catch (IOException e) {
				System.out.println(PrintUtil.printPrefix() +"��¼��¼��Ϣʱ�����쳣��");
			}

			// ͳ��
			esurfingService.analytics();
			// ά������
			esurfingService.doActive();
		} else {
			resinfo = jsonObject.optString("resinfo");
			System.out.println(PrintUtil.printPrefix() + "�������ʧ�ܣ�" + resinfo);
			return;
		}
		
	}
}
