package com.challenging.couponbook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class FeedStore {
	private List<CouponItem> couponList = new ArrayList<CouponItem>();
	private String json_url;
	
	public FeedStore(String url) {
		json_url = url;
	}
	
	public Bitmap readImageFromURL(String img_url) throws IOException {
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(img_url);
		
		HttpResponse response = client.execute(httpGet);
		StatusLine statusLine = response.getStatusLine();
	    int statusCode = statusLine.getStatusCode();
	    if (statusCode == 200) {
			HttpEntity entity = response.getEntity();
			BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity); 
			InputStream is = bufHttpEntity.getContent();
			return BitmapFactory.decodeStream(is);
	    } else {
	        Log.e(FeedStore.class.toString(), "Failed to download image");
	        return null;	    	
	    }
	}
	
	private String readFeed() throws ReadJsonFeedException, IOException {
	    StringBuilder builder = new StringBuilder();
	    HttpClient client = new DefaultHttpClient();
	    HttpGet httpGet = new HttpGet(json_url);
	    HttpResponse response = client.execute(httpGet);
	    StatusLine statusLine = response.getStatusLine();
	    int statusCode = statusLine.getStatusCode();
	    if (statusCode == 200) {
	    	HttpEntity entity = response.getEntity();
	    	InputStream content = entity.getContent();
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(content));
	    	String line;
	    	while ((line = reader.readLine()) != null) {
	    		builder.append(line);
	    	}
	    } else {
	    	Log.e(FeedStore.class.toString(), "Failed to download feed");
	    	throw new ReadJsonFeedException("Failed to download feed");
	    }
	    return builder.toString();
	}
	
	private void parseFeedString(String feedStr) throws ReadJsonFeedException {
	    try {
	        JSONArray jsonArray = new JSONArray(feedStr);
	        for (int i = 0; i < jsonArray.length(); i++) {
	          JSONObject jsonObj = jsonArray.getJSONObject(i);
	          String attrib = jsonObj.getString("attrib");
	          String desc = jsonObj.getString("desc");
	          String href = jsonObj.getString("href");
	          String src = jsonObj.getString("src");
	          Bitmap merchantPic = null;
	          try {
	        	  merchantPic = readImageFromURL(src);
		      } catch (IOException e) {
		    	  Log.e(FeedStore.class.toString(), "Failed to download image");
		      }
	          String username = jsonObj.getJSONObject("user").getString("name");
	          String avartarUrl = jsonObj.getJSONObject("user").getJSONObject("avatar").getString("src");
	          Bitmap avartar = null; 
	          try {
	        	  avartar = readImageFromURL(avartarUrl);
	          } catch (IOException e) {
		    	  Log.e(FeedStore.class.toString(), "Failed to download image");
		      }
	          couponList.add(new CouponItem(attrib, desc, href, merchantPic, username, avartar));
	        }
	      } catch (JSONException e) {
	    	  throw new ReadJsonFeedException("Invalid JSON feed");
	      }
	}
	
	public void loadFeedOnline(final ReadFeedCallback callback)  {
    	new AsyncTask<Object, Void, Void>() {
    		@Override
    		protected Void doInBackground(Object... params) {
    			String feedStr = "";
    			try {
    				feedStr = readFeed();
    				parseFeedString(feedStr);
    				callback.done();
    			} catch (IOException e) {
    				callback.error(e);
    			} catch (ReadJsonFeedException e) {
    				callback.error(e);
    			}
				return null;    		
    		}
    	}.execute();
	}
	
	public List<CouponItem> getCouponList() {
		return couponList;
	}
}

