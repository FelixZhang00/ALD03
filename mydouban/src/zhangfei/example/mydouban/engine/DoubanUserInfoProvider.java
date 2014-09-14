package zhangfei.example.mydouban.engine;

import java.io.IOException;

import zhangfei.example.mydouban.R;

import com.google.gdata.client.douban.DoubanService;
import com.google.gdata.data.douban.UserEntry;
import com.google.gdata.util.ServiceException;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用之前先判断sharpperence中是否存着后门钥匙
 * 
 * @author tmac
 *
 */
public class DoubanUserInfoProvider {
	private DoubanService myService;

	// private Context context;

	public DoubanUserInfoProvider(Context context) {
		// this.context = context;
		// 1.我们去豆瓣申请一个api key secret.
		String apiKey = context.getResources()
				.getString(R.string.douban_apiKey);
		String secret = context.getResources()
				.getString(R.string.douban_secret);
		String douban_appname = context.getResources().getString(
				R.string.douban_appname);
		myService = new DoubanService(douban_appname, apiKey, secret);

		// 密钥设置给myService
		SharedPreferences sp = context.getSharedPreferences("config",
				Context.MODE_PRIVATE);
		String tokenaccess = sp.getString("tokenaccess", "");
		String tokensecret = sp.getString("tokensecret", "");
		if (tokenaccess == null || tokensecret == null
				|| "".equals(tokenaccess)) {
			// do nothing.
		} else {
			System.out.println("setAccessToken..");
			myService.setAccessToken(tokenaccess, tokensecret);

		}
	}

	/**
	 * 返回在构造方法中已经创建好的DoubanService对象.
	 * 
	 * @return
	 */
	public DoubanService getBackDoubanService() {
		return myService;
	}

	/**
	 * 
	 * @param context
	 * @return 返回豆瓣账户的name
	 * @throws IOException
	 * @throws ServiceException
	 */
	public String getUserName() throws IOException, ServiceException {
		UserEntry ue = myService.getAuthorizedUser();
		String name = ue.getTitle().getPlainText();
		return name;
	}
}
