package zhangfei.example.mydouban;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdata.data.Link;
import com.google.gdata.data.douban.Attribute;
import com.google.gdata.data.douban.CollectionEntry;
import com.google.gdata.data.douban.CollectionFeed;
import com.google.gdata.data.douban.Subject;
import com.google.gdata.data.douban.UserEntry;
import com.google.gdata.data.extensions.Rating;
import com.google.gdata.util.ServiceException;

import zhangfei.example.mydouban.Utils.LoadImageFromServer;
import zhangfei.example.mydouban.Utils.LoadImageFromServer.LoadImageCallback;
import zhangfei.example.mydouban.domain.Book;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyReadActivity extends BaseMyBookActivity implements
		OnItemClickListener {

	private TextView wTv_title_bar;
	private TextView wTv_link;
	private LinearLayout wLL_notice;
	private ListView wLv;

	public static final String TAG = "MyReadActivity";

	private MyBookAdapter mAdapter;

	private boolean mFlag_isloading = false;

	private KillReceiver mKillReceiver;
	private IntentFilter filter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.myread_layout);
		super.onCreate(savedInstanceState);


	}

	@Override
	public void setupView() {

		mRl_loading_fromP = (RelativeLayout) findViewById(R.id.rl_myread_loading);
		wTv_title_bar = (TextView) findViewById(R.id.tv_titlebar_tietle);
		mTv_user_fromP = (TextView) findViewById(R.id.tv_titlebar_user);
		mPb_loadingFP = (ProgressBar) findViewById(R.id.pb_myread);
		mTv_loadingFP = (TextView) findViewById(R.id.txt_loading);

		wLL_notice = (LinearLayout) findViewById(R.id.ll_myread_notice);
		wLv = (ListView) findViewById(R.id.lv_myread_subject);
		wTv_link = (TextView) findViewById(R.id.tv_myread_link);
		filter = new IntentFilter();
		filter.addAction(getPackageName() + ".action.kill_activity");
		mKillReceiver = new KillReceiver();
		this.registerReceiver(mKillReceiver, filter);
	}

	@Override
	public void setupListener() {
		wLv.setOnItemClickListener(this);
		wLv.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:
					// the position of first item is 0.
					if (isNetworkAvail()) {

						int position = view.getLastVisiblePosition();
						Log.i(TAG, "position->" + position);
						int totalcount = mAdapter.getCount();
						Log.i(TAG, "end->" + totalcount);

						if (position == (totalcount - 1)) {
							// Now the item is in the end,must load more.
							if (mFlag_alreadyMax) {
								showToast("已经到底了:)");
								return;
							}
							mStartIndex += mCount;
							Log.i(TAG, "max->" + mbookMax);
							Log.i(TAG, "startindex->" + mStartIndex);
							if (mFlag_isloading) {
								return;
							} else {
								fillData();
							}

						}
					}

					break;

				default:
					break;
				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillData() {
		wTv_title_bar.setText("我读..");
		new AsyncTask<Void, Void, List<Book>>() {

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
			protected List<Book> doInBackground(Void... params) {
				try {
					return initBooks();
				} catch (Exception e) {
					e.printStackTrace();

					return null;
				}

			}

			@Override
			protected void onPostExecute(List<Book> result) {
				singleOutResult(result);
				mFlag_isloading = false;
				super.onPostExecute(result);
			}

			private void singleOutResult(List<Book> result) {
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
					if (mAdapter == null) {
						mAdapter = new MyBookAdapter(result);
						wLv.setAdapter(mAdapter);
					} else {
						mAdapter.adddMore(result);
						mAdapter.notifyDataSetChanged();
					}
				}

			}

		}.execute();
	}

	// 当按下菜单键的时候 创建出菜单对象
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.myread_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_flush:
			refreshView();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void refreshView() {

		// 自己调到自己的activity
		Intent intent = new Intent(MyReadActivity.this, MyReadActivity.class);
		startActivity(intent);
		// close this activity
		finish();

	}

	private class KillReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			mIconCache = null;
			showToast("内存不足,小弟先撤了:)");
			finish();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mKillReceiver);
	}

}
