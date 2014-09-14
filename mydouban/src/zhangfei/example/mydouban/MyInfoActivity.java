package zhangfei.example.mydouban;

import java.io.IOException;

import zhangfei.example.mydouban.Utils.LoadImageFromServer;
import zhangfei.example.mydouban.Utils.LoadImageFromServer.LoadImageCallback;

import com.google.gdata.data.Link;
import com.google.gdata.data.TextContent;
import com.google.gdata.data.douban.UserEntry;
import com.google.gdata.util.ServiceException;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ArrowKeyMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class MyInfoActivity extends BaseMyActivity {

	private ImageView mIv_prot;
	private TextView mTv_username;
	private TextView mTv_userdesc;
	private TextView mTv_address;
	private TextView mTv_title;

	private ProgressBar mPb_loading;
	private TextView mTv_loading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.myinfo_layout);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void setupView() {
		mRl_loading_fromP = (RelativeLayout) findViewById(R.id.rl_loading);
		mTv_user_fromP = (TextView) findViewById(R.id.tv_titlebar_user);
		mTv_title = (TextView) findViewById(R.id.tv_titlebar_tietle);
		mIv_prot = (ImageView) findViewById(R.id.imgUser);
		mTv_username = (TextView) findViewById(R.id.txtUserName);
		mTv_address = (TextView) findViewById(R.id.txtUserAddress);
		mTv_userdesc = (TextView) findViewById(R.id.txtUserDescription);

		mPb_loading = (ProgressBar) findViewById(R.id.pb_myinfo);
		mTv_loading = (TextView) findViewById(R.id.txt_loading);

	}

	@Override
	public void setupListener() {

	}

	@Override
	public void fillData() {
		mTv_title.setText("我的资料");
		mTv_userdesc.setTextIsSelectable(true);

		new AsyncTask<Void, Void, String>() {
			UserEntry ue;

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
			protected String doInBackground(Void... params) {
				try {
					ue = myService.getAuthorizedUser();
					String iconurl = null;
					for (Link link : ue.getLinks()) {
						if ("icon".equals(link.getRel())) {
							iconurl = link.getHref();
						}
					}
					return iconurl;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}

			}

			private void getImageFromServer(String iconurl) {
				LoadImageCallback callback = new LoadImageCallback() {

					@Override
					public void beforeLoad() {
						// mIv_prot.setImageResource(R.drawable.icon_corp);
					}

					@Override
					public void afterLoad(Bitmap result) {
						if (result != null) {
							mIv_prot.setImageBitmap(result);
						} else {
							mIv_prot.setImageResource(R.drawable.icon_corp);
						}

					}

				};

				new LoadImageFromServer(callback).execute(iconurl);

			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				getImageFromServer(result);
				if (isNetworkAvail()) {
					hideLoading();
				}
				if (ue != null) {
					mRl_loading_fromP.setVisibility(View.INVISIBLE);
					mTv_address.setText(ue.getLocation());
					mTv_username.setText(ue.getTitle().getPlainText());
					mTv_userdesc.setText(((TextContent) ue.getContent())
							.getContent().getPlainText());

				}

			}

		}.execute();

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

}
