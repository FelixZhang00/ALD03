package zhangfei.example.mydouban;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdata.data.TextContent;
import com.google.gdata.data.douban.Attribute;
import com.google.gdata.data.douban.NoteEntry;
import com.google.gdata.data.douban.NoteFeed;
import com.google.gdata.data.douban.UserEntry;
import com.google.gdata.util.ServiceException;

import zhangfei.example.mydouban.domain.Note;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author tmac
 *
 */
public class MyNoteActivity2 extends BaseMyActivity implements OnClickListener {

	private static final String TAG = "MyNoteActivity";
	private TextView wTv_title_bar;
	private ListView wLv;
	private TextView wTv_link;
	private LinearLayout wLL_notice;
	private Button wBtn_pre;
	private Button wBtn_next;
	private ImageButton wIbtn_add;
	private ProgressDialog wPd;

	private MyAdapter mAdapter;
	private UserEntry mUserEntry;
	private List<Note> mNotesTemp; // 只保存上一次 一页的信息
	private int mNumPage = 1; // 当前页号
	private int mMaxPage = 1; // 最大页号
	private int mNumItems = 0;// 当前页的条目数
	private Map<Integer, List<Note>> mMapPage; // 每页对应一组note数据
	private int mPosition;
	// private MyAdapter mAdapter;

	private int mStartIndex;
	private int mCount;
	private int mNoteMax = 0;
	/*
	 * 豆瓣好像没有提供user的日记总数。不得不用其他方法来获得。
	 */
	protected boolean mFlag_End_Page = false;
	protected boolean mFlag_alreadyMax = false;

	// 第一次fillData，发现没有日记,就把mFlag_No_Note 置为false
	// 从服务器获取数据异常，不包括网络连接
	private boolean mFlag_Server_Error = false;
	private boolean mFlag_isloading = false;
	protected boolean mFlag_delete_note;
	private boolean mFlag_NotesTemp = false; // mNotesTemp 是否被设置的标志

