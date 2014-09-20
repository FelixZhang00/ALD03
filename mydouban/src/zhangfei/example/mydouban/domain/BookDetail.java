package zhangfei.example.mydouban.domain;

public class BookDetail extends Book {

	private String author;
	private String price;
	private String summary;
	private String publisher;
	private String translator;
	private String pages;
	private String pubdate;
	private String binding;
	private String isbn;

	public BookDetail() {
		super();
	}

	
	public String getIsbn() {
		return isbn;
	}


	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}


	public String getBinding() {
		return binding;
	}

	public void setBinding(String binding) {
		this.binding = binding;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getTranslator() {
		return translator;
	}

	public void setTranslator(String translator) {
		this.translator = translator;
	}

	public String getPages() {
		return pages;
	}

	public void setPages(String pages) {
		this.pages = pages;
	}

	public String getPubdate() {
		return pubdate;
	}

	public void setPubdate(String pubdate) {
		this.pubdate = pubdate;
	}

	@Override
	public String toString() {
		return "BookDetail [author=" + author + ", price=" + price
				+ ", summary=" + summary + ", publisher=" + publisher
				+ ", translator=" + translator + ", pages=" + pages
				+ ", pubdate=" + pubdate + "]";
	}

}
