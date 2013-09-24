package com.ximoneighteen.android.rssreader.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.NetworkOnMainThreadException;
import android.util.Xml;

public class ArticleParagraphFetcher {
	public static ArrayList<String> fetch(final URL url) {
		return fetch(new HttpHelper(), url);
	}

	public static ArrayList<String> fetch(final HttpHelper httpHelper, final URL url) {
		InputStream is = null;
		try {
			is = httpHelper.getInputStreamFromURL(url);
			if (is != null) {
				return parseXML(is);
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
		} catch (XmlPullParserException e) {
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
		return null;
	}

	private static ArrayList<String> parseXML(InputStream is) throws XmlPullParserException, IOException {
		ArrayList<String> paragraphs = new ArrayList<String>();
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setValidating(false);
		factory.setFeature(Xml.FEATURE_RELAXED, true);
		factory.setNamespaceAware(true);
		XmlPullParser parser = factory.newPullParser();

		// auto-detect the encoding from the stream
		parser.setInput(is, "UTF-8");

		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				String name = parser.getName();
				if (name.equalsIgnoreCase("p")) {
					paragraphs.add(combineChildTexts(name, parser));
				}
			}
			eventType = parser.next();
		}

		return paragraphs;
	}

	private static String combineChildTexts(String endTag, XmlPullParser parser) throws XmlPullParserException, IOException {
		StringBuilder builder = new StringBuilder();

		int eventType = parser.getEventType();
		int depth = 0;
		do {
			switch (eventType) {
			case XmlPullParser.TEXT:
				builder.append(parser.getText());
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			case XmlPullParser.END_TAG:
				depth--;
				break;
			}
			eventType = parser.next();
		} while (eventType != XmlPullParser.END_DOCUMENT && depth > 0);

		return builder.toString();
	}
}
