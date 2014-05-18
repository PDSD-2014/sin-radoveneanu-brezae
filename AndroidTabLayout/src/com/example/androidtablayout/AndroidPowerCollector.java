package com.example.androidtablayout;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.SystemClock;
import android.util.SparseArray;


public class AndroidPowerCollector implements PowerCollector {

	private static final String BATTERY_STATS_IMPL_CLASS = "com.android.internal.os.BatteryStatsImpl";
	private static final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
	private static final String M_BATTERY_INFO_CLASS = "com.android.internal.app.IBatteryStats";
	private static final String BATTERY_STATS_CLASS = "android.os.BatteryStats";
	private static final String UID_CLASS = BATTERY_STATS_CLASS + "$Uid";
	private static final String PROC_CLASS = UID_CLASS + "$Proc";
	private static final String SENSOR_CLASS = UID_CLASS + "$Sensor";
	private static final String BATTER_STATS_TIMER_CLASS = BATTERY_STATS_CLASS + "$Timer";


	private Object mBatteryInfo_;
	private Object mStats_;
	private Object mPowerProfile_;
	private int mStatsType_ = BatteryStatsConstants.STATS_TOTAL;
	private Activity parent_;
	private ArrayList<RecordingStrategy> recorder_ = new ArrayList<RecordingStrategy> ();

	public AndroidPowerCollector(){ }
	
	public void addRecorder(RecordingStrategy rs){
		recorder_.add(rs);
	}

