package com.gmail.nelsonr462.bestie.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.easing.Glider;
import com.daimajia.easing.Skill;
import com.gmail.nelsonr462.bestie.BestieConstants;
import com.gmail.nelsonr462.bestie.ParseConstants;
import com.gmail.nelsonr462.bestie.R;
import com.gmail.nelsonr462.bestie.adapters.BestieListAdapter;
import com.gmail.nelsonr462.bestie.adapters.UploadGridAdapter;
import com.gmail.nelsonr462.bestie.helpers.UserDataHelper;
import com.hookedonplay.decoviewlib.DecoView;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class BestieRankFragment extends android.support.v4.app.Fragment {
    private String TAG = BestieRankFragment.class.getSimpleName();

    private UserDataHelper mUserDataHelper;
    public static Context mContext;

    private ParseUser mCurrentUser;
    private ParseRelation<ParseObject> mBatchImageRelation;
    public static ParseObject mUserBatch;
    public static ArrayList<ParseObject> mActiveBatchImages = new ArrayList<ParseObject>();;

    private View mView;
    private ListView mRankedPictureList;
    public static GridView mUploadGrid;
    private RelativeLayout mBestieHeader;
    private RelativeLayout mBatchView;
    private DecoView mBatchCompletionGraph;
    private TextView mCompletionPercentage;
    private Button mStartOverButton;
    private Button mShareButton;
    private Button mFindBestieButton;

    private ArrayList<Bitmap> mBitmaps;

    private OnFragmentInteractionListener mListener;

    public BestieRankFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_bestie_rank, container, false);
        mBestieHeader = (RelativeLayout) inflater.inflate(R.layout.header_bestie_top_picture, null, false);

        mContext = getActivity();

        mBatchView = (RelativeLayout) mView.findViewById(R.id.batchView);
        mBatchCompletionGraph = (DecoView) mView.findViewById(R.id.batchCompletionGraph);

        mRankedPictureList = (ListView) mView.findViewById(R.id.listView);
        mRankedPictureList.addHeaderView(mBestieHeader);
        mRankedPictureList.setAdapter(new BestieListAdapter(getActivity()));

        mUploadGrid = (GridView) mView.findViewById(R.id.photoGridView);

        /* CONNECT UPLOAD GRID TO PARSE HERE */
        mCurrentUser = ParseUser.getCurrentUser();

        if(mCurrentUser == null) {
            navigateToLogin();
        }

        mUserDataHelper = new UserDataHelper(mBatchView);



//        mUploadGrid.setAdapter(new UploadGridAdapter(getActivity()));

        mStartOverButton = (Button) mView.findViewById(R.id.startOverButton);
        mStartOverButton.setOnClickListener(ButtonClickListener(1));
        mShareButton = (Button) mView.findViewById(R.id.shareButton);
        mShareButton.setOnClickListener(ButtonClickListener(2));
        mFindBestieButton = (Button) mView.findViewById(R.id.findNewBestieButton);
        mFindBestieButton.setOnClickListener(ButtonClickListener(3));


        mCurrentUser.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {


                if (e != null) {
                    Log.d(TAG, "Get current installation failed");
                    return;
                }

                mUserBatch = mCurrentUser.getParseObject(ParseConstants.KEY_USER_BATCH);

                if (mUserBatch != null) {
                    Log.d(TAG, "USER NAME:  " + mCurrentUser.getUsername());


                    mUserBatch.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject userBatch, ParseException e) {
                            Log.d(TAG, "Batch ID:   " + userBatch.getObjectId());


                            mBatchImageRelation = mUserBatch.getRelation(ParseConstants.KEY_BATCH_IMAGE_RELATION);

                            ParseQuery<ParseObject> query = mBatchImageRelation.getQuery();
                            query.addAscendingOrder(ParseConstants.KEY_CREATED_AT);
                            query.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> list, ParseException e) {
                                    if (e != null) {
                                        Toast.makeText(mView.getContext(), "Parse Query failed :(", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    for (int i = 0; i < list.size(); i++) {
                                        mActiveBatchImages.add(list.get(i));
                                    }

                                    mUploadGrid.setAdapter(new UploadGridAdapter(getActivity(), mActiveBatchImages));
                                }
                            });

                        }
                    });
                } else {
                    Toast.makeText(mView.getContext(), "No Active batch!", Toast.LENGTH_SHORT).show();
                    mUploadGrid.setAdapter(new UploadGridAdapter(getActivity(), mActiveBatchImages));
                }


            }
        });


        return mView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private View.OnClickListener ButtonClickListener(final int buttonType) {
        View.OnClickListener onClickListener;

        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout addPhotosLayout = (RelativeLayout) mView.findViewById(R.id.addPhotosBestieLayout);

                if(buttonType == 1) {
                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(
                            Glider.glide(Skill.ExpoEaseOut, 800, ObjectAnimator.ofFloat(addPhotosLayout, "translationY", 2100, 0))
                    );
                    set.setDuration(500);
                    addPhotosLayout.setVisibility(View.VISIBLE);
                    set.start();

                } else if(buttonType == 3) {
                    if(mUploadGrid.getAdapter().getCount() < 3) {
                        Toast.makeText(mView.getContext(), "Upload some images first!", Toast.LENGTH_SHORT).show();
                    } else {

                        AnimatorSet set = new AnimatorSet();
                        set.playTogether(
                                Glider.glide(Skill.ExpoEaseOut, 800, ObjectAnimator.ofFloat(addPhotosLayout, "translationY", 0, 2100))
                        );
                        set.setDuration(500);
                        set.start();
                    }
                }
            }
        };
        return onClickListener;
    }

    private void navigateToLogin() {
        Intent intent = new Intent(mView.getContext(), WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }



    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
