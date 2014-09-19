package zhangfei.example.mydouban;

import zhangfei.example.mydouban.Utils.MyCrashHandler;
import zhangfei.example.mydouban.domain.Note;
import android.app.Application;
import android.content.Intent;

public class MyApp extends Application {

	public Note Anote;

	@Override
	public void onCreate() {
		super.onCreate();
		MyCrashHandler myCrashHandler = MyCrashHandler.getInstance();
		myCrashHandler.init(getApplicationContext());
		// 把自定义的异常处理类设置 给主线程
		Thread.currentThread().setUncaughtExceptionHandler(myCrashHandler);

	}

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
