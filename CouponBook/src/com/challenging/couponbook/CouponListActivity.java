package com.challenging.couponbook;

import java.util.List;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.challenging.couponbook.R;


public class CouponListActivity extends ListActivity  {
	private final String JSON_URL = "http://sheltered-bastion-2512.herokuapp.com/feed.json";
	private FeedStore feedStore;
	private CouponListAdapter couponListAdapter;
	private MenuItem refreshMenuItem;
	boolean refreshingInProcess = false;
	private ImageView refreshAnimView;
	private Animation animRefresh;
	
	public class CouponListAdapter extends ArrayAdapter<CouponItem> {
		private final Context context;
		public CouponListAdapter(Context ctxt, List<CouponItem> qList) {
		    super(ctxt, R.layout.item_row, qList);
		    context = ctxt;
	    }

		  @Override
		public View getView(int position, View convertView, ViewGroup parent) {
		    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    View rowView = inflater.inflate(R.layout.item_row, parent, false);
		    CouponItem unit = getItem(position);

		    TextView merchantName = (TextView) rowView.findViewById(R.id.merchant_name);
		    String merchantURL = getString(R.string.mertchant_href, unit.getMerchantLink(), unit.getMerchantName());
		    merchantName.setText(Html.fromHtml(merchantURL));
	        Linkify.addLinks(merchantName, Linkify.ALL);
	        merchantName.setMovementMethod(LinkMovementMethod.getInstance());

	        ImageView merchantIcon = (ImageView) rowView.findViewById(R.id.merchant_icon);
            Bitmap logo = unit.getMerchantIcon();
            if (logo != null) {
            	merchantIcon.setImageBitmap(logo);
            }
            
            TextView descView = (TextView) rowView.findViewById(R.id.coupon_desc);
		    descView.setText(unit.getAttrib());

            TextView userView = (TextView) rowView.findViewById(R.id.user_name);
            userView.setText(unit.getUserName());	
            
	        ImageView userIcon = (ImageView) rowView.findViewById(R.id.user_icon);
            Bitmap userlogo = unit.getUserAvartar();
            if (userlogo != null) {
            	userIcon.setImageBitmap(userlogo);
            }		    
		    return rowView;
		}
	} 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getListView().setDividerHeight(4);
        animRefresh = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.refresh_rotate);
        animRefresh.setRepeatCount(Animation.INFINITE);
    	refreshAnimView = (ImageView) getLayoutInflater().inflate(R.layout.action_refresh, null);
    	
		feedStore = new FeedStore(JSON_URL);
		couponListAdapter = new CouponListAdapter(this, feedStore.getCouponList());
	    setListAdapter(couponListAdapter);
		loadFeedData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.coupon_list, menu);
        refreshMenuItem = menu.findItem(R.id.action_main_refresh);
		return super.onCreateOptionsMenu(menu);
	}
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
    	if (refreshingInProcess)
    		refreshMenuItem.setActionView(refreshAnimView);
    	return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
    	boolean ret;
        switch (item.getItemId()) {
            case R.id.action_main_refresh:
            	loadFeedData();
                ret = true;
                break;
            default:
            	ret = super.onOptionsItemSelected(item);
        }
        return ret;
    }
    
	private void loadFeedData() {
		refreshingInProcess = true;
		runOnUiThread(new Runnable() {
			public void run() {
				refreshAnimView.startAnimation(animRefresh);
	    		if (refreshMenuItem != null)
	    			refreshMenuItem.setActionView(refreshAnimView);
			}
		});
		feedStore.loadFeedOnline(new ReadFeedCallback() {
			@Override
			public void done() {
				runOnUiThread(new Runnable() {
					public void run() {
						if (couponListAdapter != null)
							couponListAdapter.notifyDataSetChanged();
						refreshAnimView.clearAnimation();
						if (refreshMenuItem != null)
							refreshMenuItem.setActionView(null);
						refreshingInProcess = false;
					}
				});

			}
			@Override
			public void error(Exception e) {
				final Exception ef = e;
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(CouponListActivity.this, ef.getLocalizedMessage(), Toast.LENGTH_LONG).show();
						refreshAnimView.clearAnimation();
						if (refreshMenuItem != null)
							refreshMenuItem.setActionView(null);
						refreshingInProcess = false;
					}
					
				});
			}
		});
	}

}
