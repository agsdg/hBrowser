package com.example.dsf;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;

public class SettingsManager {
	
	SharedPreferences prefs;
	
	public boolean setSupportZoom;
	public boolean setBuiltInZoomControls;
	public boolean setJavaScriptEnabled;
	public boolean setAllowFileAccess;
	public boolean setSaveFormData;
	public boolean setAppCacheEnabled;
	public boolean setBlockNetworkImage;
	public boolean setUseWideViewPort;
	public boolean disableCookies;
	public boolean setPluginState;
	
	public String homepage;

	public boolean interceptPages;
	public String[] interceptResources;
	
	
	public SettingsManager(MainActivity act){
		prefs = PreferenceManager.getDefaultSharedPreferences(act);
		
		setSupportZoom=prefs.getBoolean("setSupportZoom", true);
		setBuiltInZoomControls=prefs.getBoolean("setBuiltInZoomControls", true);
		setJavaScriptEnabled=prefs.getBoolean("setJavaScriptEnabled", true);
		setAllowFileAccess=prefs.getBoolean("setAllowFileAccess", false);
		setSaveFormData=prefs.getBoolean("setSaveFormData", false);
		setAppCacheEnabled=prefs.getBoolean("setAppCacheEnabled", true);
		setBlockNetworkImage=prefs.getBoolean("setBlockNetworkImage", false);
		setUseWideViewPort=prefs.getBoolean("setUseWideViewPort", false);
		disableCookies=prefs.getBoolean("disableCookies", false);
		setPluginState=prefs.getBoolean("setPluginState", true);
		
		homepage=prefs.getString("homepage", "file:///android_asset/a.html");
		
		interceptPages=prefs.getBoolean("interceptPages", true);
		interceptResources=prefs.getString("interceptResources", "file:///").split(",");
	}
	
	public void setWebSettings(WebView view){
		WebSettings s = view.getSettings();  
		s.setSupportZoom(setSupportZoom);
		s.setBuiltInZoomControls(setBuiltInZoomControls);
		s.setJavaScriptEnabled(setJavaScriptEnabled);
		s.setAllowFileAccess(setAllowFileAccess);
		s.setSaveFormData(setSaveFormData);
		s.setAppCacheEnabled(setAppCacheEnabled);
		s.setBlockNetworkImage(setBlockNetworkImage);
		s.setUseWideViewPort(setUseWideViewPort);
		/*if(setPluginState){
			s.setPluginState(WebSettings.PluginState.ON);
		}*/
		 s.setPluginState(WebSettings.PluginState.ON);
		   s.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
	
	}
	
}