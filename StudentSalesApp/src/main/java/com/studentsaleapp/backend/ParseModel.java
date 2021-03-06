package com.studentsaleapp.backend;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

//import android.os.Parcel;
//import android.os.Parcelable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseException;
import com.parse.ParseRelation;

/**
 * A collection of methods to interact with the Parse database:
 * see www.parse.com/docs/android_guide
 */
public class ParseModel implements BackendModel {
	
	private static final String applicationID = "JHwOtzWPwbcx3z9PaSVngpUXMkKZs1LgSPW5mii2";
	private static final String clientKey = "DHgBqJxm534gfMSAiX8wkWpVFtCN1zYjsmWqgzhW";
	
	/**
	 * List of objects retrieved from Parse.
	 */
	private List<ParseObject> retrievedObjects = new ArrayList<ParseObject>();

	/**
	 * Create a new ParseModel, and make Parse available to all contexts
	 * stemming from the given context.
	 * 
	 * @param context the main application context of the app
	 */
	public ParseModel(Context context) {
		Parse.initialize(context, applicationID, clientKey);
	}

	/**
	 * Retrieve a list of ParseObjects matching query.
	 * 
	 * @param query the ParseQuery used to query the database
	 */
	private void objectQuery(ParseQuery query) {		
		try {
			this.retrievedObjects = query.find();
		} catch (ParseException e) {
			Log.e("ParseModel.objectQuery", e.toString());
		}
	}

	/**
	 * Return a list of SaleItems matching query.
	 * 
	 * @param query the ParseQuery used to query the database
	 * @return a list of SaleItems matching query
	 */
	private ArrayList<SaleItem> itemQuery(ParseQuery query) {
		return this.itemQuery(query, 0.0, 0.0, BEST_MATCH);
	}

	/**
	 * Returns all items from the database matching the given query.
	 * 
	 * @param query a ParseQuery with all required parameters preset
	 * @param latitude the latitude of the user
	 * @param longitude the longitude of the user
	 * @param sortMethod one of the BackendModel sorting parameters
	 * @return all items from the database matching the given query
	 */
	private ArrayList<SaleItem> itemQuery(ParseQuery query, double latitude, 
			double longitude, int sortMethod) {
		
		query = this.sortParseQuery(query, latitude, longitude, sortMethod);
		ArrayList<SaleItem> allItems = new ArrayList<SaleItem>();
		this.objectQuery(query);
		for (ParseObject object : this.retrievedObjects) {
			allItems.add(this.parseObjectToSaleItem(object));
		}
	
		Log.e("ParseModel.itemQuery.retrievedObjects", this.retrievedObjects.toString());
		Log.e("ParseModel.itemQuery.allItems", allItems.toString());
		return allItems;
	}

	/**
	 * Add sorting logic to query.
	 * 
	 * @param query the current query to Parse
	 * @param sortMethod one of the BackendModel sorting parameters
	 * @return 
	 */
	private ParseQuery sortParseQuery(ParseQuery query, 
			double latitude, double longitude, int sortMethod) {
		
		switch(sortMethod) {
            case BEST_MATCH:

                // If dummy lat/long have been set, sorting is meaningless: skip
                if ((latitude == 0.0) && (longitude == 0.0)) {
                    break;
                }

                // The rest seems pretty hard to do
                // Let's stick with the default for now
                // I guess that's distance, how 'bout you?
                // So falling through we will allow

            case DISTANCE:
                ParseGeoPoint here = new ParseGeoPoint(latitude, longitude);
                query.whereNear("coordinates", here);
                query.whereWithinKilometers("coordinates", here, 20.0);
                break;
            case HIGHEST_PRICE:
                query.addDescendingOrder("price");
                break;
            case LOWEST_PRICE:
                query.addAscendingOrder("price");
                break;
            }
		
		return query;
	}

    /**
     * Return the object in the database with ID itemID.
     *
     * @param itemID the itemID to search for
     * @return the object in the database matching itemID
     */
	public ParseObject findItem(String itemID) {
		ParseQuery query = new ParseQuery("SaleItem");
		query.whereEqualTo("objectId", itemID);
		
		try {
			this.retrievedObjects = query.find();
		} catch (ParseException e) {
			Log.e("ParseModel.findItem", e.toString());
		}
		
		return this.retrievedObjects.get(0);
	}

	/**
	 * Return the object in the database with the same itemID as item.
	 * 
	 * @param item the item to find in the database
	 * @return the object in the database matching item
	 */
	private ParseObject findItem(SaleItem item) {
		return this.findItem(item.getItemID());
	}

