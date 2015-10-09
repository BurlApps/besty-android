package com.gmail.nelsonr462.bestie.ui;

import android.content.Intent;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gmail.nelsonr462.bestie.BestieConstants;
import com.gmail.nelsonr462.bestie.ParseConstants;
import com.gmail.nelsonr462.bestie.R;
import com.gmail.nelsonr462.bestie.adapters.MainFragmentPagerAdapter;
import com.gmail.nelsonr462.bestie.adapters.UploadGridAdapter;
import com.gmail.nelsonr462.bestie.helpers.SlidingTabLayout;
import com.parse.ConfigCallback;
import com.parse.GetCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    protected ParseObject mUserBatch;
    protected ParseUser mCurrentUser;
    private LinearLayout mRootView;
    private BestieRankFragment mBestieFragment;
    private VoteFragment mVoteFragment;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRootView = (LinearLayout) findViewById(R.id.mainRootView);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        mCurrentUser = ParseUser.getCurrentUser();
        if(mCurrentUser == null) {
            navigateToLogin();
            return;
        } else {
            Log.i(TAG, mCurrentUser.getUsername());
            mCurrentUser.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    mCurrentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            ParseConfig.getInBackground(new ConfigCallback() {
                                @Override
                                public void done(ParseConfig parseConfig, ParseException e) {
                                    inflateTabLayout();

                                }
                            });

                        }
                    });

                }
            });

        }

    }

    private void inflateTabLayout() {
        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        MainFragmentPagerAdapter adapter =  new MainFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this);

        // Assigning ViewPager View and setting the adapter
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setCustomTabView(R.layout.tab_icon_layout, 0);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.bestieYellow);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
        pager.setCurrentItem(1);

        mBestieFragment = (BestieRankFragment) adapter.getItem(2);
        mVoteFragment = (VoteFragment) adapter.getItem(1);
    }


    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            ParseUser.logOut();
            BestieRankFragment.mActiveBatchImages.clear();
            navigateToLogin();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void navigateToLogin() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == BestieConstants.PICK_PHOTO_REQUEST && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, CropPhotoActivity.class);
            intent.setData(result.getData());
            startActivity(intent);
        }

        if(requestCode == BestieConstants.SHARE_REQUEST) {
            Toast.makeText(this, "Content shared!", Toast.LENGTH_SHORT).show();
            mCurrentUser.put(ParseConstants.KEY_SHARED, true);
            mCurrentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    BestieRankFragment.mUploadGrid.setAdapter(new UploadGridAdapter(BestieRankFragment.mContext, BestieRankFragment.mActiveBatchImages));
                    final Snackbar snackbar = Snackbar.make(mRootView, "Upload limit increased!", 5000);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(getResources().getColor(R.color.bestieBlue));

                    snackbar.setAction("Okay", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    }).setActionTextColor(getResources().getColor(R.color.bestieYellow)).show();
                    mVibrator.vibrate(300);
                }
            });
        }
    }

}
