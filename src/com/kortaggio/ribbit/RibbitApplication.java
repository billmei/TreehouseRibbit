package com.kortaggio.ribbit;

import android.app.Application;

import com.kortaggio.ribbit.ui.MainActivity;
import com.kortaggio.ribbit.utils.ParseConstants;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;

public class RibbitApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "dp1IAacyNxLUl0opCCqOFMvwdq2Ip1TAkdBt3gR6",
				"bqF6Y76Karh9nWy9oOPKRM9XnP8laU8ExgeJWd7E");
		
		PushService.setDefaultPushCallback(this, MainActivity.class);
		ParseInstallation.getCurrentInstallation().saveInBackground();
	}
	
	public static void updateParseInstallation(ParseUser user) {
		ParseInstallation installation = ParseInstallation.getCurrentInstallation();
		installation.put(ParseConstants.KEY_USER_ID, user.getObjectId());
		installation.saveInBackground();
	}
}
