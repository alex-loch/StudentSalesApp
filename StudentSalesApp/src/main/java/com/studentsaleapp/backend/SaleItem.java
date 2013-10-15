package com.studentsaleapp.backend;

import java.util.ArrayList;
import java.util.Date;

import android.graphics.Bitmap;
import android.provider.MediaStore.Images;
import android.util.Log;

public class SaleItem {
	
	private String title = "";
	private String description = "";
	private String contact = "";
	private double latitude = 0;
	private double longitude = 0;
    private String location = "";
	private double price = 0;
	private String userID = "";
	private Date createdAt;
	private String itemID = null;
	private Images thumbnail;

	/**
	 * Create an empty SaleItem.
	 */
	public SaleItem() {
	}

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

	/**
	 * @return the item's title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title set the item's title to title
	 */
	public void setTitle(String title) {
		this.title = title;
	}	

	/**
	 * @return the item's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description set the item's description to description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the user's contact details
	 */
	public String getContact() {
		return contact;
	}

	/**
	 * @param contact set the item's contact to contact
	 */
	public void setContact(String contact) {
		this.contact = contact;
	}
	
	/**
	 * @return the longitude
	 */
	public double getLatitude() {
		return latitude;
	}
	
	/**
	 * @return the latitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Set the item location to (latitude, longitude)
	 * @param latitude the latitude
	 * @param longitude the longitude
	 */
	public void setLocation(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

    /**
     * @return the item's location
     */
    public String getLocationString() {
        return location;
    }

    /**
     * @param location set the item's location to location
     */
    public void setLocationString(String location) {
        this.location = location;
    }

	/**
	 * @return the item's price
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * @param price set the item's price to price
	 */
	public void setPrice(double price) {
		this.price = price;
	}
	
	/**
	 * @return the userID
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * @param userID set the item's userID to userID
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}

	/**
	 * @return the item's thumbnail image
	 */
	public Images getThumbnail() {
		return thumbnail;
	}

	/**
	 * @param thumbnail set the item's thumbnail to thumbnail
	 */
	public void setThumbnail(Images thumbnail) {
		this.thumbnail = thumbnail;
	}

	/**
	 * @return the itemID
	 */
	public String getItemID() {
		return itemID;
	}

	/**
	 * Note: this field should be automatically generated,
	 * not set manually.
	 * @param itemID set the item's itemID to itemID
	 */
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
	
	@Override
	public String toString() {
		return this.title + this.itemID;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((itemID == null) ? 0 : itemID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SaleItem other = (SaleItem) obj;
		if (itemID == null) {
			if (other.itemID != null)
				return false;
		} else if (!itemID.equals(other.itemID))
			return false;
		return true;
	}
	
}

