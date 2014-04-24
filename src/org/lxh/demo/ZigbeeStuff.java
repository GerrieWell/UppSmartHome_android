package org.lxh.demo;

import zigbeeNet.DeviceInfo;
import zigbeeNet.NwkDesp;

public class ZigbeeStuff {
	public class NwkDesp {
		long  	panid; 
		long 	channel;  
		byte  	maxchild; 
		byte  	maxdepth;
		byte  	maxrouter;
		public NwkDesp(){
			
		}
	}

	
	class SensorDesp {
	    long  	nwkaddr;
	    byte  	sensortype;
	    long	sensorvalue;
	};

	//public static NwkDesp pNwkDesp2;

	
	public class NodeInfo{

		public NodeInfo(){
			
		}
	}
		
	class NodeNwkTopoInfo_message{
        DeviceInfo     devinfo;
        byte   row;
        byte   num;
		long   flag;
	}
	
	class head{
		byte protrol;
		long size;
		byte opcode;
		long ack;
	}
}
