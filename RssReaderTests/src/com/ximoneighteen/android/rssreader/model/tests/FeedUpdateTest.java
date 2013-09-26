package com.ximoneighteen.android.rssreader.model.tests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.test.AndroidTestCase;

import com.ximoneighteen.android.rssreader.model.Article;
import com.ximoneighteen.android.rssreader.model.Feed;
import com.ximoneighteen.android.rssreader.util.HttpHelper;
import com.ximoneighteen.android.rssreader.util.RssDownloader;

public class FeedUpdateTest extends AndroidTestCase {
	private class MockFeedSource extends HttpHelper {
		private String rssString;

		public MockFeedSource(final Date buildDate, final List<Article> articles) {
			rssString = createRssForArticles(buildDate, articles);
		}

		public String createRssForArticles(final Date buildDate, final List<Article> articles) {
			String formattedDate = Article.FORMATTER.format(buildDate);
			StringBuilder sb = new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>").append('\n');
			sb.append("<rss version=\"2.0\">").append('\n');
			sb.append("<channel>").append('\n');
			sb.append('\t').append("<title>MockTitle</title>").append('\n');
			sb.append('\t').append("<lastBuildDate>");
			sb.append(formattedDate);
			sb.append("</lastBuildDate>").append('\n');
			for (Article a : articles) {
				sb.append('\t').append("<item>").append('\n');
				sb.append('\t').append('\t').append("<title>").append(a.getTitle()).append("</title>").append('\n');
				sb.append('\t').append('\t').append("<guid>").append(a.getGuid()).append("</guid>").append('\n');
				sb.append('\t').append('\t').append("<pubDate>");
				sb.append(Article.FORMATTER.format(a.getDate()));
				sb.append("</pubDate>").append('\n');
				sb.append('\t').append("</item>").append('\n');
			}
			return sb.toString();
		}

		@Override
		public InputStream getInputStreamFromURL(URL url) throws IOException, ProtocolException {
			return new ByteArrayInputStream(rssString.getBytes("UTF-8"));
		}

	}

	private static List<Article> generateArticles(final String titleFormat, final int numArticles) {
		List<Article> articles = new ArrayList<Article>(numArticles);
		for (int i = 0; i < numArticles; i++) {
			articles.add(generateArticle(String.format(titleFormat, i)));
		}
		return articles;
	}

	private static Article generateArticle(final String title) {
		String guid = String.valueOf(new SecureRandom().nextInt());
		Article a = new Article(title, "Test description", "http://testlink/", guid, new Date());
		return a;
	}

	private static List<Article> deepCopy(final List<Article> articlesToClone) throws CloneNotSupportedException {
		List<Article> clonedArticles = new ArrayList<Article>(articlesToClone.size());
		for (Article a : articlesToClone) {
			clonedArticles.add((Article) a.clone());
		}
		return clonedArticles;
	}

	public void testUpdateOfEmptyFeedShouldAddAllAvailableArticles() {
		int NUM_NEW_ARTICLES = 10;

		// Given an empty feed
		Feed feed = new Feed();

		// And a feed source with 10 articles
		List<Article> generatedArticles = generateArticles("Item %d", NUM_NEW_ARTICLES);
		MockFeedSource mockFeedSource = new MockFeedSource(new Date(), generatedArticles);

		// When the feed is updated from the source
		RssDownloader downloader = new RssDownloader(mockFeedSource, feed);
		downloader.run();

		// Then the feed should now contain 10 articles
		List<Article> articles = feed.getArticles();
		assertNotNull(articles);
		assertEquals(NUM_NEW_ARTICLES, articles.size());
	}

	public void testFeedUpdateShouldPreserveExistingArticlesNoLongerOfferedByTheSource()
			throws CloneNotSupportedException {
		int NUM_NEW_ARTICLES = 10;

		// Given a feed that contains a few articles
		List<Article> existingArticles = generateArticles("Existing %d", 3);
		List<Article> expectedArticles = deepCopy(existingArticles);

		Feed feed = new Feed();
		feed.setArticles(existingArticles);

		// And a feed source with 10 other articles
		List<Article> generatedArticles = generateArticles("Item %d", NUM_NEW_ARTICLES);
		MockFeedSource mockFeedSource = new MockFeedSource(new Date(), generatedArticles);

		// When the feed is updated from the source
		RssDownloader downloader = new RssDownloader(mockFeedSource, feed);
		downloader.run();

		// Then the feed should now contain 13 articles
		List<Article> articles = feed.getArticles();
		assertNotNull(articles);
		assertEquals(NUM_NEW_ARTICLES + 3, articles.size());

		// And our original articles should have been preserved by the
		// downloader
		for (Article expectedArticle : expectedArticles) {
			assertNotNull(feed.findArticleByGuid(expectedArticle.getGuid()));
		}
	}