	/**
	 * Convert a bitmap to a ParseFile.
	 * 
	 * @param b the bitmap to convert
	 * @return the ParseFile created from the given bitmap
	 */
	private ParseFile bitmapToParseFile(Bitmap b) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream(b.getWidth() * b.getHeight());
		b.compress(CompressFormat.JPEG, 25, stream);
		ParseFile file = new ParseFile(stream.toByteArray());
		return file;
	}

	/**
	 * Convert a ParseFile to a bitmap.
	 * 
	 * @param f the ParseFile to convert
	 * @return the bitmap created from the given ParseFile
	 */
	public Bitmap parseFileToBitmap(ParseFile f) {

		try {
			byte[] data = f.getData();
			Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
			return b;
		} catch (ParseException e) {
			Log.e("ParseModel.parseFileToBitmap", e.toString());
		}
		
		return null;
	}

	/**
	 * Convert a ParseObject to a SaleItem.
	 * 
	 * @param p the ParseObject to convert
	 * @return the SaleItem created from the given ParseObject
	 */
	private SaleItem parseObjectToSaleItem(ParseObject p) {
        ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        String[] imageKeys = {"imageOne", "imageTwo", "imageThree"};
        ParseFile[] parseImage = new ParseFile[3];
		SaleItem s = new SaleItem();
		s.setTitle(p.getString("title"));
		s.setDescription(p.getString("description"));
		s.setContact(p.getString("contact"));
		ParseGeoPoint gp = p.getParseGeoPoint("coordinates");
		s.setLocation(gp.getLatitude(), gp.getLongitude());
        s.setLocationString(p.getString("location"));
		s.setPrice(p.getDouble("price"));
		s.setUserID(p.getString("userid"));
		s.setItemID(p.getObjectId());
        s.setCreatedAt(p.getCreatedAt());

//        // Convert parse files into bitmaps
//        for (int i = 0; i < 3; i++) {
//            parseImage[i] = p.getParseFile(imageKeys[i]);
//            if (parseImage[i].isDataAvailable()) {
//                images.set(i, parseFileToBitmap(parseImage[i]));
//            }
//        }

		return s;
	}
	/**
	 * Add all fields in a SaleItem to a ParseObject.
	 * 
	 * @param s the SaleItem to convert
	 * @param o the ParseObject to fill
	 * @return the filled ParseObject
	 */
	private void fillParseObject(SaleItem s, ParseObject o) {
		o.put("title", s.getTitle());
		o.put("description", s.getDescription());
		o.put("contact", s.getContact());
		o.put("price", s.getPrice());
		o.put("coordinates", new ParseGeoPoint(s.getLatitude(), s.getLongitude()));
        o.put("location", s.getLocationString());
		o.put("userid", s.getUserID());
	}

	public ParseObject addItem(SaleItem item, ArrayList<Bitmap> images) {
		ParseObject saleItemObject = new ParseObject("SaleItem");
        ParseObject imagesObject = new ParseObject("SaleItemImages");

		this.fillParseObject(item, saleItemObject);
        if (images != null) {
            this.setItemImages(item, images, imagesObject);
        }

		try {
			saleItemObject.save();
            imagesObject.save();

            ParseRelation<ParseObject> relation = saleItemObject.getRelation("imageRelation");
            relation.add(imagesObject);
            saleItemObject.saveInBackground();
		} catch (ParseException e) {
			Log.e("ParseModel.addItem", e.toString());
		}
		item.setItemID(saleItemObject.getObjectId());
        return imagesObject;
	}

    @Override
    public void setItemImages(SaleItem item, List<Bitmap> images, ParseObject o) {
        String[] imageKeys = {"imageOne", "imageTwo", "imageThree"};

        if (images == null || images.size() == 0) {
            return;
        }
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i) == null) {
                continue;
            }
            o.put(imageKeys[i], bitmapToParseFile(images.get(i)));
        }
        try {
            o.save();
        } catch (ParseException e) {
            Log.e("ParseModel.addItem", e.toString());
        }
    }

	@Override
	public void updateItem(SaleItem item) {
		ParseObject object = this.findItem(item);
		this.fillParseObject(item, object);
		object.saveInBackground();
	}

	@Override
	public void removeItem(SaleItem item) {
		ParseObject o = this.findItem(item);
		o.deleteInBackground();
	}

    @Override
    public void removeItem(String itemID) {
        ParseObject o = this.findItem(itemID);
        o.deleteInBackground();
    }

	@Override
	public ArrayList<Bitmap> getItemImages(SaleItem item) {
		
		ParseObject object = this.findItem(item);
		ParseQuery query = object.getRelation("images").getQuery();
		ArrayList<Bitmap> allImages = new ArrayList<Bitmap>();
		
		this.objectQuery(query);
		for (ParseObject o : this.retrievedObjects) {
			Bitmap b = this.parseFileToBitmap(o.getParseFile("file"));
			allImages.add(b);
		}
		
		return allImages;
	}

	@Override
	public ArrayList<SaleItem> getItemList() {
		String searchQuery = "";
		double latitude = 0.0;
		double longitude = 0.0;
		int start = 0;
		int sortMethod = DISTANCE;
		return this.getItemList(searchQuery, latitude, longitude, start, sortMethod);
	}

	@Override
	public ArrayList<SaleItem> getItemList(int start) {
		String searchQuery = "";
		double latitude = 0.0;
		double longitude = 0.0;
		int sortMethod = DISTANCE;
		return this.getItemList(searchQuery, latitude, longitude, start, sortMethod);
	}

	@Override
	public ArrayList<SaleItem> getItemList(String searchQuery, double latitude,
			double longitude, int start, int sortMethod) {

		ParseQuery query = new ParseQuery("SaleItem");
        String regex = Pattern.quote(searchQuery);
        query.whereMatches("title", regex, "i");
		query.setSkip(start);
		query.setLimit(20);
		
		return this.itemQuery(query, latitude, longitude, sortMethod);
	}
	
	@Override
	public ArrayList<SaleItem> getItemListByUser(String userID) {
		ParseQuery query = new ParseQuery("SaleItem");
		query.whereEqualTo("userid", userID);
		return this.itemQuery(query);
	}

}

