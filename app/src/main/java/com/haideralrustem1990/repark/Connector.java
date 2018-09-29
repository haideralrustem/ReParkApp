package com.haideralrustem1990.repark;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;

public interface Connector {
    public void adjustTextView(int id, int pos, Fragment fragment);
    public void adjustImageView(int id, int pos, Fragment fragment);
    //public void adjustImageView(int id, int pos, Fragment fragment);
    public Bitmap getCurrentBitmapImage();
}
