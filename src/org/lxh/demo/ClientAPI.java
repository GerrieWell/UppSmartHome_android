package org.lxh.demo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import android.util.Log;

public class ClientAPI  {
	public static PrintStream shOut;
	public static BufferedReader shBuf ;
	public static OutputStream os;
	public static PrintStream getShOut() {
		return shOut;
	}

	public static void setShOut(PrintStream shOut) {
		ClientAPI.shOut = shOut;
	}

	public static BufferedReader getShBuf() {
		return shBuf;
	}

	public static void setShBuf(BufferedReader shBuf) {
		ClientAPI.shBuf = shBuf;
	}

	public static OutputStream getOs() {
		return os;
	}

	public static void setOs(OutputStream os) {
		ClientAPI.os = os;
	}

	public static InputStream getIs() {
		return is;
	}

	public static void setIs(InputStream is) {
		ClientAPI.is = is;
	}
	public static InputStream is;
	
	private static boolean write(OutputStream o,int buf[],int count){
		
		
		return false;
	}
	
    void Api_Cliect_GetZigBeeNwkInfo(){
    	boolean ret;
        //Log.d("Client\t","Api_Cliect_GetZigBeeNwkInfo send \n");
        int buffer[]= new int[5];
        buffer[0]=0x15;
        buffer[1]=0x02;

        buffer[2]='\n';

        ret=write(os, buffer, buffer.length);
        if(!ret)
        {

            Log.d("Client\t","Api_Cliect_GetZigBeeNwkInfo send error\n");
            Log.d("Client\t","Api_Cliect_GetZigBeeNwkInfo send error\n");
        }
    }

    void Api_Cliect_GetZigBeeNwkTopo(){
        //Log.d("Client\t","Api_Cliect_GetZigBeeNwkTopo send \n");
        int buffer[] = new int[3];
        boolean ret;
        buffer[0]=0x15;
        buffer[1]=0x01;

        buffer[2]='\n';
        //Log.d("Client\t","Api_Cliect_GetZigBeeNwkInfo send sockfd:%d\n",sockfd);
        ret = write(os, buffer, buffer.length);
        if(!ret)
        	
        {
            Log.d("Client\t","Api_Cliect_GetZigBeeNwkTopo send error\n");
        }
    }
    void Api_Cliect_GetTempHum(){
        int[] buffer = new int[3];
        boolean ret;
        buffer[0]=0x15;
        buffer[1]=0x05;
        
        buffer[2]='\n';
        //Log.d("Client\t","Api_Cliect_GetTempHum send sockfd:%d\n",sockfd);
        ret = write(os, buffer, buffer.length);
        if(!ret)
        {
            Log.d("Client\t","Api_Cliect_GetTempHum send error\n");
        }
    }
    void Api_Cliect_GetRfidId(){
        int buffer[] = new int[3];
        boolean ret;
        buffer[0]=0x15;
        buffer[1]=0x05;
        
        buffer[2]='\n';
        //Log.d("Client\t","Api_Cliect_GetTempHum send sockfd:%d\n",sockfd);
        ret = write(os, buffer, buffer.length);
        if(!ret)
        {
            Log.d("Client\t","Api_Cliect_GetTempHum send error\n");
        }
    }
    void Api_Cliect_GetGPRSSignal(){
    	
    }

    void Api_Cliect_SendGprsMessage(byte []phone,int sensor){
    	
    }
    void Api_Cliect_ClearIntlock(){
    	
    }
    void Api_Cliect_SetSensorStatus(int nwkaddr,int Mode){
        int buffer[] = new int[5];
        boolean ret;
        buffer[0]=0x15;
        buffer[1]=0x03;

        buffer[2]=nwkaddr;
        buffer[3]=Mode;
        buffer[4]='\n';
        //Log.d("Client\t","Api_Cliect_GetZigBeeNwkInfo send sockfd:%d\n",sockfd);
        ret = write(os, buffer, buffer.length);
        if(!ret)
        {

            Log.d("Client\t","Api_Cliect_SetSensorStatus send error\n");
        }
    }
}
