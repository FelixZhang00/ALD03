package zhangfei.example.mydouban;

import zhangfei.example.mydouban.domain.BookDetail;
import zhangfei.example.mydouban.engine.DetailBookProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BookDetailActivity extends BaseMyActivity {
	private TextView wTv_title_bar;
	private TextView wTv_publish;
	private TextView wTv_summary;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.bookdetail_layout2);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void setupView() {
		mRl_loading_fromP = (RelativeLayout) findViewById(R.id.rl_myread_loading);
		mTv_user_fromP = (TextView) findViewById(R.id.tv_titlebar_user);
		mPb_loadingFP = (ProgressBar) findViewById(R.id.pb_myread);
		mTv_loadingFP = (TextView) findViewById(R.id.txt_loading);

		wTv_title_bar = (TextView) findViewById(R.id.tv_titlebar_tietle);
		wTv_publish = (TextView) findViewById(R.id.tv_publish);
		wTv_summary = (TextView) findViewById(R.id.tv_summary);
		
		wTv_publish.setTextIsSelectable(true);
		wTv_summary.setTextIsSelectable(true);
	}

	@Override
	public void setupListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillData() {
		wTv_title_bar.setText("图书详情");

		final String id = getIntent().getStringExtra("id");
		System.out.println("id--->" + id);
		new AsyncTask<String, Void, Boolean>() {
			BookDetail _bookDetail;

			@Override
			protected void onPreExecute() {
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
			protected Boolean doInBackground(String... params) {
				try {
					_bookDetail = DetailBookProvider.getBookDetail(
							BookDetailActivity.this, params[0]);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				hideLoading();
				singleOutResult(result);
			}

			private void singleOutResult(Boolean result) {
				if (!result) {
					// 从服务器获取数据出错
					mPb_loadingFP.setVisibility(View.GONE);
					mTv_loadingFP.setText("加载失败，请重试");
				} else {
					if (_bookDetail == null) {
						mPb_loadingFP.setVisibility(View.GONE);
						mTv_loadingFP.setText("没有该书的详细信息");
					} else {
						// 设置界面
						mRl_loading_fromP.setVisibility(View.INVISIBLE);
						StringBuilder publishsb = new StringBuilder();
						String author = _bookDetail.getAuthor();
						publishsb.append("作者:" + author);

						String translator=_bookDetail.getTranslator();
						if (translator!=null) {
							publishsb.append("\n");
							publishsb.append("译者:" + translator);
						}
						
						String publisher = _bookDetail.getPublisher();
						publishsb.append("\n");
						publishsb.append("出版社:" + publisher);

						String pubdate=_bookDetail.getPubdate();
						if (pubdate!=null) {
							publishsb.append("\n");
							publishsb.append("出版年:"+pubdate);
						}
						
						String pages=_bookDetail.getPages();
						if (pages!=null) {
							publishsb.append("\n");
							publishsb.append("页数:"+pages);
						}
						
						String price =_bookDetail.getPrice();
						if (price!=null) {
							publishsb.append("\n");
							publishsb.append("定价:"+price+"元");
						}
						
						String bing=_bookDetail.getBinding();
						if (bing!=null) {
							publishsb.append("\n");
							publishsb.append("装帧:"+bing);
						}
						String isbn=_bookDetail.getIsbn();
						if (isbn!=null) {
							
							publishsb.append("\n"+"isbn:"+isbn);
						}
						
						
						if (publishsb.length() != 0) {
							wTv_publish.setText(publishsb.toString());
						}

						String summary = _bookDetail.getSummary();
						if (summary != null) {
							wTv_summary.setText(summary);
						}

					}
				}

			}

		}.execute(id);

	}

}
