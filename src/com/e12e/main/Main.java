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
		System.out.println("         地腿（LandLeg） Java版 v1.1 By Coande");
		System.out.println("                  http://coande.github.io");
		System.out.println("                   ！！！请低调使用！！！");
		System.out.println("----------------------------------------------------");
		try {
			esurfingService = new EsurfingService();
		} catch (IOException e) {
			System.out.println(PrintPrefix.print()+"配置文件出问题了！");
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
				System.out.println(PrintPrefix.print()+"验证码获取失败："+resinfo);
				return;
			}
		} catch (Exception e) {
			System.out.println(PrintPrefix.print()+"获取验证码出问题了！");
			e.printStackTrace();
			return;
		}

		try {
			String loginString=esurfingService.doLogin(verifyCode);
			JSONObject jsonObject=new JSONObject(loginString);
			String rescode=(String) jsonObject.opt("rescode");
			if("0".equals(rescode)){
				System.out.println(PrintPrefix.print()+"宽带连接成功！");
				System.out.println("小提示：关闭本窗口若干时间内仍然可以上网");
				System.out.println("小提示：若不关闭本窗口则每隔"+esurfingService.getActiveTime()+"分钟自动维持连接");
				/**
				 *顺便 做个小统计，不要介意哈~~
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
					//不要报错~~~
				}
			}
			else{
				String resinfo=(String) jsonObject.opt("resinfo");
				System.out.println(PrintPrefix.print()+"宽带连接失败："+resinfo);
				return ;
			}
		} catch (Exception e) {
			System.out.println(PrintPrefix.print()+"登录时出问题了！");
			e.printStackTrace();
			return ;
		}
		
		
		
		
		
		//维持连接
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
						System.out.println(PrintPrefix.print()+"维持连接成功！");
					}else{
						String resinfo=jsonObject.optString("resinfo");
						System.out.println(PrintPrefix.print()+"维持连接失败："+resinfo);
						return ;
					}
				} catch (Exception e) {
					System.out.println(PrintPrefix.print()+"维持连接时出问题了！");
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
