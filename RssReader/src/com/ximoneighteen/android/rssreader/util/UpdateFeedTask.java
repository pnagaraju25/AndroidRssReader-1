package com.ximoneighteen.android.rssreader.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import com.ximoneighteen.android.rssreader.model.Article;
import com.ximoneighteen.android.rssreader.model.DbHelper;
import com.ximoneighteen.android.rssreader.model.Feed;

public class UpdateFeedTask extends AsyncTask<Feed, Integer, Void> {
	private final DbHelper db;
	private final ProgressBar progressBar;
	private final int maxProgress;

	public UpdateFeedTask(DbHelper db, ProgressBar progressBar, int maxProgress) {
		this.db = db;
		this.progressBar = progressBar;
		this.maxProgress = maxProgress;
	}

	public UpdateFeedTask(DbHelper db) {
		this.db = db;
		this.progressBar = null;
		this.maxProgress = 0;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (progressBar != null) {
			progressBar.incrementProgressBy(values[0]);
		}
	}

	@Override
	protected Void doInBackground(Feed... params) {
		Feed feed = params[0];

		Log.d("XIMON", "Feed[" + feed.getId() + "] Updating feed...");

		if (feed.getArticles() == null) {
			// We need the article metadata, let's see if we can get it.
			feed.setArticles(db.getArticlesByFeedId(feed.getId()));
		}

		Log.d("XIMON", "Feed[" + feed.getId() + "] Num articles before update: "
				+ (feed.getArticles() == null ? 0 : feed.getArticles().size()));

		// Grab the dates and GUIDs of the current articles, as they may be
		// changed by the update
		Map<String, Date> articleGuidToDate = new HashMap<String, Date>();
		if (feed.getArticles() != null) {
			for (Article a : feed.getArticles()) {
				articleGuidToDate.put(a.getGuid(), a.getDate());
			}
		}

		// Update the feed and thus also the list of articles and their GUIDs
		// and dates
		RssDownloader downloader = new RssDownloader(feed);
		downloader.run();

		// TODO: Move this into the Article object with some kind of 'status'
		// field?
		// Find the new or modified articles
		List<Article> articles = feed.getArticles();
		List<Article> newOrModifiedArticles = new ArrayList<Article>();
		if (articles != null) {
			for (Article a : articles) {
				Date date = articleGuidToDate.get(a.getGuid());
				if (date == null) {
					// This GUID wasn't in the feed before the update, this is a
					// new article
					newOrModifiedArticles.add(a);
				} else if (a.getDate().after(date)) {
					// This article has been updated by the server
					newOrModifiedArticles.add(a);
				} else {
					// This is an existing article which hasn't changed, let's
					// not update it
				}
			}
		}

		Log.d("XIMON", "Feed[" + feed.getId() + "] Num articles after update: "
				+ (feed.getArticles() == null ? 0 : feed.getArticles().size()));
		Log.d("XIMON", "Feed[" + feed.getId() + "] Num modified or new articles: " + newOrModifiedArticles.size());

		if (!newOrModifiedArticles.isEmpty()) {
			// Tracking progress, and updating articles are different concerns
			// TODO: Split these out as much as possible
			int progressPerArticle = (int) ((float) maxProgress / newOrModifiedArticles.size());
			Integer[] progressIncrement = new Integer[] { progressPerArticle };

			for (Article article : newOrModifiedArticles) {
				// Fetch the article content, since it's either new, or changed
				article.setParagraphs(ArticleParagraphFetcher.fetch(article.getLink()));

				// Store the article in the database. The DB layer will take
				// care of determining if this should be an
				// insert or update
				db.putArticle(article);

				// Let the user know we're making progress
				publishProgress(progressIncrement);

				// Allow the update process to be cancelled from outside, for
				// whatever reason
				if (isCancelled())
					break;
			}
		}

		Log.d("XIMON", "Feed[" + feed.getId() + "] Update complete");

		return null;
	}
}
