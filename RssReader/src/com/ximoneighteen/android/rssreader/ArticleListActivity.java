package com.ximoneighteen.android.rssreader;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ximoneighteen.android.rssreader.model.Article;
import com.ximoneighteen.android.rssreader.model.DbHelper;
import com.ximoneighteen.android.rssreader.model.Feed;
import com.ximoneighteen.android.rssreader.model.Identifiable;
import com.ximoneighteen.android.rssreader.util.IdentifiableListAdapter;

public class ArticleListActivity extends Activity {
	private DbHelper db;

	private IdentifiableListAdapter adapter;

	private Feed feed;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_list);

		db = DbHelper.getInstance(this);

		Intent intent = getIntent();
		feed = (Feed) intent.getSerializableExtra("feed");
		List<? extends Identifiable> articles = db.getArticlesByFeedId(feed.getId());

		final ListView listview = (ListView) findViewById(R.id.articleListView);
		adapter = new IdentifiableListAdapter(this, android.R.layout.simple_list_item_1, (List<Identifiable>) articles);
		listview.setAdapter(adapter);

		final Context context = this;

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				final Article article = (Article) parent.getItemAtPosition(position);
				final Intent intent = new Intent(context, ArticleViewActivity.class);
				intent.putExtra("body", article);
				startActivity(intent);
			}

		});

		setTitle(feed.getTitle());

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
}
