package com.studentsaleapp.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;

import com.studentsaleapp.activities.R;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the Layout
        setContentView(R.layout.activity_main);

        // Get the intent from the search bar and run the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.main, menu);

        // Add the search function
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        int searchPlateID = searchView.getContext().getResources()
                .getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateID);
        searchPlate.setBackgroundResource(R.drawable.textfeild_searchview_holo_light);

        // FOR PAGES THAT ARE NOT HOME PAGE ADD THIS to enable navigation back
        // getActionBar().setDisplayHomeAsUpEnabled(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                // PUT FUNCTION HERE openSearch();
                return true;
            case R.id.sell_option:
                Intent sellIntent = new Intent(this, SellActivity.class);
                startActivity(sellIntent);
                return true;
            case R.id.review_option:
                // PUT FUNCTION HERE openSettings();
                return true;
            case R.id.settings_option:
                // PUT FUNCTION HERE openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    private void doMySearch(String query){
        Log.e("SEARCH", query);
    }
}
