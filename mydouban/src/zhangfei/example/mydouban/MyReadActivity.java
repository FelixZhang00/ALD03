package zhangfei.example.mydouban;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MyReadActivity extends BaseMyActivity {

	
	private TextView mTv_title_bar;

	private ProgressBar mPb_loading;
	private TextView mTv_loading;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setContentView(R.layout.myread_layout);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void setupView() {
		mTv_title_bar = (TextView) findViewById(R.id.tv_titlebar_tietle);
		mTv_user_fromP = (TextView) findViewById(R.id.tv_titlebar_user);

	}

	@Override
	public void setupListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void fillData() {
		mTv_title_bar.setText("我读..");

	}

}
