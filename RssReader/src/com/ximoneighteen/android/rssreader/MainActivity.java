package com.ximoneighteen.android.rssreader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ximoneighteen.android.rssreader.model.Article;
import com.ximoneighteen.android.rssreader.model.Contract.Articles;
import com.ximoneighteen.android.rssreader.model.Contract.Paragraphs;
import com.ximoneighteen.android.rssreader.model.DbHelper;

public class MainActivity extends Activity {
	private static final String RSS_FEED_TO_DOWNLOAD = "http://www.dutchnews.nl/news/index.xml";

	private DbHelper dbHelper;

	private StableArrayAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listviewexampleactivity);

		dbHelper = new DbHelper(this);

		final ListView listview = (ListView) findViewById(R.id.listview);
		final ArrayList<String> list = new ArrayList<String>();
		adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, list);
		listview.setAdapter(adapter);

		final Context context = this;

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				final String articleTitle = (String) parent.getItemAtPosition(position);
				final Article article = dbHelper.getArticleByTitle(articleTitle, true);
				final Intent intent = new Intent(context, ArticleViewActivity.class);
				intent.putExtra("body", article);
				startActivity(intent);
			}

		});

		// debug
		//dbHelper.onUpgrade(dbHelper.getWritableDatabase(), -1, -1);

//		new AsyncTask<Long, Void, List<Article>>() {
//
//			@Override
//			protected List<Article> doInBackground(Long... params) {
//				return dbHelper.getArticlesForFeed(params[0]);
//			}
//
//			// runs in the UI thread
//			@Override
//			protected void onPostExecute(List<Article> result) {
//				replaceItems(result);
//			}
//
//		}.execute(0);

		// try {
		// new AsyncTask<URL, Void, List<Article>>() {
		//
		// @Override
		// protected List<Article> doInBackground(URL... urls) {
		// RssDownloader downloader = new RssDownloader(urls[0]);
		// downloader.run();
		//
		// for (Article item : downloader.getItems()) {
		// item.setParagraphs(ArticleParagraphFetcher.fetch(item.getLink()));
		// }
		//
		// return downloader.getItems();
		// }
		//
		// // runs in the UI thread
		// @Override
		// protected void onPostExecute(List<Article> result) {
		// // update our internal model
		// items.clear();
		//
		// if (result != null) {
		// items.addAll(result);
		//
		// // add the items to the UI list view
		// adapter.clear();
		// for (Article item : result) {
		// adapter.add(item.getTitle());
		// }
		// }
		// }
		//
		// }.execute(new
		// URL("http://feeds.bbci.co.uk/news/rss.xml"));//"http://www.dutchnews.nl/news/index.xml"));
		// } catch (MalformedURLException e) {
		// e.printStackTrace();
		// } finally {
		// }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private class StableArrayAdapter extends ArrayAdapter<String> {

		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			return mIdMap.get(item);
		}

		@Override
		public void add(String object) {
			super.add(object);
			mIdMap.put(object, mIdMap.size());
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_reload:
			new AsyncTask<Void, Void, List<Article>>() {

				@Override
				protected List<Article> doInBackground(Void... params) {
					return reload();
				}

				// runs in the UI thread
				@Override
				protected void onPostExecute(List<Article> result) {
					replaceItems(result);
				}

			}.execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private List<Article> reload() {
		try {
			dbHelper.onUpgrade(dbHelper.getWritableDatabase(), -1, -1);

			Log.d("XIMON", "Downloading articles");
			RssDownloader downloader = new RssDownloader(new URL(RSS_FEED_TO_DOWNLOAD));
			downloader.run();

			List<Article> downloadedItems = downloader.getItems();
			if (downloadedItems != null) {
				for (Article item : downloader.getItems()) {
					item.setParagraphs(ArticleParagraphFetcher.fetch(item.getLink()));
				}

				Log.d("XIMON", "Storing downloaded articles");
				SQLiteDatabase writeDb = dbHelper.getWritableDatabase();
				for (Article item : downloadedItems) {
					ContentValues values = new ContentValues();
					values.put(Articles.COLUMN_NAME_TITLE, item.getTitle());
					values.put(Articles.COLUMN_NAME_DESCRIPTION, item.getDescription());
					values.put(Articles.COLUMN_NAME_LINK, item.getLink().toExternalForm());
					long id = writeDb.insert(Articles.TABLE_NAME, null, values);
					item.setId(id);
					Log.d("XIMON", "Stored article " + id + ": " + item.getTitle());

					for (String para : item.getParagraphs()) {
						ContentValues paraValues = new ContentValues();
						paraValues.put(Paragraphs.COLUMN_NAME_ITEM_ID, item.getId());
						paraValues.put(Paragraphs.COLUMN_NAME_PARAGRAPH, para);
						writeDb.insert(Paragraphs.TABLE_NAME, null, paraValues);
						Log.d("XIMON", "Stored paragraph for article " + id);
					}
				}
			}

			return downloadedItems;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void replaceItems(List<Article> result) {
		// add the items to the UI list view
		adapter.clear();
		for (Article item : result) {
			adapter.add(item.getTitle());
		}
	}
}
