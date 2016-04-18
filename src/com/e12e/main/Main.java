package com.e12e.main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.e12e.utils.AddressUtil;
import com.e12e.utils.HttpUtil;
import com.e12e.utils.MD5Util;
import com.e12e.utils.PrintPrefix;

public class Main {
	
	public static void main(String [] args){
		
		EsurfingService esurfingService = null;
		String verifyCode = null;
		System.out.println("----------------------------------------------------");
		System.out.println("         ���ȣ�LandLeg�� Java�� v1.1 By Coande");
		System.out.println("                  http://coande.github.io");
		System.out.println("                   ��������͵�ʹ�ã�����");
		System.out.println("----------------------------------------------------");
		try {
			esurfingService = new EsurfingService();
		} catch (IOException e) {
			System.out.println(PrintPrefix.print()+"�����ļ��������ˣ�");
			e.printStackTrace();
			return;
		}
		
				String verifyCodeString = null;
		try {
			verifyCodeString = esurfingService.getVerifyCodeString();
			JSONObject jsonObject=new JSONObject(verifyCodeString);
			if("0".equals(jsonObject.opt("rescode"))){
				verifyCode =(String) jsonObject.opt("challenge");
			}else{
				String resinfo=(String) jsonObject.opt("resinfo");
				System.out.println(PrintPrefix.print()+"��֤���ȡʧ�ܣ�"+resinfo);
				return;
			}
		} catch (Exception e) {
			System.out.println(PrintPrefix.print()+"��ȡ��֤��������ˣ�");
			e.printStackTrace();
			return;
		}

		try {
			String loginString=esurfingService.doLogin(verifyCode);
			JSONObject jsonObject=new JSONObject(loginString);
			String rescode=(String) jsonObject.opt("rescode");
			if("0".equals(rescode)){
				System.out.println(PrintPrefix.print()+"������ӳɹ���");
				System.out.println("С��ʾ���رձ���������ʱ������Ȼ��������");
				System.out.println("С��ʾ�������رձ�������ÿ��"+esurfingService.getActiveTime()+"�����Զ�ά������");
				/**
				 *˳�� ����Сͳ�ƣ���Ҫ�����~~
				 */
				try{
					String resString=HttpUtil.doGet("http://ip.taobao.com/service/getIpInfo.php", "ip=myip");
					JSONObject jsonObject2=new JSONObject(resString);
					int code=jsonObject2.optInt("code");
					String city = null;
					if(code==0){
						JSONObject jsonObject3=(JSONObject) jsonObject2.opt("data");
						city=(String) jsonObject3.opt("city");
						city=URLEncoder.encode(city, "utf-8");
						String param="uid="+MD5Util.MD5(AddressUtil.getLocalMac(InetAddress.getLocalHost()))+"&city="+city+"&type=1";
						HttpUtil.doGet("http://s2.e12e.com:8080/Analytics/", param);
					}
				}catch(IOException e){
					//��Ҫ����~~~
				}
			}
			else{
				String resinfo=(String) jsonObject.opt("resinfo");
				System.out.println(PrintPrefix.print()+"�������ʧ�ܣ�"+resinfo);
				return ;
			}
		} catch (Exception e) {
			System.out.println(PrintPrefix.print()+"��¼ʱ�������ˣ�");
			e.printStackTrace();
			return ;
		}
		
		
		
		
		
		//ά������
		String activeTime=esurfingService.getActiveTime();
		int activeTimeInt=Integer.parseInt(activeTime);
		
		TimerTask timerTask=new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					EsurfingService esurfingService=new EsurfingService();
					String activeString =esurfingService.doActive();
					JSONObject jsonObject=new JSONObject(activeString);
					String rescode=(String) jsonObject.opt("rescode");
					if("0".equals(rescode)){
						System.out.println(PrintPrefix.print()+"ά�����ӳɹ���");
					}else{
						String resinfo=jsonObject.optString("resinfo");
						System.out.println(PrintPrefix.print()+"ά������ʧ�ܣ�"+resinfo);
						return ;
					}
				} catch (Exception e) {
					System.out.println(PrintPrefix.print()+"ά������ʱ�������ˣ�");
					e.printStackTrace();
					return ;
				}
			}
		};
		
		Timer timer=new Timer();
		timer.schedule(timerTask, activeTimeInt*60000, activeTimeInt*60000);
		//timer.schedule(timerTask, 2000, 2000);

	}
}
