package zhangfei.example.mydouban.domain;

public class NewBook {
	private String title;
	private String description;
	private String summary;
	private String imgurl;

	public NewBook() {
		super();
	}

	public NewBook(String title, String description, String summary,
			String imgurl) {
		super();
		this.title = title;
		this.description = description;
		this.summary = summary;
		this.imgurl = imgurl;
	}

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

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getImgurl() {
		return imgurl;
	}

	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}

	@Override
	public String toString() {
		return "NewBook [title=" + title + ", description=" + description
				+ ", summary=" + summary + ", imgurl=" + imgurl + "]";
	}

}
