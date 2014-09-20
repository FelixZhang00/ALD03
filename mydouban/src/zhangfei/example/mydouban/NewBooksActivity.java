package zhangfei.example.mydouban;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zhangfei.example.mydouban.Utils.LoadImageFromServer;
import zhangfei.example.mydouban.Utils.NetUtil;
import zhangfei.example.mydouban.Utils.LoadImageFromServer.LoadImageCallback;
import zhangfei.example.mydouban.domain.Book;
import zhangfei.example.mydouban.domain.NewBook;

import com.google.gdata.data.douban.UserEntry;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NewBooksActivity extends BaseMyBookActivity {

	private TextView wTv_title_bar;
	private TextView wTv_link;
	private LinearLayout wLL_notice;
	private ListView wLv;

	private NewBooksAdapter mAdapter;

	private boolean mFlag_isloading = false;

	private boolean mBackKeyPressedTimes = false; // false 不允许退出

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.myread_layout);
		super.onCreate(savedInstanceState);

	}

	@Override
	public void setupView() {
		wTv_title_bar = (TextView) findViewById(R.id.tv_titlebar_tietle);
		wLL_notice = (LinearLayout) findViewById(R.id.ll_myread_notice);
		wLv = (ListView) findViewById(R.id.lv_myread_subject);
		wTv_link = (TextView) findViewById(R.id.tv_myread_link);
		mRl_loading_fromP = (RelativeLayout) findViewById(R.id.rl_myread_loading);
		mTv_user_fromP = (TextView) findViewById(R.id.tv_titlebar_user);
		mPb_loadingFP = (ProgressBar) findViewById(R.id.pb_myread);
		mTv_loadingFP = (TextView) findViewById(R.id.txt_loading);

	}

	@Override
	public void setupListener() {

	}

	@Override
	public void fillData() {
		wTv_title_bar.setText("新书");
		new AsyncTask<Void, Void, List<NewBook>>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mFlag_isloading = true;
				mRl_loading_fromP.setVisibility(View.VISIBLE);
				if (isNetworkAvail()) {
					showLoading();

				} else {
					mPb_loadingFP.setVisibility(View.GONE);
					mTv_loadingFP.setText("加载失败，请重试");
				}

			}

			@Override
			protected List<NewBook> doInBackground(Void... params) {
				try {
					return NetUtil.getNewBooks(NewBooksActivity.this);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onPostExecute(List<NewBook> result) {
				super.onPostExecute(result);
				singleOutResult(result);
				mFlag_isloading = false;
			}

		}.execute();

	}

	private void singleOutResult(List<NewBook> result) {
		if (result == null) {
			// 从服务器获取数据失败
			hideLoading();
			mPb_loadingFP.setVisibility(View.GONE);
			mTv_loadingFP.setText("加载失败，请重试");
		} else if (result.isEmpty()) {
			// 能获取到数据，但为空
			// @leaveit textview link hyperlink. I failed.
			wLL_notice.setVisibility(View.VISIBLE);
			wTv_link.setMovementMethod(LinkMovementMethod.getInstance());
			mRl_loading_fromP.setVisibility(View.INVISIBLE);
			hideLoading();
		} else {
			// 能获取到数据
			mRl_loading_fromP.setVisibility(View.INVISIBLE);
			// if (mAdapter == null) {

			mAdapter = new NewBooksAdapter(result);
			wLv.setAdapter(mAdapter);
			// } else {
			// mAdapter.adddMore(result);
			// mAdapter.notifyDataSetChanged();
			// }
		}
	}

	class NewBooksAdapter extends BaseAdapter {
		private List<NewBook> newBooks;

		public NewBooksAdapter(List<NewBook> newBooks) {
			super();
			this.newBooks = newBooks;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return newBooks.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return newBooks.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = View.inflate(NewBooksActivity.this,
					R.layout.newbook_item, null);
			final ImageView iv_book = (ImageView) view
					.findViewById(R.id.book_img);
			TextView tv_title = (TextView) view.findViewById(R.id.book_title);
			TextView tv_desc = (TextView) view
					.findViewById(R.id.book_description);
			RatingBar rb = (RatingBar) view.findViewById(R.id.ratingbar);

			NewBook newBook = newBooks.get(position);
			tv_title.setText(newBook.getTitle());
			tv_desc.setText(newBook.getDescription());
			// rb.setRating(4.0f);
			rb.setVisibility(View.INVISIBLE);
			final String imgurl = newBook.getImgurl();
			loadimgMethod2(iv_book, imgurl);
			return view;
		}

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
