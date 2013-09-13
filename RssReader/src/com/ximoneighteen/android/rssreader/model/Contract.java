package com.ximoneighteen.android.rssreader.model;

import android.provider.BaseColumns;

public final class Contract {
	private Contract() {}

	public static abstract class Articles implements BaseColumns {
		public static final String TABLE_NAME = "item";
		public static final String COLUMN_NAME_ID = "id";
		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_LINK = "link";
		public static final String COLUMN_NAME_DESCRIPTION = "desc";
		public static final String COLUMN_NAME_DATE = "date";
		public static final String COLUMN_NAME_GUID = "guid";
		public static final String COLUMN_NAME_FEED_ID = "feed_id";
	}

	public static abstract class Paragraphs implements BaseColumns {
		public static final String TABLE_NAME = "paras";
		public static final String COLUMN_NAME_ID = "id";
		public static final String COLUMN_NAME_ITEM_ID = "item_id";
		public static final String COLUMN_NAME_PARAGRAPH = "paragraph";
	}

	public static abstract class Feeds implements BaseColumns {
		public static final String TABLE_NAME = "feeds";
		public static final String COLUMN_NAME_ID = "id";
		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_URI = "uri";
	}
}
