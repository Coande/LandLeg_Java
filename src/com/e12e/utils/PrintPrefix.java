package com.e12e.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PrintPrefix {
	public static String print(){
		SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
		String prefix=sdf.format(new Date())+"£º";
		return prefix;
	}
}
