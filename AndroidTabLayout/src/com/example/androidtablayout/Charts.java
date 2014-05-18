package com.example.androidtablayout;


import java.util.ArrayList;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;

public class Charts extends Activity {

	private static Charts instance_;
	
	Handler handler =new Handler();
	Runnable update = new Runnable() {
			
		public void run() {
			updateValues();
			handler.postDelayed(this, 10000);
		}
	};
	
	GraphView cpuGraph, gpsGraph, wifiGraph;
	LinearLayout cpuLay, gpsLay, wifiLay;
	ArrayList<AppRes> data;
	GraphViewSeries cpuGraphData, gpsGraphData, wifiGraphData;
	
	public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charts_layout);
        instance_ = this;
        
        data = new ArrayList<AppRes>(5);
    	
    	//Fill with empty data
    	for(int i=0; i<5; i++)
    		data.add(new AppRes());
       
        setValues();
        updateValues();
        
        handler.postDelayed(update, 10000);
	}
      
	
	public static Charts getInstance()
	{
		return instance_;
	}
	
	public void setValues()
	{
		
		// CPU, GPS, WiFi Power Graph
		GraphViewData[] dataCpu = new GraphViewData[5];
		GraphViewData[] dataGps = new GraphViewData[5];
		GraphViewData[] dataWiFi = new GraphViewData[5];
		
		for(int i=0; i<5; i++){
			dataCpu[i] = new GraphViewData(i, getCpuPow(data, i));
			dataGps[i] = new GraphViewData(i, getGpsPow(data, i));
			dataWiFi[i] = new GraphViewData(i, getWifiPow(data, i));
		}
		cpuGraphData = new GraphViewSeries("mJ", new GraphViewSeriesStyle(Color.rgb(240, 0, 0),3), dataCpu);
		gpsGraphData = new GraphViewSeries("mJ", new GraphViewSeriesStyle(Color.rgb(240, 0, 0),3), dataGps);
		wifiGraphData = new GraphViewSeries("J", new GraphViewSeriesStyle(Color.rgb(240, 0, 0),3), dataWiFi);
	
		//GPU Graph
		cpuGraph = new LineGraphView(this, "CPU (Power)");		
		cpuGraph.addSeries(cpuGraphData);
		cpuGraph.setViewPort(0, 4.5);
		cpuGraph.getGraphViewStyle().setNumHorizontalLabels(5);
		cpuGraph.getGraphViewStyle().setTextSize(12.0f);
	
		//GPS Power Graph
		gpsGraph = new LineGraphView(this, "GPS (Power)");		
		gpsGraph.addSeries(gpsGraphData);
		gpsGraph.setViewPort(0, 4.5);
		gpsGraph.getGraphViewStyle().setNumHorizontalLabels(5);
		gpsGraph.getGraphViewStyle().setTextSize(12.0f);
		
		//Wifi Power Graf
		wifiGraph = new LineGraphView(this, "WiFi (Power)");		
		wifiGraph.addSeries(wifiGraphData);
		wifiGraph.setViewPort(0, 4.5);
		wifiGraph.getGraphViewStyle().setNumHorizontalLabels(5);
		wifiGraph.getGraphViewStyle().setTextSize(12.0f);
		
		
		
		cpuLay = (LinearLayout)findViewById(R.id.cpuG);
		cpuLay.addView(cpuGraph);
		gpsLay = (LinearLayout)findViewById(R.id.gpsG);
		gpsLay.addView(gpsGraph);
		wifiLay = (LinearLayout)findViewById(R.id.wifiG);
		wifiLay.addView(wifiGraph);
		
	}
	
	public Double getCpuPow(ArrayList<AppRes> data, int index)
	{
		return data.get(index).cpuPow;
	}
	
	public Double getGpsPow(ArrayList<AppRes> data, int index)
	{
		return data.get(index).gpsPow;
	}
	
	public Double getWifiPow(ArrayList<AppRes> data, int index)
	{
		return data.get(index).wifiPow;
	}
	
	public void updateValues()
	{
		if(data.size() > 1)
			data.remove(0);
		
		//Read data
		AndroidPowerCollector apc = new AndroidPowerCollector();
		AppRes resources = apc.processApplicationUsage(2);
		
		if(resources == null)
		{
			resources = new AppRes();
		}
		
		//Store data
		data.add(resources);
		// remove all view to update graph
		cpuLay.removeAllViews();
		gpsLay.removeAllViews();
		wifiLay.removeAllViews();
		
		//set new values;
		setValues();
	}
}
