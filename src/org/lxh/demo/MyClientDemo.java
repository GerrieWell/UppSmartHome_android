package org.lxh.demo;

import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
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
	private Button mode =null,buttonGate=null,buttonAlarm=null,button_lights=null,realPic=null;
	private EditText smInfoText;
	private Spinner wiRingSpin = null,lightGroups=null;
	private ArrayAdapter<String> adapter,lightAdapter,eventAdapter;
	public static final String[] wir_str_temp={"电器A","电器B","电器C","电器D","电器E","电器F","点击开关电器"};
	private static final String[] wirings={"电器A","电器B","电器C","电器D","电器E","电器F","点击开关电器"};
	private static final String[] light_str={"灯光组A","灯光组B"};
	private static final String[] sw={"(关闭)","(打开)"};
	public static final byte CATE_GATE=(byte) 0xa2,CATE_LIGHT=(byte) 0xa1,CATE_EF=(byte)0xef
			,CATE_EE=(byte) 0xee,CATE_ED=(byte)0xeb,FLAG_REMOTE_STATE_CATE_TODO=(byte)0xb0;
	static byte CATE_WIRING=(byte) 0xa0,CATE_IR=(byte)0xa4;
	public static String IP="10.0.136.249";
	public static int PORT=7838;
	public static final String ALARM_LISTENER_ACTION="ALARMLISTENERSERVICE";
/*tools*/
	Calendar calendar;
	private Handler messageHandler;


/* views */
	//private boolean con_staus=false,outMode=false;

	PopupWindow popWin,alarmWin,outTodoWin;
	View popView,alarmView,outTodoView;
	ToggleButton alarmSW;
	ToggleButton light[]=null;
	
	Button pic_cancel,testButton,out_todo_button1;
	Spinner event_wiring;
	CheckBox out_todo_check1;
	EditText out_todo_et;
