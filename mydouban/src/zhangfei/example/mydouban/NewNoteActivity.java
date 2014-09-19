package zhangfei.example.mydouban;

import java.io.IOException;

import zhangfei.example.mydouban.domain.Note;

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

	private SharedPreferences mSp1;
	private SharedPreferences mSp2;
	private String mTitle;
	private String mContent;
	private Note mOldNote; // 修改日记时传进来的note对象
	private Note mCurrNote; // 当前的note对象
	private boolean mFlag_model; // true: 修改 , false: 新建

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mSp1 = getSharedPreferences("notetemp", Context.MODE_PRIVATE);
		mSp2 = getSharedPreferences("note_remote_temp", Context.MODE_PRIVATE);
		setContentView(R.layout.note_edit);
		super.onCreate(savedInstanceState);
		mFlag_model = getIntent().getBooleanExtra("ismodify", false);
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
		if (mFlag_model) {
			initRemoteNoteUI();
		} else {
			initLocalNoteUI();
		}

	}

	/**
	 * user修改日记 从服务器获取日记信息，填充到手机UI上
	 */
	private void initRemoteNoteUI() {
		wTv_title_bar.setText("修改日记");
		MyApp app = (MyApp) getApplication();
		mOldNote = app.Anote;
		System.out.println("mOldNote->" + mOldNote.toString());
		if (mOldNote != null) {
			String title = mOldNote.getTitle();
			String content = mOldNote.getContent();
			String privacy = mOldNote.getPrivacy();
			String can_reply = mOldNote.getCan_reply();
			initNoteUI(title, privacy, content, can_reply);
		}
	}

	/**
	 * 检查shared_prefs中是否有保存的note_temp,有则设置到界面上
	 */
	private void initLocalNoteUI() {
		wTv_title_bar.setText("编辑日记");
		String title = mSp1.getString("title", "");
		String auth = mSp1.getString("auth", "");
		String content = mSp1.getString("content", "");
		String can_reply = mSp1.getString("can_reply", "");
		initNoteUI(title, auth, content, can_reply);

	}

	/**
	 * 将获取的信息填充到UI上
	 * 
	 * @param title
	 * @param auth
	 * @param content
	 * @param can_reply
	 */
	private void initNoteUI(String title, String auth, String content,
			String can_reply) {
		wEt_title.setText(title);

		wEt_content.setText(content);

		if (auth != null && !auth.equals("")) {
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
				backPress();
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnSave:
			checkUpLoadModel();
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

	/**
	 * 选择上传数据的方式：提交新日记，更新修改的日记，对未修改的日记不提交。
	 */
	private void checkUpLoadModel() {
		setCurrentNote();
		if (mFlag_model) {
			// 修改
			if (mCurrNote.toString().equals(mOldNote.toString())) {
				showToast("日记未修改");
			} else {
				// 提交修改后的日记，不用设置当前的时间，在豆瓣网上只给新建的日记设置发布时间
				if (checkNote(mCurrNote.getTitle(), mCurrNote.getContent())) {
					upLoadData(mCurrNote);
				}
			}
		} else {
			// 新建
			if (checkNote(mCurrNote.getTitle(), mCurrNote.getContent())) {
				upLoadData(mCurrNote);
			}

		}

	}

	/**
	 * 设置当前UI上的Note对象
	 */
	private void setCurrentNote() {
		String title = wEt_title.getText().toString();
		String content = wEt_content.getText().toString();
		String auth = checkAuth();
		String can_reply = checkCan_reply();
		if (mFlag_model) {
			mCurrNote = new Note(content, title, auth, can_reply,
					mOldNote.getPubdate());
		} else {
			mCurrNote = new Note(content, title, auth, can_reply);
		}
	}

	private void upLoadData(Note note) {
		new AsyncTask<Note, Void, Boolean>() {

			@Override
			protected void onPreExecute() {
				wPd = new ProgressDialog(NewNoteActivity.this);
				if (mFlag_model) {
					wPd.setMessage("正在更新..");
				} else {
					wPd.setMessage("正在发表..");
				}
				wPd.show();
				super.onPreExecute();
			}

			@Override
			protected Boolean doInBackground(Note... params) {
				Note note = params[0];
				try {
					if (mFlag_model) {
						myService.updateNote(mOldNote.getEntry(),
								new PlainTextConstruct(note.getTitle()),
								new PlainTextConstruct(note.getContent()),
								note.getPrivacy(), note.getCan_reply());
					} else {
						// 提交新建的日记
						myService.createNote(
								new PlainTextConstruct(note.getTitle()),
								new PlainTextConstruct(note.getContent()),
								note.getPrivacy(), note.getCan_reply());
					}

				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				return true;

			}

			@Override
			protected void onPostExecute(Boolean result) {
				wPd.dismiss();
				if (mFlag_model) {
					if (result) {
						showToast("更新成功");
						// setResult(RESULT_OK);
						backMyNote();

					} else {
						showToast("更新日记失败，请重试");
					}
				} else {
					if (result) {
						showToast("发表成功");
						clearNoteTemp();
						// setResult(RESULT_OK);
						backMyNote();

					} else {
						showToast("发表日记失败，请重试");
					}
				}

				super.onPostExecute(result);
			}

		}.execute(note);

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
		Editor editor = mSp1.edit();
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
			return backPress();
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private boolean backPress() {
		if (mFlag_model) {
			// 处理 用户修改已发布日记 后的回退
			setCurrentNote();
			if (mCurrNote.toString().equals(mOldNote.toString())) {
				showToast("日记未修改");
				backMyNote();
				return false;
			} else {
				if (checkSave()) {
					showbackNotice2();
					return true;
				} else {
					backMyNote();
					return false;
				}

			}
		} else {
			// 新建
			if (checkSave()) {
				showbackNotice();
				return true;
			} else {
				backMyNote();
				return false;
			}
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
	 * 当用户在新建日记时 当用户点击 取消 或者按返回键，弹出对话框是否保存
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

							}
						})
				.setNeutralButton("清空草稿",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								clearNoteTemp();
								backMyNote();

							}
						})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();

	}

	/**
	 * 当用户在修改已发布日记时 当用户点击 取消 或者按返回键，弹出对话框是否保存
	 * 
	 */
	private void showbackNotice2() {

		new AlertDialog.Builder(this)
				.setTitle("提醒")
				.setMessage("已经修改了日记，需要更新吗？")
				.setPositiveButton("更新", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						checkUpLoadModel();

					}
				})
				.setNeutralButton("保存到草稿",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								showToast("需要保存复杂对象到shared_prefs,本功能尚未实现");
								backMyNote();
								
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
		Editor editor = mSp1.edit();
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
		finish();
	}

}
