package com.triggertrap.sample;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String [] items = getResources().getStringArray(R.array.items);
		setListAdapter(new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1, items));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent;
		switch (position) {
			case 0:
				intent = new Intent(this, SimpleActivity.class);
				startActivity(intent);
				break;
			case 1:
				intent = new Intent(this, CustomActivity.class);
				startActivity(intent);
				break;
			case 2:
				intent = new Intent(this, ScrollViewActivity.class);
				startActivity(intent);
				break;
			case 3:
				intent = new Intent(this, DisabledActivity.class);
				startActivity(intent);
				break;
		}
	}

}
