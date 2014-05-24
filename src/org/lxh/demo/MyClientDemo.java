package org.lxh.demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import zigbeeNet.DeviceInfo;
import zigbeeNet.NodeInfo;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

//import RouteStruct;
//待完善,socket超时(放到线程,线程放到service), stream流读超时超次数,,

//private class SmartHomeTxData{
//}

public class MyClientDemo extends Activity {
	private Button tcp_connect = null;									// 定义按钮组件
	private TextView info = null;								// 定义文本组件
	private Button mode =null,buttonGate=null,buttonAlarm=null,button_lights=null,realPic=null,buttonSecure=null;
	private TextView smInfoText;
	private Spinner wiRingSpin = null,lightGroups=null;
	private ArrayAdapter<String> adapter,lightAdapter,eventAdapter;
	public static final String[] wir_str_temp={"电器A","电器B","电器C","电器D","电器E","电器F","点击开关电器"};
	private static final String[] wirings={"电器A","电器B","电器C","电器D","电器E","电器F","点击开关电器"};
	private static final String[] light_str={"灯光组A","灯光组B"};
	private static final String[] sw={"(关闭)","(打开)"};
	public static final byte CATE_GATE=(byte) 0xa2,CATE_LIGHT=(byte) 0xa1,CATE_EF=(byte)0xef
			,CATE_EE=(byte) 0xee,CATE_ED=(byte)0xeb,FLAG_REMOTE_STATE_CATE_TODO=(byte)0xb0;
	static byte CATE_WIRING=(byte) 0xa0,CATE_IR=(byte)0xa4;
	public static String IP="10.0.136.142";
	public static int PORT=7838,qtPORT = 7070;//7839;
	public static final String ALARM_LISTENER_ACTION="ALARMLISTENERSERVICE";
	public static final int UI_MESG_ALARM = 1;
	public static final int UI_MESG_UPDATE_TOPO = 2;
	public static final int UI_MESG_TIP		=3;
	public final static String[] DEVTYPESTR 	= {"无","蓝色","green"};
	public final static String[] SENSORTYPESTR 	= {"温湿度","人体红外","可然气体","none1","none2","引脚信息","six","门禁"};
	public final static String[] SENSORSTATUS 	= {"正常","警告"};
	public final static long SENSORTYPE_WENSHI =0;
	public final static long SENSORTYPE_RF		=1;
	public final static long SENSORTYPE_SMOG	=2;
	public final static long SENSORTYPE_PINS	=5;
	public final static long SENSORTYPE_RFID =7;
/*tools*/
	Calendar calendar;
	public Handler messageHandler;
	public static boolean secureSW = false;
	public static boolean doorTipsSW = false;
/* views */
	//private boolean con_staus=false,outMode=false;

	PopupWindow popWin,alarmWin,outTodoWin;
	View popView,alarmView,outTodoView;
	ToggleButton alarmSW;
	ToggleButton light[]=null;
	
