package com.example.androidtablayout;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class PackageStats extends Activity{
	// Show all installed packages
	public void onCreate(Bundle savedInstanceState) {
        
		super.onCreate(savedInstanceState);
        setContentView(R.layout.package_layout);
        
        TextView pack = (TextView)findViewById(R.id.textView1);
		pack.setMovementMethod(new ScrollingMovementMethod());
        StringBuilder strpack = new StringBuilder();
        ArrayList<Integer> uids = new ArrayList<Integer>(1000);
        
        PackageManager pm = getPackageManager();
        //Get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        
        for (ApplicationInfo packageInfo : packages) {
        	if (uids.contains(packageInfo.uid) == false)
        	{
        		strpack.append("Package Name:  " + packageInfo.packageName + "\n");
            	strpack.append("UID: " + packageInfo.uid+"\n\n\n");
            	uids.add(packageInfo.uid);
        	}
        	
      }
        
      pack.setText(strpack.toString());
      uids.clear();
    }
}
