package com.davidwilemski.umichdining;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CampusTab extends ListActivity {
	public static final String PREFS_NAME = "UmichPrefFile";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final String[] campus = getIntent().getExtras().getStringArray("array");
		
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, campus));
		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		final Intent intent = new Intent();
		intent.setClass(this, DayMeals.class);
		 
		lv.setOnItemClickListener(new OnItemClickListener(){
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			DatabaseModel dbMod = new DatabaseModel(getApplicationContext());
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			String date = settings.getString("DATE", "NONE");
			try {
				Cursor c = dbMod.getLocationMeal(campus[position], date, "breakfast");
				String b, l, d;
				if(c.moveToFirst()) {
					b = c.getString(0);
				} else {
					b = "[\"No Menu Avaliable\"]";
				}
				 
				c = dbMod.getLocationMeal(campus[position], date, "lunch");
				if(c.moveToFirst()) {
					l = c.getString(0);
				} else {
					l = "[\"No Menu Avaliable\"]";
				}
				 
				c = dbMod.getLocationMeal(campus[position], date, "dinner");
				if(c.moveToFirst()) {
					d = c.getString(0);
				} else {
					d = "[\"No Menu Avaliable\"]";
				}
				 
				c.close();
				intent.putExtra("b", b);
				intent.putExtra("l", l);
				intent.putExtra("d", d);
				startActivity(intent);
				 
			} catch (Exception e) {
				// TODO Error
				Toast.makeText(getApplicationContext(), "Something went wrong in Campus Tabs.", Toast.LENGTH_SHORT).show();
			}
							 
			}
		});
	}
}
