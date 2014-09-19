package zhangfei.example.mydouban;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gdata.data.TextContent;
import com.google.gdata.data.douban.Attribute;
import com.google.gdata.data.douban.NoteEntry;
import com.google.gdata.data.douban.NoteFeed;
import com.google.gdata.data.douban.UserEntry;
import com.google.gdata.util.ServiceException;

import zhangfei.example.mydouban.domain.Note;
import android.app.ProgressDialog;
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
 * @deprecated change to MyNoteActivity2 当 把最后一页的数据删光之后，回到第一页，再点击上一页显示
 *             “加载失败”，并且删除这一页的item后虽然成功了，但没有更新listview
 */
public class MyNoteActivity extends BaseMyActivity implements OnClickListener {

	private static final String TAG = "MyNoteActivity";
	private TextView wTv_title_bar;
	private ListView wLv;
	private TextView wTv_link;
	private LinearLayout wLL_notice;
	private Button wBtn_pre;
	private Button wBtn_next;
	private ImageButton wIbtn_add;
	private ProgressDialog wPd;

	private UserEntry mUserEntry;
	private List<Note> mNotesTemp;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.mynote_layout);
		super.onCreate(savedInstanceState);
		// 应该根据屏幕尺寸，选择每页的数目
		mCount = 7;
		mStartIndex = 1;
		fillData(false);
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

	private void deleteNote(NoteEntry entry) {
		new AsyncTask<NoteEntry, Void, Boolean>() {

			@Override
			protected void onPreExecute() {
				wPd = new ProgressDialog(MyNoteActivity.this);
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
					mFlag_delete_note = true;
					fillData(false);
				} else {
					showToast("删除失败，请重试:(");
				}

				super.onPostExecute(result);
			}

		}.execute(entry);
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

			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_mynote_next:
			System.out.println("mStartIndex begin next ->" + mStartIndex);

			if (!mFlag_isloading) {
				// 加载完一页后才允许点按

				if (mNoteMax != 0) {
					if (!mFlag_End_Page) {
						// 如果当前已经是最后一页了，还需要在从服务器上获取一次数据，才知道是否是最后一页
						// mStartIndex += mCount;
						fillData(true);

					} else {
						showToast("已经是最后一页:)");
					}
				} else {
					// user没有note
				}
				System.out.println("mStartIndex after next->" + mStartIndex);
			}

			break;
		case R.id.btn_mynote_pre:

			System.out.println("mStartIndex begin pre->" + mStartIndex);
			if (mStartIndex > mCount) {
				if (mFlag_End_Page) {
					// 如果当前在最后一页的话，需要再减一次
					mStartIndex -= mCount;
				}
				mFlag_End_Page = false;
				mStartIndex -= mCount;
				fillData(false);

			} else {
				showToast("已经是第一页:)");

			}
			System.out.println("mStartIndex after pre->" + mStartIndex);
			break;

		case R.id.btn_titlebar_add:
			if (!mFlag_isloading) {
				Log.i(TAG, "btn_titlebar_edit");

			}
			break;

		default:
			break;
		}

	}

	/*
	 * 
	 * 
	 * @p flag :true 表示点击下一页按钮 扩大mStartIndex.
	 */

	/**
	 * 可以是此方法更健壮，增加flag，以此区分 是点击上一页后、点击下一页后 还是删除日记后 加载数据
	 * 
	 * @param flag
	 *            :true 表示点击下一页按钮 扩大mStartIndex.
	 */
	private void fillData(boolean flag) {
		wTv_title_bar.setText("我的日记");
		if (flag) {
			mStartIndex += mCount;
		}
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
				NoteFeed noteFeed = myService.getUserNotes(uid, mStartIndex,
						mCount);

				List<Note> notes = new ArrayList<Note>();
				for (NoteEntry ne : noteFeed.getEntries()) {
					Note note = new Note();
					note.setEntry(ne);
					note.setTitle(ne.getTitle().getPlainText());

					if (ne.getContent() != null) {
						note.setContent(((TextContent) ne.getContent())
								.getContent().getPlainText());
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
				// if (!result.isEmpty()) {
				//
				// } else if (result.isEmpty()&&result != null) {
				//
				// } else if (result == null) {
				//
				// }

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
						wTv_link.setMovementMethod(LinkMovementMethod
								.getInstance());
					} else if (mFlag_delete_note) {
						// 处理在最后一页删除日记的情况
						mFlag_delete_note = false;
						if (mNotesTemp != null) {
							mNotesTemp.remove(mPosition);
							MyAdapter adapter = new MyAdapter(mNotesTemp);
							// @leaveit 变化数据太突兀，给listview设置动画

							wLv.setAdapter(adapter);
						}

					} else {
						mFlag_End_Page = true;
						mFlag_alreadyMax = true;
						showToast("已经是最后一页:)");
					}

					mRl_loading_fromP.setVisibility(View.INVISIBLE);

					hideLoading();
				} else {
					// 能获取到数据
					mRl_loading_fromP.setVisibility(View.INVISIBLE);
					if (!mFlag_alreadyMax) {
						mNoteMax += result.size();
					}
					mNotesTemp = result;
					MyAdapter adapter = new MyAdapter(result);
					// @leaveit 变化数据太突兀，给listview设置动画

					wLv.setAdapter(adapter);

				}
			}

		}.execute();

	}

	class MyAdapter extends BaseAdapter {
		private List<Note> notes;

		public MyAdapter(List<Note> notes) {
			super();
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
				view = View.inflate(MyNoteActivity.this, R.layout.note_item,
						null);
				TextView tv_title = (TextView) view
						.findViewById(R.id.tv_note_item_title);
				tv_title.setText(notes.get(position).getTitle());
			}
			return view;
		}

	}

	@Override
	public void fillData() {
		// TODO Auto-generated method stub

	}

}
