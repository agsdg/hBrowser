package com.example.dsf;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends ActionBarActivity {

	WebView webView;
	EditText uri;
	TextView html;
	ProgressBar progress;
	Button htmlRaw;
	Button headers;
	Button preview;
	Button console;
	Map<String, String> cookies = new HashMap<String, String>();
	Map<String,Boolean> exempted = new HashMap<String,Boolean>();
	int deviceHeight;
	String htmlCode="";
	String resHeaders="";
	CookiesManager cm ;
	public Handler ha;
	MyWebViewClient mw;
	SettingsManager sm;
	JavaScriptInterface JSInterface;
	boolean shouldIntercept;
	String consoleErr="";
	
	static String postJS="var x=document.getElementsByTagName('form');for(var i in x){if(x[i].action!=undefined){x[i].setAttribute('onSubmit', 'evtSubmit(event);report(this)');};};function evtSubmit(event){event.preventDefault();};function report(form){var str='';var inp=form.getElementsByTagName('input');for(var q in inp){if(inp[q].name!=undefined && inp[q].value!=undefined ){str+=inp[q].name+'='+inp[q].value+'&';};};if(str.charAt(str.length)=='&'){str=str.substring(0,str.length-1);};JSInterface.post(location.href,form.action,str,true);};";
	/*ajaxJS*///"var xmlhttp;if(window.XMLHttpRequest){xmlhttp=new XMLHttpRequest();}else{xmlhttp=new ActiveXObject('Microsoft.XMLHTTP');};xmlhttp.onreadystatechange=function(){if (xmlhttp.readyState==4 && xmlhttp.status==200){};};xmlhttp.open('GET','ajax_info.txt',true);xmlhttp.send();function XMLHttpRequest(){alert('new');this.urlNow='';this.open=function(method,url,sync){this.urlNow=url;};this.send=function(data){this.responseText='hi';alert(this.responseText);this.readyState=4;this.status=200;this.onreadystatechange();};};";
			//"var urlNow;XMLHttpRequest.prototype.realOpen = XMLHttpRequest.prototype.open;XMLHttpRequest.prototype.realSend = XMLHttpRequest.prototype.send;XMLHttpRequest.prototype.open=function(method,url,async,user,pass){urlNow=url;JSInterface.exempt(urlNow,location.href);this.realOpen(method,url,async,user,pass);};XMLHttpRequest.prototype.send=function(body){var res=JSInterface.post(location.href,urlNow,body,false);Object.defineProperty(this,'responseText',{get:function(){return res}});alert(this.responseText);Object.defineProperty(this,'readyState',{get:function(){return 4}});alert(this);Object.defineProperty(this,'status',{get:function(){return 200}});alert(this.status);this.onreadystatechange({target:this});};";
	//"function XMLHttpRequest(){this.urlNow='';alert('new')this.open=function(method,url,sync){JSInterface.exempt(url,location.href);this.urlNow=url;};this.send=function(data){this.responseText=JSInterface.post(location.href,urlNow,data,false);this.readyState=4;this.status=200;this.onreadystatechange();};this.setRequestHeader=function(a,b);};"+
			//"function ActiveXObject(s){this.urlNow='';alert('new')this.open=function(method,url,sync){JSInterface.exempt(url,location.href);this.urlNow=url;};this.send=function(data){this.responseText=JSInterface.post(location.href,urlNow,data,false);this.readyState=4;this.status=200;this.onreadystatechange();};this.setRequestHeader=function(a,b);};";
	//"function XMLHttpRequest(){this.urlNow='';alert('new');this.open=function(method,url,sync){JSInterface.exempt(url,location.href);this.urlNow=url;};this.send=function(data){this.responseText=JSInterface.post(location.href,urlNow,data,false);this.readyState=4;this.status=200;this.onreadystatechange();};this.setRequestHeader=function(a,b){};};function ActiveXObject(s){this.urlNow='';alert('new');this.open=function(method,url,sync){JSInterface.exempt(url,location.href);this.urlNow=url;};this.send=function(data){this.responseText=JSInterface.post(location.href,urlNow,data,false);this.readyState=4;this.status=200;this.onreadystatechange();};this.setRequestHeader=function(a,b){};};";
	static String ajaxJS="function XMLHttpRequest(){this.readyState=4;this.status=200;this.urlNow='';this.open=function(method,url,sync){this.urlNow=url;};this.send=function(data){var me=this;Object.defineProperty(me,'responseText',{configurable:true,get:function(){var f=JSInterface.post(location.href,me.urlNow,data,false);return f}});var z=this.responseText;Object.defineProperty(me,'responseText',{configurable:true,value:z});};this.setRequestHeader=function(a,b){};};";
	public void setValue(int progress) {
		this.progress.setProgress(progress);		
	}
	private class MyWebChromeClient extends WebChromeClient {	
		@Override
		public void onProgressChanged(WebView view, int newProgress) {			
			MainActivity.this.setValue(newProgress);
			super.onProgressChanged(view, newProgress);
		}
		@Override
		public boolean onConsoleMessage (ConsoleMessage consoleMessage){
			consoleErr+=consoleMessage.message()+" at "+consoleMessage.sourceId()+":"+consoleMessage.lineNumber()+"\n";
			return true;
		}
	}
	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
		 getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		 getActionBar().hide();
		setContentView(R.layout.activity_main);
		//serve.start();
		
		deviceHeight = MainActivity.this.getWindowManager().getDefaultDisplay().getHeight();
		
		html=(TextView) findViewById(R.id.html);
		//html.setHeight(deviceHeight-330);
		
		 webView =(WebView) findViewById(R.id.webView);
		 uri=(EditText) findViewById(R.id.uri);
		 progress=(ProgressBar) findViewById(R.id.progressBar);
		 headers=(Button) findViewById(R.id.Button01);
		 htmlRaw=(Button)findViewById(R.id.button1);
		 
		
		 
		 cm = new CookiesManager(webView);
		
		 headers.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				html.setText(resHeaders);
				
			}
		 });
		
		 htmlRaw.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					html.setText(htmlCode);
					
				}
			 });
		 
		 console=(Button) findViewById(R.id.err);
		 console.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				html.setText(consoleErr);
				
			}
			 
		 });
		
		   if (android.os.Build.VERSION.SDK_INT > 9) {
			      StrictMode.ThreadPolicy policy1 = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			      StrictMode.setThreadPolicy(policy1);
			    }
		 
		 sm=new SettingsManager(this);
		 sm.setWebSettings(webView);
		 shouldIntercept=sm.interceptPages;
	     
	    
	     
		
	     
	    mw=new MyWebViewClient();
	    webView.setWebViewClient(mw);
	     webView.setWebChromeClient(new MyWebChromeClient());
	     JSInterface = new JavaScriptInterface(this);
	        webView.addJavascriptInterface(new JavaScriptInterface(this), "JSInterface"); 
	  
	        preview=(Button) findViewById(R.id.Button02);
			 preview.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					editHTML();
				}
				 
			 });
	     
	     progress = (ProgressBar) findViewById(R.id.progressBar);
		progress.setMax(100);
		progress.setProgress(0);
	    
	   
		 
		 uri.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView arg0, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) { 
					if(!arg0.getText().toString().contains("://")){
						
							arg0.setText("http://"+arg0.getText().toString());
						
					}
					webView.loadUrl( arg0.getText().toString());
					MainActivity.this.progress.setProgress(0);
					return true;
			    }
				return true;
			} 
		 });
		 
		html.setMovementMethod(new ScrollingMovementMethod());
		 webView.loadUrl(sm.homepage);
		 uri.setText(sm.homepage);
		//html.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,(int) (deviceHeight-300)));
		
	}
	protected void editHTML() {
		final EditText hr=new EditText(this);
		hr.setText(htmlCode);
		AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Edit HTML code and Preview"); //設定dialog 的title顯示內容
        dialog.setIcon(android.R.drawable.ic_dialog_alert);//設定dialog 的ICON
        dialog.setCancelable(false); //關閉 Android 系統的主要功能鍵(menu,home等...)
        dialog.setView(hr);
        dialog.setPositiveButton("Apply", new DialogInterface.OnClickListener() {  
            @Override
			public void onClick(DialogInterface dialog, int which) { 
            	dialog.dismiss();
            	htmlCode=hr.getText().toString();
            	html.setText(hr.getText().toString());
            	webView.loadDataWithBaseURL(webView.getUrl(), hr.getText().toString(), "text/html", "UTF-8",webView.getUrl());
        		
			      
            }  
        }); 
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				
			}
		});
        dialog.show();
		
		
	}
	public String popUp(final String urlNow, final URLConnection conexion){
		 //determine whether go or not
		final EditText hr=  new EditText(MainActivity.this);
		 final CountDownLatch latch = new CountDownLatch(1);
		new Thread(new Runnable(){
			@Override
			public void run(){
				hr.setText("Request URL:"+urlNow+"\n");
        	for (String header : conexion.getRequestProperties().keySet()) {
        		   if (header != null) {
        		     for (String value : conexion.getRequestProperties().get(header)) {
        		        hr.append(header + ":" + value+"\n");
        		      }
        		   }
        		}
		
        	//wait response
        	
        	
        
				try{
					
				 final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				
		            dialog.setTitle(urlNow); //設定dialog 的title顯示內容
		            dialog.setIcon(android.R.drawable.ic_dialog_alert);//設定dialog 的ICON
		            dialog.setCancelable(false); //關閉 Android 系統的主要功能鍵(menu,home等...)
		            dialog.setView(hr);
		          
		            dialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {  
		                @Override
						public void onClick(DialogInterface dialog, int which) {  
		                	dialog.dismiss();
		                  mw.go1.put(urlNow, true);
		                  latch.countDown();
		                  //dialog.dismiss();
		                }  
		            }); 
		            dialog.setNegativeButton("Hold", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							// TODO Auto-generated method stub
							mw.go1.put(urlNow, false);
							latch.countDown();
							//dialog.dismiss();
							
						}
					});
		         
		            runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							 dialog.show();
						}
		            	
		            });
		           
		           
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
			}).start();
			
			
			try {
				latch.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		
		
		
		return hr.getText().toString();
        	
       
	}
	private class MyWebViewClient extends WebViewClient {
		URL url = null;
        URLConnection conexion = null;
      //  String htmlContent = null;
		String urlConection;
		HttpURLConnection secCon=null;
		Map<String,Boolean> go1 = new HashMap();
		String tmpCH="";
		String[] extensions=new SettingsManager(MainActivity.this).interceptResources;
		
		
		
		
		@Override
	    public WebResourceResponse shouldInterceptRequest(WebView  view, final String  urlNow){
			if(!shouldIntercept){
				return null;
			}
			if(sm.interceptResources.length>0){
			for(String ext:sm.interceptResources){
				if(urlNow.contains(ext)){
					System.out.print("exempt");
					return null;
				}
			}
			}
			/*if(exempted.get(urlNow)!=null){
				if(exempted.get(urlNow)){
					System.out.println("Exempted");
					System.out.println("EXEMPTED:"+urlNow);
					exempted.put(urlNow, false);
					return null;
				}
			}
			*/
			
			//go=true; 
			System.out.println("interceptreq"+urlNow);
			boolean g=false;
			for(String ext : extensions){
				
				if(urlNow.contains(ext)){
					g=true;
				}
			}
			if(!g){
				System.out.println("ooo");
				//return null;
			}
			
			
			
		//urlcheck if valid
			try {
				URL urlCheck=new URL(urlNow);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				
				
					return null;
				
			}
			
		
			 
			
			urlConection=urlNow;
			
			 //uri.setText(urlConection);

			String baseUrl="";
			
	        
	       
	            try {
					url = new URL(urlNow);
					
					
					conexion = url.openConnection();
					cm.setCookies(conexion);
					// Acts like a browser
					conexion.setUseCaches(false);
					conexion.setRequestProperty("User-Agent", "Mozilla/5.0");
					conexion.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
					conexion.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
					conexion.setRequestProperty("Connection", "keep-alive");
					
	            }catch(IOException e){
	            	e.printStackTrace();
	            }
	            
	            
	           
	                   
	           String hdrs=popUp(urlNow,conexion);
	          
							if(go1.get(urlNow)!=null){
								try {
									return  ld(hdrs);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}else{
								
								
							}
					
	           
							return new WebResourceResponse(null, null, null);
							
	            		
	            			
							
	            
		}
		@Override
		public boolean  shouldOverrideUrlLoading  (WebView  view, String  urlNow){
			/*if(exempted.get(urlNow)!=null){
				if(exempted.get(urlNow)){
					System.out.println("Exempted by urllo");
					System.out.println("EXEMPTED:"+urlNow);
					exempted.put(urlNow, false);
					return false;
				}
			}*/
			
			
			if(shouldIntercept){
				try {
					URL urlCheck=new URL(urlNow);
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					
					
						e1.printStackTrace();
					
				}
			System.out.println("urllo"+urlNow);
			
			
			uri.setText(urlNow);
			URLConnection newConn = null;
			BufferedInputStream in = null;
			URLConnection newnewConn = null;
			try {
				
				newConn = new URL(urlNow).openConnection();
				newConn.setDoInput(true);
			//	newConn.setDoOutput(true);
				cm.setCookies(newConn);
			//act like browser
				newConn.setUseCaches(true);
				newConn.setRequestProperty("User-Agent", "Mozilla/5.0");
				newConn.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				newConn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
				newConn.setRequestProperty("Connection", "keep-alive");
			
				
			
				 in = new BufferedInputStream(newConn.getInputStream());
			       ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
			       int c;
			       while ((c = in.read()) != -1) {
			         byteArrayOut.write(c);
			       }
			   
			       cm.storeCookies(newConn);
			}catch(IOException e1){
				e1.printStackTrace();
			}
			
			
			try{
			newnewConn=new URL(urlNow).openConnection();
				newnewConn.setDoInput(true);
				//newnewConn.setDoOutput(true);
				cm.setCookies(newnewConn);
				// Acts like a browser
				newnewConn.setUseCaches(false);
				newnewConn.setRequestProperty("User-Agent", "Mozilla/5.0");
				newnewConn.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				newnewConn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
				newnewConn.setRequestProperty("Connection", "keep-alive");
			}catch(IOException e2){
				e2.printStackTrace();
			}
			secCon=null;
			secCon=(HttpURLConnection) newnewConn;
			 //determine whether go or not
            final EditText hr = new EditText(MainActivity.this);
           System.out.println("requestproperty connection:"+secCon.getRequestProperty("Connection"));
            	for (String header : newnewConn.getRequestProperties().keySet()) {
            		   if (header != null) {
            		     for (String value : newnewConn.getRequestProperties().get(header)) {
            		        hr.append(header + ":" + value+"\n");
            		        
            		      }
            		   }
            		}
			
            
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle(urlNow); //設定dialog 的title顯示內容
            dialog.setIcon(android.R.drawable.ic_dialog_alert);//設定dialog 的ICON
            dialog.setCancelable(false); //關閉 Android 系統的主要功能鍵(menu,home等...)
            dialog.setView(hr);
            dialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {  
                @Override
				public void onClick(DialogInterface dialog, int which) { 
                	dialog.dismiss();
                	 try {
						secPut(hr.getText().toString(),true);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
   			      
                }  
            }); 
            dialog.setNegativeButton("Hold", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					
				}
			});
            dialog.show();
				
				
		
			
			
	         return true;
			}else{
				return false;
			}
	    }
		
		@Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
			System.out.println("ajaxJS");
           view.loadUrl("javascript:"+ajaxJS);
        }
		@Override
        public void onPageFinished(WebView view, String url) {
			System.out.println("postJS");
           view.loadUrl("javascript:"+postJS);
        }
		/*@Override
		public void loadUrl(String urlNow){
			
		}*/
		
		protected void secPut(String headers, boolean isSecPut) throws IOException{
		
			String[] heads = headers.split("\n");
			for(String row:heads){
				
				if(row.contains(":")){
					String key=row.split(":")[0];
				
					if(key!=null&&row.split(":").length>1){
						String value=row.split(":")[1];
						if(key.equals("POST-data")){
							DataOutputStream wr=new DataOutputStream(secCon.getOutputStream ());
					        wr.writeBytes (value);
					        wr.flush ();
					        wr.close ();
						}else{
							secCon.setRequestProperty(key, value);
						}
					}
				}
			}
			try {
				String htmlCont=convertToString(secCon.getInputStream());
				System.out.println("h");
				//System.out.println("HTML:"+htmlCont);
				
				
				//inject js AT FIRST PLACE!! ITS MY PRIORTY
				//htmlCont=htmlCont.replace("<html>", "<html><script>"+postJS+"</script>");
				//System.out.println(htmlCont);
				//isSecPut only true when AJAX: ajax no need webview to load and save as htmlraw/header
				if(isSecPut){
					webView.loadDataWithBaseURL(secCon.getURL().toString(), htmlCont, "text/html", "UTF-8",secCon.getURL().toString());
					//webView.loadData(htmlCont, "text/html", "UTF-8"); 
					html.setText(htmlCont);
					
					htmlCode=htmlCont;
				
					try {
						getHeaders(secCon,headers);
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		public WebResourceResponse ld(String cH) throws IOException{//ch=changed header
		
			
			
			if(go1.get(url.toString())!=null){
				
				if(go1.get(url.toString())){
				
					URLConnection newConn = null;
					try {
				
						newConn = url.openConnection();
						newConn.setDoInput(true);
						//newConn.setDoOutput(true);
						
					} catch (IOException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
			
					String newURL=newConn.getURL().toString();
					String[] heads = cH.split("\n");
					for(String row:heads){
						
						if(row.contains(":")){
							String key=row.split(":")[0];
							
							if(key!=null&&row.split(":").length>1){
								String value=row.replaceFirst(key+":", "");
								if(key.equals("Request URL")){
									
									newURL=value;//.replaceAll(" ", "%20");
									
								}else{	
									newConn.setRequestProperty(key, value);
								}
								
							}
						}
					}
					BufferedInputStream in = new BufferedInputStream(newConn.getInputStream());
					ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
					int c;
					while ((c = in.read()) != -1) {
						byteArrayOut.write(c);
					}
				//	System.out.println("first"+byteArrayOut.toString());
					cm.storeCookies(newConn);
					
					newConn.setConnectTimeout(3000);
					
		       
					URLConnection newnewConn=new URL(newURL).openConnection();
					String[] heads1 = cH.split("\n");
					for(String row:heads1){
						
						if(row.contains(":")){
							String key=row.split(":")[0];
							
							if(key!=null&&row.split(":").length>1){
								String value=row.split(":")[1];
								newnewConn.setRequestProperty(key, value);
								
							}
						}
					}
					
					return new WebResourceResponse("text/html","UTF-8",newnewConn.getInputStream());
				}else{//dont go
					return new WebResourceResponse(null,null,null);
				}
					
					
				
			}else{
				return new WebResourceResponse(null,null,null);
			}
		}
	    private String getHeaders(URLConnection conexion, String headers) throws URISyntaxException {  	
	    	//request
	    	String temp=conexion.getURL().toString()+"\n\n";
	    			
	    			temp+="####Request Headers: #####\n"+headers+"\n\n";
	    	
	   
	    	
	    	
	    	temp+="####Response Headers: #####\n";
	    	Map<String, List<String>> map1 = conexion.getHeaderFields();
	    	for (Map.Entry<String, List<String>> entry : map1.entrySet()) {
	    		if(entry.getKey()!=null){
	    			temp+=(entry.getKey() + ":" + entry.getValue())+"\n";		
	    		}	
	    	}
	    	resHeaders=temp; 	
	    	return resHeaders;
		}

		public String convertToString(InputStream inputStream){
	        StringBuffer string = new StringBuffer();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	        String line;
	        try {
	            while ((line = reader.readLine()) != null) {
	                string.append(line + "\n");
	            }
	        } catch (IOException e) {}
	        
	        return string.toString();
	    }
		
		
	}
	public void setURL(String url) {
		uri.setText(url);
		
	}
	
	//optuion menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		  switch (item.getItemId()) {
		    case R.id.refresh:
		    	webView.reload();
		    	break;
		    case R.id.back:
		    	webView.goBack();
		    	break;
		    case R.id.next:
		    	webView.goForward();
		    	break;
		    case R.id.stop:
		    	webView.stopLoading();
		    	break;
		    case R.id.settings:
		    	Intent intent = new Intent(this, SettingsActivity.class);
		    	startActivity(intent);
		    	
		  }
		  return true;
	}
	public class JavaScriptInterface {
        Context mContext;
        
        /** Instantiate the interface and set the context */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        public void test(){
        	System.out.println("test");
        }
        public void exempt(String url1,String base) throws MalformedURLException{
        	URL baseUrl = new URL(base);
       	 URL url = new URL( baseUrl , url1);
        	System.out.println("reg exempt"+url.toString());
        	exempted.put(url.toString(), true);
        }
        public String post(final String base,final String url1,final String data,final boolean isSecPut) throws IOException
        {
        	URL baseUrl = new URL(base);
        	 URL url = new URL( baseUrl , url1);
        	final String urlNow=url.toString();
        	final EditText hr = new EditText(MainActivity.this);//*
        	final CountDownLatch latch = new CountDownLatch(1);
            
        	System.out.println(base+"P"+urlNow);
        	new Thread(new Runnable(){

				@Override
				public void run() {
					System.out.println(data);
		        		
		        		
		    			System.out.println("post"+urlNow);
		    			
		    			
		    		//	uri.setText(urlNow);
		    			//HttpURLConnection newConn = null;
		    			BufferedInputStream in = null;
		    			HttpURLConnection newnewConn1 = null;
		    			try {
		    				exempted.put(urlNow.toString(), true);
		    				System.out.println("regg exempt "+urlNow.toString()+" "+exempted.get(urlNow));
		    			/*
		    				newConn = (HttpURLConnection) new URL(urlNow).openConnection();
		    				newConn.setRequestMethod("POST");
		    				newConn.setDoInput(true);
		    				newConn.setDoOutput(true);
		    				cm.setCookies(newConn);
		    			//act like browser
		    				newConn.setUseCaches(false);
		    				newConn.setRequestProperty("User-Agent", "Mozilla/5.0");
		    				newConn.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*\/*;q=0.8");
		    				newConn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		    				newConn.setRequestProperty("Connection", "keep-alive");
		    				//set post data
		    				System.out.println("A"+newConn.getRequestProperty("Accept"));
		    				OutputStreamWriter wr = new OutputStreamWriter(newConn.getOutputStream());
		    			    wr.write(data);
		    			   // wr.flush();*/
		    			
		    				
		    			
		    				/* in = new BufferedInputStream(newConn.getInputStream());
		    			       ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		    			       int c;
		    			       while ((c = in.read()) != -1) {
		    			         byteArrayOut.write(c);
		    			       }
		    			   System.out.println(byteArrayOut.toString());*/
		    			      // cm.storeCookies(newConn);
		    		
		    			newnewConn1=(HttpURLConnection) new URL(urlNow).openConnection();
		    				newnewConn1.setRequestMethod("POST");
		    			newnewConn1.setDoInput(true);
		    				newnewConn1.setDoOutput(true);
		    				cm.setCookies(newnewConn1);
		    				// Acts like a browser
		    				newnewConn1.setUseCaches(true);
		    				newnewConn1.setRequestProperty("User-Agent", "Mozilla/5.0");
		    				newnewConn1.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		    				newnewConn1.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		    				newnewConn1.setRequestProperty("Connection", "keep-alive");
		    				newnewConn1.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
		    				System.out.println("C"+newnewConn1.getRequestProperty("Connection"));
		    				//set post data
		    				
		    			}catch(IOException e2){
		    				e2.printStackTrace();
		    			}
		    			mw.secCon=null;
		    			mw.secCon=newnewConn1;
		    			 //determine whether go or not
		                
		             //   hr.setText("");
		              // System.out.println(newnewConn1.getRequestProperty("Accept"));
		                	
		    			for (String header1 : newnewConn1.getRequestProperties().keySet()) {
		                		   if (header1 != null) {
		                		     for (String value1 : newnewConn1.getRequestProperties().get(header1)) {
		                		        hr.append(header1 + ":" + value1+"\n");
		                		        
		                		      }
		                		   }
		                		}
		                	
		                	hr.append("POST-data:"+data);
		                	/* try {
		                		 OutputStreamWriter wr1 = new OutputStreamWriter(newnewConn1.getOutputStream());
			    			   
								wr1.write(URLEncoder.encode(data));
								wr1.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		    			    */
		                	
		               
		    				
		    				
		    		
		    		
		                if(shouldIntercept){
				        	
		                	 final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				                dialog.setTitle(urlNow); //設定dialog 的title顯示內容
				                dialog.setIcon(android.R.drawable.ic_dialog_alert);//設定dialog 的ICON
				                dialog.setCancelable(false); //關閉 Android 系統的主要功能鍵(menu,home等...)
				                dialog.setView(hr);
				                dialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {  
				                    @Override
									public void onClick(DialogInterface dialog, int which) {  
				                    	dialog.dismiss();
				                    
				                    	latch.countDown();
				                    	try {
				                    		if(isSecPut){
											mw.secPut(hr.getText().toString(),isSecPut);
				                    		}
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
				       			      
				                    }  
				                }); 
				                dialog.setNegativeButton("Hold", new DialogInterface.OnClickListener() {
				    				
				    				@Override
				    				public void onClick(DialogInterface dialog, int which) {
				    					// TODO Auto-generated method stub
				    					dialog.dismiss();
				    					latch.countDown();
				    					
				    				}
				    			});
				                MainActivity.this.runOnUiThread(new Runnable(){

									@Override
									public void run() {
										// TODO Auto-generated method stub
										 dialog.show();
									}
				                	
				                });
		    			}else{
		    				//not intercept
		    				latch.countDown();
		    			}
				}
        		
        	}).start();
        	System.out.println(1);
        	try {
				latch.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	System.out.println(2);
        	
        	
        	final HttpURLConnection con3=(HttpURLConnection) new URL(urlNow).openConnection();
        	con3.setRequestMethod("POST");
        	con3.setDoInput(true);
        	con3.setDoOutput(true);
        	exempted.put(urlNow.toString(), true);
			System.out.println("regg exempt "+urlNow.toString()+" "+exempted.get(urlNow));
        	//return loaded statement
        	System.out.println(hr.getText());
        	String[] heads =hr.getText().toString().split("\n");
			for(String row:heads){
				
				if(row.contains(":")){
					String key=row.split(":")[0];
				
					if(key!=null&&row.split(":").length>1){
						final String value=row.split(":")[1];
						if(key.equals("POST-data")){
							System.out.println(value);
							System.out.println(".");
							
							MainActivity.this.runOnUiThread(new Runnable(){

								@Override
								public void run() {
									try {
										DataOutputStream out = new DataOutputStream(con3.getOutputStream());
								         out.writeBytes(value);
								         out.flush();
								         out.close();
									
									
							      
							        
							       
							        System.out.println("c");
							        
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								
							});
							
						}else{
							System.out.println(key+":"+value);
							con3.setRequestProperty(key, value);
						}
					}
				}
			}
			System.out.println(con3.getRequestProperty("Cookie"));
		
			StringBuilder string = new StringBuilder();
			try {
				
		        BufferedReader reader = new BufferedReader(new InputStreamReader(con3.getInputStream()));
		        String line;
		        try {
		            while ((line = reader.readLine()) != null) {
		                string.append(line + "\n");
		            }
		        } catch (IOException e) {}
				
				 cm.storeCookies(con3);
				//isSecPut only true when AJAX: ajax no need webview to load and save as htmlraw/header
				
			System.out.println(string.toString());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return string.toString().replace("\n", "");
        	
        }
    }

	

}
