package com.ximoneighteen.android.rssreader.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.ximoneighteen.android.rssreader.model.DbHelper;
import com.ximoneighteen.android.rssreader.model.Feed;

public class UpdateFeedsTask extends AsyncTask<Feed, Integer, Void> {
	private DbHelper db;
	private ProgressBar progressBar;

	public UpdateFeedsTask(DbHelper db, ProgressBar progressBar) {
		this.db = db;
		this.progressBar = progressBar;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setProgress(0);
		progressBar.setMax(100);
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		progressBar.setVisibility(View.INVISIBLE);
	}

	@Override
	protected Void doInBackground(Feed... feeds) {
		List<AsyncTask<Feed, Integer, Void>> tasks = new ArrayList<AsyncTask<Feed, Integer, Void>>();

		int progressBarPercentagePerFeed = (int) (100.0 / feeds.length);
		for (Feed feed : feeds) {
			AsyncTask<Feed, Integer, Void> task = new UpdateFeedTask(db, progressBar, progressBarPercentagePerFeed)
					.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, feed);
			tasks.add(task);
		}

		for (AsyncTask<Feed, Integer, Void> task : tasks) {
			try {
				task.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
}
