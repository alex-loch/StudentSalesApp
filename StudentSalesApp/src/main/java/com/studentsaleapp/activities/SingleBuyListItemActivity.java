package com.studentsaleapp.activities;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.studentsaleapp.activities.R;
import com.studentsaleapp.backend.BackendModel;

import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SingleBuyListItemActivity extends FragmentActivity {

    private TextView txtProduct;
    PagerAdapter mPagerAdapter;
    ViewPager mViewPager;
    ArrayList<Bitmap> imageArray = new ArrayList<Bitmap>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_item_detail);

		// Storing properties to variables
		TextView txtProduct = (TextView) findViewById(R.id.product_label);
		TextView txtDesc = (TextView) findViewById(R.id.singleItemDescription); 
		TextView txtPrice = (TextView) findViewById(R.id.singleItemPrice); 
		TextView txtContact = (TextView) findViewById(R.id.singleItemPhoneNo);
		TextView txtLocation = (TextView) findViewById(R.id.singleItemLocation);
        TextView txtCreationTime = (TextView) findViewById(R.id.singleItemTime);

        Intent i = getIntent();

		// Getting attached intent data
		String product = i.getStringExtra("product");
		String desc = i.getStringExtra("desc");
		String price = i.getStringExtra("price");
		String contact = i.getStringExtra("contact");
		String location = i.getStringExtra("location");
        String creationTime = i.getStringExtra("creationTime");
        String itemID = i.getStringExtra("itemID");
        boolean isNoImages = i.getBooleanExtra("isNoImages", false);

		// Displaying selected product name
		txtProduct.setText(product);
		txtDesc.setText(desc);
		txtPrice.setText(price);
		txtContact.setText(contact);
		txtLocation.setText(location);
        txtCreationTime.setText(creationTime);

        if (!(isNoImages)) {
            // Get item images
            new ImageGrabber(itemID).execute();
        }

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mPagerAdapter =
                new PagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.item_gallery);
        mViewPager.setAdapter(mPagerAdapter);

		// Show the Up button in the action bar.
		setupActionBar();
        // Sets the Items title to the activities title.
        setTitle(product);

        // Checks if the contact number is set.
        if (contact == null || contact.equals("")){
            findViewById(R.id.ImageButtonCallSeller).setClickable(false);
            findViewById(R.id.ImageButtonCallSeller).setBackgroundColor(0xFFBFBFBF);
        }else{
            findViewById(R.id.ImageButtonCallSeller).setClickable(true);
        }
	}

    /** Calls the users selling the currently selected items onClick */
    public void callSellerButton(View Button){
        Intent i = getIntent();
        String contact = i.getStringExtra("contact").toString();
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+contact));
        startActivity(callIntent);
    }

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.single_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
		}
		return super.onOptionsItemSelected(item);
	}

    public class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new ImageObjectFragment();
            Bundle args = new Bundle();
            args.putInt(ImageObjectFragment.ARG_OBJECT, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            if (imageArray.size() == 0) {
                return 1;
            } else {
                return imageArray.size();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }

    // Instances of this class are fragments representing a single
// object in our collection.
    public class ImageObjectFragment extends Fragment {
        public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            Bundle args = getArguments();
            int i = args.getInt("ARG_OBJECT");

            View rootView = inflater.inflate(
                    R.layout.fragment_collection_object, container, false);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.item_gallery);

            if (imageArray.size() > 0) {
                imageView.setImageBitmap(imageArray.get(i));
            } else {
                // Insert image to indicate no images present
                Bitmap noImageBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.no_image_found);
                imageView.setImageBitmap(noImageBitmap);
            }

            return rootView;

        }
    }

    class ImageGrabber extends AsyncTask<Void, Void, String> {
        private String itemID;

        public ImageGrabber(String itemID){
            this.itemID = itemID;
        }

        /**
         * Get all images
         * */
        protected String doInBackground(Void... params) {
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
                return;
            }

            ImageSize targetSize = new ImageSize(480,640);
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
                    imageArray.add(loadedImage);
                    mViewPager.setAdapter(mPagerAdapter);
                }

                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    Log.e("MainBuyActivity.onPostExecute.onLoadingFailed",
                            "Loading image failed." + failReason.toString());
                }
            });
        }
    }

}
