package zhangfei.example.mydouban.domain;

import android.graphics.Bitmap;

public class Book {
	public String title;
	public String description;
	private float rating;
	public String imgurl;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public float getRating() {
		return rating;
	}
	public void setRating(float rate) {
		this.rating = rate;
	}
	public String getImgurl() {
		return imgurl;
	}
	public void setImgurl(String img) {
		this.imgurl = img;
	}
	
	

}
