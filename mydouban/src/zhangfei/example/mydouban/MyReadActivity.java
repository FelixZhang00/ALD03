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

public class MyReadActivity extends BaseMyActivity implements
		OnItemClickListener {

	public static final String TAG = "MyReadActivity";
	private TextView mTv_title_bar;
	private ListView mLv;
	private TextView mTv_link;
	private LinearLayout mLL_notice;
	private ProgressBar mPb_loading;
	private TextView mTv_loading;
	private Map<String, SoftReference<Bitmap>> mIconCache;
	private MyAdapter mAdapter;

	private UserEntry mUserEntry;
	private boolean mFlag_isloading = false;
	private int mStartIndex;
	private int mCount;
	private int mbookMax = 0;
	
	private KillReceiver mKillReceiver;

	/*
	 * 豆瓣好像没有提供user的读书总数。不得不用其他方法来获得。
	 */
	protected boolean mFlag_alreadyMax = false;
	private int mTemp = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.myread_layout);
		super.onCreate(savedInstanceState);
		mIconCache = new HashMap<String, SoftReference<Bitmap>>();
		mStartIndex = 1;
		String packname= getApplicationContext().getPackageName();
		System.out.println("packname->"+packname);
		/*
		 * @leaveit It should be adjusted by the size of screen.But I have
		 * little money to buy many phones.
		 */
		mCount = 10;

	}

	@Override
	public void setupView() {
		mRl_loading_fromP = (RelativeLayout) findViewById(R.id.rl_myread_loading);
		mTv_title_bar = (TextView) findViewById(R.id.tv_titlebar_tietle);
		mTv_user_fromP = (TextView) findViewById(R.id.tv_titlebar_user);
		mLL_notice = (LinearLayout) findViewById(R.id.ll_myread_notice);
		mLv = (ListView) findViewById(R.id.lv_myread_subject);
		mTv_link = (TextView) findViewById(R.id.tv_myread_link);

		mPb_loading = (ProgressBar) findViewById(R.id.pb_myread);
		mTv_loading = (TextView) findViewById(R.id.txt_loading);
		
		IntentFilter filter=new IntentFilter();
		filter.addAction(getPackageName()+".action.kill_activity");
		mKillReceiver=new KillReceiver();
		this.registerReceiver(mKillReceiver, filter);
	}

	@Override
	public void setupListener() {
		mLv.setOnItemClickListener(this);
		mLv.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:
					// the position of first item is 0.
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
		mTv_title_bar.setText("我读..");
		new AsyncTask<Void, Void, List<Book>>() {

			@Override
			protected void onPreExecute() {
				mFlag_isloading = true;
				mRl_loading_fromP.setVisibility(View.VISIBLE);
				if (isNetworkAvail()) {
					showLoading();

				} else {
					mPb_loading.setVisibility(View.GONE);
					mTv_loading.setText("加载失败，请返回重试");
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

			/**
			 * found books from douban server.
			 * 
			 * @return
			 * @throws IOException
			 * @throws ServiceException
			 */
			private List<Book> initBooks() throws IOException, ServiceException {
				if (mUserEntry == null) {
					mUserEntry = myService.getAuthorizedUser();
				}
				String uid = mUserEntry.getUid();
				// 首先获取用户的 所有收集的信息
				CollectionFeed feeds = myService.getUserCollections(uid,
						"book", null, null, mStartIndex, mCount);
				mbookMax += feeds.getEntries().size();
				/*
				 * 如果连续两次mbookMax 都不变，就表示已经到了最大值
				 */
				Log.i(TAG, "initBooks...mbookMax ->" + mbookMax);
				if (mTemp == mbookMax) {
					mFlag_alreadyMax=true;
				}
				mTemp=mbookMax;
				
				List<Book> books = new ArrayList<Book>();

				for (CollectionEntry ce : feeds.getEntries()) {
					Subject se = ce.getSubjectEntry();
					// Log.i(TAG, "CollectionEntry->" + ce.getId());
					// String str = se.toString();
					// if (str != null) {
					// Log.i(TAG, "Subject->" + str);
					// } else {
					// Log.i(TAG, "Subject is null");
					// }
					if (se != null) {
						Book book = new Book();
						book.setTitle(se.getTitle().getPlainText());

						StringBuilder sb = new StringBuilder();
						for (Attribute attr : se.getAttributes()) {
							if ("author".equals(attr.getName())) {
								sb.append(attr.getContent());
								sb.append("/");
							} else if ("publisher".equals(attr.getName())) {
								sb.append(attr.getContent());
								sb.append("/");
							} else if ("pubdate".equals(attr.getName())) {
								sb.append(attr.getContent());
								sb.append("/");
							} else if ("isbn10".equals(attr.getName())) {
								sb.append(attr.getContent());
								sb.append("/");
							}

						}
						book.setDescription(sb.toString());

						Rating rating = se.getRating();
						if (rating != null) {
							book.setRating(rating.getAverage());
						}
						for (Link link : se.getLinks()) {
							if ("image".equals(link.getRel())) {
								book.setImgurl(link.getHref());
							}
						}

						books.add(book);
					}

				}
				return books;
			}

			@Override
			protected void onPostExecute(List<Book> result) {
				if (isNetworkAvail()) {
					hideLoading();
					if (result != null) {
						mRl_loading_fromP.setVisibility(View.INVISIBLE);
						if (mAdapter == null) {
							mAdapter = new MyAdapter(result);
							mLv.setAdapter(mAdapter);
						} else {
							mAdapter.adddMore(result);
							mAdapter.notifyDataSetChanged();
						}
					} else {
						// @leaveit textview link hyperlink. I failed.
						mLL_notice.setVisibility(View.VISIBLE);
						mTv_link.setMovementMethod(LinkMovementMethod
								.getInstance());

					}
				}
				mFlag_isloading = false;
				super.onPostExecute(result);
			}

		}.execute();
	}

	private class MyAdapter extends BaseAdapter {
		private List<Book> books;

		public MyAdapter(List<Book> books) {
			this.books = books;
		}

		private void adddMore(List<Book> books) {
			for (Book book : books) {
				this.books.add(book);
			}
		}

		@Override
		public int getCount() {

			return books.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return books.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;

			view = View.inflate(MyReadActivity.this, R.layout.book_item, null);

			final ImageView iv_book = (ImageView) view
					.findViewById(R.id.book_img);
			TextView tv_title = (TextView) view.findViewById(R.id.book_title);
			TextView tv_desc = (TextView) view
					.findViewById(R.id.book_description);
			RatingBar rb = (RatingBar) view.findViewById(R.id.ratingbar);

			Book book = books.get(position);
			String title = book.getTitle();
			String desc = book.getDescription();
			String imgurl = book.getImgurl();
			float rating = book.getRating();

			if (rating != 0) {
				rb.setRating(rating);
				Log.i(TAG, "rating is " + rating);
			} else {
				rb.setVisibility(View.INVISIBLE);
				// rb.setRating(4.8f);
			}

			tv_title.setText(title);
			tv_desc.setText(desc);
			// loadimgMethod1(iv_book, imgurl);
			loadimgMethod2(iv_book, imgurl);

			return view;
		}

		/**
		 * Just for learning. Method2:load img from memory.
		 * 
		 * @param iv_book
		 * @param imgurl
		 */
		private void loadimgMethod2(final ImageView iv_book, String imgurl) {
			if (mIconCache.containsKey(imgurl)) {
				SoftReference<Bitmap> softRef = mIconCache.get(imgurl);
				if (softRef != null) {
					Bitmap bitmap = softRef.get();
					if (bitmap != null) {
						Log.i(TAG, "load img from cache.");
						iv_book.setImageBitmap(bitmap);

					} else {
						loadimgFromS(iv_book, imgurl, null, false);
					}
				}

			} else {
				loadimgFromS(iv_book, imgurl, null, false);
			}
		}

		/**
		 * Just for learning. Method1:load img from sdcard.
		 * 
		 * @param iv_book
		 * @param imgurl
		 * @param filename
		 */
		private void loadimgMethod1(final ImageView iv_book, String imgurl) {
			String filename = imgurl.substring(imgurl.lastIndexOf("/") + 1,
					imgurl.length());
			File file = new File(Environment.getExternalStorageDirectory()
					.getPath(), filename);
			if (file.exists()) {
				Log.i(TAG, "load img from sdcard.");
				iv_book.setImageURI(Uri.fromFile(file));
			} else {
				Log.i(TAG, "load img from server.");
				loadimgFromS(iv_book, imgurl, file, true);

			}
		}

		/**
		 * From server load img.
		 * 
		 * @param iv_book
		 * @param imgurl
		 * @param file
		 *            :"null" means needn't store img to sdcard.
		 * @param flag
		 *            true:store img to sdcard; false:store img to cache;
		 */
		private void loadimgFromS(final ImageView iv_book, final String imgurl,
				final File file, final boolean flag) {
			LoadImageCallback callback = new LoadImageCallback() {

				@Override
				public void beforeLoad() {

				}

				@Override
				public void afterLoad(Bitmap result) {
					if (result != null) {
						iv_book.setImageBitmap(result);

						if (flag) {
							// store the img to sdcard.
							try {
								FileOutputStream fos = new FileOutputStream(
										file);
								result.compress(CompressFormat.JPEG, 100, fos);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						} else {
							// store the img to cache.
							mIconCache.put(imgurl, new SoftReference<Bitmap>(
									result));

						}

					}

				}

			};
			new LoadImageFromServer(callback).execute(imgurl);
		}

	}
	
	private class KillReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			mIconCache=null;
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
