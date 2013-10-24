package com.studentsaleapp.activities;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import com.studentsaleapp.activities.R;
import com.studentsaleapp.backend.BackendModel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class BuyListViewAdapter extends ArrayAdapter<BuyRowItem> {

	/** The context */
	private Context context;

    private boolean reviewMode;

    private MainBuyActivity mainBuyActivity;

    private AlertDialog alertDialog;

	/**
	 * Constructor for the Buy List View Adapter
	 * 
	 * @param context - context to run on
	 * @param resourceId - identification of the resource
	 * @param items - items to display
	 */
	public BuyListViewAdapter(MainBuyActivity mainActivity, Context context, int resourceId,
			List<BuyRowItem> items, boolean reviewMode) {
		super(context, resourceId, items);
        this.mainBuyActivity = mainActivity;
        this.reviewMode = reviewMode;
		this.context = context;
	}

	/**
	 * The ViewHolder Class which holds the item fields as Image and Text Views
	 */
	private class ViewHolder {
		ImageView imageView;
		TextView txtTitle;
		TextView txtDesc;
		TextView txtPrice;
		TextView txtContact;
		TextView txtLocation;
		TextView txtCreationTime;
        ProgressBar pBar;
        Button buttonEdit;
        Button buttonDelete;
	}

	/**
	 * Get the type of View that will be created for BuyRowItem
	 * 
	 * @param position - the position containing the item in the adapter for the View
	 * @param convertView - reuse of old view to display data at specific position in dataset
	 * @param parent - main view that current view will be grouped with
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		final BuyRowItem rowItem = getItem(position);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);


		// TODO: Attempt to fix this; code recycles views when scrolling for performance reasons, but if user scrolls whilst images are still loading it put images in wrong positions temporarily
		// Check null value; if null, inflate from layout
		// Assign holder to store the Image View and Text Views
		if (true) {

			holder = new ViewHolder();
            if (reviewMode) {
                convertView = mInflater.inflate(R.layout.single_review_row, null);
                holder.txtDesc = (TextView) convertView.findViewById(R.id.reviewDesc);
                holder.txtTitle = (TextView) convertView.findViewById(R.id.reviewTitle);
                holder.imageView = (ImageView) convertView.findViewById(R.id.reviewIcon);
                holder.txtPrice = (TextView) convertView.findViewById(R.id.reviewPrice);
                holder.txtContact = (TextView) convertView.findViewById(R.id.reviewContact);
                holder.txtLocation = (TextView) convertView.findViewById(R.id.reviewLocation);
                holder.pBar = (ProgressBar) convertView.findViewById((R.id.pBar));
                holder.txtCreationTime = (TextView) convertView.findViewById(R.id.reviewCreationTime);
                // holder.buttonEdit = (Button) convertView.findViewById(R.id.reviewEdit);
                holder.buttonDelete = (Button) convertView.findViewById(R.id.reviewDelete);

                final AlertDialog.Builder builder = new AlertDialog.Builder(mainBuyActivity)
                        .setTitle("Delete Listing")
                        .setMessage("Do you really want to delete your listing?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((MainApplication) context.getApplicationContext())
                                        .getBackendModel().removeItem(rowItem.getItemID());
                                mainBuyActivity.finish();
                            }})
                        .setNegativeButton(android.R.string.no, null);

                holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                    String itemID = rowItem.getItemID();
                    public void onClick(View view) {
                        alertDialog = builder.create();
                        alertDialog.show();
                    }
                });
            } else {
                convertView = mInflater.inflate(R.layout.single_buy_row, null);
                holder.txtDesc = (TextView) convertView.findViewById(R.id.desc);
                holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
                holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
                holder.txtPrice = (TextView) convertView.findViewById(R.id.price);
                holder.txtContact = (TextView) convertView.findViewById(R.id.contact);
                holder.txtLocation = (TextView) convertView.findViewById(R.id.location);
                holder.pBar = (ProgressBar) convertView.findViewById((R.id.pBar));
                holder.txtCreationTime = (TextView) convertView.findViewById(R.id.creationTime);
            }
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

//        convertView = mInflater.inflate(R.layout.single_buy_row, null);
//        holder = new ViewHolder();
//        holder.txtDesc = (TextView) convertView.findViewById(R.id.desc);
//        holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
//        holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
//        holder.txtPrice = (TextView) convertView.findViewById(R.id.price);
//        holder.txtContact = (TextView) convertView.findViewById(R.id.contact);
//        holder.txtLocation = (TextView) convertView.findViewById(R.id.location);
//        holder.pBar = (ProgressBar) convertView.findViewById((R.id.pBar));
//
//        convertView.setTag(holder);
		
		// Assign values from BuyRowItem data values
		holder.txtDesc.setText(rowItem.getDesc());
		holder.txtTitle.setText(rowItem.getTitle());
		holder.txtPrice.setText(rowItem.getPrice());
		holder.txtContact.setText(rowItem.getContact());
		holder.txtLocation.setText(rowItem.getLocation());
		holder.txtCreationTime.setText(rowItem.getCreationTime().toString());
        holder.pBar.animate();

        if (rowItem.getImages().size() > 0) {
            holder.pBar.setVisibility(View.INVISIBLE);
            holder.imageView.setImageBitmap(rowItem.getImages().get(0));
        } else if (rowItem.isNoImages()) {
            holder.pBar.setVisibility(View.INVISIBLE);
            holder.imageView.setImageResource(R.drawable.no_image_found);
        }

		return convertView;
	}



}
