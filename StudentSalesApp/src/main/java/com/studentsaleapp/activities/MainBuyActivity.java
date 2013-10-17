package com.studentsaleapp.activities;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.studentsaleapp.backend.BackendModel;
import com.studentsaleapp.backend.SaleItem;

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainBuyActivity extends ListActivity {

	/** The row of items list */
	private ArrayList<BuyRowItem> rowItems;

	/** The backend model */
	private BackendModel model;

    private ProgressDialog pDialog;

    private BuyListViewAdapter adapter;

    private Handler handler;

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
        if (query != null) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        new getListings(this.getApplicationContext(), query).execute();

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(searchQuery);
        }
	}

	/**
	 * Creates, populates and starts the single item activity once an item is clicked
	 */
    @Override
	public void onListItemClick(ListView parent, View view, int position, long id) {

		// Get the item values
		final int iconimages = 0; //images[position];
		String product = ((TextView) view.findViewById(R.id.title)).getText().toString();
		String desc = ((TextView) view.findViewById(R.id.desc)).getText().toString();
		String price = ((TextView) view.findViewById(R.id.price)).getText().toString();
		String contact = ((TextView) view.findViewById(R.id.contact)).getText().toString();
		String location = ((TextView) view.findViewById(R.id.location)).getText().toString();
        String createdAt = ((TextView) view.findViewById(R.id.creationTime)).getText().toString();

		// Create, populate and start the single item activity
		Intent singleItem = new Intent(this, SingleBuyListItemActivity.class);
		singleItem.putExtra("product", product);
		singleItem.putExtra("desc", desc);
		singleItem.putExtra("price", price);
		singleItem.putExtra("contact", contact);
		singleItem.putExtra("location", location);
		singleItem.putExtra("iconimages", iconimages);
        singleItem.putExtra("creationTime", createdAt);
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
            case android.R.id.home:
                Intent goHome = new Intent(this, MainBuyActivity.class);
                startActivity(goHome);
                return true;
		}
		return (super.onOptionsItemSelected(item));
	}

    private String formatPrice(double price) {
        String buffer;
        DecimalFormat f;
        if (price == 0) {
            return "Free";
        }
        f = new DecimalFormat("0");
        if (price % 1 != 0) {
            f = new DecimalFormat("0.00");
        }
        buffer = "$" + f.format(price);
        return buffer;
    }

    private void doMySearch(String query){

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
                        item.getItemID(),
						item.getCreatedAt()
                ));
                temp_counter++;
            }

            // Setup the adapter
            adapter = new BuyListViewAdapter(mContext,
                    R.layout.single_buy_row, rowItems);
            setListAdapter(adapter);

            for (BuyRowItem item: rowItems) {
               new ImageGrabber(mContext, item, model).execute();
            }

        }
    }

    class ImageGrabber extends AsyncTask<Void, Void, String> {
        private Context mContext;
        private BuyRowItem item;

        public ImageGrabber(Context mContext, BuyRowItem item, BackendModel model){
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
                    ParseFile file = (ParseFile) imageResult.get("imageOne");
                    if (file != null) {
                        String imageUri = file.getUrl();
                        return imageUri;
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
        protected void onPostExecute(String imageUri) {
            if (imageUri == null) {
                item.setNoImages(true);
                adapter.notifyDataSetChanged();
                return;
            }
            ImageSize targetSize = new ImageSize(96,128);
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .resetViewBeforeLoading(true)  // default
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .showImageForEmptyUri(R.drawable.no_image_found)
                    .showImageOnFail(R.drawable.no_image_found)
                    .build();
            ImageLoader imageLoader = ImageLoader.getInstance();

            // Load image, decode it to Bitmap and return Bitmap to callback
            imageLoader.loadImage(imageUri, targetSize, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    // Do whatever you want with Bitmap
                    ArrayList<Bitmap> tempArray = new ArrayList<Bitmap>();
                    tempArray.add(loadedImage);
                    item.setImages(tempArray);

                    // Update the adapter
                    adapter.notifyDataSetChanged();
                }

                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    Log.e("MainBuyActivity.onPostExecute.onLoadingFailed",
                            "Loading image failed." + failReason.toString());
                }
            });
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

