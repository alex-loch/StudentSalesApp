package com.studentsaleapp.activities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.studentsaleapp.activities.R;

import com.studentsaleapp.backend.ParseModel;
import com.studentsaleapp.backend.SaleItem;


public class SellActivity extends Activity {
	
	/** The logging tag */
	private static final String TAG = "StudentSaleApp::SellActivity";
	
	/** The Action Code for picture taking requests */
	private static final int TAKE_PICTURE_REQUEST = 1;
	
	/** The maximum numbers of images for a listing */
	private final int NUMBER_OF_IMAGES = 3;
	
	/** The locally stored file directory */
	private final String FILE_DIRECTORY = "ssa";
	
	/** The locally stored image file name */
	private final String IMAGE_FILENAME = "ImageFileName";
	
	/** The locally stored image extension type */
	private final String FILENAME_TYPE = ".jpg";
	
	/** The local paths at which the images are stored */
	private ArrayList<File> mImagePaths = new ArrayList<File>();
	
	/** The views used for displaying the images */
	private ArrayList<ImageView> mImageViews = new ArrayList<ImageView>();
	
	/** The bitmaps of the images */
	private ArrayList<Bitmap> mImageBitmaps = new ArrayList<Bitmap>();
	
	/** The current image counter */
	private int currentImage = 0;
	
	/** The location manager */
	private LocationManager locationManager;

    private ProgressDialog pDialog;
	
	/** The backend model */
	//TODO: The backend model is currently not in use
	//private BackendModel model;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Setup the layout and buttons
		setContentView(R.layout.activity_sell);
		//Button sButton = (Button) findViewById(R.id.ButtonSellItem);
		//sButton.setBackgroundColor(Color.parseColor("#9c2e2e"));
		
		// Get the backend model
		//MainApplication appState = (MainApplication)getApplicationContext();
		//model = appState.getBackendModel();
		
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Setup the views to hold the images
		mImageViews.add((ImageView) findViewById(R.id.photoView1));
		mImageViews.add((ImageView) findViewById(R.id.photoView2));
		mImageViews.add((ImageView) findViewById(R.id.photoView3));
		for (int i = 0; i < NUMBER_OF_IMAGES; i++) {
			mImageViews.get(i).setVisibility(View.VISIBLE);
		}
		Log.i(TAG, "Image views created");
		
		// Setup the file (including directory) paths
		try {
			super.onCreate(savedInstanceState);         
			File root = new File(Environment
					.getExternalStorageDirectory()
					+ File.separator + FILE_DIRECTORY + File.separator);
			root.mkdirs();
			
			for (int i = 0; i < NUMBER_OF_IMAGES; i++) {
				mImagePaths.add(new File(root, IMAGE_FILENAME + i + FILENAME_TYPE));
			}
			
		} catch (Exception e) {
			finish();
			Toast.makeText(this, "Error occured creating file paths. Please try again later.",
					Toast.LENGTH_SHORT).show();
		}
		Log.i(TAG, "Image file paths created");
		
		// Auto fill the phone number and location fields from phone data
		setPhoneNumber();
		setupLocation(this);
		Log.i(TAG, "setPhoneNumber and setupLocation");
		
		Log.i(TAG, "Sell Activity is setup");
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
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
            case R.id.sell_option:
                return true;
            /*case R.id.buy_option:
                finish();
                return (true);*/
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Dispatch the picture request when the button is pressed, 
	 * if the maximum number of images has not yet been reached
	 * 
	 * @param view - the view to hold the image
	 */
	public void takePhoto(View view) {
		if (currentImage >= NUMBER_OF_IMAGES) {
			Toast.makeText(this, NUMBER_OF_IMAGES + " Images Already Added", Toast.LENGTH_SHORT).show();
		} else if (isIntentAvailable(this, MediaStore.ACTION_IMAGE_CAPTURE)) {
			Log.i(TAG, "Dispatching take picture request");
			dispatchTakePictureIntent(TAKE_PICTURE_REQUEST);
		}
	} 
	
	/**
	 * Check if the intent is available
	 * 
	 * @param context - context to check within
	 * @param action - action to check
	 * @return - true for if the intent is available, false otherwise
	 */
	private boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
				packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
	
