package com.studentsaleapp.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReviewActivity extends ListActivity {

    private ReviewAdapter adapter;

    private ProgressDialog pDialog;

    private BackendModel model;

    private ArrayList<ReviewRowItem> rowItems;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_review);
        setupActionBar();

        Intent i = getIntent();
        String query = i.getStringExtra("query");
        new ListingsGrabber(this.getApplicationContext(), query).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sell_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            /*case R.id.sell_option:
                return true;
            case R.id.buy_option:
                finish();
                return (true);*/
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
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



    class ListingsGrabber extends AsyncTask<Void, Void, String> {
        ArrayList<SaleItem> fetchedRowItems;
        private Context mContext;
        private String mQuery;

        public ListingsGrabber(Context context, String query){
            mContext = context;
            mQuery = query;
        }

        /**
         * Before background thread starts
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ReviewActivity.this);
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

            // Get row array
            rowItems = new ArrayList<ReviewRowItem>();

            // Set empty string if string is null (i.e. initial loading of app)
            if (mQuery == null) {
                mQuery = "";
            }
            fetchedRowItems = model.getItemListByUser(getDeviceID());

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
                rowItems.add(new ReviewRowItem(
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
            adapter = new ReviewAdapter(mContext,
                    R.layout.single_buy_row, rowItems);
            setListAdapter(adapter);

            for (ReviewRowItem item : rowItems) {
                new ImageGrabber(mContext, item, model).execute();
            }
        }
    }

    /**
     * Get and return the unique device ID
     * @return - the device ID as a String
     */
    private String getDeviceID() {
        String deviceID;
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        deviceID = telephonyManager.getDeviceId();
        if (deviceID == null) {
            deviceID = Settings.Secure.ANDROID_ID;
        }
        return deviceID;
    }

    class ImageGrabber extends AsyncTask<Void, Void, String> {
        private Context mContext;
        private ReviewRowItem item;

        public ImageGrabber(Context mContext, ReviewRowItem item, BackendModel model){
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
                Log.e("ReviewActivity.ImageGrabber.doInBackground", "Query error.");
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

            ImageSize targetSize = new ImageSize(96, 128);
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

    class ReviewAdapter extends ArrayAdapter<ReviewRowItem> {
        private Context context;

        public ReviewAdapter(Context context, int resourceId,
                                  List<ReviewRowItem> items) {

            super(context, resourceId, items);
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            ReviewRowItem rowItem = getItem(position);

            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            convertView = mInflater.inflate(R.layout.single_buy_row, null);
            holder = new ViewHolder();
            holder.txtTitle = (TextView) findViewById(R.id.reviewTitle);
            holder.txtDescription = (TextView) findViewById(R.id.reviewDesc);
            holder.txtCreationTime = (TextView) findViewById(R.id.creationTime);
            holder.txtPrice = (TextView) findViewById(R.id.reviewPrice);
            holder.txtContact = (TextView) findViewById(R.id.reviewContact);
            holder.txtLocation = (TextView) findViewById(R.id.reviewLocation);
            holder.imgThumb = (ImageView) findViewById(R.id.reviewIcon);
            //holder.progressBar = (ProgressBar) findViewById(R.id.);

            holder.txtTitle.setText(rowItem.getTitle());
            holder.txtDescription.setText(rowItem.getDescription());
            holder.txtCreationTime.setText(rowItem.getCreationTime().toString());
            holder.txtPrice.setText(rowItem.getPrice());
            holder.txtContact.setText(rowItem.getContact());
            holder.txtLocation.setText(rowItem.getLocation());

            return convertView;
        }
    }

    class ViewHolder {
        TextView txtTitle;
        TextView txtDescription;
        TextView txtCreationTime;
        TextView txtLocation;
        TextView txtPrice;
        TextView txtContact;
        ProgressBar progressBar;
        ImageView imgThumb;
    }

    class ReviewRowItem {
        private String title;
        private String description;
        private Date creationTime;
        private String location;
        private String price;
        private String contact;
        private String itemID;

        private boolean noImages = false;

        private ArrayList<Bitmap> images = null;

        ReviewRowItem(String title, String description, String price, String contact,
                      String location, String itemID, Date creationTime) {

            this.contact = contact;
            this.title = title;
            this.description = description;
            this.creationTime = creationTime;
            this.location = location;
            this.price = price;
            this.itemID = itemID;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Date getCreationTime() {
            return creationTime;
        }

        public void setCreationTime(Date creationTime) {
            this.creationTime = creationTime;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getContact() {
            return contact;
        }

        public void setContact(String contact) {
            this.contact = contact;
        }

        public String getItemID() {
            return itemID;
        }

        public void setItemID(String itemID) {
            this.itemID = itemID;
        }

        public boolean isNoImages() {
            return noImages;
        }

        public void setNoImages(boolean noImages) {
            this.noImages = noImages;
        }

        public ArrayList<Bitmap> getImages() {
            return images;
        }

        public void setImages(ArrayList<Bitmap> images) {
            this.images = images;
        }


        public String toString() {
            return title;
        }
    }
}
