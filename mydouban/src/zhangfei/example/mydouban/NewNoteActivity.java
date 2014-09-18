package zhangfei.example.mydouban;

import java.io.IOException;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.util.ServiceException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class NewNoteActivity extends BaseMyActivity implements OnClickListener {

	private TextView wTv_title_bar;
	private EditText wEt_title;
	private EditText wEt_content;
	private RadioButton wRb_private;
	private RadioButton wRb_friend;
	private RadioButton wRb_public;
	private CheckBox wCb;
	private Button wBtn_save, wBtn_cancel;
	private ProgressDialog wPd;
	private ImageButton wIbtn_back;

	private SharedPreferences mSp;
	private String mTitle;
	private String mContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mSp = getSharedPreferences("notetemp", Context.MODE_PRIVATE);
		setContentView(R.layout.note_edit);
		super.onCreate(savedInstanceState);
		setupView();
		setupListener();
		fillData();
	}

	@Override
	public void setupView() {
		mTv_user_fromP = (TextView) findViewById(R.id.tv_titlebar_user);
		wTv_title_bar = (TextView) findViewById(R.id.tv_titlebar_tietle);
		wEt_title = (EditText) findViewById(R.id.EditTextTitle);
		wEt_content = (EditText) findViewById(R.id.EditTextContent);
		wRb_friend = (RadioButton) findViewById(R.id.rb_friend);
		wRb_private = (RadioButton) findViewById(R.id.rb_private);
		wRb_public = (RadioButton) findViewById(R.id.rb_public);
		wCb = (CheckBox) findViewById(R.id.cb_can_reply);
		wBtn_save = (Button) findViewById(R.id.btnSave);
		wBtn_cancel = (Button) findViewById(R.id.btnCancel);

		wCb.setChecked(true);
		wRb_public.setChecked(true);
		wIbtn_back = (ImageButton) findViewById(R.id.back_button);

	}

	@Override
	public void fillData() {
		wTv_title_bar.setText("编辑日记");
		initNoteUI();
	}

	/**
	 * 检查shared_prefs中是否有保存的note_temp,有则设置到界面上
	 */
	private void initNoteUI() {

		String title = mSp.getString("title", "");
		String auth = mSp.getString("auth", "");
		String content = mSp.getString("content", "");
		String can_reply = mSp.getString("can_reply", "");
		wEt_title.setText(title);

		wEt_content.setText(content);

		if (!auth.equals("")) {
			initRadioButton(auth);
		}

		if (can_reply.equals("yes")) {
			wCb.setChecked(true);
		} else if (can_reply.equals("no")) {
			wCb.setChecked(false);
		}

	}

	private void initRadioButton(String auth) {
		if (auth.equals("public")) {
			wRb_public.setChecked(true);
		} else if (auth.equals("private")) {
			wRb_private.setChecked(true);
		} else if (auth.equals("friend")) {
			wRb_friend.setChecked(true);
		}
	}

	@Override
	public void setupListener() {
		wBtn_save.setOnClickListener(this);
		wBtn_cancel.setOnClickListener(this);
		wIbtn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checkSave()) {
					showbackNotice();
				} else {
					backMyNote();
					finish();
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnSave:
			uploadData();
			break;

		case R.id.btnCancel:
			// 弹出对话框，提醒是否保存
			if (checkSave()) {
				showbackNotice();
			}
			break;

		default:
			break;
		}
	}

	private void uploadData() {
		final String title = wEt_title.getText().toString();
		final String content = wEt_content.getText().toString();
		String auth = null;
		String can_reply = null;
		if (checkNote(title, content)) {
			auth = checkAuth();
			can_reply = checkCan_reply();

			new AsyncTask<String, Void, Boolean>() {

				@Override
				protected void onPreExecute() {
					wPd = new ProgressDialog(NewNoteActivity.this);
					wPd.setMessage("正在提交..");
					wPd.show();
					super.onPreExecute();
				}

				@Override
				protected Boolean doInBackground(String... params) {
					try {
						myService.createNote(new PlainTextConstruct(title),
								new PlainTextConstruct(content), params[0],
								params[1]);
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
					return true;

				}

				@Override
				protected void onPostExecute(Boolean result) {
					wPd.dismiss();
					if (result) {
						showToast("提交成功");
						clearNoteTemp();
						// setResult(RESULT_OK);
						backMyNote();
						finish();
					} else {
						showToast("发表日记失败，请重试");
					}
					super.onPostExecute(result);
				}

			}.execute(auth, can_reply);
		}

	}

	private String checkCan_reply() {
		String can_reply;
		if (wCb.isChecked()) {
			can_reply = "yes";
		} else {
			can_reply = "no";
		}
		return can_reply;
	}

	private String checkAuth() {
		String result = null;
		if (wRb_public.isChecked()) {
			result = "public";
		} else if (wRb_private.isChecked()) {
			result = "private";
		} else if (wRb_friend.isChecked()) {
			result = "friend";
		}
		return result;
	}

	/**
	 * 清除shared_prefs 中的 note_temp
	 */
	protected void clearNoteTemp() {
		Editor editor = mSp.edit();
		editor.clear();
		editor.commit();
	}

	/**
	 * @param title
	 * @param content
	 * @return true: user's note is right.
	 */
	private boolean checkNote(String title, String content) {
		boolean flag = true;
		if (title == null || title.trim().equals("")) {
			showToast("给日记加个标题吧");
			flag = false;
		} else if (content == null || content.trim().equals("")) {
			showToast("日记正文还没有写呢");
			flag = false;
		}
		return flag;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 按下键盘上返回按钮
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (checkSave()) {
				showbackNotice();
				return true;
			} else {
				backMyNote();
				finish();
				return false;
			}
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * 根据日记的标题、内容，判断是否保存
	 * 
	 * @return true 标题or内容被user编辑，应该保存
	 */
	private boolean checkSave() {
		mTitle = wEt_title.getText().toString();
		mContent = wEt_content.getText().toString();

		if (mTitle != null && !mTitle.trim().equals("")) {
			return true;
		}
		if (mContent != null && !mContent.trim().equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * 当用户点击 取消 或者按返回键，弹出对话框是否保存
	 * 
	 */
	private void showbackNotice() {

		new AlertDialog.Builder(this)
				.setTitle("提醒")
				.setMessage("您想在退出之前，保存到手机吗？")
				.setPositiveButton("保存草稿",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								saveTempNote();
								backMyNote();
								finish();
							}
						})
				.setNeutralButton("清空草稿",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								clearNoteTemp();
								backMyNote();
								finish();
							}
						})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();

	}

	/**
	 * 把当前的note信息 存放在shareperence里
	 */
	private void saveTempNote() {
		Editor editor = mSp.edit();
		editor.putString("title", mTitle);
		editor.putString("content", mContent);
		editor.putString("auth", checkAuth());
		editor.putString("can_reply", checkCan_reply());
		editor.commit();
		showToast("保存成功");
	}

	/**
	 * 重新进入MyNoteActivity2 初始化所有成员变量
	 */
	private void backMyNote() {
		Intent backMyNoteIntent = new Intent(NewNoteActivity.this,
				MyNoteActivity2.class);
		startActivity(backMyNoteIntent);
	}

}
