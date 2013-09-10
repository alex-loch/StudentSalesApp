package com.studentsaleapp.activities;

import java.util.ArrayList;
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

	/**
	 * Constructor for BuyRowItem ArrayList
	 * 
	 * @param images - value of id for image identifier
	 * @param title - value and type for title set and display
	 * @param desc - value and type for description set and display
	 * @param price - value and type for price set and display
	 * @param contact - value and type for contact set and display
	 * @param location - value and type for location set and display
	 */
	public BuyRowItem(ArrayList<Bitmap> images, String title, String desc, String price,
                      String contact, String location) {
		this.images = images;
		this.title = title;
		this.desc = desc;
		this.price = price;
		this.contact = contact;
		this.location = location;
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
	
	public String getTitle() {
		return title;
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