	Button pic_cancel,testButton,out_todo_button1;
	TextView textTemp,textHumi;
	Spinner event_wiring;
	CheckBox out_todo_check1;
	EditText out_todo_et;
/*remote info*/	
	private int str_num=6;
	private TCPClient client;
	public static int lightGroupNum=2;
	int wir_sws[]={0,1,1,0,1,0,0};
	public static byte lightStates[]=null;
	public static byte WIRING_ACTION_OPEN=1;
	//private monitorPicListener mp;
	public static int i;
	boolean opened;
	//static boolean mutexEnble=true;
	static byte cut_P6=0;
	static boolean checkChangeble=true;
	private boolean alarmToggle = false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.main);							// 调用布局
		this.tcp_connect = (Button) super.findViewById(R.id.send);		// 取得组件
		this.mode = (Button) super.findViewById(R.id.mode);				// 取得组件
		this.button_lights = (Button) super.findViewById(R.id.button_lights);	// 取得组件
		this.buttonAlarm = (Button) super.findViewById(R.id.button_alarm);		// 取得组件
		this.info = (TextView) super.findViewById(R.id.info);			// 取得组件
		textTemp = (TextView)super.findViewById(R.id.text_temp);
		textHumi = (TextView)super.findViewById(R.id.text_humi);
		smInfoText=(TextView)findViewById(R.id.home_state);				//将可选内容与ArrayAdapter连接
		smInfoText.setTextColor(Color.BLACK);
		realPic=(Button)findViewById(R.id.real_time_pic);
		//mp=new monitorPicListener();
		
		findViewById(R.id.door_rfid).setOnClickListener(new TestButtonListener());
		
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,wirings); 		//设置下拉列表风格 
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 		//将adapter添加到spinner中 
		//wiRingSpin.setAdapter(adapter); 		//添加Spinner事件监听 
		//some test
		((Button) super.findViewById(R.id.button2)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				smInfoText.setText(" ");
			}
		});
		//实时图片
		realPic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i=new Intent(MyClientDemo.this,RealPicActivity.class);
				startActivity(i);
			}
		});
		//连接按钮
		this.tcp_connect.setOnClickListener(new ConnectOnClickListenerImpl());
		//外出模式
		this.mode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

			}
		});
		//灯光控制listneer
		this.button_lights.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte temp;
