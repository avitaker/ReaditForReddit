package com.avinashdavid.readitforreddit.UI;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.avinashdavid.readitforreddit.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

/**
 * Created by avinashdavid on 4/27/17.
 */

public class FragmentViewImage extends DialogFragment {
    public static final String TAG_IMAGE_FRAGMENT = "imageFragment";
    private static final String KEY_IMAGE_URL = "imageUrl";
    private String mLinkUrl;
    private PhotoView mImageView;
    int width;
    int height;

    public static FragmentViewImage getImageViewFragment(String linkUrl){
        Bundle bundle = new Bundle();
        bundle.putString(KEY_IMAGE_URL, linkUrl);
        FragmentViewImage fragmentViewImage = new FragmentViewImage();
        fragmentViewImage.setArguments(bundle);
        return fragmentViewImage;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Timber.d("creating");
        Bundle arguments = getArguments();
        if (arguments!=null){
            mLinkUrl = arguments.getString(KEY_IMAGE_URL);
        } else if (savedInstanceState!=null){
            mLinkUrl = savedInstanceState.getString(KEY_IMAGE_URL);
        }
//        Fresco.initialize(getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        int title = getArguments().getInt("title");

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        LayoutInflater inflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Dialog dialog=new Dialog(getActivity(), R.style.DialogTheme);
        Window window = dialog.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        View view = inflater.inflate(R.layout.fragment_view_image, null);
        mImageView = (PhotoView) view.findViewById(R.id.imageview_main);


//        AlertDialog dialog = new AlertDialog.Builder(getActivity())
//                .setView(view)
//                .create();

        dialog.setContentView(view);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Picasso.with(getActivity()).load(mLinkUrl).fit().centerInside().into(mImageView);
//        mImageView.setImageURI(Uri.parse(mLinkUrl));
    }

    //    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_view_image, container, false);
//        mImageView = (ImageView)view.findViewById(R.id.imageview_main);
//        Timber.d("we're in here now: " + mLinkUrl);
//
//        if (mLinkUrl==null){
//            Timber.e("noooo link here");
//        }
//
//        Picasso.with(getActivity()).load(mLinkUrl).into(mImageView);
//
//        return super.onCreateView(inflater, container, savedInstanceState);
//    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_IMAGE_URL, mLinkUrl);
        super.onSaveInstanceState(outState);
    }
}
