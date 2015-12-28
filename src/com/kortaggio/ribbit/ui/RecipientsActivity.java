package com.kortaggio.ribbit.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kortaggio.ribbit.R;
import com.kortaggio.ribbit.adapters.UserAdapter;
import com.kortaggio.ribbit.utils.FileHelper;
import com.kortaggio.ribbit.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class RecipientsActivity extends Activity {

	public static final String TAG = RecipientsActivity.class.getSimpleName();

	protected List<ParseUser> mFriends;
	protected ParseUser mCurrentUser;
	protected ParseRelation<ParseUser> mFriendsRelation;
	protected MenuItem mSendMenuItem;
	protected Uri mMediaUri;
	protected String mFileType;
	protected GridView mGridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.user_grid);
		mGridView = (GridView) findViewById(R.id.friendsGrid);
		mGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mGridView.setOnItemClickListener(mOnItemClickListener);
		TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
		mGridView.setEmptyView(emptyTextView);
		// Get the media URI we passed to this activity
		mMediaUri = getIntent().getData();
		mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);

	}

	@Override
	public void onResume() {
		super.onResume();

		mCurrentUser = ParseUser.getCurrentUser();
		mFriendsRelation = mCurrentUser
				.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

		setProgressBarIndeterminateVisibility(true);

		ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
		query.addAscendingOrder(ParseConstants.KEY_USERNAME);
		query.whereExists(ParseConstants.KEY_USERNAME);
		query.findInBackground(new FindCallback<ParseUser>() {

			@Override
			public void done(List<ParseUser> friends, ParseException e) {
				setProgressBarIndeterminateVisibility(false);
				if (e == null) {
					mFriends = friends;

					String[] usernames = new String[mFriends.size()];

					int i = 0;
					for (ParseUser user : mFriends) {
						usernames[i] = user.getUsername();
						i++;
					}

					if (mGridView.getAdapter() == null) {
						// Create grid view if it doesn't exist
						UserAdapter adapter = new UserAdapter(RecipientsActivity.this,
								mFriends);
						mGridView.setAdapter(adapter);
					} else {
						((UserAdapter) mGridView.getAdapter()).refill(mFriends);
					}

				} else {
					alertException(e);
				}

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recipients, menu);
		mSendMenuItem = menu.getItem(0); // First item is position 0
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_send) {
			ParseObject message = createMessage();
			if (message != null) {
				sendMessage(message);
				finish();
			} else {
				// there was an error
				alertException(new ParseException(R.string.error_file_selection, null));
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void sendMessage(ParseObject message) {
		// save the message to parse
		message.saveInBackground(new SaveCallback() {

			// tell the user that sending is done
			@Override
			public void done(ParseException e) {
				if (e == null) {
					// Successfully sent
					sendPushNotifications();
					
					// Toasts stay alive even upon activity.finish();
					Toast.makeText(RecipientsActivity.this, R.string.success_sent,
							Toast.LENGTH_LONG).show();
				} else {
					alertException(e);
				}
			}
		});
	}

	protected ParseObject createMessage() {
		ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
		message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser()
				.getObjectId());
		message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser()
				.getUsername());
		message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
		message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

		byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);
		if (fileBytes == null) {
			alertException(new ParseException(R.string.error_cant_process_bytes, null));
			return null;
		} else {
			if (mFileType.equals(ParseConstants.KEY_TYPE_IMAGE)) {
				fileBytes = FileHelper.reduceImageForUpload(fileBytes);
			}
			String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
			ParseFile file = new ParseFile(fileName, fileBytes);
			message.put(ParseConstants.KEY_FILE, file);
			return message;
		}
	}

	protected ArrayList<String> getRecipientIds() {
		ArrayList<String> recipientIds = new ArrayList<String>();
		for (int i = 0; i < mGridView.getCount(); i++) {
			if (mGridView.isItemChecked(i)) {
				recipientIds.add(mFriends.get(i).getObjectId());
			}
		}
		return recipientIds;
	}


	private void alertException(ParseException e) {
		// log the exception
		Log.e(TAG, e.getMessage());
		AlertDialog.Builder builder = new AlertDialog.Builder(
				RecipientsActivity.this);
		builder.setMessage(e.getMessage()).setTitle(R.string.error_title)
				.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	protected OnItemClickListener mOnItemClickListener = new OnItemClickListener () {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			if (mGridView.getCheckedItemCount() > 0) {
				mSendMenuItem.setVisible(true);
			} else {
				mSendMenuItem.setVisible(false);
			}
			
			ImageView checkImageView = (ImageView) view
					.findViewById(R.id.userCheckmarkOverlay);

			if (mGridView.isItemChecked(position)) {
				checkImageView.setVisibility(View.VISIBLE);
			} else {
				checkImageView.setVisibility(View.INVISIBLE);
			}
		}
		
	};
	protected void sendPushNotifications() {
		ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
		query.whereContainedIn(ParseConstants.KEY_USER_ID, getRecipientIds());
		// Send push notification
		ParsePush push = new ParsePush();
		push.setQuery(query);
		push.setMessage(getString(R.string.push_message, ParseUser.getCurrentUser().getUsername()));
		push.sendInBackground();
	}
}
