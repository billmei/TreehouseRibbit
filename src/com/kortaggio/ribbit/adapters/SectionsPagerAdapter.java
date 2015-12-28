package com.kortaggio.ribbit.adapters;

import java.util.Locale;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kortaggio.ribbit.R;
import com.kortaggio.ribbit.ui.FriendsFragment;
import com.kortaggio.ribbit.ui.InboxFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

	protected Context mContext;

	public SectionsPagerAdapter(Context context, FragmentManager fragmentManager) {
		super(fragmentManager);
		mContext = context;
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// Return a PlaceholderFragment (defined as a static inner class
		// below).

		switch (position) {
		case 0:
			return new InboxFragment();
		case 1:
			return new FriendsFragment();
		default:
			return new InboxFragment();
		}
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return mContext.getString(R.string.title_inbox).toUpperCase(l);
		case 1:
			return mContext.getString(R.string.title_friends).toUpperCase(l);
		}
		return null;
	}

	public int getIcon(int position) {
		switch (position) {
		case 0:
			return R.drawable.ic_tab_inbox;
		case 1:
			return R.drawable.ic_tab_friends;
		}
		return R.drawable.ic_tab_inbox;
	}
}