/*remote info*/	
	private int str_num=6;
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
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.main);							// 调用布局
		this.tcp_connect = (Button) super.findViewById(R.id.send);		// 取得组件
		this.mode = (Button) super.findViewById(R.id.mode);				// 取得组件
		this.buttonGate = (Button)this.findViewById(R.id.button_gate);	// 取得组件
		this.button_lights = (Button) super.findViewById(R.id.button_lights);	// 取得组件
		this.buttonAlarm = (Button) super.findViewById(R.id.button_alarm);		// 取得组件
		this.info = (TextView) super.findViewById(R.id.info);			// 取得组件
		wiRingSpin=(Spinner)findViewById(R.id.wiRingSpin); 
		smInfoText=(EditText)findViewById(R.id.home_state);				//将可选内容与ArrayAdapter连接 
		realPic=(Button)findViewById(R.id.real_time_pic);
		//mp=new monitorPicListener();
		
		findViewById(R.id.test).setOnClickListener(new VoiceRecognitionListener());
		
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,wirings); 		//设置下拉列表风格 
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 		//将adapter添加到spinner中 
		wiRingSpin.setAdapter(adapter); 		//添加Spinner事件监听 
		//some test
		byte cc=(byte) String.valueOf(3).toCharArray()[0];
		System.out.print("value c : %c"+cc);
		
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
				// TODO Auto-generated method stub
				//outMode=!outMode;
				if(!AlarmListenerService.outMode){
					MyClientDemo.this.mode.setText("关闭外出模式");
					
					for(int i=0;i<lightGroupNum;i++){
					//关闭所有灯
						byte cmd1[] = {MyClientDemo.CATE_EF,0x05,MyClientDemo.CATE_LIGHT,(byte)(lightGroupNum)
								,(byte)0x0};
						//MyClientDemo.sendCmdByTCP(MyClientDemo.this,cmd1, "OK");
					}
					for(int i=0;i<2;i++){
					//目前只有两个电器//adapter.getCount()
						byte cmd2[] = {MyClientDemo.CATE_EF,0x05,CATE_WIRING,(byte)( i)
								,(byte)0};
						//MyClientDemo.sendCmdByTCP(MyClientDemo.this,cmd2, "OK");
					}
					//打开红外安防
					//byte cmd3[]={MyClientDemo.CATE_EF,0x05,CATE_IR,(byte)(0),(byte)1};
					//MyClientDemo.sendCmdByTCP(MyClientDemo.this,"Rsecurity on", "ONOK");
					
					Toast.makeText(MyClientDemo.this, "关闭所有电器以节能", Toast.LENGTH_SHORT).show();
					Toast.makeText(MyClientDemo.this, "已打开安防系统", Toast.LENGTH_SHORT).show();
				}else{
					//关闭红外安防
					//byte cmd3[]={MyClientDemo.CATE_EF,0x05,CATE_IR,(byte)(0),(byte)0};
					//MyClientDemo.sendCmdByTCP(MyClientDemo.this,"Rsecurity off", "OFFOK");
					Toast.makeText(MyClientDemo.this, "已关闭安防系统", Toast.LENGTH_SHORT).show();
					MyClientDemo.this.mode.setText("打开外出模式");
				}
				AlarmListenerService.outMode=!AlarmListenerService.outMode;
			}
		});
		wiRingSpin.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if(arg2==str_num){
					arg0.setVisibility(View.VISIBLE);
					return;
				}
				if(wir_sws[arg2]!=0)
					wir_sws[arg2]=0;
				else
					wir_sws[arg2]=1;
				wirings[arg2]=wir_str_temp[arg2]+sw[wir_sws[arg2]];
				wiRingSpin.setSelection(str_num);
				arg0.setVisibility(View.VISIBLE);
				//发送命令 
				//构造数据包: ef(包标示) 05(包长) a0(电器) arg2(电器n) wir_sws(开关,0表示关,1~255表示动作)
				//响应: OK
				byte cmd1[] = {MyClientDemo.CATE_EF,0x05,CATE_WIRING,(byte)(arg2)
						,(byte)(wir_sws[arg2])};
				//MyClientDemo.this.sendCmdByTCP(MyClientDemo.this,cmd1, "OK");
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}

		});
		//灯光控制listneer
		this.button_lights.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte temp;
				cut_P6=lightStates[0];
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
				}
				popWin.showAtLocation(button_lights, Gravity.CENTER, 0, 0);
			}
		});
		
		buttonGate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(opened){
					byte cmd1[] = {(byte) MyClientDemo.CATE_EF,0x05,(byte) 0xa2,0,0};
					//MyClientDemo.sendCmdByTCP(MyClientDemo.this,cmd1, "OK");
					buttonGate.setText("打开车库大门");
					opened=false;
				}else{
					byte cmd1[] = {(byte) MyClientDemo.CATE_EF,0x05,(byte) 0xa2,0,1};
					//MyClientDemo.sendCmdByTCP(MyClientDemo.this,cmd1, "OK");
					buttonGate.setText("关闭车库大门");
					opened=true;
				}

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
		//刷新按钮
		((Button)findViewById(R.id.refresh_button)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//bf 刷新, 	ee 05 bf 0管脚信息   0   请求更新
				//			ee 05 bf 1/2被读管脚的节点   0   请求更新
				//bf 刷新 ,	ee 05 bf 0状态信息  0 返回节点数 
				new RefreshThread().start();
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
				//outTodoWin.showAtLocation(button_lights, Gravity.CENTER, 0, 0);
				long[] getTopuInfo = {0x15,0x01,0x0a};
				long [] ackArr = new long[128];
				AlarmListenerService.sendCmdByTCP(getTopuInfo, ackArr);
			}
		});
		Looper looper =Looper.getMainLooper();
		messageHandler = new MessageHandler(looper);

		//new ServerThread().start();
