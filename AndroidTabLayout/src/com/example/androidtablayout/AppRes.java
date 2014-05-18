package com.example.androidtablayout;


//This class is used to store application resources
public class AppRes {
	Double cpuPow, gpsPow, wifiPow;
	Long cpuTime, gpsTime;
	Long bytesRecv, bytesSend;
	String appName;
	
	public AppRes()
	{
		this.cpuPow = 0.0; this.cpuTime = 0L;
		this.gpsPow = 0.0; this.gpsTime = 0L;
		this.bytesRecv = 0L; this.bytesSend = 0L;
		this.wifiPow = 0.0;
		this.appName = new String(" ");
	}
	
	
}
