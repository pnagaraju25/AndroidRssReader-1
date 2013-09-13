package com.ximoneighteen.android.rssreader.model;

import java.io.Serializable;
import java.net.URL;

public class Feed implements Comparable<Feed>, Serializable, Identifiable {
	private static final long serialVersionUID = 8247717596952493083L;

	private long id;
	private String title;
	private URL url;

	@Override
	public int compareTo(Feed another) {
		if (another == null)
			return 1;
		// sort ascending by title
		return title.compareTo(another.title);
	}

	@Override
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL uri) {
		this.url = uri;
	}

	@Override
	public String toString() {
		return getTitle();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		Feed other = (Feed) obj;
		if (id != other.id)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

}
