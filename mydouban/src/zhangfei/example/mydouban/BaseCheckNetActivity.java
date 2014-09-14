package zhangfei.example.mydouban;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

/**
 * @author tmac If child activity want to extends this class , must set
 *         mContext_fromP=this;
 */
public class BaseCheckNetActivity extends Activity {
	public Context mContext_fromP;
	private String TAG = "CheckNetBaseActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	/**
	 *
	 * This method is optimize for xiaomi ,and other device at least won't
	 * crash.
	 * 
	 * @param flag
	 *            choose the intent of NegativeButton . False means do nothing,
	 *            true means finish the current activity.
	 */
	public void showNetSetDialog(final boolean flag) {
		final PackageManager pm = mContext_fromP.getPackageManager();
		final Intent settingIntent = new Intent();
		AlertDialog.Builder builder = new Builder(mContext_fromP);
		builder.setTitle("! 设置网络");
		builder.setMessage("网络错误，请检查网络设置。");
		builder.setCancelable(false);
		builder.setPositiveButton("设置网络", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				ComponentName componentName = new ComponentName(
						"com.android.settings",
						"com.android.settings.MiuiSettings");
				try {
					pm.getActivityInfo(componentName, 0);
					settingIntent.setClassName("com.android.settings",
							"com.android.settings.MiuiSettings");
				} catch (NameNotFoundException e) {
					e.printStackTrace();
					settingIntent.setClassName("com.android.settings",
							"com.android.settings.Settings");
				}

				startActivity(settingIntent);

				/*
				 * I think the method blew is more humanize. But I cann't get
				 * result from setting. I GIVE UP.
				 */
				// startActivityForResult(settingIntent, REQUESTCODE_SETTING);
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (flag) {
					((Activity) mContext_fromP).finish();
				}
			}
		});
		builder.create().show();

	}

	/**
	 * check out whether the network is available.
	 * 
	 * @return true if the network is available.
	 */
	public boolean isNetworkAvail() {
		ConnectivityManager cman = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = cman.getActiveNetworkInfo();
		Log.i(TAG, "network avail:" + (info != null && info.isConnected()));
		return (info != null && info.isConnected());
	}

	/**
	 * 判断是否得到了用户授权
	 * 
	 * @return
	 */
	public boolean isUserAuthoroized() {
		SharedPreferences sp = mContext_fromP.getSharedPreferences("config",
				Context.MODE_PRIVATE);
		String tokenaccess = sp.getString("tokenaccess", null);
		String tokensecret = sp.getString("tokensecret", null);
		if (tokenaccess == null || tokensecret == null
				|| "".equals(tokenaccess)) {
			return false;
		} else {
			return true;
		}
	}

}
