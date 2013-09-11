package com.ximoneighteen.android.rssreader;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.ximoneighteen.android.rssreader.model.Article;

import android.os.NetworkOnMainThreadException;
import android.util.Xml;

public class RssDownloader implements Runnable {

	private static final String ITEM = "item";
	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";
	private static final String LINK = "link";
	private static final String PUB_DATE = "pubDate";
	private static final String CHANNEL = "channel";

	private final URL urlToDownload;

	private List<Article> items = null;

	public RssDownloader(final URL url) {
		this.urlToDownload = url;
	}

	@Override
	public void run() {
		InputStream is = null;
		try {
			is = HttpHelper.getInputStreamFromURL(urlToDownload);
			if (is != null) {
				parseXML(is);
			}
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NetworkOnMainThreadException e) {
			// shouldn't happen! developer error!
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void parseXML(final InputStream is) {
		XmlPullParser parser = Xml.newPullParser();
		try {
			// auto-detect the encoding from the stream
			parser.setInput(is, null);
			int eventType = parser.getEventType();
			Article currentItem = null;
			boolean done = false;
			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
				String name = null;
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					items = new ArrayList<Article>();
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase(ITEM)) {
						currentItem = new Article();
					} else if (currentItem != null) {
						if (name.equalsIgnoreCase(LINK)) {
							currentItem.setLink(parser.nextText());
						} else if (name.equalsIgnoreCase(DESCRIPTION)) {
							currentItem.setDescription(parser.nextText());
						} else if (name.equalsIgnoreCase(PUB_DATE)) {
							currentItem.setDate(parser.nextText());
						} else if (name.equalsIgnoreCase(TITLE)) {
							currentItem.setTitle(parser.nextText());
						}
					}
					break;
				case XmlPullParser.END_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase(ITEM) && currentItem != null) {
						items.add(currentItem);
					} else if (name.equalsIgnoreCase(CHANNEL)) {
						done = true;
					}
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<Article> getItems() {
		return items;
	}

}
