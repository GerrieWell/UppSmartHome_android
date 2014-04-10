package org.lxh.demo;

import android.widget.Toast;

public class HelpUtils {
	public static void qDebug(CharSequence s){
		System.out.println("dbg: "+s );
	}
	 /**
	  * 将int类型的数据转换为byte数组
	  * @param n int数据
	  * @return 生成的byte数组
	  */
	 public static byte[] intToBytes(int n){
	  String s = String.valueOf(n);
	  return s.getBytes(); 
	 }
	 
	 /**
	  * 将byte数组转换为int数据
	  * @param b 字节数组
	  * @return 生成的int数据
	  */
	 public static int bytesToInt(byte[] b){
	  String s = new String(b);
	  return Integer.parseInt(s); 
	 }
	 
	 /**
	  * 将int类型的数据转换为byte数组
	  * 原理：将int数据中的四个byte取出，分别存储
	  * @param n int数据
	  * @return 生成的byte数组
	  */
	 public static byte[] longToBytes(long n){
	  byte[] b = new byte[4];
	  for(int i = 0;i < 4;i++){
	   b[i] = (byte)(n >> (24 - i * 8)); 
	  }
	  return b;
	 }
	 
	 /**
	  * 将byte数组转换为int数据
	  * @param b 字节数组
	  * @return 生成的int数据
	  */
	 public static long StrToLong(byte[] b){
	  return (((long)b[0]) << 24) + (((long)b[1]) << 16) + (((long)b[2]) << 8) + b[3];
	 }
}
