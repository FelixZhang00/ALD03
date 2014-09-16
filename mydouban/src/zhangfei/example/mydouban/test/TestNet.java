package zhangfei.example.mydouban.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import zhangfei.example.mydouban.R;

import com.google.gdata.client.douban.DoubanService;
import com.google.gdata.data.douban.UserEntry;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

public class TestNet extends AndroidTestCase {

	public void testNeedCaptha() throws Exception {
		URL url = new URL("http://www.douban.com/accounts/login");

		URLConnection conn = url.openConnection();
		Source source = new Source(conn);

		List<Element> elements = source.getAllElements("input");
		for (Element element : elements) {
			String result = element.getAttributeValue("name");
			System.out.println(element.getAttributeValue("name"));
			if ("captcha-id".equals(result)) {
				System.out.println("需要验证码");
				System.out.println(element.getAttributeValue("value"));
				return;
			}
		}
		System.out.println("不需要验证码");
	}

	public void testLogin() throws Exception {

		// 1.我们去豆瓣申请一个api key secret.
		String apiKey = "088575cb2541e15d199061616fc5a417";
		String secret = "60cad41f3af7aac3";

		DoubanService myService = new DoubanService("douban的android客户端嘻嘻", apiKey, secret);

		System.out
				.println("please paste the url in your webbrowser, complete the authorization then come back:");
		// 2.获取到授权的链接地址
		String url = myService.getAuthorizationUrl(null);
		System.out.println(url);
		// 手动的访问浏览器 点击同意按钮
		// 通过httpclient 模拟 用户点击同意的事件
		// 用httpclient 打开 登陆界面 保存登陆成功的cookie
		// http://www.douban.com/accounts/login
		// 当用户在浏览器里面点击登陆的按钮的时候
		// 实际上是往服务器发送了一个post的信息(下面是未经整理的Raw Post Data)
		/*
		 * source=simple&redir=http%3A%2F%2Fwww.douban.com%2Fservice%2Fauth%2F
		 * authorize
		 * %3Foauth_token%3Dae4f777efc4c2be00aee667cf4e483a0&form_email=
		 * 2650129380
		 * %40qq.com&form_password=**--&captcha-solution=sound&
		 * captcha-id
		 * =N8khpMEggxW87ShDw6KB5tXb%3Aen&user_login=%E7%99%BB%E5%BD%95
		 */

		HttpPost httpPost = new HttpPost("http://www.douban.com/accounts/login");
		// 设置httppost提交的类型
		List<NameValuePair> namevaluepairs = new ArrayList<NameValuePair>();
		namevaluepairs.add(new BasicNameValuePair("source", "simple"));
		namevaluepairs.add(new BasicNameValuePair("redir",
				"http://www.douban.com"));
		namevaluepairs.add(new BasicNameValuePair("form_email",
				"2650129380@qq.com"));
		namevaluepairs.add(new BasicNameValuePair("form_password",
				"**"));
		// //填入验证码信息
		// namevaluepairs
		// .add(new BasicNameValuePair("captcha-solution", "lecture"));
		// namevaluepairs.add(new BasicNameValuePair("captcha-id",
		// "RLdtpHPUmC9h9mKZQ7EleeET"));

		namevaluepairs.add(new BasicNameValuePair("user_login", "登录"));

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(namevaluepairs,
				"utf-8");
		httpPost.setEntity(entity);
		// 创建一个浏览器
		DefaultHttpClient client = new DefaultHttpClient();
		// 完成了用户登陆豆瓣的操作
		HttpResponse response = client.execute(httpPost);
		System.out.println(response.getStatusLine().getStatusCode());
		// 获取登陆成功的cookie
		CookieStore cookie = client.getCookieStore();

		// 带着cookie访问豆瓣认证网站
		// 模拟用户点击 同意按钮
		// ck=4EZp&oauth_token=ae4f777efc4c2be00aee667cf4e483a0&oauth_callback=&ssid=95a27568&confirm=%E5%90%8C%E6%84%8F
		HttpPost post1 = new HttpPost(url);

		String oauth_token = url.substring(url.lastIndexOf("=") + 1,
				url.length());
		System.out.println("oauth_token=" + oauth_token);
		List<NameValuePair> namevaluepairs1 = new ArrayList<NameValuePair>();
		namevaluepairs1.add(new BasicNameValuePair("ck", "4EZp"));
		namevaluepairs1.add(new BasicNameValuePair("oauth_token", oauth_token));
		namevaluepairs1.add(new BasicNameValuePair("oauth_callback", ""));
		namevaluepairs1.add(new BasicNameValuePair("ssid", "8adc304a"));
		namevaluepairs1.add(new BasicNameValuePair("confirm", "同意"));
		UrlEncodedFormEntity entity1 = new UrlEncodedFormEntity(
				namevaluepairs1, "utf-8");
		post1.setEntity(entity1);
		DefaultHttpClient client2 = new DefaultHttpClient();
		client2.setCookieStore(cookie);

		HttpResponse respose1 = client2.execute(post1);
		InputStream is = respose1.getEntity().getContent();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = is.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
		}
		is.close();
		System.out.println(new String(bos.toByteArray()));

		// 3. 获取到授权后的令牌和密钥
		ArrayList<String> tokens = myService.getAccessToken();
		System.out.println(tokens.get(0));
		System.out.println(tokens.get(1));

	}

	public void testGetUserName() throws Exception {
		SharedPreferences sp = getContext().getSharedPreferences("config",
				Context.MODE_PRIVATE);
		String tokenaccess = sp.getString("tokenaccess", "");
		String tokensecret = sp.getString("tokensecret", "");

		// 1.我们去豆瓣申请一个api key secret.
		String apiKey = getContext().getResources().getString(
				R.string.douban_apiKey);
		String secret = getContext().getResources().getString(
				R.string.douban_secret);
		String douban_appname = getContext().getResources().getString(
				R.string.douban_appname);

		DoubanService myService = new DoubanService(douban_appname, apiKey,
				secret);
		myService.setAccessToken(tokenaccess, tokensecret);

		UserEntry ue = myService.getAuthorizedUser();
		String name = ue.getTitle().getPlainText();
		System.out.println("name="+name);

	}

}
