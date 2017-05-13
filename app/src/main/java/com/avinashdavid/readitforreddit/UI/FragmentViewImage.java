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
import android.widget.ImageView;

import com.avinashdavid.readitforreddit.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;

/**
 * Created by avinashdavid on 4/27/17.
 * Fullscreen DialogFragment that displays an image from a valid URL
 */

public class FragmentViewImage extends DialogFragment {
    public static final String TAG_IMAGE_FRAGMENT = "imageFragment";
    private static final String KEY_IMAGE_URL = "imageUrl";
    private String mLinkUrl;
    private ImageView mImageView;
    int width;
    int height;
    GlideDrawableImageViewTarget imageViewTarget;
    View progressBar;

    public static FragmentViewImage getImageViewFragment(String linkUrl){
        Bundle bundle = new Bundle();
        bundle.putString(KEY_IMAGE_URL, linkUrl);
        FragmentViewImage fragmentViewImage = new FragmentViewImage();
        fragmentViewImage.setArguments(bundle);
        return fragmentViewImage;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments!=null){
            mLinkUrl = arguments.getString(KEY_IMAGE_URL);
        } else if (savedInstanceState!=null){
            mLinkUrl = savedInstanceState.getString(KEY_IMAGE_URL);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

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
        mImageView = (ImageView) view.findViewById(R.id.imageview_main);

        progressBar = view.findViewById(R.id.loadingPanel);

        imageViewTarget = new GlideDrawableImageViewTarget(mImageView);

        dialog.setContentView(view);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
//        Picasso.with(getActivity()).load(mLinkUrl).fit().centerInside().into(mImageView);
        Glide.with(getActivity()).load(mLinkUrl).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                progressBar.setVisibility(View.GONE);
                return false;
            }
        }).fitCenter().into(imageViewTarget);
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
