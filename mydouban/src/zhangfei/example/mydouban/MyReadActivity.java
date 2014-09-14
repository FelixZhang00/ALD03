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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyReadActivity extends BaseMyActivity {

	public static final String TAG = "MyReadActivity";
	private TextView mTv_title_bar;
	private ListView mLv;
	private TextView mTv_link;
	private LinearLayout mLL_notice;
	private ProgressBar mPb_loading;
	private TextView mTv_loading;
	private Map<String, SoftReference<Bitmap>> mIconCache;
	private MyAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.myread_layout);
		super.onCreate(savedInstanceState);
		mIconCache = new HashMap<String, SoftReference<Bitmap>>();

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
	}

	@Override
	public void setupListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillData() {
		mTv_title_bar.setText("我读..");
		new AsyncTask<Void, Void, List<Book>>() {

			@Override
			protected void onPreExecute() {
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
				UserEntry ue = myService.getAuthorizedUser();
				String uid = ue.getUid();
				// 首先获取用户的 所有收集的信息
				CollectionFeed feeds = myService.getUserCollections(uid,
						"book", null, null, 1, 20);

				List<Book> books = new ArrayList<Book>();

				for (CollectionEntry ce : feeds.getEntries()) {
					Subject se = ce.getSubjectEntry();
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
				}
				if (result != null) {
					mRl_loading_fromP.setVisibility(View.INVISIBLE);
					mAdapter = new MyAdapter(result);
					mLv.setAdapter(mAdapter);
				} else {
					// textview link hyperlink.
					mLL_notice.setVisibility(View.VISIBLE);
					mTv_link.setMovementMethod(LinkMovementMethod.getInstance());

				}

				super.onPostExecute(result);
			}

		}.execute();
	}

	private class MyAdapter extends BaseAdapter {
		private List<Book> books;

		public MyAdapter(List<Book> books) {
			this.books = books;
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
			String filename = imgurl.substring(imgurl.lastIndexOf("/") + 1,
					imgurl.length());
			// loadimgMethod1(iv_book, imgurl, filename);
			loadimgMethod2(iv_book, imgurl);

			return view;
		}

		/**
		 * Just for learning. Method2:load img from memory.
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
						loadimgFromS(iv_book, imgurl, null,false);
					}
				}

			} else {
				loadimgFromS(iv_book, imgurl, null,false);
			}
		}

		/**
		 * Just for learning. Method1:load img from sdcard.
		 * 
		 * @param iv_book
		 * @param imgurl
		 * @param filename
		 */
		private void loadimgMethod1(final ImageView iv_book, String imgurl,
				String filename) {
			File file = new File(Environment.getExternalStorageDirectory()
					.getPath(), filename);
			if (file.exists()) {
				Log.i(TAG, "load img from sdcard.");
				iv_book.setImageURI(Uri.fromFile(file));
			} else {
				Log.i(TAG, "load img from server.");
				loadimgFromS(iv_book, imgurl, file,true);

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

}
