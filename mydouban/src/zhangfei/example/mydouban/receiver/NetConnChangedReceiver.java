package zhangfei.example.mydouban.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
				Toast.makeText(context, "您正在使用2g/3g/4g网络", 0).show();
			} else if (wifiNetInfo != null && wifiNetInfo.isConnected()) {
				//3g切换wifi时重复显示
				// Toast.makeText(context, "您正在使用wifi", 0).show();

			}
		} else {
			Toast.makeText(context, "网络不可用", 0).show();

		}

	}

}