/*		if(!(AlarmListenerService.serviceRan))
			startService(new Intent(MyClientDemo.this,AlarmListenerService.class));*/
			//java.net.InetAddress x;
	    TCPClient client;
		new Thread(
				new Runnable() {
					@Override
					public void run() {
						try {
							TCPClient client = new TCPClient(IP,PORT);
							client.sendMsg("12345678 ");
							//mutexEnble=true;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			}).start();
	    
	    
/*	    try {
	            //x = java.net.InetAddress.getByName("qq324791986.3322.org");
	            //IP = x.getHostAddress();//得到字符串形式的ip地址
	    		//IP = "10.0.136.124";
	            System.out.println("IP is "+IP);
	    } catch (UnknownHostException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	    } */
	}
	/*
	 * 在 发送刷新命令后调用*/
	private boolean judgeUpdateAck(byte[] ack){
		if(ack!=null){
			if(ack[0]==(byte)0xee){
				//EE 00(cate) 00(Pn) 00(value) 97 0A 
				//ee 00 00 xy	'\r' '\n'// (标示) (节点index) (管脚组n) (管脚状态) (回车) (换行)
				/**
				 * 		    update_chars[1]=0;//route 0
            				update_chars[2]=0;//p6
				            update_chars[3]=rout_structs[0].pin6_state;//value */
				System.out.println("ready to update");
				if(ack[1]==(byte)0){
					lightStates[0]=(byte)ack[3];
					cut_P6=(byte)ack[3];
				}else if(ack[1]==(byte)0x1){
					lightStates[1]=(byte)ack[3];
				}
				//cut_P6=(byte)ack[3];
				int ret=0;
				ret=(lightStates[0]&((byte)0x40));
				
				if(ack[1]==(byte)0x00){
					if(ret>=1){
						wirings[0]=wir_str_temp[0]+sw[wir_sws[1]];
					}else
						wirings[0]=wir_str_temp[0]+sw[wir_sws[0]];
					
				}else if(ack[1]==(byte)0x01){
/*					if(ret>=1){
						wirings[1]=wir_str_temp[1]+sw[wir_sws[1]];
					}else
						wirings[1]=wir_str_temp[1]+sw[wir_sws[0]];*/
				}
			}
		}
		return AlarmListenerService.isConnect;
	}
	
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
/*		Intent intent=getIntent();
		int rx=intent.getIntExtra("alarm", 0);
		if(intent.getAction().endsWith(ALARM_LISTENER_ACTION)){
			monitorPicWin.setFocusable(true);
			if(client.isConnected()&&!client.isClosed()){
				MyClientDemo.this.getPicByTCP(client);
			} else {
				toastShow(MyClientDemo.this, "didn't connected");
			}
			//fill_pic_view();
			monitorPicWin.setFocusable(true);
			//monitorPicWin.showAtLocation(button_lights, Gravity.CENTER, 0, 0);
			pic_foced=true;
		}
		if(rx==1){
			if(((CheckBox)alarmView.findViewById(R.id.lights_event)).isChecked()){
	//@假设台灯在 0组 第3个.
				lightStates[0]|=(byte)(1<<3);
				byte cmd1[] = {(byte) MyClientDemo.CATE_EF,0x05,(byte) 0xa1,0,lightStates[0]};
				MyClientDemo.sendCmdByTCP(MyClientDemo.this,cmd1, "OK");
			}
			if(((CheckBox)alarmView.findViewById(R.id.curtain_event)).isChecked()){
				byte cmd2[] = {(byte) MyClientDemo.CATE_EF,0x05,(byte) 0xa4,0,0};
				//MyClientDemo.this.sendCmdByTCP(MyClientDemo.this,cmd2, "OK");							
			}
			if(((CheckBox)alarmView.findViewById(R.id.event_wiring)).isChecked()){
				int num=event_wiring.getSelectedItemPosition();
				byte cmd3[] = {(byte) MyClientDemo.CATE_EF,0x05,CATE_WIRING,(byte)( num)
						,1};
				MyClientDemo.sendCmdByTCP(MyClientDemo.this,cmd3, "OK");			
			}
		}*/
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
		popWin.setWidth(LayoutParams.FILL_PARENT);
		popWin.setHeight(LayoutParams.WRAP_CONTENT);
		popWin.setAnimationStyle(android.R.style.Animation_Dialog);
		popWin.setFocusable(true);
		lightGroups=(Spinner)popView.findViewById(R.id.lightsspinner); 
		lightAdapter=new ArrayAdapter<String>(MyClientDemo.this,android.R.layout.simple_spinner_item,MyClientDemo.this.light_str); 		//设置下拉列表风格 
		lightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 		//将adapter添加到spinner中 
		lightGroups.setAdapter(lightAdapter); 		//添加Spinner事件监听 
		lightGroups.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				byte temp;
				cut_P6=lightStates[position];
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
				}
				ret=(temp&((byte)0x1));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}


		});
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
		System.out.println("3");
		for(i=0;i<8;i++){
			System.out.print("i is "+i);
			light[i].setOnCheckedChangeListener(new OnCheckedChangeListener() {
				int index=i;
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(checkChangeble){
						lightCheckChange(index,isChecked);
						getLineNumber(new Exception());
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
		outTodoWin.setWidth(LayoutParams.FILL_PARENT);
		outTodoWin.setHeight(LayoutParams.WRAP_CONTENT);
		outTodoWin.setAnimationStyle(android.R.style.Animation_Dialog);
		outTodoWin.setFocusable(true);
		
		out_todo_button1=(Button)outTodoView.findViewById(R.id.out_todo_button1); 
		out_todo_check1=(CheckBox)outTodoView.findViewById(R.id.out_todo_check1);
		//出门备忘
		out_todo_button1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte cmd1[] = {(byte) MyClientDemo.CATE_EE,0x05,FLAG_REMOTE_STATE_CATE_TODO,(byte)0,(byte)0};
				//String temp=""
				
				if(out_todo_check1.isChecked()){
					cmd1[3]=(byte)0x1;
				}
				String temp=((EditText)outTodoView.findViewById(R.id.out_todo_context_editText)).getText().toString();
				System.out.println("temp 596:    "+temp);
				AlarmListenerService.notificationShowStr=temp;
				/*if(MyClientDemo.sendCmdByTCP(MyClientDemo.this,cmd1, "OK")){
					System.out.println("cmdreturn #: todo sw open success!");
				}*/
				outTodoWin.dismiss();
			}
		});
		return ;
	}

	public void lightCheckChange(int lightIndex ,boolean isChecked){
		int a,b;
		int lightGroupIndex;
		lightGroupIndex=lightGroups.getSelectedItemPosition();
		a=(int)lightStates[lightGroupIndex];
		b=(int)~(1<<lightIndex);
		a&=0xff;
		a=a&b;
		lightGroupIndex&=0xff;
		if(isChecked){
			//router1_P6=(byte)((int)router1_P6))|(1<<i);
			a=a|(1<<lightIndex);
			a&=0xff;
		}
		lightStates[lightGroupIndex]=(byte)a;
		byte cmd1[] = {(byte) MyClientDemo.CATE_EF,0x05,MyClientDemo.CATE_LIGHT,(byte)lightGroupIndex,lightStates[lightGroupIndex]};
		//MyClientDemo.sendCmdByTCP(MyClientDemo.this,cmd1, "OK");
	}

	
	
	private class ConnectOnClickListenerImpl implements OnClickListener{

		@Override
		public void onClick(View view) {
			String rx_buf = null;
			byte temp[] = null;
			//Intent i=new Intent(MyClientDemo.this,AlarmListenerService.class);
			//i.setAction(AlarmListenerService.TCP_SERVICE_ACTION_CONNECT);
			//startService(i);
			AlarmListenerService.TCPConnectFunction();
			//@wei
			//wait Socket create successfully.
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(AlarmListenerService.isConnect){
				MyClientDemo.this.tcp_connect.setText("已连接(点击关闭连接)");
				Toast.makeText(MyClientDemo.this, "已连接到智能家居系统", Toast.LENGTH_SHORT).show();
			}else{
				MyClientDemo.this.tcp_connect.setText("tcp连接");
				Toast.makeText(MyClientDemo.this, "已关闭连接", Toast.LENGTH_SHORT).show();
			}
		}

	}
	
	public static void toastShow(Context c,String str){
		Toast.makeText(c, str, Toast.LENGTH_SHORT).show();
	}

	private class VoiceRecognitionListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent i =new Intent(MyClientDemo.this,VoiceRecognition.class);
			MyClientDemo.this.startActivity(i);
		}
		
	}


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
            //处理收到的消息，把天气信息显示在title上
        	smInfoText.append((String) msg.obj);
        }
    }


    class RefreshThread extends Thread{
    	public void run(){

			// TODO Auto-generated method stub
			//bf 刷新, 	ee 05 bf 0管脚信息   0   请求更新
			//			ee 05 bf 1/2被读管脚的节点   0   请求更新
			//bf 刷新 ,	ee 05 bf 0状态信息  0 返回节点数 
			byte cmd0[] = {MyClientDemo.CATE_EE,0x05,(byte) 0xbf,(byte) 0,(byte) 0};
			//byte cmd1[] = {MyClientDemo.CATE_EE,0x05,(byte) 0xbf,(byte) 1,(byte) 0};
			byte ack[]=new byte[6];
			//MyClientDemo.sendCmdByTCP(MyClientDemo.this,cmd0, ack);
			try {
				AlarmListenerService.mutexEnble=false;
			MyClientDemo.getLineNumber(new Exception());
			Thread.sleep(3000);
			AlarmListenerService.mutexEnble=true;
			MyClientDemo.getLineNumber(new Exception());
			cmd0[3]=(byte)0x1;
			
			//MyClientDemo.sendCmdByTCP(MyClientDemo.this,cmd0, ack);
			MyClientDemo.getLineNumber(new Exception());
			judgeUpdateAck(ack);
			cmd0[3]=(byte)0x2;
			
			AlarmListenerService.mutexEnble=false;
			Thread.sleep(100);
			AlarmListenerService.mutexEnble=true;
			MyClientDemo.getLineNumber(new Exception());
			//MyClientDemo.sendCmdByTCP(MyClientDemo.this,cmd0, ack);
			MyClientDemo.getLineNumber(new Exception());
			judgeUpdateAck(ack);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            String str = "lightStates is "+Integer.toHexString(lightStates[0])
            		+"route2 :"+Integer.toHexString(lightStates[1])+"\n";
            //创建一个Message对象，并把得到的天气信息赋值给Message对象
            Message message = Message.obtain();
            message.obj = str;
            //通过Handler发布携带有天气情况的消息
            messageHandler.sendMessage(message);


			//smInfoText.append();
		
    	}
    }
}