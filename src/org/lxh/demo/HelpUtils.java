package org.lxh.demo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

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
		  b[3-i] = (byte)(n >> (24 - i * 8));//b[i] = (byte)(n >> (24 - i * 8)); 
	  }
	  return b;
	 }
	 
	 /**
	  * 将byte数组转换为int数据
	  * @param b 字节数组
	  * @return 生成的int数据
	  */
	 public static long bytesToLong(byte[] b){
	  return (((long)b[3]) << 24) + (((long)b[2]) << 16) + (((long)b[1]) << 8) + b[0];
	 }
	 public static long bytesToLong2(byte[] b){
		  return ((((long)b[0]) << 24)&0xff000000) 
				  + ((((long)b[1]) << 16)&0xff0000)
				  + ((((long)b[2]) << 8)&0xff00)
				  + ((long)b[3]&0xff);
	 }
	public static byte[] longToByte(long number) {
		long temp = number;
		byte[] b = new byte[8];
		System.out.println("bytes  :");
		for (int i = 0; i < b.length; i++) {
			b[i] = new Long(temp & 0xff).byteValue();//
			// 将最低位保存在最低位 temp = temp >> 8; // 向右移8位
			System.out.println(" " + b[i]);
		}
		return b;
	}
	
	//byte[]转float
	public static float byteToFloat(byte[] v){
	        ByteBuffer bb = ByteBuffer.wrap(v);
	        FloatBuffer fb = bb.asFloatBuffer();
	        return fb.get();
	}
	
	public static int bToI(boolean b){
		if(b)
			return 1;
		else
			return 0;
	}
	
}
