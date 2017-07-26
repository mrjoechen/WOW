package com.thunder.wow;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

/**
 * Created by valentinorossi on 2016/12/27.
 */

public class AdvertisementViewFlipper extends ViewFlipper {
    public interface OnPageFlipListener {
        void onPageFlip(ViewFlipper flipper, int whichChild);
    }

    private OnPageFlipListener mOnPageFlipListener;

    public AdvertisementViewFlipper(Context context) {
        super(context);
    }

    public AdvertisementViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public void setDisplayedChild(int whichChild) {
        super.setDisplayedChild(whichChild);
        if (mOnPageFlipListener != null) {
            mOnPageFlipListener.onPageFlip(this, whichChild - 1);
        }
    }

    public void setOnPageFlipperListener(OnPageFlipListener listener) {
        this.mOnPageFlipListener = listener;
    }

}
