package zhangfei.example.mydouban;

import com.google.gdata.util.ServiceException;

import zhangfei.example.mydouban.SplashActivity.LoadMainTabTask;
import zhangfei.example.mydouban.engine.DoubanUserInfoProvider;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.DataSetObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MeActivity extends BaseCheckNetActivity implements
		OnItemClickListener {

	protected static final int GET_USERNAME_SUCCESS = 10;
	protected static final int GET_USERNAME_FAILED = 11;
	private static final String items[] = { "我读...", "我看...", "我听...", "我评...",
			"我的日记", "我的资料", "小组" };
	private String TAG = "MeActivity";
	private boolean mBackKeyPressedTimes = false; // false 不允许退出

	private ListView mLv;
	private static TextView mTv_user;
	private MenuItem mItem_user;
	private ImageButton mIbtn_back;

	private NetConnChangedReceiver mNetReceiver;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GET_USERNAME_SUCCESS:
				String username = (String) msg.obj;
				Log.i(TAG, "username" + username);
				if (username != null) {
					mTv_user.setText("(" + username + ")");
				}
				break;

			default:
				break;
			}

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.me_layout);
		mContext_fromP = this;
		super.onCreate(savedInstanceState);

		
		setupView();
		mLv.setAdapter(new ArrayAdapter<String>(this, R.layout.me_item,
				R.id.tv_me_item, items));
		setupListener();

		if (!isUserAuthoroized()) {
			TitleBarState.setTitleBarState(TitleBarState.NO_LOGIN);
		} else {
			// 显示已登录账户信息的功能，由NetConnChangedReceiver来完成.
		}

		if (!isNetworkAvail()) {
			showNetSetDialog(false);
		}

	}

	private void setupListener() {
		mLv.setOnItemClickListener(this);
		mIbtn_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();

			}
		});
	}

	private void setupView() {
		mLv = (ListView) this.findViewById(R.id.lv_me);
		mTv_user = (TextView) findViewById(R.id.tv_titlebar_user);
		mIbtn_back = (ImageButton) findViewById(R.id.back_button);

	}

	@Override
	protected void onStart() {
		// 完成NetConnChangedReceiver的初始化
		mNetReceiver = new NetConnChangedReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		filter.setPriority(1000);
		registerReceiver(mNetReceiver, filter);
		super.onStart();
	}

	@Override
	protected void onStop() {

		unregisterReceiver(mNetReceiver);
		super.onStop();
	}

	/**
	 * 如果用户已经授权就显示用户名信息
	 */
	private void showUser() {
		if (isUserAuthoroized()) {
			// 显示xxx已登录
			/*
			 * 获取服务器上的数据是耗时操作，要在worker thread中执行
			 */
			new Thread() {

				@Override
				public void run() {
					super.run();
					DoubanUserInfoProvider infoProvider = new DoubanUserInfoProvider(
							getApplicationContext());
					Message msg = Message.obtain();
					try {
						String username = infoProvider.getUserName();
						msg.what = GET_USERNAME_SUCCESS;
						msg.obj = username;
						handler.sendMessage(msg);
					} catch (Exception e) {
						// msg.what = GET_USERNAME_FAILED;
						// handler.sendMessage(msg);
						e.printStackTrace();
					}

				}

			}.start();

		} else {
			// 显示未授权的状态

		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (isUserAuthoroized()) {
			// 进入到对应的界面
			Log.i(TAG, "User has already Authoroized.");
			Log.i(TAG, "选中的是：" + items[position]);
			switch (position) {
			case 0:
				Intent myreadIntent = new Intent(this, MyReadActivity.class);
				startActivity(myreadIntent);
				break;
			case 4:
				Intent mynoteIntent = new Intent(this, MyNoteActivity2.class);
				startActivity(mynoteIntent);
				break;
			case 5:
				Intent myInfoIntent = new Intent(this, MyInfoActivity.class);
				startActivity(myInfoIntent);
				break;

			default:
				break;
			}

		} else {
			enterLoginActivity(false);
		}
	}

	/**
	 * 进入登录界面
	 * 
	 * @param flag
	 *            true : requestCode=0 ; flase : requestCode=1
	 */
	private void enterLoginActivity(boolean flag) {
		Intent longinIntent = new Intent(MeActivity.this, LoginActivity2.class);
		if (flag) {
			startActivityForResult(longinIntent, 0);
		} else {
			startActivityForResult(longinIntent, 1);
		}
	}

	/**
	 * @author tmac 在内部类中 接受的广播范围更大：在onCreate() 的时候也被认为是NET CHANGE.
	 */
	class NetConnChangedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// abortBroadcast();

			if (isNetworkAvail()) {
				// 网络改变了 ，从无到有
				showUser();
			} else {
				if (isUserAuthoroized()) {
					// 将账户信息改为离线
					// String text = "(" + "<font color='red'>离线</font>" + ")";
					// mTv_user.setText(Html.fromHtml(text));
					TitleBarState.setTitleBarState(TitleBarState.OFF_LINE);
				} else {
					// String text = "(" + "<font color='red'>未登录</font>" + ")";
					// mTv_user.setText(Html.fromHtml(text));
					TitleBarState.setTitleBarState(TitleBarState.NO_LOGIN);

				}
			}

		}

	}

	// 当按下菜单键的时候 创建出菜单对象
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.me_menu, menu);
		mItem_user = menu.findItem(R.id.menu_item_clearuser);
		initMenuItem();

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * 初始化菜单项
	 */
	private void initMenuItem() {
		if (isUserAuthoroized()) {
			mItem_user.setTitle("登出");
		} else {
			mItem_user.setTitle("登录");

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_clearuser:
			if (isUserAuthoroized()) {
				// 清除当前帐号的后门钥匙
				SharedPreferences sp = getSharedPreferences("config",
						Context.MODE_PRIVATE);
				Editor editor = sp.edit();
				editor.putString("tokenaccess", "");
				editor.putString("tokensecret", "");
				editor.commit();
				new Handler().postDelayed(new DelayChangeMenu("登录"), 500);
				Toast.makeText(getApplicationContext(), "登出成功", 0).show();
				// 改变titlebar上的状态
				// String text = "(" + "<font color='red'>未登录</font>" + ")";
				// mTv_user.setText(Html.fromHtml(text));
				TitleBarState.setTitleBarState(TitleBarState.NO_LOGIN);
			} else {
				enterLoginActivity(true);
				// 留给onActivityResult来处理
				// new Handler().postDelayed(new DelayChangeMenu("登出"), 500);
			}

			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * show user the chang process of menu item title is equal to suicide.
	 * 
	 * @author tmac
	 *
	 */
	class DelayChangeMenu implements Runnable {
		private String title;

		public DelayChangeMenu() {
		}

		public DelayChangeMenu(String title) {
			this.title = title;
		}

		@Override
		public void run() {
			mItem_user.setTitle(title);
		}

	}

	/**
	 * @author tmac 在单独的类中管理TitleBar上显示的账户状态
	 */
	static class TitleBarState {
		private final static short NO_LOGIN = 0;
		private final static short LOGIN = 1;
		private final static short OFF_LINE = 2;

		public static void setTitleBarState(short state) {
			switch (state) {
			case NO_LOGIN:
				String text = "(" + "<font color='red'>未登录</font>" + ")";
				mTv_user.setText(Html.fromHtml(text));
				break;
			case LOGIN:

				break;
			case OFF_LINE:
				String text2 = "(" + "<font color='red'>离线</font>" + ")";
				mTv_user.setText(Html.fromHtml(text2));
				break;

			default:
				break;
			}

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			Log.i(TAG, "onActivityResult requestCode == 0");
			if (resultCode == RESULT_OK) {
				// 在LoginActivity中setResult(RESULT_OK) 表示登录成功
				/*
				 * 此intent是我菜单键触发的，所以有关的menu一定被实例化了，不会出现空指针的问题.
				 */
				Log.i(TAG, "onActivityResult resultCode == RESULT_OK 0");
				if (mItem_user != null) {
					initMenuItem();
				}
			}
		} else {
			Log.i(TAG, "onActivityResult requestCode == 1");
			if (resultCode == RESULT_OK) {
				Log.i(TAG, "onActivityResult resultCode == RESULT_OK 1");
				if (mItem_user != null) {
					initMenuItem();
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/*
	 * 每当此activity重新可见时就检查下网络，并设置titlebar的状态(non-Javadoc) 不合理，改用广播接受.
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// 在登录界面退出后就检查下是否能获取user
		showUser();
		/*
		 * onCreateOptionsMenu 在菜单键被按后才调用的，在onResume中mItem_user还没有被实例化.
		 */
		// initMenuItem();

	}

	// 按返回键后调用的方法
	@Override
	public void onBackPressed() {
		if (!mBackKeyPressedTimes) {
			Toast.makeText(getApplicationContext(), "再按一次退出应用",
					Toast.LENGTH_SHORT).show();
			mBackKeyPressedTimes = true;
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(1800);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						mBackKeyPressedTimes = false;
					}
					super.run();
				}
			}.start();
			return; // 在打开上面的新线程的同时，已经return了
		} else {
			finish();
		}
		super.onBackPressed(); // 抢在调用父类方法之前
	}

}
