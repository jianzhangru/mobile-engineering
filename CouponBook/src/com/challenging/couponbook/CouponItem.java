package com.challenging.couponbook;

import android.graphics.Bitmap;

public class CouponItem {
	private String attrib;
	private String merchantName;
	private String merchantLink;
	private Bitmap merchantIcon;
	private String userName;
	private Bitmap userAvartar;
	
	public CouponItem(String attr, String mName, String mLink, Bitmap mIcon, String uName, Bitmap uAvartar) {
		attrib = attr;
		merchantName = mName;
		merchantLink = mLink;
		merchantIcon = mIcon;
		userName = uName;
		userAvartar = uAvartar;
	}
	
	public String getAttrib() {
		return attrib;
	}
	
	public String getMerchantName() {
		return merchantName;
	}
	
	public String getMerchantLink() {
		return merchantLink;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public Bitmap getMerchantIcon() {
		return merchantIcon;
	}	
	
	public Bitmap getUserAvartar() {
		return userAvartar;
	}	
}