	/**
	 * Dispatch the take picture intent and start the activty for a result
	 * 
	 * @param actionCode - action code for taking a picture
	 */
	private void dispatchTakePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImagePaths.get(currentImage)));
		startActivityForResult(takePictureIntent, actionCode);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
			case TAKE_PICTURE_REQUEST: {
				if (resultCode == RESULT_OK) {
					if (currentImage < NUMBER_OF_IMAGES) {
						// Open the bitmap in a smaller size 
						// (can change to dynamically determine size)
						float requiredHeight = 128;
						float requiredWidth = 96;
						
						// Extract only the bitmap dimensions
						String currentFile = mImagePaths.get(currentImage).toString();
						BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
						bitmapFactoryOptions.inJustDecodeBounds = true;
						Bitmap currentBitmap = BitmapFactory.decodeFile(currentFile, bitmapFactoryOptions);
                        // Add the bitmap to the array
                        // mImageBitmaps.add(currentBitmap);
						
						// Calculate the height and width ratios
						int ratioHeight = (int) Math.ceil(bitmapFactoryOptions.outHeight / requiredHeight);
						int ratioWidth = (int) Math.ceil(bitmapFactoryOptions.outWidth / requiredWidth);
						
						// Set the sample size if required
						if (ratioHeight > 1 || ratioWidth > 1) {
							if (ratioHeight >= ratioWidth) {
								bitmapFactoryOptions.inSampleSize = ratioHeight;
							} else {
								bitmapFactoryOptions.inSampleSize = ratioWidth;
							}
						}
						
						// Extract the resized bitmap
                        bitmapFactoryOptions.inJustDecodeBounds = false;
						currentBitmap = BitmapFactory.decodeFile(currentFile, bitmapFactoryOptions);

						// Display the image in the image view
						mImageViews.get(currentImage).setImageBitmap(currentBitmap);
						
						// Increment the image counter
						currentImage++;
					}
				}
			}
			break;
		}
	}
	
	public void buttonSellItem(View button) {
		// Get the field handles
		final EditText titleTextField = (EditText) findViewById(R.id.titleTextField);  
		final EditText descriptionTextField = (EditText) findViewById(R.id.descriptionTextField);  
		final EditText phoneNumberTextField = (EditText) findViewById(R.id.phoneNumberTextField);  
		final EditText locationTextField = (EditText) findViewById(R.id.locationTextField);  
		final EditText priceTextField = (EditText) findViewById(R.id.priceTextField);
        final TextView titleValidateField = (TextView) findViewById(R.id.titleValidateField);
        final TextView descriptionValidateField = (TextView) findViewById(R.id.descriptionValidateField);
        final TextView contactValidateField = (TextView) findViewById(R.id.contactValidateField);
        final TextView priceValidateField = (TextView) findViewById(R.id.priceValidateField);

		// Extract the field text
		String titleText = titleTextField.getText().toString();
		String descriptionText = descriptionTextField.getText().toString();
		String phoneNumberText = phoneNumberTextField.getText().toString();
		String locationText = locationTextField.getText().toString();
		String priceText = priceTextField.getText().toString();

        // Validate necessary fields
        boolean validTitle = validateTitle(titleText, titleValidateField);
        boolean validDescription = validateDescription(descriptionText, descriptionValidateField);
        boolean validPrice = validatePrice(priceText, priceValidateField);
        boolean validContact = validateContact(phoneNumberText, contactValidateField);
        if (!(validTitle && validDescription && validPrice && validContact)) {
            if (!validTitle) {
                titleTextField.requestFocus();
            } else if (!validDescription) {
                descriptionTextField.requestFocus();
            } else if (!validPrice) {
                priceTextField.requestFocus();
            } else if (!validContact){
                phoneNumberTextField.requestFocus();
            }
            return;
        }
        button.setEnabled(false);

        pDialog = new ProgressDialog(SellActivity.this);
        pDialog.setMessage("Uploading items...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();


		// Create the sale item and add the fields
		final SaleItem saleItem = new SaleItem();
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		saleItem.setTitle(titleText);
		saleItem.setDescription(descriptionText);
		saleItem.setContact(phoneNumberText);
        if (lastLocation != null) {
            saleItem.setLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
            saleItem.setLocationString(locationText);
        } else {
            saleItem.setLocationString("Location Unknown.");
        }
		saleItem.setPrice(Double.parseDouble(priceText));
		saleItem.setUserID(getDeviceID());

		Log.i(TAG, "Sell Item: " + titleText + ", " + descriptionText + ", " + phoneNumberText +
                ", " + locationText + ", " + priceText);
		 	
		// For testing the following lines have been extracted from the above comment
		Toast.makeText(this, "Listed Item '" + titleText + "'", Toast.LENGTH_SHORT).show();
		
        // Add the item to the backend model and finish
		final ParseModel model = new ParseModel(getApplicationContext());
        final ParseObject imageObject = model.addItem(saleItem, null);

        Thread thread = new Thread() {
            @Override
            public void run() {
                ArrayList<Bitmap> imagesToAdd;
                if (currentImage > 0) {
                    for (int i = 0; i < mImagePaths.size(); i++) {
                        BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
                        bitmapFactoryOptions.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(mImagePaths.get(i).getAbsolutePath(), bitmapFactoryOptions);

                        int requiredHeight = 600;
                        int requiredWidth = 800;
                        int ratioHeight = (int) Math.ceil(bitmapFactoryOptions.outHeight / requiredHeight);
                        int ratioWidth = (int) Math.ceil(bitmapFactoryOptions.outWidth / requiredWidth);

                        if (ratioHeight > 1 || ratioWidth > 1) {
                            if (ratioHeight >= ratioWidth) {
                                bitmapFactoryOptions.inSampleSize = ratioHeight;
                            } else {
                                bitmapFactoryOptions.inSampleSize = ratioWidth;
                            }
                        }
                        bitmapFactoryOptions.inJustDecodeBounds = false;
                        imagesToAdd = new ArrayList<Bitmap>();
                        for (int j = 0; j <= i; j++) {
                            imagesToAdd.add(j == i ? BitmapFactory.decodeFile(
                                    mImagePaths.get(i).getAbsolutePath(), bitmapFactoryOptions) : null);
                        }

                        model.setItemImages(saleItem, imagesToAdd, imageObject);
                        //mImageBitmaps.add(BitmapFactory.decodeFile(mImagePaths.get(i).getAbsolutePath(), bitmapFactoryOptions));
                    }
                }
                pDialog.dismiss();
                finish();
            }
        };
        thread.start();
	}
	
	/**
	 * Get and return the user's phone number
	 * @return - the user's phone number as a String
	 */
	private String getUserPhoneNumber() {
		String mPhoneNumber;
		TelephonyManager teleManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneNumber = teleManager.getLine1Number();
		return mPhoneNumber;
	}
	
	/**
	 * Set the phone number text field to the user's phone number
	 */
	private void setPhoneNumber() {
		final EditText phoneNumberTextField = (EditText) findViewById(R.id.phoneNumberTextField);  
		phoneNumberTextField.setText(getUserPhoneNumber());		
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
            deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return deviceID;
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
		
		// Set the location in the text field and prompt the user if location is unavailable
		if (!networkAvailable) {
			Toast.makeText(this, "Network Provider is not available", Toast.LENGTH_SHORT).show();
			final EditText locationTextField = (EditText) findViewById(R.id.locationTextField);
			locationTextField.setText("No Location Found");
		} 
		
		// Setup the location listener
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                networkLocationListener);
	}
	
	/**
	 * Set the location in the location field
	 * 
	 * @param location - location to be set
	 * @throws IOException
	 */
	public void setLocation(Location location) throws IOException{
		// Get the latitude and longitude from the location
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		Log.i(TAG, "setLocation: " + latitude + ", " + longitude);
		
		int roundAmount = (int) 1E9;
			
		Geocoder geocoder = new Geocoder(this);
		List<Address> addresses = null;

		// Iterate until an address is found
		do {
			// Remove a decimal place from the latitude and longitude
			roundAmount /= 10;
			if (roundAmount <= 0) break;
			latitude = (double) Math.round(latitude * roundAmount) / roundAmount;
			longitude = (double) Math.round(longitude * roundAmount) / roundAmount;

			// Check if an address can be found
			addresses = geocoder.getFromLocation(latitude, longitude, 1);
			Log.i(TAG, "setLocation Iteration: " + latitude + ", " + longitude);
		} while (addresses.size() == 0);

        Address address = addresses.get(0);
        String locationString = address.getLocality() + ", " + address.getAdminArea();

		// Set the location in the text field
		Log.i(TAG, "setLocation Address: " + locationString);
		final EditText locationTextField = (EditText) findViewById(R.id.locationTextField);
		locationTextField.setText(locationString);
	}

    private boolean validateTitle(String title, TextView field) {
        title.replaceAll("[^a-zA-Z0-9()]", "");
        if (title.length() <= 3 || title.length() > 90) {
            field.setText("Please enter a title.");
            return false;
        }
        field.setText("");
        return true;
    }

    private boolean validateDescription(String description, TextView field) {
        description.replaceAll("[^a-zA-Z0-9()]", "");
        if (description.length() == 0) {
            field.setText("Please enter a description.");
            return false;
        }
        field.setText("");
        return true;
    }

    private boolean validatePrice(String price, TextView field) {
        price.replaceAll("[^0-9]", "");
        if (price.length() == 0) {
            field.setText("Please enter a price.");
            return false;
        }
        field.setText("");
        return true;
    }

    private boolean validateContact(String contact, TextView field){
        contact.replaceAll("[^0-9]", "");
        if (contact.length() == 0) {
            field.setText("Please enter a contact number.");
            return false;
        }
        else if (contact.length() < 8 || contact.length() > 11){
            field.setText("Please enter a valid contact number.");
            return false;
        }
        field.setText("");
        return true;
    }

	/**
	 * The network provider location listener which is only called once
	 */
	LocationListener networkLocationListener = new LocationListener() {
		
		/** Called when the location is found */
		public void onLocationChanged(Location location) {
			locationManager.removeUpdates(this);
			try {
				setLocation(location);
			} catch (IOException e) {
				return;
			}
		}

		public void onProviderDisabled(String provider) {}
		public void onProviderEnabled(String provider) {}
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	};
	
}
