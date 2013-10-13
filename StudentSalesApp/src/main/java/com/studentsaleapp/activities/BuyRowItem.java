package com.studentsaleapp.activities;

import java.util.ArrayList;
import java.util.Date;

import android.graphics.Bitmap;

public class BuyRowItem {
	
	/** The image identifier */
	private ArrayList<Bitmap> images;
	
	/** The title set */
	private String title;
	
	/** The description set */
	private String desc;
	
	/** The price set */
	private String price;

	/** The contact set */
	private String contact;
	
	/** The location set */
	private String location;

    /** The itemID */
    private String itemID;

    /** Created Time */
    private Date creationTime;

	/**
	 * Constructor for BuyRowItem ArrayList
	 * 
	 * @param title - value and type for title set and display
	 * @param desc - value and type for description set and display
	 * @param price - value and type for price set and display
	 * @param contact - value and type for contact set and display
	 * @param location - value and type for location set and display
	 */
	public BuyRowItem(String title, String desc, String price,
                      String contact, String location, String itemID, Date creationTime) {
        this.images = new ArrayList<Bitmap>();
		this.title = title;
		this.desc = desc;
		this.price = price;
		this.contact = contact;
		this.location = location;
        this.itemID = itemID;
        this.creationTime = creationTime;
	}

	public ArrayList<Bitmap> getImages() {
		return images;
	}

	public void setImages(ArrayList<Bitmap> images) {
		this.images = images;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

	public String getTitle() {
		return title;
	}

    public String getItemID() {
        return itemID;
    }
	
	public void setTitle(String title) {
		this.title = title;
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
	
	public String getLocation() {
		return location;
	}
	
	public void setlocation(String location) {
		this.location = location;
	}
	
	@Override
	public String toString() {
		return title + "\n" + desc;
	}
	
}
