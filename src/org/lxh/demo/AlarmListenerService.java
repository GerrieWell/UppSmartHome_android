package org.lxh.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;

public class AlarmListenerService extends Service {
	/*
	 * 在Service中的变量会永驻内存.
	 * 完善的做法是把状态保存在这里,activity每次启动到这里取值更新控件
	 */
	/*final*/
	public static final String TCP_SERVICE_ACTION_CONNECT="abcd",TCP_SERVICE_ACTION_SEND="abce";
	
	static boolean serviceRan=false;
	public static boolean electric_sw=false;
	public static boolean light_opened=false;
	public static int eltNum=0;
	public static byte lightchar=0;
	public static byte elecAction=0;
	public static String notificationShowStr="test";
	private boolean alarmRecved=false;
	
	/*component*/
/*	public static PrintStream shOut;
	public static BufferedReader shBuf ;
	public static OutputStream os;
	public static InputStream is;*/
	public static TCPClient client;
	
	/*remote info*/
	public static boolean isConnect;
	public static boolean outMode=false;
	public static int str_num=6;
	public static int lightGroupNum=2;
	public static int wir_sws[]={0,1,1,0,1,0,0};
	public static byte lightStates[]=null;
	public static byte WIRING_ACTION_OPEN=1;
	public static int i;
	public static String rxStrBuf = null;
	boolean opened;
	static boolean mutexEnble=true;
	static byte cut_P6=0;
	static boolean checkChangeble=true;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		System.out.println("Service : onStartCommand");
		
