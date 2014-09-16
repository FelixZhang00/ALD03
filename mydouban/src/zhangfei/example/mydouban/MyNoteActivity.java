package zhangfei.example.mydouban;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gdata.data.TextContent;
import com.google.gdata.data.douban.Attribute;
import com.google.gdata.data.douban.NoteEntry;
import com.google.gdata.data.douban.NoteFeed;
import com.google.gdata.data.douban.UserEntry;

import zhangfei.example.mydouban.domain.Note;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyNoteActivity extends BaseMyActivity implements OnClickListener {

	private TextView wTv_title_bar;
	private ListView wLv;
	private TextView wTv_link;
	private LinearLayout wLL_notice;
	private Button wBtn_pre;
	private Button wBtn_next;

	private UserEntry mUserEntry;
	// private MyAdapter mAdapter;

	private int mStartIndex;
	private int mCount;
	private int mNoteMax = 0;
	/*
	 * 豆瓣好像没有提供user的日记总数。不得不用其他方法来获得。
	 */
	protected boolean mFlag_alreadyMax = false;
	private int mTemp = 0;

	// 第一次fillData，发现没有日记,就把mFlag_No_Note 置为false
	private int mNum_fill = 0;
	private boolean mFlag_No_Note = false;
	// 从服务器获取数据异常，不包括网络连接
	private boolean mFlag_Server_Error = false;
	private boolean mFlag_isloading = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.mynote_layout);
		super.onCreate(savedInstanceState);
		// 应该根据屏幕尺寸，选择每页的数目
		mCount = 7;
		mStartIndex = 1;
	}

	@Override
	public void setupView() {
		mRl_loading_fromP = (RelativeLayout) findViewById(R.id.rl_myread_loading);
		wTv_title_bar = (TextView) findViewById(R.id.tv_titlebar_tietle);
		mTv_user_fromP = (TextView) findViewById(R.id.tv_titlebar_user);
		mPb_loadingFP = (ProgressBar) findViewById(R.id.pb_myread);
		mTv_loadingFP = (TextView) findViewById(R.id.txt_loading);

		wLv = (ListView) findViewById(R.id.lv_mynote_subject);
		wLL_notice = (LinearLayout) findViewById(R.id.ll_myread_notice);
		wTv_link = (TextView) findViewById(R.id.tv_myread_link);
		wBtn_next = (Button) findViewById(R.id.btn_mynote_next);
		wBtn_pre = (Button) findViewById(R.id.btn_mynote_pre);
	}

	@Override
	public void setupListener() {
		wBtn_next.setOnClickListener(this);
		wBtn_pre.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_mynote_next:
			System.out.println("mStartIndex begin next ->"+mStartIndex);
			if (isNetworkAvail()) {
				if (!mFlag_isloading) {
					// 加载完一页后才允许点按

					if (mNoteMax != 0) {
						if (!mFlag_alreadyMax) {
							mStartIndex += mCount;
							fillData();
							
						} else {
							showToast("已经是最后一页:)");
						}

					} else {
						// user没有note
					}
					System.out.println("mStartIndex after next->"+mStartIndex);					

				}
			}

			break;
		case R.id.btn_mynote_pre:

			System.out.println("mStartIndex begin pre->"+mStartIndex);
			if (mStartIndex > mCount) {
				mStartIndex -= mCount;
				fillData();
			} else {
				showToast("已经是第一页:)");
			}
			System.out.println("mStartIndex after pre->"+mStartIndex);
			break;

		default:
			break;
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
					note.setTitle(ne.getTitle().getPlainText());

					if (ne.getContent() != null) {
						note.setContent(((TextContent) ne.getContent())
								.getContent().getPlainText());
					}

					for (Attribute attr : ne.getAttributes()) {
						if (attr.getName().equals("can_reply")) {
							note.setCan_reply(attr.getContent());
						} else if (attr.getName().equals("")) {
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
					} else {
						mFlag_alreadyMax = true;
						showToast("已经是最后一页:)");
					}
					mRl_loading_fromP.setVisibility(View.INVISIBLE);

					hideLoading();
				} else {
					// 能获取到数据
					mRl_loading_fromP.setVisibility(View.INVISIBLE);
					mNoteMax += result.size();
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

}
