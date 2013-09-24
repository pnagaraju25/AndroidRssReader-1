package com.ximoneighteen.android.rssreader.util;

import java.util.List;

import android.os.AsyncTask;
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
		RssDownloader downloader = new RssDownloader(feed);
		downloader.run();

		List<Article> articles = feed.getArticles();
		if (articles != null) {
			int progressPerArticle = (int) ((float) maxProgress / articles.size());
			Integer[] progressIncrement = new Integer[] { progressPerArticle };
			for (Article article : articles) {
				article.setParagraphs(ArticleParagraphFetcher.fetch(article.getLink()));
				db.putArticle(article);
				publishProgress(progressIncrement);
			}
		}

		return null;
	}
}
