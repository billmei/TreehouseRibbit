package com.kortaggio.ribbit.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.kortaggio.ribbit.R;
import com.kortaggio.ribbit.adapters.SectionsPagerAdapter;
import com.kortaggio.ribbit.utils.ParseConstants;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	public static final String TAG = MainActivity.class.getSimpleName();

	public static final int REQUEST_TAKE_PHOTO = 0;
	public static final int REQUEST_TAKE_VIDEO = 1;
	public static final int REQUEST_CHOOSE_PHOTO = 2;
	public static final int REQUEST_CHOOSE_VIDEO = 3;

	public static final int MEDIA_TYPE_IMAGE = 4;
	public static final int MEDIA_TYPE_VIDEO = 5;

	public static final int MAX_FILE_SIZE = 1024 * 1024 * 10; // 10 MB

	protected Uri mMediaUri;

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;

	protected DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener() {
		// Dialog box that asks the user to take a photo, take a video, choose a
		// picture, or choose a video.
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case 0:
				Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
				if (mMediaUri == null) {
					Toast.makeText(MainActivity.this, R.string.error_external_storage,
							Toast.LENGTH_SHORT).show();
				} else {
					takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
					startActivityForResult(takePhotoIntent, REQUEST_TAKE_PHOTO);
				}
				break;
			case 1:
				Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
				mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
				if (mMediaUri == null) {
					Toast.makeText(MainActivity.this, R.string.error_external_storage,
							Toast.LENGTH_SHORT).show();
				} else {
					videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
					videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
					// 0 == lowest resolution
					videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
					startActivityForResult(videoIntent, REQUEST_TAKE_VIDEO);
				}
				break;
			case 2:
				Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
				choosePhotoIntent.setType("image/*");
				startActivityForResult(choosePhotoIntent, REQUEST_CHOOSE_PHOTO);
				break;
			case 3:
				Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
				chooseVideoIntent.setType("video/*");
				Toast.makeText(MainActivity.this, R.string.video_file_size_warning,
						Toast.LENGTH_LONG).show();
				startActivityForResult(chooseVideoIntent, REQUEST_CHOOSE_VIDEO);
				break;
			default:
				break;
			}
		}

		private Uri getOutputMediaFileUri(int mediaType) {
			// To be safe, you should check that the SDCard is mounted
			// using Environment.getExternalStorageState() before doing this.
			if (isExternalStorageAvailable()) {
				// 1. Get the external storage directory
				String appName = MainActivity.this.getString(R.string.app_name);
				File mediaStorageDir = new File(
						Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						appName);

				// 2. Create our own subdirectory
				if (!mediaStorageDir.exists()) {
					if (!mediaStorageDir.mkdirs()) {
						Log.e(TAG, "Failed to create directory.");
						return null;
					}
				}

				// 3. Create a file name
				File mediaFile;
				Date now = new Date();
				String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
						.format(now);
				String path = mediaStorageDir.getPath() + File.separator;

				// 4. Create the file
				if (mediaType == MEDIA_TYPE_IMAGE) {
					mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
				} else if (mediaType == MEDIA_TYPE_VIDEO) {
					mediaFile = new File(path + "VID_" + timestamp + ".mp4");
				} else {
					Log.e(TAG, "Failed to create the file on disk.");
					return null;
				}

				Log.d(TAG, "File: " + Uri.fromFile(mediaFile));

				// 5. Return the file's URI
				return Uri.fromFile(mediaFile);
			} else {
				return null;
			}
		}

		private boolean isExternalStorageAvailable() {
			String state = Environment.getExternalStorageState();
			if (state.equals(Environment.MEDIA_MOUNTED)) {
				return true;
			} else {
				return false;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		ParseAnalytics.trackAppOpened(getIntent());

		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			Log.i(TAG, currentUser.getUsername());
		} else {
			navigateToLogin();
		}

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(this,
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setIcon(mSectionsPagerAdapter.getIcon(i))
					.setContentDescription(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CHOOSE_PHOTO
					|| requestCode == REQUEST_CHOOSE_VIDEO) {
				// set the media URI into your data if the user chooses an existing
				// photo
				if (data == null) {
					showToastError(R.string.error_generic);
				} else {
					mMediaUri = data.getData();
				}

				if (requestCode == REQUEST_CHOOSE_VIDEO) {
					// make sure the file is less than 10 MB
					int fileSize = 0;
					InputStream inputStream = null;
					try {
						inputStream = getContentResolver().openInputStream(mMediaUri);
						fileSize = inputStream.available();
					} catch (FileNotFoundException e) {
						showToastError(R.string.error_opening_file);
						e.printStackTrace();
						return;
					} catch (IOException e) {
						showToastError(R.string.error_opening_file);
						e.printStackTrace();
						return;
					} finally {
						try {
							// Close the input stream after you open it
							inputStream.close();
						} catch (IOException e) {/* Intentionally blank */
						}
					}
					if (fileSize >= MAX_FILE_SIZE) {
						showToastError(R.string.error_filesize_too_large);
						return;
					}
				}

			} else {
				// Make the photo you just took available in the gallery
				Intent mediaScanIntent = new Intent(
						Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				mediaScanIntent.setData(mMediaUri);
				sendBroadcast(mediaScanIntent);
			}

			Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
			recipientsIntent.setData(mMediaUri);
			String fileType;
			if (requestCode == REQUEST_TAKE_PHOTO
					|| requestCode == REQUEST_CHOOSE_PHOTO) {
				fileType = ParseConstants.KEY_TYPE_IMAGE;
			} else {
				fileType = ParseConstants.KEY_TYPE_VIDEO;
			}
			recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
			startActivity(recipientsIntent);

		} else if (resultCode != RESULT_CANCELED) {
			showToastError(R.string.error_generic);
		}
	}

	private void showToastError(int errorToDisplay) {
		Toast.makeText(this, errorToDisplay, Toast.LENGTH_LONG).show();
	}

	private void navigateToLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id) {
		case R.id.action_logout:
			ParseUser.logOut();
			navigateToLogin();
			break;
		case R.id.action_edit_friends:
			Intent intent = new Intent(this, EditFriendsActivity.class);
			startActivity(intent);
			break;
		case R.id.action_camera:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setItems(R.array.camera_choices, mDialogListener);
			AlertDialog dialog = builder.create();
			dialog.show();

			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

}
