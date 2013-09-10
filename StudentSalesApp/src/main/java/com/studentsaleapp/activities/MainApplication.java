package com.studentsaleapp.activities;

import com.studentsaleapp.backend.BackendModel;
import com.studentsaleapp.backend.ParseModel;

import android.app.Application;
import android.util.Log;

public class MainApplication extends Application {
	
	/** The logging tag */
	private static final String TAG = "StudentSaleApp::MainApplication";
	
	/** The backend model */
	private BackendModel model;
		
	@Override
	public void onCreate(){
		super.onCreate();
		model = new ParseModel(this);
		Log.i(TAG, "Backend ParseModel created");
	}
	
	/**
	 * Getter for the backend model
	 * 
	 * @return - the backend model
	 */
	protected BackendModel getBackendModel() {
		return model;
	}
	
}
