package com.davidwilemski.umichdining;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseModel extends SQLiteOpenHelper {
	static final String dbName = "umichdining";
	static final String tbName = "menus";
	static final String id = "id";
	static final String location = "location";
	static final String date = "date";
	static final String meal = "meal";
	static final String menu = "menu";
	public static final String PREFS_NAME = "UmichPrefFile";
	//public final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	
	public DatabaseModel(Context context) {
		super(context, dbName, null, 1); // Version number is the last one
	}
	
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + tbName + " (" +
        		id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        		location + " TEXT, " +
        		date + " TEXT, " +
        		meal + " TEXT, " +
        		menu + " TEXT)");
        
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	public void insertRecord(String newlocation, String newdate, String newmeal, String newmenu) {
		SQLiteDatabase db = this.getWritableDatabase();
		// Check to make sure that the information isn't in the db already
		// If it is, we update.
		Cursor cur = db.query(tbName, new String[]{menu, id, meal}, location + "=? AND " + date + "=? AND " + meal + "=?", new String[]{newlocation, newdate, newmeal}, null, null, null);
		//String c = this.getLocationMeal(newlocation, newdate, newmeal);
		if(!cur.moveToFirst()) {
			ContentValues cv = new ContentValues();
			cv.put(location, newlocation);
			cv.put(date, newdate);
			cv.put(meal, newmeal);
			cv.put(menu, newmenu);
			db.insert(tbName, null, cv);
		} else {
			String changeID = cur.getString(cur.getColumnIndex(id));
			ContentValues cv = new ContentValues();
			cv.put(menu, newmenu);
			db.update(tbName, cv, id + "=?" , new String[]{changeID});
		}
		cur.close();
		db.close();
	}
	
	public String getLocationMeal(String currLocation, String currDate, String currMeal) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.query(tbName, new String[]{menu, id, meal}, location + "=? AND " + date + "=? AND " + meal + "=?", new String[]{currLocation, currDate, currMeal}, null, null, null);
		String rString;
		if(cur.moveToFirst()) {
			rString = cur.getString(0);
		} else {
			rString = "[\"No Menu Avaliable\"]";
		}
		cur.close();
		db.close();
		return rString;
	}
	
	public void clearDatabase() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM " + tbName + " WHERE 1");
		db.close();
		return;
	}
	
	public void cleanOldDates() {
		Calendar c = Calendar.getInstance();
		SQLiteDatabase db = this.getWritableDatabase();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		c.add(Calendar.DATE, -1);
	    String yesterday = dateFormat.format(c.getTime());
	    Cursor cur = db.query(tbName, new String[]{id, date}, date + "=?", new String[]{"" + yesterday + ""}, null, null, id + " desc");

	    if(cur.moveToFirst()) {
		    db.execSQL("DELETE FROM " + tbName + " WHERE " + id + " <= " + cur.getString(cur.getColumnIndex(id)));
	    }
	    cur.close();
	    db.close();
	}
	
	@SuppressWarnings("unchecked")
	public int fetchData(String d) {
		//SQLiteDatabase db = this.getWritableDatabase();
		try {
			String urlLocation = "http://roadrunner.davidwilemski.com/UMichDining/index.php/menu/getAllMenus/" + d;
			String response = "";
			String line = "";
			URL url = new URL(urlLocation);
			BufferedReader input = new BufferedReader(new InputStreamReader(url.openStream()));
		    //System.out.println(dateFormat.format(date));
			while((line = input.readLine()) != null) {
				response += line;
			} try {
				JSONObject jO = new JSONObject(response);
				//System.out.println(jArray.get(0).toString());
				Iterator it = jO.keys();
				this.cleanOldDates();
				//this.clearDatabase();
				while(it.hasNext()) {
					String item = (String) it.next();
					JSONObject place = jO.optJSONObject(item);
					//System.out.println(item);
					if(place.has("breakfast"))
						this.insertRecord(item, d, "breakfast", place.getString("breakfast").toString());
					//System.out.println(place.getString("breakfast").toString());
					if(place.has("lunch"))
						this.insertRecord(item, d, "lunch", place.getString("lunch").toString());
					if(place.has("dinner"))
						this.insertRecord(item, d, "dinner", place.getString("dinner").toString());
				}
				//JSONObject place = new JSONObject(jO.getString("Bursley"));
			} catch (JSONException e) {
				//System.out.println(e.getMessage());
				return -1;
			}
		} catch (Exception e) {
			// TODO Error message
			//Toast.makeText(cText, "Something went wrong refreshing the data.", Toast.LENGTH_SHORT).show();
			return -1;
		}
		
		return 0;
	}
	
	
}