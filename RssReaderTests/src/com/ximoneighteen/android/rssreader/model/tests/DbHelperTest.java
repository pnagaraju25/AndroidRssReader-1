package com.ximoneighteen.android.rssreader.model.tests;

import java.util.Arrays;
import java.util.List;

import android.test.AndroidTestCase;

import com.ximoneighteen.android.rssreader.model.Article;
import com.ximoneighteen.android.rssreader.model.DbHelper;
import com.ximoneighteen.android.rssreader.model.Feed;

public class DbHelperTest extends AndroidTestCase {
	private DbHelper dbHelper;
	private Article article;

	public void setUp() {
		dbHelper = new DbHelper(getContext());
		dbHelper.onUpgrade(dbHelper.getWritableDatabase(), -1, -1);

		article = new Article();
		article.setTitle("My title");
		article.setDescription("My description");
		article.setLink("http://mylink");
		article.setParagraphs(Arrays.asList(new String[] {"Para1", "Para2"}));
	}

	public void testCreateAndFetchFeed() {
		Feed feed = new Feed();
		feed.setTitle("My feed");
		dbHelper.putFeed(feed);

		List<Feed> feeds = dbHelper.getFeeds();
		assertEquals(1, feeds.size());
		assertEquals(feed, feeds.get(0));
	}

	public void testCreateAndFetchMultipleFeedsAndArticles() {
		Feed feed1 = new Feed();
		feed1.setTitle("My feed1");
		dbHelper.putFeed(feed1);

		Feed feed2 = new Feed();
		feed2.setTitle("My feed2");
		dbHelper.putFeed(feed2);

		List<Feed> feeds = dbHelper.getFeeds();
		assertEquals(2, feeds.size());
		assertEquals(feed1, feeds.get(0));
		assertEquals(feed2, feeds.get(1));

		article.setFeedId(1);
		dbHelper.putArticle(article);

		List<Article> articlesForFeed1 = dbHelper.getArticlesForFeed(1);
		assertEquals(1, articlesForFeed1.size());
		assertEquals(article, articlesForFeed1.get(0));

		List<Article> articlesForFeed2 = dbHelper.getArticlesForFeed(2);
		assertTrue(articlesForFeed2.isEmpty());
	}

	public void testCreateAndFetchArticle() {
		dbHelper.putArticle(article);
		assertEquals(1, article.getId());

		Article foundArticle = dbHelper.getArticleByTitle(article.getTitle(), true);
		assertEquals(article, foundArticle);
	}
	
}
