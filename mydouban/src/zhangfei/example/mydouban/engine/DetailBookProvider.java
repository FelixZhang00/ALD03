package zhangfei.example.mydouban.engine;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import net.htmlparser.jericho.Source;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import zhangfei.example.mydouban.R;
import zhangfei.example.mydouban.domain.BookDetail;

public class DetailBookProvider {

	public static BookDetail getBookDetail(Context context, String id)
			throws Exception {
		if (id != null) {

			// 根据isbn url获取
			// String urlstr = context.getResources().getString(
			// R.string.detailbookurl)
			// + isbn + "?alt=json";

			// String urlstr = context.getResources().getString(
			// R.string.detailbookidurl)
			// + id + "?alt=json";

			String urlstr = id + "?alt=json";

			BookDetail bookDetail;

			URL url = new URL(urlstr);
			URLConnection conn = url.openConnection();
			Source source = new Source(conn);
			String jsonstr = source.toString();
			// System.out.println(jsonstr);

			bookDetail = new BookDetail();
			JSONObject jsonObject = new JSONObject(jsonstr);

			String titlestr = jsonObject.getString("title").toString();
			JSONObject titleJson = new JSONObject(titlestr);
			String title = titleJson.getString("$t").toString();
			bookDetail.setTitle(title);

			String summarystr = jsonObject.getString("summary").toString();
			if (summarystr != null) {
				JSONObject summaryjson = new JSONObject(summarystr);
				String summary = summaryjson.getString("$t").toString();

				bookDetail.setSummary(summary);
			}

			String attrstr = jsonObject.getString("db:attribute").toString();
			JSONArray attrArrayJson = new JSONArray(attrstr);
			StringBuilder authorsb = new StringBuilder();
			StringBuilder bindingsb = new StringBuilder();
			StringBuilder translatorsb = new StringBuilder();
			for (int i = 0; i < attrArrayJson.length(); i++) {
				// StringBuilder
				JSONObject attrItemJson = new JSONObject(attrArrayJson.get(i)
						.toString());

				if ("isbn10".equals(attrItemJson.getString("@name").toString())) {
					String isbn = attrItemJson.getString("$t").toString();
					bookDetail.setIsbn(isbn);
				}
				if ("price".equals(attrItemJson.getString("@name").toString())) {
					String price = attrItemJson.getString("$t").toString();
					bookDetail.setPrice(price);
				}
				if ("publisher".equals(attrItemJson.getString("@name")
						.toString())) {
					String publisher = attrItemJson.getString("$t").toString();
					bookDetail.setPublisher(publisher);
				}
				if ("author".equals(attrItemJson.getString("@name").toString())) {
					String author = attrItemJson.getString("$t").toString();
					if (authorsb.length() == 0) {
						authorsb.append(author);
					} else {
						authorsb.append("/");
						authorsb.append(author);
					}
				}
				if ("pubdate"
						.equals(attrItemJson.getString("@name").toString())) {
					String pubdate = attrItemJson.getString("$t").toString();
					bookDetail.setPubdate(pubdate);
				}
				if ("pages".equals(attrItemJson.getString("@name").toString())) {
					String pages = attrItemJson.getString("$t").toString();
					bookDetail.setPages(pages);
				}
				if ("binding"
						.equals(attrItemJson.getString("@name").toString())) {
					String binding = attrItemJson.getString("$t").toString();
					if (bindingsb.length() == 0) {
						bindingsb.append(binding);
					} else {
						bindingsb.append("/");
						bindingsb.append(binding);
					}
				}
				if ("translator".equals(attrItemJson.getString("@name")
						.toString())) {
					String translator = attrItemJson.getString("$t").toString();
					if (translatorsb.length() == 0) {
						translatorsb.append(translator);
					} else {
						translatorsb.append("/");
						translatorsb.append(translator);
					}
				}

			}
			// 会有多个作家
			if (authorsb.length() != 0) {
				bookDetail.setAuthor(authorsb.toString());
			}
			if (bindingsb.length() != 0) {
				bookDetail.setBinding(bindingsb.toString());
			}
			if (translatorsb.length() != 0) {
				bookDetail.setTranslator(translatorsb.toString());
			}
			return bookDetail;

		} else {
			return null;
		}
	}
}