	public void testFeedUpdateShouldPreserveExistingArticlesThatAreOfferedUnchangedByTheSource()
			throws CloneNotSupportedException {
		int NUM_NEW_ARTICLES = 10;

		// Given a feed that contains a few articles
		List<Article> existingArticles = generateArticles("Existing %d", 3);
		List<Article> expectedArticles = deepCopy(existingArticles);

		Feed feed = new Feed();
		feed.setArticles(existingArticles);

		// And a feed source with these few articles plus 10 other articles
		List<Article> generatedArticles = generateArticles("Item %d", NUM_NEW_ARTICLES);
		generatedArticles.addAll(existingArticles);
		MockFeedSource mockFeedSource = new MockFeedSource(new Date(), generatedArticles);

		// When the feed is updated from the source
		RssDownloader downloader = new RssDownloader(mockFeedSource, feed);
		downloader.run();

		// Then the feed should now contain both the original few articles and
		// the 10 new articles
		List<Article> articles = feed.getArticles();
		assertNotNull(articles);
		assertEquals(NUM_NEW_ARTICLES + 3, articles.size());

		// And our original articles should have been preserved by the
		// downloader
		for (Article expected : expectedArticles) {
			Article found = feed.findArticleByGuid(expected.getGuid());
			assertNotNull(found);
			assertEquals(expected, found);
		}
	}

	public void testFeedUpdateShouldUpdateExistingArticlesThatAreChangedByTheSource() throws CloneNotSupportedException {
		int NUM_NEW_ARTICLES = 10;

		// Given a feed that contains a few articles
		List<Article> existingArticles = generateArticles("Existing %d", 3);

		Feed feed = new Feed();
		feed.setArticles(existingArticles);

		// And a feed source with 10 other articles
		List<Article> remoteArticles = generateArticles("Item %d", NUM_NEW_ARTICLES);

		// And the feed alaso contains the existing articles, some of which
		// changed since we last downloaded them
		Map<Article, Boolean> remoteVersionsOfExistingArticles = new HashMap<Article, Boolean>();
		for (Article article : deepCopy(existingArticles)) {
			boolean changeTheArticle = new SecureRandom().nextBoolean();
			if (changeTheArticle) {
				ageArticleBy(article, Calendar.DAY_OF_YEAR, 5);
				article.setTitle("Changed title (was: " + article.getTitle() + ")");
			}
			remoteVersionsOfExistingArticles.put(article, changeTheArticle);
		}

		remoteArticles.addAll(remoteVersionsOfExistingArticles.keySet());
		MockFeedSource mockFeedSource = new MockFeedSource(new Date(), remoteArticles);

		// When the feed is updated from the source
		RssDownloader downloader = new RssDownloader(mockFeedSource, feed);
		downloader.run();

		// Then the feed should now contain both the original few articles and
		// the 10 new articles
		List<Article> articles = feed.getArticles();
		assertNotNull(articles);
		assertEquals(NUM_NEW_ARTICLES + 3, articles.size());

		/*
		 * And our original articles should have been preserved by the
		 * downloader but any remotely changed articles should have caused their
		 * local existing counterparts to have been updated with the remote
		 * changes.
		 */
		for (Entry<Article, Boolean> entry : remoteVersionsOfExistingArticles.entrySet()) {
			Article expectedArticle = entry.getKey();
			boolean shouldHaveBeenChanged = entry.getValue();

			Article foundArticle = feed.findArticleByGuid(expectedArticle.getGuid());

			assertNotNull(foundArticle);
			if (shouldHaveBeenChanged) {
				assertFalse(expectedArticle.equals(foundArticle));
			} else {
				// Android JUnit doesn't provide assertThat().
				assertEquals(expectedArticle, foundArticle);
			}
		}
	}

	private void ageArticleBy(Article article, int timeType, int relTimeValue) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(article.getDate());
		cal.add(timeType, relTimeValue);
		article.setDate(cal.getTime());
	}
}
