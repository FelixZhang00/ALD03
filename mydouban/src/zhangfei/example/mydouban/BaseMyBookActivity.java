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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

public class BaseMyBookActivity extends BaseMyActivity {

	public Map<String, SoftReference<Bitmap>> mIconCache;
	private String TAG = "BaseMyBookActivity";
	public UserEntry mUserEntry;
	public int mStartIndex;
	public int mCount;
	public int mbookMax = 0;
	/*
	 * 豆瓣好像没有提供user的读书总数。不得不用其他方法来获得。
	 */
	public boolean mFlag_alreadyMax = false;
	public int mTemp = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mIconCache = new HashMap<String, SoftReference<Bitmap>>();
		mStartIndex = 1;
		String packname = getApplicationContext().getPackageName();
		System.out.println("packname->" + packname);
		/*
		 * @leaveit It should be adjusted by the size of screen.But I have
		 * little money to buy many phones.
		 */
		mCount = 10;

		super.onCreate(savedInstanceState);
	}

	@Override
	public void setupView() {

	}

	@Override
	public void setupListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillData() {
		// TODO Auto-generated method stub

	}

	/**
	 * found books from douban server.
	 * 
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	public List<Book> initBooks() throws IOException, ServiceException {
		if (mUserEntry == null) {
			mUserEntry = myService.getAuthorizedUser();
		}
		String uid = mUserEntry.getUid();
		// 首先获取用户的 所有收集的信息
		CollectionFeed feeds = myService.getUserCollections(uid, "book", null,
				null, mStartIndex, mCount);
		mbookMax += feeds.getEntries().size();
		/*
		 * 如果连续两次mbookMax 都不变，就表示已经到了最大值
		 */
		Log.i(TAG, "initBooks...mbookMax ->" + mbookMax);
		if (mTemp == mbookMax) {
			mFlag_alreadyMax = true;
		}
		mTemp = mbookMax;

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
	public void loadimgFromS(final ImageView iv_book, final String imgurl,
			final File file, final boolean flag) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(BaseMyBookActivity.this);
		boolean isdownload = sp.getBoolean("isdownloadimg", true);
		if (isdownload) {
			if (isNetworkAvail()) {

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
									result.compress(CompressFormat.JPEG, 100,
											fos);
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}
							} else {
								// store the img to cache.
								mIconCache.put(imgurl,
										new SoftReference<Bitmap>(result));

							}

						}

					}

				};
				new LoadImageFromServer(callback).execute(imgurl);
			}
		} else {
			iv_book.setImageResource(R.drawable.book);
		}

	}

	/**
	 * Just for learning. Method2:load img from memory.
	 * 
	 * @param iv_book
	 * @param imgurl
	 */
	public void loadimgMethod2(final ImageView iv_book, String imgurl) {
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
	public void loadimgMethod1(final ImageView iv_book, String imgurl) {
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

	public class MyBookAdapter extends BaseAdapter {
		private List<Book> books;

		public MyBookAdapter(List<Book> books) {
			this.books = books;
		}

		public void adddMore(List<Book> books) {
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

			view = View.inflate(BaseMyBookActivity.this, R.layout.book_item,
					null);

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

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "设置界面");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Intent settingIntent = new Intent(BaseMyBookActivity.this,
					SettingActivity.class);
			startActivity(settingIntent);
			// System.out.println("点击菜单");
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
