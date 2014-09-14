package zhangfei.example.mydouban;

import zhangfei.example.mydouban.Utils.NetUtil;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * @author tmac 增加功能：点击回车键隐藏键盘. 先判断 mLLCaptcha 是否可见
 */
public class LoginActivity2 extends BaseCheckNetActivity implements
		OnClickListener {
	protected static final int NEED_CAPTCHA = 10;
	protected static final int NOT_NEED_CAPTCHA = 11;
	protected static final int GET_CAPTCHA_ERROR = 12;
	protected static final int LOGIN_FAILED = 13;
	protected static final int LOGIN_SUCCESS = 14;

	private Button mBtnLog;
	private Button mBtnExit;
	private LinearLayout mLLCaptcha;
	private EditText mEtName;
	private EditText mEtPwd;
	private EditText mEtCaptcha;
	private ImageView mIvCaptcha;
	private RelativeLayout mRL;
	private ProgressDialog mPd;
	private View mKeyboard;

	// if net is'n connected ,set the flag false.
	private boolean NET_CONNED = false;
	private short HAVE_GETCAPTCHA_NUM = 0;
	private String TAG = "LoginActivity";
	private String mResultCaptcha = null;
	private Bitmap mBitmap;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			closeCurrentPd();
			switch (msg.what) {

			case LOGIN_SUCCESS:
				Toast.makeText(getApplicationContext(), "登陆成功", 1).show();
				setResult(RESULT_OK);
				finish();
				break;
			case LOGIN_FAILED:
				Toast.makeText(getApplicationContext(), "登陆失败", 1).show();
				// 登录失败后，必须重新向服务器提交请求
				getCaptcha();
				break;
			default:
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login2);
		mContext_fromP = this;
		// 1.寻找控件
		// 2.赋值控件
		setupView();
		// 3.设置点击事件
		setupClick();
		getCaptcha();
	}

	private void checkNet() {
		// 检查网络状态，没联网就弹出对话框提示用户设置
		NET_CONNED = isNetworkAvail();
		if (!NET_CONNED) {
			showNetSetDialog(true);
		}

	}

	private void setupView() {
		mBtnExit = (Button) findViewById(R.id.btnExit);
		mBtnLog = (Button) findViewById(R.id.btnLogin);
		mLLCaptcha = (LinearLayout) findViewById(R.id.ll_captcha);
		mEtCaptcha = (EditText) findViewById(R.id.EditTextCaptchaValue);
		mEtName = (EditText) findViewById(R.id.EditTextEmail);
		mEtPwd = (EditText) findViewById(R.id.EditTextPassword);
		mIvCaptcha = (ImageView) findViewById(R.id.ImageViewCaptcha);
		mRL = (RelativeLayout) findViewById(R.id.inner_login);

	}

	// OnKeyListener onkey = new OnKeyListener() {
	//
	// @Override
	// public boolean onKey(View v, int keyCode, KeyEvent event) {
	// return false;
	// }
	// };

	class MyOnKeyListener implements OnKeyListener {
		// 是否恢复系统默认的按键逻辑
		private boolean LOGOFF_ONKEY = false;

		public MyOnKeyListener() {
		}

		public MyOnKeyListener(boolean flag) {
			this.LOGOFF_ONKEY = flag;
		}

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			mKeyboard=v;
			if (!LOGOFF_ONKEY) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					hideKeyBoard(v);
					// 既然能把键盘隐藏了，下面把login按钮的功能也实现了吧
					/*
					 * I give up. it's enough for keyboard to hide. it's
					 * difficult to Let onKey() to do multithreading medthod.
					 */
					// loginBtn();

					return true;
				}
			} else {
				return false;
			}

			return false;
		}

	}

	private void hideKeyBoard(View v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
		}

	}

	/**
	 * 设置哪一个EditText为最后一个输入框
	 * 
	 * @param flag
	 *            根据是否需要验证码来判断
	 */
	private void setWhichEndEt(boolean flag) {
		if (flag) {
			// 将验证码框设置为最后一个
			mEtCaptcha.setOnKeyListener(new MyOnKeyListener());
			mEtPwd.setOnKeyListener(new MyOnKeyListener(true));
		} else {
			// 将密码框设置为最后一个
			mEtPwd.setOnKeyListener(new MyOnKeyListener());
			mEtCaptcha.setOnKeyListener(new MyOnKeyListener(true));
		}
	}

	/**
	 * @author tmac A AsyncTask for get captcha.
	 */
	class AsyncTask4getCaptcha extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			mEtCaptcha.setText("");
			mPd = new ProgressDialog(LoginActivity2.this);
			mPd.setMessage("正在查询验证码");
			mPd.show();
			mBitmap = null;
			// the ui of Captcha need be gone.
			mIvCaptcha.setImageBitmap(mBitmap);
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			try {
				mResultCaptcha = NetUtil.isNeedCaptcha(getApplicationContext());
				if (mResultCaptcha != null) {
					// Download the picture the captha value to.
					String imagepath = getResources().getString(
							R.string.captchaurl)
							+ mResultCaptcha + "&amp;size=s";
					mBitmap = NetUtil.getImage(imagepath);
					return NEED_CAPTCHA;
				} else {
					return NOT_NEED_CAPTCHA;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return GET_CAPTCHA_ERROR;
			}

		}

		@Override
		protected void onPostExecute(Integer result) {
			closeCurrentPd();
			switch (result) {

			case NEED_CAPTCHA:
				mLLCaptcha.setVisibility(View.VISIBLE);
				mIvCaptcha.setImageBitmap(mBitmap);
				setWhichEndEt(true);
				break;
			case NOT_NEED_CAPTCHA:
				Log.i(TAG, "不需要验证码.");
				setWhichEndEt(false);

				// 处理服务器很奇怪的逻辑：先要验证码，用户登录失败后，反而不要验证码了
				if (mPd != null) {
					if (mPd.isShowing()) {
						mPd.dismiss();
						mPd = null;
						loginBtn();
					}
				}
				break;

			case GET_CAPTCHA_ERROR:
				// Log.i(TAG, "获取验证码失败.");
				Toast.makeText(getApplicationContext(), "获取验证码失败", 0).show();
				// 刷新
				if (HAVE_GETCAPTCHA_NUM < 3) {
					getCaptcha();
					HAVE_GETCAPTCHA_NUM++;
				}
				break;

			default:
				break;
			}

			super.onPostExecute(result);
		}

	}

	/**
	 * 如果需要验证码，则查询.
	 */
	private void getCaptcha() {
		checkNet();
		// If the net doesn't work ,it's no need to getCaptcha.
		if (NET_CONNED) {
			AsyncTask4getCaptcha task4getCaptcha = new AsyncTask4getCaptcha();
			task4getCaptcha.execute();
		}
	}

	private void setupClick() {
		mBtnExit.setOnClickListener(this);
		mBtnLog.setOnClickListener(this);
		mIvCaptcha.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnExit:
			// finish();
			/*
			 * To look more comfortable , I make a delay to play some anim.
			 */
			delayfinsh(500);
			break;
		case R.id.btnLogin:
			// try {
			// boolean flag = NetUtil.login(null, null, null, null,
			// getApplicationContext());
			// Toast.makeText(getApplicationContext(), "成功", 0).show();
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			loginBtn();
			break;
		case R.id.ImageViewCaptcha:
			getCaptcha();
			break;

		default:
			break;
		}
	}

	private void loginBtn() {
		checkNet();
		if (NET_CONNED) {

			if (mEtName.getText().toString().equals("")) {
				Toast.makeText(getApplicationContext(), "邮箱不能为空", 0).show();
				return;
			} else {
				String username = mEtName.getText().toString();
				String pwd = mEtPwd.getText().toString();
				if (mResultCaptcha == null) {
					chooseLogin(username, pwd, "");
					hideKeyBoard(mKeyboard);
				} else {
					String captchavalue = mEtCaptcha.getText().toString();
					if ("".equals(captchavalue)) {
						Toast.makeText(getApplicationContext(), "请输入验证码", 0)
								.show();
						return;
					} else {
						Log.i(TAG, mResultCaptcha);
						Log.i(TAG, captchavalue);
						chooseLogin(username, pwd, captchavalue);
						hideKeyBoard(mKeyboard);
					}
				}
			}
		} else {
			Toast.makeText(getApplicationContext(), "网络不可用", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * 选择登录的方式，是否需要验证码. 并且在新开的线程中执行登录
	 * 
	 * @param pwd
	 * @param username
	 * @param captchavalue
	 */
	private void chooseLogin(final String username, final String pwd,
			final String captchavalue) {
		mPd = new ProgressDialog(this);

		mPd.setMessage("正在登录...");
		mPd.show();
		mPd.setCancelable(false);

		new Thread() {

			@Override
			public void run() {
				super.run();
				try {
					boolean flag = NetUtil.login(username, pwd, captchavalue,
							mResultCaptcha, getApplicationContext());
					Message msg = Message.obtain();
					if (flag) {
						msg.what = LOGIN_SUCCESS;
					} else {
						msg.what = LOGIN_FAILED;
					}
					handler.sendMessage(msg);

				} catch (Exception e) {
					e.printStackTrace();
					Message msg = Message.obtain();
					msg.what = LOGIN_FAILED;
					handler.sendMessage(msg);
				}

			}

		}.start();
	}

	/**
	 * 让当前activity 以渐变的动画退出
	 * 
	 * @param delayMillis
	 *            延迟时间 ms
	 */
	private void delayfinsh(long delayMillis) {
		AlphaAnimation anim = new AlphaAnimation(1.0f, 0.4f);
		anim.setDuration(delayMillis);
		mRL.startAnimation(anim);
		new Handler().postDelayed(new Delayfinish(), delayMillis);
	}

	class Delayfinish implements Runnable {

		@Override
		public void run() {
			finish();
		}

	}

	@Override
	protected void onDestroy() {
		closeCurrentPd();
		super.onDestroy();
	}

	private void closeCurrentPd() {

		if (mPd != null) {
			if (mPd.isShowing()) {
				mPd.dismiss();
			}
			mPd = null;
		}

	}

}
