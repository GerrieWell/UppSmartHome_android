package org.lxh.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class LightsActivity extends Activity {
	ToggleButton light[]=null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.lights_layout);
		light[0]=(ToggleButton)super.findViewById(R.id.toggleButton1);
		light[1]=(ToggleButton)super.findViewById(R.id.toggleButton2);
		light[3]=(ToggleButton)super.findViewById(R.id.toggleButton2);
		light[3]=(ToggleButton)super.findViewById(R.id.toggleButton4);
		light[4]=(ToggleButton)super.findViewById(R.id.toggleButton5);
		light[5]=(ToggleButton)super.findViewById(R.id.toggleButton6);
		light[6]=(ToggleButton)super.findViewById(R.id.toggleButton7);
		light[7]=(ToggleButton)super.findViewById(R.id.toggleButton8);
		for(int i=0;i<8;i++){
			light[0].setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					if(isChecked){
						//构造数据包: ef(包标示) 05(包长) a1(灯) 0(第0组灯) wir_sws(使用位域表示开关)
						//响应: OK
						char cmd1[] = {0xef,0x05,0xa1,0,0x0};
						//MyClientDemo.this.sendCmdByTCP(cmd1, "OK");
					}
				}
			});
		}
	}
}
