package com.ximoneighteen.android.rssreader.util;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.ximoneighteen.android.rssreader.model.Identifiable;

public class IdentifiableListAdapter extends ArrayAdapter<Identifiable> {
	HashMap<Identifiable, Long> objectIDs = new HashMap<Identifiable, Long>();

	public IdentifiableListAdapter(Context context, int textViewResourceId,
			List<Identifiable> objects) {
		super(context, textViewResourceId, objects);
		for (Identifiable object : objects) {
			objectIDs.put(object, object.getId());
		}
	}

	@Override
	public long getItemId(int position) {
		Identifiable item = getItem(position);
		return objectIDs.get(item);
	}

	@Override
	public void add(Identifiable object) {
		super.add(object);
		objectIDs.put(object, object.getId());
	}

	@Override
	public void remove(Identifiable object) {
		super.remove(object);
		objectIDs.remove(object);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}
}