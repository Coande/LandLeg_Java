package com.e12e.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PrintUtil {
	public static String printPrefix() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String prefix = sdf.format(new Date()) + "：";
		return prefix;
	}

	public static void printAbout() {
		System.out
				.println("----------------------------------------------------");
		System.out.println("         地腿（LandLeg） Java版 v2.0 By Coande");
		System.out.println("                  http://coande.github.io");
		System.out.println("                   ！！！请低调使用！！！");
		System.out
				.println("----------------------------------------------------");
	}
}
