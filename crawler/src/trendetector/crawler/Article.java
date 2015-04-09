package trendetector.crawler;

import java.util.Date;

public class Article {
	private int article_no;
	private String subject;
	private String author;
	private int replies;
	private int hit;
	private int votes;
	private Date date;
	private String url;
	
	public int getArticleNo() {
		return article_no;
	}
	
	public void setArticleNo(int articleNo) {
		this.article_no = articleNo;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public int getReplies() {
		return replies;
	}
	
	public void setReplies(int replies) {
		this.replies = replies;
	}
	
	public int getHit() {
		return hit;
	}
	
	public void setHit(int hit) {
		this.hit = hit;
	}
	
	public int getVotes() {
		return votes;
	}

	public void setVotes(int votes) {
		this.votes = votes;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
}
