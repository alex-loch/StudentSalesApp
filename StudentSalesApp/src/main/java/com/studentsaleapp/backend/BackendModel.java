package com.studentsaleapp.backend;

import java.util.ArrayList;
import java.util.List;
//import android.os.Parcelable;
//import android.provider.MediaStore.Bitmap;

import android.graphics.Bitmap;

import com.parse.ParseFile;
import com.parse.ParseObject;

public interface BackendModel {
	
	/**
	 * Sorting constants
	 */
	public static int BEST_MATCH = 0;
	public static int DISTANCE = 1;
	public static int HIGHEST_PRICE = 2;
	public static int LOWEST_PRICE = 3;
	
	/**
	 * Add the given SaleItem to the database.
	 * The SaleItem should have all fields filled, except itemID.
	 * This method will associate an itemID with the SaleItem.	
	 *  
	 * Currently this method will log an error to the debug console
	 * if this fails, but it would be nice to return an error value.
	 *
     * @param item the item to be added
     */
	public ParseObject addItem(SaleItem item, ArrayList<Bitmap> images);

	/**
	 * Finds the item in the database with the same itemID as item,
	 * and updates it to match item's fields.
	 * 
	 * Currently this method will log an error to the debug console
	 * if this fails, but it would be nice to return an error value.
	 * 
	 * @param item the item to be updated
	 */
	public void updateItem(SaleItem item);

	/**
	 * Removes the item with the same itemID from the database.
	 * If no item or multiple items match, no item is removed.
	 * 
	 * Currently this method will log an error to the debug console
	 * if this fails, but it would be nice to return an error value.
	 * 
	 * @param item the item to be removed
	 */
	public void removeItem(SaleItem item);

	/**
	 * Get the list of images associated with the item in the database
	 * with the same itemID as item.
	 * 
	 * @param item retrieve the images associated with this item
	 * @return a list of images associated with item
	 */
	public List<Bitmap> getItemImages(SaleItem item);

	/**
	 * Set the list of images associated with item to images.
	 * 
	 * @param item set the images associated with this item
	 * @param images a list of images to be associated with item
	 */
	public void setItemImages(SaleItem item, List<Bitmap> images, ParseObject o);

	/**
	 * Return a list of the next 20 items up for sale. This list doesn't have
	 * to be in any particular order as there are other methods for sorting
	 * by particular comparator. Maybe a particular implementation will
	 * return it by some default order...
	 * 
	 * This doesn't give location information, so location is totally
	 * ignored when searching (matched based on title). Ideally this method
	 * should not be used (just leaving it here so nothing breaks).
	 * 
//	 * @param start the index to start retrieving data
	 * 
	 * @return the 20 items up for sale after index start
	 */
	public ArrayList<SaleItem> getItemList();
	
	/**
	 * Return a list of the next 20 items up for sale, but skip the first
	 * start values. This is intended for use when the user requests to see more
	 * items matching the same search criteria as previously, so initial
	 * items don't have to be redownloaded.
	 * 
	 * @param start the number of items to skip (for "Load/See More")
	 * @return a list of items matching the given conditions
	 */
	public ArrayList<SaleItem> getItemList(int start);

	/**
	 * Return a list of items for sale matching the given conditions.
	 * 
	 * @param searchQuery the user-entered search keyword string
	 * @param latitude latitude of the phone
	 * @param longitude longitude of the phone
	 * @param start the number of items to skip (for "Load/See More")
	 * @param sortMethod one of the BackendModel sorting constants
	 * @return a list of items matching the given conditions
	 */
	public ArrayList<SaleItem> getItemList(String searchQuery,
			double latitude, double longitude, int start, int sortMethod);
	

	/**
	 * Get all items added by the user with ID userID.
	 * 
	 * @param userID the ID of the user
	 * @return a list of all items added by the user
	 */
	public ArrayList<SaleItem> getItemListByUser(String userID);

    /**
     * Convert a ParseFile to a bitmap.
     *
     * @param f the ParseFile to convert
     * @return the bitmap created from the given ParseFile
     */
    public Bitmap parseFileToBitmap(ParseFile f);

}

