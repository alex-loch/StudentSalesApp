package com.studentsaleapp.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.studentsaleapp.backend.BackendModel;
import com.studentsaleapp.backend.ParseModel;
import com.studentsaleapp.backend.SaleItem;

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainBuyActivity extends ListActivity {

	public static final String[] location = new String[] {
		"St. Lucia",
		"Toowong",
		"Auchenflower",
		"Beenleigh",
		"Brisbane CBD",
		"South Bank",
		"West End",
		"Nerang"
	};
	/** ----- end static data -----*/

	/** The row of items list */
	private ArrayList<BuyRowItem> rowItems = new ArrayList<BuyRowItem>();

	/** The backend model */
	private BackendModel model;

    private ProgressDialog pDialog;

    private BuyListViewAdapter adapter;

    /** The location manager */
    private LocationManager locationManager;

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Setup the layout
		setContentView(R.layout.activity_main);

        Intent i = getIntent();
        String query = i.getStringExtra("query");
        new getListings(this.getApplicationContext(), query).execute();

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            doSearch(searchQuery);
        }

	}

	/**
	 * Creates, populates and starts the single item activity once an item is clicked
	 */
    @Override
	public void onListItemClick(ListView parent, View view, int position, long id) {

		// Get the item values
		final int iconImages = 0; //images[position];
		String product = ((TextView) view.findViewById(R.id.title)).getText().toString();
		String desc = ((TextView) view.findViewById(R.id.desc)).getText().toString();
		String price = ((TextView) view.findViewById(R.id.price)).getText().toString();
		String contact = ((TextView) view.findViewById(R.id.contact)).getText().toString();
		String location = ((TextView) view.findViewById(R.id.location)).getText().toString();

		// Create, populate and start the single item activity
		Intent singleItem = new Intent(this, SingleBuyListItemActivity.class);
		singleItem.putExtra("product", product);
		singleItem.putExtra("desc", desc);
		singleItem.putExtra("price", price);
		singleItem.putExtra("contact", contact);
		singleItem.putExtra("location", location);
		singleItem.putExtra("iconimages", iconImages);
		startActivity(singleItem);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch(item.getItemId()) {
			/*case R.id.buy_option:
				return true;*/
			case R.id.sell_option:
				Intent sellIntent = new Intent(this, SellActivity.class);
				startActivity(sellIntent);
				return true;
		}
		return (super.onOptionsItemSelected(item));
	}

    private String formatPrice(double price) {
        StringBuffer buffer = new StringBuffer(Double.toString(price));
        buffer.insert(0, '$');
        return buffer.toString();
    }

    private void doSearch(String query){

        // Create, populate and start the single item activity
        Intent search = new Intent(this, MainBuyActivity.class);
        search.putExtra("query", query);
        startActivity(search);

    }

    class getListings extends AsyncTask<Void, Void, String> {
        ArrayList<SaleItem> fetchedRowItems;
        private Context mContext;
        private String mQuery;
        private Location mLocation;

        public getListings (Context context, String query){
            mContext = context;
            mQuery = query;
            setupLocation(mContext);
            mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        /**
         * Before background thread starts
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainBuyActivity.this);
            pDialog.setMessage("Fetching items...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Get all listings
         * */
        protected String doInBackground(Void... params) {
            // Get the backend model
            MainApplication appState = (MainApplication)getApplicationContext();
            model = appState.getBackendModel();

            // Set dummy location if location is null
            if (mLocation == null) {
                mLocation = new Location(LocationManager.NETWORK_PROVIDER);
                mLocation.setLatitude(-27.5981086);
                mLocation.setLongitude(153.1372595);
            }

            // Get row array
            rowItems = new ArrayList<BuyRowItem>();

            // Set empty string is string is null (i.e. initial loading of app)
            if (mQuery == null) {
                mQuery = "";
            }
            fetchedRowItems = model.getItemList(mQuery, mLocation.getLatitude(),
                    mLocation.getLongitude(), 0, BackendModel.DISTANCE);

            return null;
        }

        /**
         * After background task has completed
         * **/
        protected void onPostExecute(String file_url) {
            pDialog.cancel();

            // Temporary counter to go through static data as images & locations not yet in database.
            int temp_counter = 0;
            for (SaleItem item : fetchedRowItems) {
                rowItems.add(new BuyRowItem(
                        item.getTitle(),
                        item.getDescription(),
                        formatPrice(item.getPrice()),
                        item.getContact(),
                        item.getLocationString(),
                        item.getItemID()
                ));
                temp_counter++;
            }

            // Setup the adapter
            adapter = new BuyListViewAdapter(mContext, R.layout.single_buy_row, rowItems);
            setListAdapter(adapter);

            for (BuyRowItem item : rowItems) {
                new getImages(mContext, item, model).execute();
            }
        }
    }

    class getImages extends AsyncTask<Void, Void, String> {
        private Context mContext;
        private BuyRowItem item;

        public getImages (Context mContext, BuyRowItem item, BackendModel model){
            this.mContext = mContext;
            this.item = item;
        }

        /**
         * Get all images
         * */
        protected String doInBackground(Void... params) {
            String itemID = item.getItemID();

            ParseQuery<ParseObject> query = ParseQuery.getQuery("SaleItem");
            query.whereEqualTo("objectId", itemID);

            try {
                List<ParseObject> tempList = query.find();
                ParseObject result = tempList.get(0);
                ParseRelation relation = result.getRelation("imageRelation");

                ParseQuery<ParseObject> imageQuery = relation.getQuery();
                List<ParseObject> imageTempList = imageQuery.find();

                if (imageTempList.size() > 0) {
                    ParseObject imageResult = imageTempList.get(0);
                    ParseFile file = imageResult.getParseFile("imageOne");
                    if (file != null) {
                        Bitmap b = model.parseFileToBitmap(file);
                        ArrayList<Bitmap> tempArray = new ArrayList<Bitmap>();
                        tempArray.add(b);
                        item.setImages(tempArray);
                    }
                }

            } catch (com.parse.ParseException e) {
                // Query error
            }


            return null;
        }

        /**
         * After background task has completed
         * **/
        protected void onPostExecute(String file_url) {
            // Update the adapter
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Setup the network provider location listener and display fields
     *
     * @param context - context to be run on
     */
    private void setupLocation(Context context) {
        boolean networkAvailable;

        // Setup the location manager and determine if the GPS and Network Providers are available
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        networkAvailable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // Setup the location listener
        if (networkAvailable) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocationListener);
        }
    }

    /**
     * The network provider location listener which is only called once
     */
    LocationListener networkLocationListener = new LocationListener() {

        /** Called when the location is found */
        public void onLocationChanged(Location location) {
            locationManager.removeUpdates(this);
        }

        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };


    }

