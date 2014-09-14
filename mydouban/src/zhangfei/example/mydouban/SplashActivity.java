package zhangfei.example.mydouban;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author tmac It's not wise to check and show related dialog, I put that
 *         module in the MeActivity.
 */
public class SplashActivity extends Activity {
	private TextView tv_version;
	private LinearLayout mLL;
	private final String TAG = "SplashActivity";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.splash);
		tv_version = (TextView) this
				.findViewById(R.id.tv_splash_version_number);
		mLL = (LinearLayout) this.findViewById(R.id.ll_splash);
		tv_version.setText(getVersion());

		// give a anim ,than go into MainTabActivity
		AlphaAnimation anim = new AlphaAnimation(1.0f, 0.6f);
		anim.setDuration(1200);
		mLL.startAnimation(anim);
		// using handler duration some time , than exec task.
		new Handler().postDelayed(new LoadMainTabTask(), 1200);

	}

	class LoadMainTabTask implements Runnable {

		@Override
		public void run() {
			Intent mainTabIntent = new Intent(SplashActivity.this,
					MainTabActivity.class);
			startActivity(mainTabIntent);

			// Intent testIntent = new Intent(SplashActivity.this,
			// TestActivity.class);
			// startActivity(testIntent);
			finish();
		}

	}

	private String getVersion() {

		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			return "Version" + info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "版本号未知";
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// if (requestCode==REQUESTCODE_SETTING) {
		// Log.i(TAG, "onActivityResult from setting");
		// }

		/*
		 * The method above is invalid , because the log is printed when app
		 * enter the setting activity rather than come back from setting.
		 */

		/*
		 * resultCode must be set on the activity(or other component)
		 * requested,so the blew medthod also failed.
		 */
		// if (resultCode==RESULT_OK) {
		// Log.i(TAG, "onActivityResult from setting");
		// }

		/*
		 * Finally , I choose this method.But I failed again ,before come back
		 * from setting ,the method has already been exec.
		 */
		// if (requestCode == REQUESTCODE_SETTING) {
		// if (isNetworkAvail()) {
		// Log.i(TAG, "after setting network is avail");
		// startActivity(settingIntent);
		// finish();
		// } else {
		// /*
		// * @leaveit I can do something to identify the app now is
		// * off-line, but now leave it.
		// */
		// Log.i(TAG, "network is still not avail");
		// startActivity(settingIntent);
		// finish();
		// }

		// }

	}

}
