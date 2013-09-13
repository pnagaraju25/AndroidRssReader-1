package com.ximoneighteen.android.rssreader.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ximoneighteen.android.rssreader.model.Contract.Articles;
import com.ximoneighteen.android.rssreader.model.Contract.Feeds;
import com.ximoneighteen.android.rssreader.model.Contract.Paragraphs;

public class DbHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "RssReader.db";

	private static final String SQL_CREATE_FEEDS = String.format(
			"CREATE TABLE %s (%s INTEGER PRIMARY KEY,%s TEXT,%s TEXT)", Feeds.TABLE_NAME, Feeds.COLUMN_NAME_ID,
			Feeds.COLUMN_NAME_TITLE, Feeds.COLUMN_NAME_URI);

	private static final String SQL_CREATE_ARTICLES = String.format(
			"CREATE TABLE %s (%s INTEGER PRIMARY KEY,%s TEXT,%s TEXT,%s TEXT,%s INTEGER, %s INTEGER)",
			Articles.TABLE_NAME, Articles.COLUMN_NAME_ID, Articles.COLUMN_NAME_TITLE, Articles.COLUMN_NAME_DESCRIPTION,
			Articles.COLUMN_NAME_LINK, Articles.COLUMN_NAME_DATE, Articles.COLUMN_NAME_FEED_ID);

	private static final String SQL_CREATE_PARAS = String.format(
			"CREATE TABLE %s (%s INTEGER PRIMARY KEY,%s INTEGER,%s TEXT)", Paragraphs.TABLE_NAME,
			Paragraphs.COLUMN_NAME_ID, Paragraphs.COLUMN_NAME_ITEM_ID, Paragraphs.COLUMN_NAME_PARAGRAPH);

	private static final String SQL_DELETE_FEEDS = "DROP TABLE IF EXISTS " + Feeds.TABLE_NAME;

	private static final String SQL_DELETE_ARTICLES = "DROP TABLE IF EXISTS " + Articles.TABLE_NAME;

	private static final String SQL_DELETE_PARAS = "DROP TABLE IF EXISTS " + Paragraphs.TABLE_NAME;

	private static DbHelper instance = null;

	public static DbHelper getInstance(Context context) {
		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (instance == null) {
			instance = new DbHelper(context.getApplicationContext());
		}
		return instance;
	}

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_FEEDS);
		db.execSQL(SQL_CREATE_ARTICLES);
		db.execSQL(SQL_CREATE_PARAS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_PARAS);
		db.execSQL(SQL_DELETE_ARTICLES);
		db.execSQL(SQL_DELETE_FEEDS);
		onCreate(db);
	}

	public Article getArticleByTitle(final String title, final boolean fetchParagraphs) {
		String[] fetchColumns = { Articles.COLUMN_NAME_ID, Articles.COLUMN_NAME_TITLE,
				Articles.COLUMN_NAME_DESCRIPTION, Articles.COLUMN_NAME_LINK, Articles.COLUMN_NAME_FEED_ID };
		String sortOrder = null;
		String whereClause = Articles.COLUMN_NAME_TITLE + " = ?";
		String[] whereClauseArgs = new String[] { title };
		String groupBy = null;
		String filterBy = null;

		Log.d("XIMON", "Fetching article '" + title + "' from the database");
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(Articles.TABLE_NAME, fetchColumns, whereClause, whereClauseArgs, groupBy, filterBy,
				sortOrder);

		try {
			cursor.moveToFirst();
			if (cursor.getCount() == 1) {
				Article article = new Article();
				article.setId(cursor.getLong(0));
				article.setTitle(cursor.getString(1));
				article.setDescription(cursor.getString(2));
				article.setLink(cursor.getString(3));
				article.setFeedId(cursor.getLong(4));
				Log.d("XIMON", "Fetched article " + article.getId() + ": " + article.getTitle());

				if (fetchParagraphs) {
					getArticleParagraphs(article);
				}

				return article;
			}

			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	private void getArticleParagraphs(final Article article) {
		String[] fetchColumns = { Paragraphs.COLUMN_NAME_PARAGRAPH };
		String sortOrder = null;
		String whereClause = Paragraphs.COLUMN_NAME_ITEM_ID + " = ?";
		String[] whereClauseArgs = new String[] { String.valueOf(article.getId()) };
		String groupBy = null;
		String filterBy = null;

		Log.d("XIMON", "Fetching paragraphs for article " + article.getId());
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(Paragraphs.TABLE_NAME, fetchColumns, whereClause, whereClauseArgs, groupBy, filterBy,
				sortOrder);

		try {
			cursor.moveToFirst();
			ArrayList<String> paras = new ArrayList<String>();
			while (!cursor.isAfterLast()) {
				paras.add(cursor.getString(0));
				cursor.moveToNext();
			}
			article.setParagraphs(paras);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public List<Article> getArticlesForFeed(long feedId) {
		String[] fetchColumns = { Articles.COLUMN_NAME_ID, Articles.COLUMN_NAME_TITLE,
				Articles.COLUMN_NAME_DESCRIPTION, Articles.COLUMN_NAME_LINK, Articles.COLUMN_NAME_FEED_ID };
		String sortOrder = Articles.COLUMN_NAME_DATE + " DESC";
		String whereClause = Articles.COLUMN_NAME_FEED_ID + " = ?";
		String[] whereClauseArgs = { String.valueOf(feedId) };
		String groupBy = null;
		String filterBy = null;

		Log.d("XIMON", "Fetching articles from the database for feed " + feedId);
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(Articles.TABLE_NAME, fetchColumns, whereClause, whereClauseArgs, groupBy, filterBy,
				sortOrder);

		try {
			List<Article> articles = new ArrayList<Article>();
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Article article = new Article();
				article.setId(cursor.getLong(0));
				article.setTitle(cursor.getString(1));
				article.setDescription(cursor.getString(2));
				article.setLink(cursor.getString(3));
				article.setFeedId(cursor.getLong(4));
				getArticleParagraphs(article);
				articles.add(article);
				cursor.moveToNext();
				Log.d("XIMON", "Fetched article " + article.getId() + ": " + article.getTitle());
			}
			return articles;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public List<Feed> getFeeds() {
		String[] fetchColumns = { Feeds.COLUMN_NAME_ID, Feeds.COLUMN_NAME_TITLE, Feeds.COLUMN_NAME_URI };
		String sortOrder = Feeds.COLUMN_NAME_TITLE + " ASC";
		String whereClause = null;
		String[] whereClauseArgs = null;
		String groupBy = null;
		String filterBy = null;

		Log.d("XIMON", "Fetching feeds from the database");
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(Feeds.TABLE_NAME, fetchColumns, whereClause, whereClauseArgs, groupBy, filterBy,
				sortOrder);

		try {
			List<Feed> feeds = new ArrayList<Feed>();
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Feed feed = new Feed();
				feed.setId(cursor.getLong(0));
				feed.setTitle(cursor.getString(1));
				try {
					feed.setUrl(new URL(cursor.getString(2)));
					feeds.add(feed);
					Log.d("DbHelper::getFeeds", "Fetched feed " + feed.getId() + ": " + feed.getTitle());
				} catch (MalformedURLException e) {
					// Shouldn't be possible
					Log.d("DbHelper::getFeeds()", "Feed " + feed.getId()
							+ " could not be fetched from the DB because it has an invalid URI");
				}
				cursor.moveToNext();
			}

			return feeds;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

	}

	public long putArticle(final Article article) {
		SQLiteDatabase writeDb = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Articles.COLUMN_NAME_TITLE, article.getTitle());
		values.put(Articles.COLUMN_NAME_DESCRIPTION, article.getDescription());
		values.put(Articles.COLUMN_NAME_LINK, article.getLink().toExternalForm());
		values.put(Articles.COLUMN_NAME_FEED_ID, article.getFeedId());
		long id = writeDb.insert(Articles.TABLE_NAME, null, values);
		article.setId(id);
		Log.d("XIMON", "Stored article " + id + ": " + article.getTitle());

		List<String> paragraphs = article.getParagraphs();
		if (paragraphs == null) {
			paragraphs = Collections.singletonList("This article has not be downloaded");
		}

		for (String para : paragraphs) {
			ContentValues paraValues = new ContentValues();
			paraValues.put(Paragraphs.COLUMN_NAME_ITEM_ID, article.getId());
			paraValues.put(Paragraphs.COLUMN_NAME_PARAGRAPH, para);
			writeDb.insert(Paragraphs.TABLE_NAME, null, paraValues);
			Log.d("XIMON", "Stored paragraph for article " + id);
		}

		return id;
	}

	public long putFeed(final Feed feed) {
		SQLiteDatabase writeDb = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Feeds.COLUMN_NAME_TITLE, feed.getTitle());
		values.put(Feeds.COLUMN_NAME_URI, feed.getUrl().toString());
		long id = writeDb.insert(Feeds.TABLE_NAME, null, values);
		feed.setId(id);
		Log.d("XIMON", "Stored feed " + id + ": " + feed.getTitle());
		return id;
	}
}
