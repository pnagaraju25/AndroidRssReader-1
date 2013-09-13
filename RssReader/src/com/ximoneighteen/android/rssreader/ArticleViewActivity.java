package com.ximoneighteen.android.rssreader;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ximoneighteen.android.rssreader.model.Article;

public class ArticleViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Article item = (Article) intent.getSerializableExtra("body");
		StringBuilder body = new StringBuilder();

		List<String> bodyLines = item.getParagraphs();
		if (bodyLines != null) {
			for (String paragraph : bodyLines) {
				body.append(paragraph).append("\n\n");
			}
		}

		TextView textView = new TextView(this);
		textView.setTextSize(16);
		textView.setText(body.toString());
		textView.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f));

		ScrollView scrollView = new ScrollView(this);
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		scrollView.setVerticalScrollBarEnabled(true);
		scrollView.setFillViewport(true);
		scrollView.addView(textView);

		setContentView(scrollView);
		setTitle(item.getTitle());

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

}