		serviceRan=true;
		return super.onStartCommand(intent, flags, startId);
	}

	static int ImgNum=0;
	static int imgShowNum=0;
	static File dir=null;
	public boolean pic_foced=false;

	NotificationManager manager;
	Notification notification;
    public void initNotification(String str) {
		manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		// 创建一个Notification
		notification = new Notification();
		// 设置显示在手机最上边的状态栏的图标
		notification.icon =android.R.drawable.ic_menu_today;
		// 当当前的notification被放到状态栏上的时候，提示内容
		notification.tickerText = "日程：";
		notification.defaults=Notification.DEFAULT_SOUND;
		notification.audioStreamType= android.media.AudioManager.ADJUST_LOWER;
	// 点击状态栏的图标出现的提示信息设置
		//notification.setLatestEventInfo(this.context, c.getContent()+" ","  ", pendingIntent);
		Intent intent = new Intent(AlarmListenerService.this, HomeClientDemo.class);
		PendingIntent pendIntent = PendingIntent.getActivity(AlarmListenerService.this, 0, intent, 0);  
		//notification.contentIntent = notificationIntent;
		notification.setLatestEventInfo(AlarmListenerService.this, "SMARTHOME 提醒您:", str, pendIntent);
		//manager.notify(1, notification);
		
	}
    public static Socket qtClient;
	public static PrintStream shOut;
	public static BufferedReader shBuf ;
	public static OutputStream os;
	public static InputStream is;
	
	public static void connectQtServer(){
		try {
			if(null!=qtClient&&(qtClient.isConnected()&&(!qtClient.isClosed()))){
				isConnect=false;
				qtClient.close();
				return ;
			}
			System.out.println("readly to connet");
			qtClient = new Socket(HomeClientDemo.IP, HomeClientDemo.qtPORT);				// 指定服务器
			SocketAddress socAddress = new InetSocketAddress(HomeClientDemo.IP,HomeClientDemo.qtPORT); 
			//qtClient.connect(socAddress, 5000);
			
			System.out.println("create Socket success");
			os=qtClient.getOutputStream();	
			shOut = new PrintStream(
					os);				// 打印流输出
			is=qtClient.getInputStream();
			shBuf= new BufferedReader(		//输入流
					new InputStreamReader(
							is));		// 缓冲区读取
			if(null!=qtClient&&(qtClient.isConnected()&&(!qtClient.isClosed()))){
				isConnect=true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void closeQtServer(){
		try {
			shOut.close();
			shBuf.close();
			os.close();
			is.close();
			qtClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void requestImg(Socket client2,PrintStream ps) throws IOException{
		if(client2.isConnected()){
			//ps.print("RequestImg");
			ps.print("r");
		}			
	}
	 public static int count;
	 static byte buffer[]=new byte[1360]; 
	 static char bufChar[] = new char[1360];
	 static byte temp[]=new byte[1360];
	 static boolean FLAG_READ_PIC_COMPLETE =false;
	 static FileOutputStream fos=null;
	
	 public static Bitmap getPicByTCP(){
		 Bitmap bitmap = null;    
		 FileInputStream fis=null;
		 int waitSec = 10;
		 /*file*/
		 String path;
		 String SDPATH=Environment.getExternalStorageDirectory()+"/";
		 dir=new File(SDPATH+"zigbeeTCP/");
		 if(!dir.exists())
			 dir.mkdir();
		 path=dir.getAbsolutePath()+"/temp"+ImgNum+".jpg";
		 File file=new File(path);
		 imgShowNum=ImgNum;
		 ImgNum++;
		 ////
			if((null==qtClient)||(!qtClient.isConnected())){	//未建立 或未曾连接过
				//AlarmListenerService.this.isConnectedTCP();
				//Toast.makeText(MyClientDemo.this, "未连接到智能家居系统", Toast.LENGTH_SHORT).show();
				return null;
			}
				try {
					System.out.println("Current line:"+HomeClientDemo.getLineNumber(new Exception()));
					if(!file.exists())
						file.createNewFile();
					fos=new FileOutputStream(file);
					mutexEnble=false;
					requestImg(qtClient,shOut);		//请求实时图片
					Thread.sleep(2000);
					readPicBlock();
/*					while(!FLAG_READ_PIC_COMPLETE &&waitSec--!=0)
						Thread.sleep(3000);*/
					mutexEnble=true;
					fos.flush();
					count = 0;
					//is.reset();
					if(FLAG_READ_PIC_COMPLETE){
						bitmap=BitmapFactory.decodeFile(path);
						System.out.println("read pic success");
					}
					if(!(bitmap==null)){
						//@w monitor_pic.setImageBitmap(bitmap);
						return bitmap;
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		 return null;
	}
	//public static int sum;
	public static void readPicBlock(){
		long size;
		int sum = 0;
		try {
			byte[] tmp = new byte[4];
			count = is.read(tmp);
			long t = 0;
			for(int i =0;i<4;i++){
				
				t = (long)tmp[i] &0xff;
				System.out.println("c : "+t);
			}
			if(count !=4)
				return;
			else{
				size = (long)HelpUtils.bytesToLong(tmp);
				System.out.println("size is "+ size);
			}
			buffer = new byte[1360];
			count=is.read(buffer, 0, buffer.length);
			//isReader.read(bufChar, 0, bufChar.length);
			while(true)  
			{  
				System.out.println("while count :"+count + "buffer[0]:"+ buffer[0]+ "buffer[1]" +buffer[1]);
				if(count==-1){//||(count!=1360)){
					System.out.println("eof or error");
					//fos.write(buffer, 0, count);
					break;
				}
				fos.write(buffer,0,count);
				sum += count;
				if(sum >=(size )){//if(/*sum > 46000 &&*/ count !=1360){
					System.out.println("enough ");
					break;
				}
				//count=is.read(temp);
				//Skipped 151 frames!  The application may be doing too much work on its main thread.
				// * 
				count=is.read(buffer, 0, 1360);
				//Thread.sleep(3);
			}
			//mutexEnble=true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			HomeClientDemo.getLineNumber(new Exception());
			e.printStackTrace();
		}
		HomeClientDemo.getLineNumber(new Exception());
		FLAG_READ_PIC_COMPLETE = true;
		//temp = null;
		
	}
	public static void readPicBlock2() throws IOException{
/*		 int count = 0;
		  while (count == 0 ) {
		   count = is.available();
		   System.out.println("available : is  "+ count);
		  }
		  byte[] b = new byte[count];
		  is.read(b);*/
		int len =0;
		while((len=is.read())!=-1){
			fos.write(len);
		} 
	}
	 
	 
	public static boolean isConnectedTCP(){
		return (qtClient!=null&&qtClient.isConnected()&&qtClient.isClosed());
	}
	



}
