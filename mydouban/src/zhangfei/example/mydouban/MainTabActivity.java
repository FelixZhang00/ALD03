package zhangfei.example.mydouban;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class MainTabActivity extends TabActivity {
	private TabHost mTabHost;
	private LayoutInflater mInflater;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_tab);
		mInflater = LayoutInflater.from(MainTabActivity.this);
		mTabHost = getTabHost();
		mTabHost.addTab(getNewBookTabSpec());
		mTabHost.addTab(getMyDouBanTabSpec());

		// when this activity start,you can show the certain tab by tag.
		// mTabHost.setCurrentTabByTag("newbook");
	}

	private TabSpec getNewBookTabSpec() {
		TabSpec spec = mTabHost.newTabSpec("mydouban");
		// 指定标签显示的内容 , 激活的activity对应的intent对象
		Intent intent = new Intent(MainTabActivity.this, MeActivity.class);
		spec.setContent(intent);
		// 设置标签的文字和样式
		// Drawable icon=getResources().getDrawable(R.drawable.ic_launcher);
		// spec.setIndicator("我的豆瓣", icon);
		spec.setIndicator(getIndicatorView("我的豆瓣", R.drawable.tab_main_nav_me));
		return spec;
	}

	private TabSpec getMyDouBanTabSpec() {
		TabSpec spec = mTabHost.newTabSpec("newbook");
		Intent intent = new Intent(MainTabActivity.this, TestActivity.class);
		spec.setContent(intent);
		// Drawable icon = getResources().getDrawable(R.drawable.ic_launcher);
		// spec.setIndicator("新书", icon);
		spec.setIndicator(getIndicatorView("新书", R.drawable.tab_main_nav_book));
		return spec;
	}

	/**
	 * 获取条目的显示对象
	 * 
	 * @return
	 */
	private View getIndicatorView(String title, int iconid) {
		View view = mInflater.inflate(R.layout.tab_main_nav, null);
		ImageView iv = (ImageView) view.findViewById(R.id.iv_maintab_Icon);
		iv.setBackgroundResource(iconid);
		TextView tv = (TextView) view.findViewById(R.id.tv_maintab_Title);
		tv.setText(title);
		return view;
	}

	

}
