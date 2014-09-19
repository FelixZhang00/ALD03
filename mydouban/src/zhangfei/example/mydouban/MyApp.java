package zhangfei.example.mydouban;

import zhangfei.example.mydouban.domain.Note;
import android.app.Application;
import android.content.Intent;

public class MyApp extends Application {

	public Note Anote;

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		String packname = getApplicationContext().getPackageName();

		// 发送一些广播 关闭掉一些activity service
		Intent intent = new Intent();
		intent.setAction(packname + ".action.kill_activity");
		sendBroadcast(intent);
	}

}