/*				cut_P6=lightStates[0];
				temp=cut_P6;
				int ret=0;
				for(int i=0;i<8;i++){
					ret=((temp&0xff)&((int)0x1));
					checkChangeble=false;
					if(ret>=1){
						light[i].setChecked(true);
					}else
						light[i].setChecked(false);
					checkChangeble=true;
					temp >>>= 1;
				}*/
				popWin.showAtLocation(button_lights, Gravity.CENTER, 0, 0);
			}
		});
		/*定时事件*/
		buttonAlarm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LayoutInflater inflater=LayoutInflater.from(MyClientDemo.this);
				LayoutParams p=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
				alarmView=inflater.inflate(R.layout.alarm_layout, null);
				alarmWin=new PopupWindow(MyClientDemo.this);
				alarmWin.setContentView(alarmView);
				alarmWin.setWidth(LayoutParams.FILL_PARENT);
				alarmWin.setHeight(LayoutParams.WRAP_CONTENT);
				alarmWin.setAnimationStyle(android.R.style.Animation_Dialog);
				alarmWin.setFocusable(true);
				alarmWin.showAtLocation(button_lights, Gravity.CENTER, 0, 0);				
				alarmSW=(ToggleButton) alarmView.findViewById(R.id.alarmtoggle);
				event_wiring=(Spinner) alarmView.findViewById(R.id.event_wiring_spinner);
				
				eventAdapter=new ArrayAdapter<String>(MyClientDemo.this,android.R.layout.simple_spinner_item,wir_str_temp); 		//设置下拉列表风格 
				eventAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 		//将adapter添加到spinner中 
				event_wiring.setAdapter(eventAdapter); 		//添加Spinner事件监听 
				
				alarmSW.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						// TODO Auto-generated method stub
						if(!isChecked){
							Intent intent = new Intent(MyClientDemo.this,AlarmReceiver.class);
							PendingIntent pendingIntent = PendingIntent.getBroadcast(MyClientDemo.this, 0, intent, 0);
							//获取闹钟管理器
							AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
							alarmManager.cancel(pendingIntent);
							Toast.makeText(MyClientDemo.this, "闹钟已经取消!", Toast.LENGTH_SHORT).show();
						}
			
					}
				});
				//时间按钮
				((Button)alarmView.findViewById(R.id.timesetter)).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						calendar =Calendar.getInstance();
						calendar.setTimeInMillis(System.currentTimeMillis());
						new TimePickerDialog(MyClientDemo.this,new TimePickerDialog.OnTimeSetListener() {
							@Override
							public void onTimeSet(TimePicker arg0, int h, int m) {
							//更新按钮上的时间
								//timeBtn.setText(formatTime(h,m));
								//设置日历的时间，主要是让日历的年月日和当前同步
								calendar.setTimeInMillis(System.currentTimeMillis());
								//设置日历的小时和分钟
								calendar.set(Calendar.HOUR_OF_DAY, h);
								calendar.set(Calendar.MINUTE, m);
								//将秒和毫秒设置为0
								calendar.set(Calendar.SECOND, 0);
								calendar.set(Calendar.MILLISECOND, 0);
								//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 10*1000, pendingIntent);
								Toast.makeText(MyClientDemo.this, "设置闹钟的时间为："+String.valueOf(h)+":"+String.valueOf(m), Toast.LENGTH_SHORT).show();
								alarmSW.setChecked(true);
								//Log.d(TAG, "set the time to "+formatTime(h,m));
							}
						}
						,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true)
							.show();

					}
					
				});
				//确定按钮
				((Button)alarmView.findViewById(R.id.alarm_ok)).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//建立Intent和PendingIntent来调用闹钟管理器
						if(AlarmListenerService.isConnect==false){
							getLineNumber(new Exception());
							toastShow(MyClientDemo.this, "未连接");
						}
						Intent intent = new Intent(MyClientDemo.this,AlarmReceiver.class);
						intent.putExtra("electric", ((CheckBox)alarmView.findViewById(R.id.event_wiring)).isChecked());
						intent.putExtra("electric_num", event_wiring.getSelectedItemPosition());
						lightStates[0]&=(~((1<<3)&0xff));
						if(((CheckBox)alarmView.findViewById(R.id.lights_event)).isChecked()){
							//@假设台灯在 0组 第3个.
							//在android中已经设为打开..
							//@wei
							//待完善的写法是把状态等信息保存在Service中,所有改变通过Service改变.
							lightStates[0]|=(byte)((1<<3)&0xff);
							intent.putExtra("openLight", true);
						}else{
							intent.putExtra("openLight", false);
						}
						intent.putExtra("room_light",lightStates[0]);
						PendingIntent pendingIntent = PendingIntent.getBroadcast(MyClientDemo.this, 0, intent, 0);
						//获取闹钟管理器
						AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
						//设置闹钟
						alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
						if(AlarmListenerService.isConnect)
							getLineNumber(new Exception());
						alarmWin.dismiss();
					}
				});
				//取消按钮
				((Button)alarmView.findViewById(R.id.alarm_cancel)).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						//建立Intent和PendingIntent来调用闹钟管理器
						alarmWin.dismiss();
					}
				});
				//事件spinner

		System.out.println("readly to connet");
	
		}
	});
		//安防
		buttonSecure = (Button)findViewById(R.id.button_secure);
		buttonSecure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TCPClient.prev = 0;
				secureSW = !secureSW;
				buttonSecure.setText("安防系统"+sw[HelpUtils.bToI(secureSW)]);
			}
		});
		//灯光控制
		fillLightView();
		//fill_pic_view();
		fillOutTodoView();
		/*测试按钮*/
		((Button)findViewById(R.id.out_todo)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//manager.notify(1, notification);
				outTodoWin.showAtLocation(button_lights, Gravity.CENTER, 0, 0);
/*				if(client!=null)
					client.Client_Send(TCPClient.CLIENT_COMMAND_GETNWKINFO);
				else
					System.out.println("Activity\t# null point");*/
			}
		});
		Looper looper =Looper.getMainLooper();
		messageHandler = new MessageHandler(looper);

		/*for test*/
		long a = -1626775014L;
		initNotification();
		initVoiceRecognition();
		//new Long(a).byteValue();
		//byte[] teset = longToByte(a);
		//System.out.println("a.length = "+a. + "b.length");
	}



	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	/**
	 * light listener
	 * */
	public void fillLightView(){
		light=new ToggleButton[9];
		lightStates=new byte[8];
		ToggleButton temp;
		LayoutInflater inflater=LayoutInflater.from(MyClientDemo.this);
		LayoutParams p=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		popView=inflater.inflate(R.layout.lights_layout, null);
		popWin=new PopupWindow(MyClientDemo.this);
		popWin.setContentView(popView);
		popWin.setWidth(LayoutParams.MATCH_PARENT);
		popWin.setHeight(LayoutParams.WRAP_CONTENT);
		popWin.setAnimationStyle(android.R.style.Animation_Dialog);
		popWin.setFocusable(true);
		lightGroups=(Spinner)popView.findViewById(R.id.lightsspinner); 
		lightAdapter=new ArrayAdapter<String>(MyClientDemo.this,android.R.layout.simple_spinner_item,MyClientDemo.this.light_str); 		//设置下拉列表风格 
		lightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 		//将adapter添加到spinner中 
		lightGroups.setAdapter(lightAdapter); 		//添加Spinner事件监听 
		((Button)popView.findViewById(R.id.pop_back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popWin.dismiss();
			}
		});
		System.out.println("2");
		light[0]=(ToggleButton)popView.findViewById(R.id.toggleButton1);
		light[1]=(ToggleButton)popView.findViewById(R.id.toggleButton2);//tooggleButton1+2
		light[2]=(ToggleButton)popView.findViewById(R.id.toggleButton3);
		light[3]=(ToggleButton)popView.findViewById(R.id.toggleButton4);
		light[4]=(ToggleButton)popView.findViewById(R.id.toggleButton5);
		light[5]=(ToggleButton)popView.findViewById(R.id.toggleButton6);
		light[6]=(ToggleButton)popView.findViewById(R.id.toggleButton7);
		light[7]=(ToggleButton)popView.findViewById(R.id.toggleButton8);
		for(i=0;i<8;i++){
			light[i].setOnCheckedChangeListener(new OnCheckedChangeListener() {
				int index=i;
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(checkChangeble){
						lightCheckChange(index,isChecked);
					}
					
				}
			});
			System.out.println("for "+i);
		}
		System.out.println("4");
		return ;
	}
	
	public void fillOutTodoView(){
		LayoutInflater inflater=LayoutInflater.from(MyClientDemo.this);
		LayoutParams p=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		outTodoView=inflater.inflate(R.layout.out_todo_layout, null);
		outTodoWin=new PopupWindow(MyClientDemo.this);
		outTodoWin.setContentView(outTodoView);
		outTodoWin.setWidth(LayoutParams.MATCH_PARENT);
		outTodoWin.setHeight(LayoutParams.WRAP_CONTENT);
		outTodoWin.setAnimationStyle(android.R.style.Animation_Dialog);
		outTodoWin.setFocusable(true);
		
		out_todo_button1=(Button)outTodoView.findViewById(R.id.out_todo_button1); 
		out_todo_check1=(CheckBox)outTodoView.findViewById(R.id.out_todo_check1);
		//出门备忘
		out_todo_button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
/*				byte cmd1[] = {(byte) MyClientDemo.CATE_EE,0x05,FLAG_REMOTE_STATE_CATE_TODO,(byte)0,(byte)0};
				if(out_todo_check1.isChecked()){
					cmd1[3]=(byte)0x1;
				}*/
				doorTipsSW = out_todo_check1.isChecked();
				String temp=((EditText)outTodoView.findViewById(R.id.out_todo_context_editText)).getText().toString();
				AlarmListenerService.notificationShowStr=temp;
				/*if(MyClientDemo.sendCmdByTCP(MyClientDemo.this,cmd1, "OK")){
					System.out.println("cmdreturn #: todo sw open success!");
				}*/
				outTodoWin.dismiss();
			}
		});
		return ;
	}
	