	private final static int EDIT_NOTE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.mynote_layout);
		super.onCreate(savedInstanceState);
		// 应该根据屏幕尺寸，选择每页的数目
		mCount = 7;
		mStartIndex = 1;
		mMapPage = new HashMap<Integer, List<Note>>();
		fillData();
	}

	@Override
	public void setupView() {
		mRl_loading_fromP = (RelativeLayout) findViewById(R.id.rl_myread_loading);
		wIbtn_add = (ImageButton) findViewById(R.id.btn_titlebar_add);
		wTv_title_bar = (TextView) findViewById(R.id.tv_titlebar_tietle);
		mTv_user_fromP = (TextView) findViewById(R.id.tv_titlebar_user);

		mPb_loadingFP = (ProgressBar) findViewById(R.id.pb_myread);
		mTv_loadingFP = (TextView) findViewById(R.id.txt_loading);

		wLv = (ListView) findViewById(R.id.lv_mynote_subject);
		wLL_notice = (LinearLayout) findViewById(R.id.ll_myread_notice);
		wTv_link = (TextView) findViewById(R.id.tv_myread_link);
		wBtn_next = (Button) findViewById(R.id.btn_mynote_next);
		wBtn_pre = (Button) findViewById(R.id.btn_mynote_pre);
		registerForContextMenu(wLv);
	}

	@Override
	public void setupListener() {
		wBtn_next.setOnClickListener(this);
		wBtn_pre.setOnClickListener(this);
		wIbtn_add.setOnClickListener(this);

		// 点击条目进入编辑窗口
		wLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Note note = (Note) wLv.getItemAtPosition(position);
				System.out.println("note entry" + note.getEntry().toString());
				enterNoteEdit(note);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_mynote_next:
			goNextPage();
			break;
		case R.id.btn_mynote_pre:
			goPrePage();
			break;

		case R.id.btn_titlebar_add:

			Log.i(TAG, "btn_titlebar_edit");
			Intent addIntent = new Intent(MyNoteActivity2.this,
					NewNoteActivity.class);
			// startActivityForResult(addIntent, 0);
			startActivity(addIntent);
			finish();

			break;

		default:
			break;
		}

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mynote_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		mPosition = info.position;
		Note note = (Note) wLv.getItemAtPosition(mPosition);
		NoteEntry entry = note.getEntry();
		switch (item.getItemId()) {
		case R.id.edit:
			enterNoteEdit(note);
			return true;
		case R.id.delete:
			if (!mFlag_isloading) {
				deleteNote(entry);
			}
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void enterNoteEdit(Note note) {

		Intent modifyintent = new Intent(MyNoteActivity2.this,
				NewNoteActivity.class);
		MyApp app = (MyApp) getApplication();
		app.Anote = note;
		modifyintent.putExtra("ismodify", true);
		startActivityForResult(modifyintent, EDIT_NOTE);
	}

	private void deleteNote(NoteEntry entry) {
		new AsyncTask<NoteEntry, Void, Boolean>() {

			@Override
			protected void onPreExecute() {
				wPd = new ProgressDialog(MyNoteActivity2.this);
				wPd.setMessage("正在删除...");
				wPd.show();
				super.onPreExecute();
			}

			@Override
			protected Boolean doInBackground(NoteEntry... params) {
				try {
					myService.deleteNote(params[0]);
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
					showToast("删除成功");
					mNoteMax--;
					mNumItems--;
					mFlag_delete_note = true;
					// 当该页上的所有条目都删除的时候，调用 上一页的方法
					if (mNumItems == 0) {
						mStartIndex -= mCount;
						mFlag_End_Page = false;
					}
					fillData();

				} else {
					showToast("删除失败，请重试:(");
				}

				super.onPostExecute(result);
			}

		}.execute(entry);
	}

	private void goNextPage() {
		System.out.println("==========");
		System.out.println("mStartIndex begin next ->" + mStartIndex);

		System.out.println("mFlag_End_Page->" + mFlag_End_Page);

		System.out.println("mNoteMax->" + mNoteMax);

		if (!mFlag_isloading) {
			// 加载完一页后才允许点按

			if (mNoteMax != 0) {
				if (!mFlag_End_Page) {
					// 如果当前已经是最后一页了，还需要在从服务器上获取一次数据，才知道是否是最后一页
					mNumPage++;
					mStartIndex += mCount;
					fillData();
				} else {
					showToast("已经是最后一页:)");
				}
			} else {
				// user没有note
			}
			System.out.println("mStartIndex after next->" + mStartIndex);
		}
	}

	private void goPrePage() {
		System.out.println("mStartIndex begin pre->" + mStartIndex);
		if (!mFlag_isloading) {
			if (mStartIndex > mCount) {
				if (mFlag_End_Page) {
					// 如果当前在最后一页的话，需要再减一次
					// mStartIndex -= mCount;
				}
				mFlag_End_Page = false;
				mStartIndex -= mCount;
				mNumPage--;
				fillData();

			} else {
				showToast("已经是第一页:)");

			}

		}
	}

	@Override
	public void fillData() {
		wTv_title_bar.setText("我的日记");

		new AsyncTask<Void, Void, List<Note>>() {

			@Override
			protected void onPreExecute() {
				mFlag_isloading = true;
				mRl_loading_fromP.setVisibility(View.VISIBLE);
				if (isNetworkAvail()) {
					showLoading();
				} else {
					mPb_loadingFP.setVisibility(View.GONE);
					mTv_loadingFP.setText("加载失败，请重试");

				}
				super.onPreExecute();
			}

			@Override
			protected List<Note> doInBackground(Void... params) {

				try {
					return initNotes();
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onPostExecute(List<Note> result) {
				super.onPostExecute(result);
				singleOutResult(result);
				mFlag_isloading = false;
			}

		}.execute();

	}

	/**
	 * 从服务器获取日记信息
	 * 
	 * @return
	 * @throws Exception
	 */
	private List<Note> initNotes() throws Exception {
		if (mUserEntry == null) {
			mUserEntry = myService.getAuthorizedUser();
		}
		String uid = mUserEntry.getUid();
		NoteFeed noteFeed = myService.getUserNotes(uid, mStartIndex, mCount);

		List<Note> notes = new ArrayList<Note>();
		for (NoteEntry ne : noteFeed.getEntries()) {
			Note note = new Note();
			note.setEntry(ne);
			note.setTitle(ne.getTitle().getPlainText());

			if (ne.getContent() != null) {
				note.setContent(((TextContent) ne.getContent()).getContent()
						.getPlainText());
			}

			for (Attribute attr : ne.getAttributes()) {
				if ("can_reply".equals(attr.getName())) {
					note.setCan_reply(attr.getContent());
				} else if ("privacy".equals(attr.getName())) {
					note.setPrivacy(attr.getContent());
				}

			}
			note.setPubdate(ne.getPublished().toString());
			notes.add(note);
		}
		return notes;
	}

	/**
	 * 分拣出result的类型，在UI上做相应的处理
	 * 
	 * @param result
	 */
	private void singleOutResult(List<Note> result) {

		if (result == null) {
			// 从服务器获取数据失败
			hideLoading();
			mPb_loadingFP.setVisibility(View.GONE);
			mTv_loadingFP.setText("加载失败，请重试");
			mFlag_Server_Error = true;
		} else if (result.isEmpty()) {

			// 能获取到数据，但为空
			if (mNoteMax == 0) {
				wLL_notice.setVisibility(View.VISIBLE);
				wTv_link.setMovementMethod(LinkMovementMethod.getInstance());
			} else {
				mFlag_End_Page = true;
				mStartIndex -= mCount;
				mFlag_alreadyMax = true;
				showToast("已经是最后一页:)");
			}
			mRl_loading_fromP.setVisibility(View.INVISIBLE);
			hideLoading();
		} else {
			// 能获取到数据
			mNumItems = result.size();
			mRl_loading_fromP.setVisibility(View.INVISIBLE);
			if (!mFlag_alreadyMax) {
				mNoteMax += result.size();
			}

			setupMyAdapter(result);

		}
	}

	/**
	 * 设置adapter
	 * 
	 * @param result
	 */
	private void setupMyAdapter(List<Note> result) {
		if (mAdapter != null) {

			if (mMapPage.containsValue(result)) {
				// 向前翻页
			} else {
				if (mFlag_delete_note) {
					mMapPage.remove(mNumPage);
					mMapPage.put(mNumPage, result);
					mFlag_delete_note = false;

				} else {
					mMapPage.put(mMaxPage++, result);
				}
			}
			mAdapter.changePage(result);
			wLv.setAdapter(mAdapter);

		} else {
			mMapPage.put(mMaxPage++, result);
			mAdapter = new MyAdapter();
			wLv.setAdapter(mAdapter);
		}

	}

	class MyAdapter extends BaseAdapter {
		private List<Note> notes;

		//
		// public MyAdapter(List<Note> notes) {
		// super();
		// this.notes = notes;
		// }

		public MyAdapter() {
			super();
			notes = mMapPage.get(mNumPage);

		}

		private void prePage(List<Note> notes) {

		}

		private void changePage(List<Note> notes) {
			this.notes.clear();
			this.notes = null;
			this.notes = notes;
		}

		@Override
		public int getCount() {
			return notes.size();
		}

		@Override
		public Object getItem(int position) {
			return notes.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView != null) {
				view = convertView;

			} else {
				view = View.inflate(MyNoteActivity2.this, R.layout.note_item,
						null);
				TextView tv_title = (TextView) view
						.findViewById(R.id.tv_note_item_title);
				tv_title.setText(notes.get(position).getTitle());
			}
			return view;
		}

	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// super.onActivityResult(requestCode, resultCode, data);

	// if (resultCode == RESULT_OK) {
	// mStartIndex = 1;
	// fillData();
	// System.out.println("after onActivityResult:=====");
	// System.out.println("mFlag_End_Page->" + mFlag_End_Page);
	//
	// System.out.println("mNoteMax->"+mNoteMax);
	// System.out.println("==========");
	// }
	//
	// }

}
