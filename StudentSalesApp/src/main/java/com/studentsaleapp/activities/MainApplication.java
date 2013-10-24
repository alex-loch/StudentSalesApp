package com.studentsaleapp.activities;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.studentsaleapp.backend.BackendModel;
import com.studentsaleapp.backend.ParseModel;

import android.app.Application;
import android.util.Log;
import android.view.View;

import java.io.File;

public class MainApplication extends Application {
	
	/** The logging tag */
	private static final String TAG = "StudentSaleApp::MainApplication";
	
	/** The backend model */
	private BackendModel model;
		
	@Override
	public void onCreate(){
		super.onCreate();

        File cacheDir = StorageUtils.getCacheDirectory(getApplicationContext());
        // Create global configuration and initialize ImageGrabber with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .discCache(new UnlimitedDiscCache(cacheDir)) // default
        .discCacheSize(50 * 1024 * 1024)
        .discCacheFileCount(100)
        .build();

        ImageLoader.getInstance().init(config);

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