/*	public static String getGateWay() {
		if (Netgear_WifiManager.wifiManager != null) 
		{
		DhcpInfo   dhcpInfo=Netgear_WifiManager.wifiManager.getDhcpInfo();
					Log.e("gateway is ",
							Netgear_IpAddressTranfer.long2ip(dhcpInfo.gateway));
				}
				return null;
	}*/

	public void lightCheckChange(int lightIndex ,boolean isChecked){
		int a = 0;
		for(int i = 0 ;i<8;i++){
			a|=HelpUtils.bToI(light[i].isChecked())<<i;
		}
		MyClientDemo.state = a;
		System.out.println("a is "+ Integer.toBinaryString(a));
		if(client!=null)
			client.clientSendCommand(TCPClient.CLIENT_COMMAND_SETSENSOR);
	}
	public static long addr = 0,state = 0;
	
	
	private class ConnectOnClickListenerImpl implements OnClickListener{

		@Override
		public void onClick(View view) {
			String rx_buf = null;
			byte temp[] = null;
			//Intent i=new Intent(MyClientDemo.this,AlarmListenerService.class);
			//i.setAction(AlarmListenerService.TCP_SERVICE_ACTION_CONNECT);
			//startService(i);
			//@wei
			//wait Socket create successfully.
			if(null == client){
				tcp_connect.setText("TCP连接（已连接）");
				new Thread(
						new Runnable() {
							@Override
							public void run() {
								try {
		//连接两个socket				//AlarmListenerService.connectQtServer();
									client = new TCPClient(IP,PORT,messageHandler);
									if(client == null)
										Toast.makeText(MyClientDemo.this, "Server error", Toast.LENGTH_SHORT).show();
									Thread.sleep(1000);//等待初始化完成
									client.clientSendCommand(TCPClient.CLIENT_COMMAND_GETNWKINFO);
									while(client!=null){
										if(client!=null){
											client.clientSendCommand(TCPClient.CLIENT_COMMAND_GETNWKINFO);
											//client.clientSendCommand(TCPClient.CLIENT_COMMAND_CLEARINT);
										}
										Thread.sleep(2000);
									}
									//mutexEnble=true;
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
					}).start();
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				tcp_connect.setText("TCP连接（断开）");
				client.close();
				client = null;
			}
/*			if(AlarmListenerService.isConnect){
				MyClientDemo.this.tcp_connect.setText("已连接(点击关闭连接)");
				Toast.makeText(MyClientDemo.this, "已连接到智能家居系统", Toast.LENGTH_SHORT).show();
			}else{
				MyClientDemo.this.tcp_connect.setText("tcp连接");
				Toast.makeText(MyClientDemo.this, "已关闭连接", Toast.LENGTH_SHORT).show();
			}
			在需要是连接
			*/
			
		}

	}
	
	public static void toastShow(Context c,String str){
		Toast.makeText(c, str, Toast.LENGTH_SHORT).show();
	}
	public final static String SER_KEY	 = "com.lxh.demo.ser";
	public final static String SER_STATE = "com.lxh.demo.STATE";
	public final static String SER_ADDR  = "com.lxh.demo.ADDR";
	
	private class TestButtonListener implements OnClickListener{
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//client 的所有子类也都要serializable  此方法不通
/*			Intent i =new Intent(MyClientDemo.this,VoiceRecognition.class);
			MyClientDemo.this.startActivity(i);*/
			//挂失这张卡
			LayoutInflater inflater2=LayoutInflater.from(MyClientDemo.this);
			View popView2=inflater2.inflate(R.layout.card_manager, null);
			Dialog popwin=new AlertDialog.Builder(MyClientDemo.this).setView(popView2).create();
			
			popwin.show();
		}
		
	}
	//private void 

    /**
    *得到Exception所在代码的行数
    *如果没有行信息,返回-1
    */
    public static int getLineNumber(Exception e){
    StackTraceElement[] trace =e.getStackTrace();
    if(trace==null||trace.length==0) return -1; //
    System.out.println("line :"+trace[0].getLineNumber());
    return trace[0].getLineNumber();
    }

    //子类化一个Handler
    class MessageHandler extends Handler {
        public MessageHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
        	
        	switch(msg.arg1){
        	case UI_MESG_ALARM:
        		nb.setContentText((String)msg.obj);
        		notification = nb.getNotification();
        		manager.notify(1, notification);
        		break;
        	case UI_MESG_UPDATE_TOPO:
        		ArrayList<NodeInfo> nodeInfos = (ArrayList<NodeInfo>)msg.obj;
        		String s = processNodeInfos(nodeInfos);
        		smInfoText.setText(s/*(String) msg.obj*/);
        		break;
        	case UI_MESG_TIP:
        		nb.setContentText((String)msg.obj);
        		notification = nb.getNotification();
        		manager.notify(1, notification);
        		break;
        	
        	}
        }
    }
    /**
     * 处理节点链表 更新ui
     * @param nodeInfos
     * @return
     */
    private String processNodeInfos(ArrayList<NodeInfo> nodeInfos){
		Iterator<NodeInfo> nodeInfo = nodeInfos.iterator();
	    float C1=-4.0f; // for 12 Bit
	    float C2= 0.0405f; // for 12 Bit
	    float C3=-0.0000028f; // for 12 Bit
	    float T1=0.01f; // for 14 Bit @ 5V
	    float T2=0.00008f; // for 14 Bit @ 5V
	    String show = null;
//System.out.println("nodeInfos size ="+ nodeInfos.size());
		while(nodeInfo.hasNext()){
			NodeInfo ni = nodeInfo.next();
			DeviceInfo di = ni.devinfo;
			//System.out.println("devInfo.sensortype"+ di.sensortype);
			//System.out.println("test  di.devtype:"+  di.devtype);
			if(di.devtype==1 || di.devtype == 2)
				show= show + new String("节点" + ni.num+":\t设备类型："+DEVTYPESTR[(int) di.devtype]+"\t传感器类型："+SENSORTYPESTR[(int) di.sensortype]);
/*sensor process */				
			if(di.sensortype == SENSORTYPE_RF||di.sensortype ==SENSORTYPE_SMOG){
				show = show +"\t传感器状态："+SENSORSTATUS[(int) di.sensorvalue];
System.out.println("LINE 455 \t # di.sensorvalue"+ di.sensorvalue);
				if(di.sensorvalue == 1){//警告
					ProcessAlarm(di.sensortype,di.sensorvalue);
				}
			}else if(di.sensortype == SENSORTYPE_WENSHI){//温湿度 高地位分割  见 zigbee节点编程
				long temp,humi;
				byte[] tmp = HelpUtils.longToBytes(di.sensorvalue);
				temp =  tmp[0]*256+tmp[1];
				humi =  tmp[2]*256 + tmp[3];
				float tempValue = (float)temp;
				tempValue = (tempValue*(float)0.01-(float)42);
				float humiValue = (float)humi;

			    float rh_lin=C3*humiValue + C2*humiValue + C1; //calc. Humidity from ticks to [%RH]
			    float rh_true=(tempValue-25)*(T1+T2*humiValue)+rh_lin; //calc. Temperature compensated humidity [%RH]
				humiValue = rh_true;
				System.out.println("float is "+tempValue +tmp[0]+" " + tmp[1]);
				String textShow = new String("\t温度" + tempValue +"\t湿度："+humiValue);
				textHumi.setText(textShow);
				show = show + textShow;
				//float t=*p_temperature; // t: Temperature [Ticks] 14 Bit
			}else if(di.sensortype == SENSORTYPE_PINS){
				
				System.out.println("pins value is "+ Long.toHexString(di.sensorvalue));
				addr = di.nwkaddr;
				int temp = (int) (di.sensorvalue & 0x0000000000ff0000)>>16;
				show = show + new String("pin:"+"0x"+Long.toHexString(temp));
System.out.println("temp : "+ Integer.toHexString(temp).toString());
				for(int i=0;i<8;i++){		
					int ret=((temp&0xff)&((int)0x1));
					checkChangeble=false;
					if(ret>=1){
						light[i].setChecked(true);
					}else
						light[i].setChecked(false);
					checkChangeble=true;
					temp >>>= 1;
				}
			}else if(di.sensortype == SENSORTYPE_RFID){
				
			}else{
				show = show +new String("传感器值："+ di.sensorvalue);
			}
/*sensor process END*/	
			show = show +"\n";
		}
		return show;
    }
    
	private int count;
	/**
	 * Unable to alarm in half of hour; 
	 * @param sensorvalue
	 */
	private long now;
	private int doorTipCount =0;
	public void ProcessAlarm(long type,long sensorvalue) {
		// TODO Auto-generated method stub
		//TCPClient.this.Client_Send(CLIENT_COMMAND_CLEARINT);
		Handler UIhandler = messageHandler;
		now = System.currentTimeMillis();

        if(UIhandler!=null){
	        Message childMsg = UIhandler.obtainMessage();
	        /* 安防警告 15分钟之内不再触发 */
			if(MyClientDemo.secureSW && (TCPClient.prev + 15*60*1000) < now){
				TCPClient.prev = now;
System.out.println("ProcessAlarm \t:警告！！");
				
		        childMsg.arg1 = MyClientDemo.UI_MESG_ALARM;
		        childMsg.obj = "智能家居系统收到警告！来自传感器：" + SENSORTYPESTR[(int) type];
	        	UIhandler.sendMessage(childMsg);
		        
			}
			
        	if(MyClientDemo.doorTipsSW){
        		doorTipCount++;
        		if(doorTipCount >=2){
	        		childMsg.arg1 = MyClientDemo.UI_MESG_TIP;
	        		childMsg.obj  = "智能家居出门提醒："+AlarmListenerService.notificationShowStr;
	        		UIhandler.sendMessage(childMsg);
	        		doorTipCount = 0;
	        	}
        		
			}
        }
	}

	private NotificationManager manager;
	private Notification notification;
	private Notification.Builder nb;
    public void initNotification() {
		manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		/*		// 创建一个Notification
		notification = new Notification();
		notification.icon =android.R.drawable.ic_menu_today;
		// 当当前的notification被放到状态栏上的时候，提示内容
		notification.tickerText = "日程：";
		notification.defaults=Notification.DEFAULT_SOUND;
		notification.audioStreamType= android.media.AudioManager.ADJUST_LOWER;
	// 点击状态栏的图标出现的提示信息设置
		//notification.setLatestEventInfo(this.context, c.getContent()+" ","  ", pendingIntent);
		//notification.contentIntent = notificationIntent;
		notification.setLatestEventInfo(MyClientDemo.this, "SMARTHOME 提醒您:", str, pendIntent);
		//manager.notify(1, notification);
		notification.*/
		Intent intent = new Intent(MyClientDemo.this, RealPicActivity.class);
		PendingIntent pendIntent = PendingIntent.getActivity(MyClientDemo.this, 0, intent, 0);  
    	
		nb = new Notification.Builder(MyClientDemo.this)
         .setDefaults((Notification.DEFAULT_SOUND))
         .setContentTitle("收到来自智能家居的消息")
         .setContentText("来自传感器：")
         .setSmallIcon(android.R.drawable.ic_menu_today)
         .setContentIntent(pendIntent);
        notification = nb.getNotification();
	}
    
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private void initVoiceRecognition(){
    	Button speakButton = (Button) findViewById(R.id.mode);
        // Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            speakButton.setOnClickListener(new VoiceRecognitionsListener());
        } else {
            speakButton.setEnabled(false);
            speakButton.setText("Recognizer not present");
        }
    }
    
    private class VoiceRecognitionsListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
	        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
	        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
		}
    	
    }
    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            /*mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    matches));*/
            for(String tmp:matches){
            	System.out.println("tmp :"+ tmp);
            	if(tmp.contains("开灯")){
//            	if(tmp.startsWith("开灯")){
            		System.out.println("onActivityResult open lights");
            		if(client!=null){
            			
            			state = 0xff;
            			client.clientSendCommand(TCPClient.CLIENT_COMMAND_SETSENSOR);
            		}
            		return;
            	}else if(tmp.equals("关灯")){
            			//MyClientDemo.sendCmdByTCP(VoiceRecognition.this,cmd,"OK");
            		state = 0;
            		if(client!=null)
            			client.clientSendCommand(TCPClient.CLIENT_COMMAND_SETSENSOR);
            		return;
            	}else{
            		Toast.makeText(MyClientDemo.this, "未识别指令", Toast.LENGTH_SHORT).show();
            	}
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}




/**
 * 规划：
 * 两个socket
 * 
*/
