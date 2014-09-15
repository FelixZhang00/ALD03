package zhangfei.example.mydouban.receiver;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * @author tmac 负责统筹全局的toast 网络状态
 * 
 */
public class NetConnChangedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		NetworkInfo wifiNetInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobNetInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (activeNetInfo != null && activeNetInfo.isConnected()) {
			// 有情提示网络类型
			if (mobNetInfo != null && mobNetInfo.isConnected()) {
				showToast(context, "您正在使用2g/3g/4g网络");
			} else if (wifiNetInfo != null && wifiNetInfo.isConnected()) {
				// 3g切换wifi时重复显示
				// showToast(context, "您正在使用wifi");
			}
		} else {
			showToast(context, "网络不可用");
		}

	}

	/**
	 * When app is not seen ,don't shwo toast.
	 * 
	 * @param context
	 */
	private void showToast(Context context,String text) {
		String packageName = context.getPackageName();
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> appTask = activityManager.getRunningTasks(1);
		String paknameCurrent = appTask.get(0).topActivity.getPackageName();
		String paknameThis=context.getPackageName();
		System.out.println(paknameCurrent);
		if (paknameThis.equals(paknameCurrent)) {
			Toast.makeText(context, text, 0).show();
		}

	}
}
