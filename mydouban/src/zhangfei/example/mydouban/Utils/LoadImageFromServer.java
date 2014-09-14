package zhangfei.example.mydouban.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.httpclient.HttpConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

/**
 * Load image from server.
 * 
 * @author tmac 1 the url of portrait; 2 the progress of doinback; 3 the result
 *         of doinback.
 */
public class LoadImageFromServer extends AsyncTask<String, Void, Bitmap> {
	private LoadImageCallback callback;

	public LoadImageFromServer(LoadImageCallback loadImageCallback) {
		this.callback = loadImageCallback;
	}

	public interface LoadImageCallback {
		public void beforeLoad();

		public void afterLoad(Bitmap result);
	}

	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 * Take charge of getting the path of image.
	 */
	@Override
	protected void onPreExecute() {
		callback.beforeLoad();
		super.onPreExecute();
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		try {
			String path = params[0];
			URL url = new URL(path);
			HttpURLConnection conn= (HttpURLConnection) url.openConnection();
			InputStream is=conn.getInputStream();
			return BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 * Take charge of put the image to UI.
	 */
	@Override
	protected void onPostExecute(Bitmap result) {
		callback.afterLoad(result);
		super.onPostExecute(result);
	}

}
