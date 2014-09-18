package zhangfei.example.mydouban.domain;

import com.google.gdata.data.douban.NoteEntry;

public class Note {

	private String content;
	private String title;
	private String privacy;
	private String can_reply;
	private String pubdate;
	private NoteEntry entry;
	public Note() {
		super();
	}

	public Note(String content, String title, String privacy, String can_reply,
			String pubdate) {
		super();
		this.content = content;
		this.title = title;
		this.privacy = privacy;
		this.can_reply = can_reply;
		this.pubdate = pubdate;
	}

	
	
	public NoteEntry getEntry() {
		return entry;
	}

	public void setEntry(NoteEntry entry) {
		this.entry = entry;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPrivacy() {
		return privacy;
	}

	public void setPrivacy(String privacy) {
		this.privacy = privacy;
	}

	public String getCan_reply() {
		return can_reply;
	}

	public void setCan_reply(String can_reply) {
		this.can_reply = can_reply;
	}

	public String getPubdate() {
		return pubdate;
	}

	public void setPubdate(String pubdate) {
		this.pubdate = pubdate;
	}

	@Override
	public String toString() {
		return "Note [content=" + content + ", title=" + title + ", privacy="
				+ privacy + ", can_reply=" + can_reply + ", pubdate=" + pubdate
				+ "]";
	}
	
	

}
