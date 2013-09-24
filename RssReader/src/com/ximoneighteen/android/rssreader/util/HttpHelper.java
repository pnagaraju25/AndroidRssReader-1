package com.ximoneighteen.android.rssreader.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class HttpHelper {

	public InputStream getInputStreamFromURL(final URL url)
			throws IOException, ProtocolException {
		InputStream resultInputStream = null;
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		try {
			conn.connect();
			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				resultInputStream = conn.getInputStream();
			} else {
				conn.disconnect();
			}
		} catch (Exception e) {
			conn.disconnect();
			resultInputStream = null;
		}
		return resultInputStream;
	}

}
