package com.ximoneighteen.android.rssreader.model;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Article implements Comparable<Article>, Serializable, Identifiable, Cloneable {
	private static final long serialVersionUID = -7074530680192770748L;

	public static SimpleDateFormat FORMATTER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

	private long id;
	private long feedId;
	private String title;
	private URL link;
	private String description;
	private String guid;
	private Date date;
	private List<String> paragraphs;
	private boolean read = false;

	// sort by date
	public int compareTo(Article another) {
		if (another == null)
			return 1;
		// sort descending, most recent first
		return another.date.compareTo(date);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public void setLink(String link) {
		try {
			this.link = new URL(link);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setDate(String date) {
		// pad the date if necessary
		while (!date.endsWith("00")) {
			date += "0";
		}
		try {
			this.date = FORMATTER.parse(date.trim());
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public URL getLink() {
		return link;
	}

	public void setLink(URL link) {
		this.link = link;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getParagraphs() {
		return paragraphs;
	}

	public void setParagraphs(List<String> list) {
		this.paragraphs = list;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getFeedId() {
		return feedId;
	}

	public void setFeedId(long feedId) {
		this.feedId = feedId;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (int) (feedId ^ (feedId >>> 32));
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((paragraphs == null) ? 0 : paragraphs.hashCode());
		result = prime * result + (read ? 1231 : 1237);
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Article other = (Article) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (feedId != other.feedId)
			return false;
		if (guid == null) {
			if (other.guid != null)
				return false;
		} else if (!guid.equals(other.guid))
			return false;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (paragraphs == null) {
			if (other.paragraphs != null)
				return false;
		} else if (!paragraphs.equals(other.paragraphs))
			return false;
		if (read != other.read)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getTitle();
	}
}