	private void load(){
		try{

			byte[] data = (byte[])Class.forName(M_BATTERY_INFO_CLASS)
					.getMethod("getStatistics")
					.invoke(mBatteryInfo_);
			Parcel parcel = Parcel.obtain();
			parcel.unmarshall(data, 0, data.length);
			parcel.setDataPosition(0);
			/*
			 * Non-reflection call looks like
			 * mStats = com.android.internal.os.BatteryStatsImpl.CREATOR.createFromParcel(parcel);
			 */
			mStats_ = Class.forName(BATTERY_STATS_IMPL_CLASS).getField("CREATOR")
					.getType().getMethod("createFromParcel", Parcel.class)
					.invoke(Class.forName(BATTERY_STATS_IMPL_CLASS)
							.getField("CREATOR").get(null), parcel);

		}catch(InvocationTargetException e){
			//Log.e("BatteryTester", "Exception: " + e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.spot.android.PowerCollector#processApplicationUsage()
	 */
	public AppRes processApplicationUsage(int i){
		
		if(i == 0)
			parent_ = ResourceApp.getInstance();
		else if(i == 1)
			parent_ = Statistics.getInstance();
		else
			parent_ = Charts.getInstance();
		
		try {

			mBatteryInfo_ = Class.forName("com.android.internal.app.IBatteryStats$Stub")
							.getDeclaredMethod("asInterface", 
					IBinder.class).invoke(null, Class.forName("android.os.ServiceManager")
							.getMethod("getService", String.class).invoke(null, "batteryinfo"));
			mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
							.getConstructor(Context.class).newInstance(parent_);
			load();	

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

		try {
			AppRes app = new AppRes();
			double powerTMP = 0;

			final double powerCpuNormal = .2;
			final double averageCostPerByte = getAverageDataCost();
			final int which = mStatsType_;
			long uSecTime = (Long)Class.forName(BATTERY_STATS_IMPL_CLASS)
					.getMethod("computeBatteryRealtime", java.lang.Long.TYPE, java.lang.Integer.TYPE)
					.invoke(mStats_, SystemClock.elapsedRealtime() * 1000, which);
			updateStatsPeriod(uSecTime);

			@SuppressWarnings("unchecked")
			SparseArray<Object> uidStats = (SparseArray<Object>)Class.forName(BATTERY_STATS_IMPL_CLASS).
					getMethod("getUidStats").invoke(mStats_);
			final int NU = uidStats.size();
			for (int iu = 0; iu < NU; iu++){
				Object u = uidStats.valueAt(iu);
				double cpuPower = 0, gpsPower = 0, wifiPower = 0, highestDrainPower = -1;				
				Long bytesSend = 0L, bytesRecv = 0L;
				long cpuTime = 0, cpuFgTime = 0, gpsTime = 0;
				
				String packageWithHighestDrain = "";
				@SuppressWarnings("unchecked")
				Map<String, Object> processStats = (Map<String, Object>)Class.forName(UID_CLASS)
						.getMethod("getProcessStats")
						.invoke(u);
								
				//Process cpu usage
				if(processStats.size() > 0){
					for (Map.Entry<String, Object> ent: processStats.entrySet()){
						if(ent.getKey().length() > 1)
							packageWithHighestDrain = ent.getKey();
						if (ent.getKey().contains("com.example.androidtablayout"))
							continue;

						Object ps = ent.getValue();
						final long userTime = (Long)Class.forName(PROC_CLASS)
								.getMethod("getUserTime", java.lang.Integer.TYPE)
								.invoke(ps, which);
						final long systemTime = (Long)Class.forName(PROC_CLASS)
								.getMethod("getSystemTime", java.lang.Integer.TYPE)
								.invoke(ps, which);
						final long foregroundTime = (Long)Class.forName(PROC_CLASS)
								.getMethod("getForegroundTime", java.lang.Integer.TYPE)
								.invoke(ps, which);
						cpuFgTime += foregroundTime * 10; //convert to millis
						final long tmpCpuTime = (userTime + systemTime) * 10; //convert to millis;
						final double processPower = tmpCpuTime * powerCpuNormal;

						cpuTime += tmpCpuTime;
						
						cpuPower += processPower;
						if(ent.getKey().length() > 1){
							if (highestDrainPower < processPower){
								highestDrainPower = processPower;
								packageWithHighestDrain = ent.getKey();
							}
						}
						cpuPower /= 1000;
					}
				}
				if (packageWithHighestDrain.contains("com.example.androidtablayout"))
					continue;

				//Calculate network usage
				bytesSend = ((Long)Class.forName(UID_CLASS).getMethod("getTcpBytesSent", java.lang.Integer.TYPE).invoke(u, mStatsType_));
				bytesRecv = ((Long)Class.forName(UID_CLASS).getMethod("getTcpBytesReceived", java.lang.Integer.TYPE).invoke(u, mStatsType_));
				wifiPower = (bytesRecv + bytesSend) * averageCostPerByte;

				//Process sensor usage
				@SuppressWarnings("unchecked")
				Map<Integer, Object> sensorStats = (Map<Integer, Object>)Class.forName(UID_CLASS).getMethod("getSensorStats").invoke(u);

				for(Map.Entry<Integer, Object> sensorEntry: sensorStats.entrySet()){
					Object sensor = sensorEntry.getValue();
					int sensorType = (Integer)Class.forName(SENSOR_CLASS).getMethod("getHandle", (Class[])null).invoke(sensor);
					Object timer = Class.forName(SENSOR_CLASS).getMethod("getSensorTime", (Class[])null).invoke(sensor);
					long sensorTime = (Long)Class.forName(BATTER_STATS_TIMER_CLASS).getMethod("getTotalTimeLocked", java.lang.Long.TYPE, java.lang.Integer.TYPE).invoke(timer, uSecTime, which);
					double multiplier = 0;

					if(sensorType == -10000){
					
					//GPS
					
						multiplier = (Double)Class.forName(POWER_PROFILE_CLASS).getMethod("getAveragePower", String.class).invoke(mPowerProfile_, PowerProfileConstants.POWER_GPS_ON);
						gpsTime = sensorTime;
						gpsPower += (multiplier * sensorTime) / 1000;
					}
				}
				
				double power = cpuPower + gpsPower + wifiPower ;
				
				if (power >= 0){
					if((power > powerTMP) && (packageWithHighestDrain.length() > 1))
					{
						powerTMP = power;
						app.appName = packageWithHighestDrain;
						app.cpuPow = cpuPower;
						app.cpuTime = cpuFgTime + cpuTime;
						app.gpsPow = gpsPower;
						app.gpsTime = gpsTime;
						app.bytesRecv = bytesRecv;
						app.bytesSend = bytesSend;
						app.wifiPow = wifiPower;
					}
				}
			}
			return app;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/* According to this http://www.cl.cam.ac.uk/~acr31/pubs/rice-80211power.pdf
	 * I've set average power per kB at 0.05 j/kB
	 */
	private double getAverageDataCost(){
		return 0.05/1024.0;
	}


	private void updateStatsPeriod(long uSecTime){

	}
	private class PowerProfileConstants{

		/**
		 * Power consumption when GPS is on.
		 */
		public static final String POWER_GPS_ON = "gps.on";


	}

	private class BatteryStatsConstants{
		/**
		 * Include all of the data in the statistics, including previously saved data.
		 */
		public static final int STATS_TOTAL = 0;

	}
}