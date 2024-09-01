package com.existfragger.rnimagesize;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import java.io.InputStream;
import java.net.URL;

public class RNImageSizeModule extends NativeImageSizeModuleSpec {
    public static final String NAME = "RNImageSize";

    private ReactApplicationContext reactContext;

    public RNImageSizeModule(final ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public void getSize(String uri, final Promise promise) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Uri u = Uri.parse(uri);
                    String scheme = u.getScheme();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    ExifInterface exifInterface = null;
                    options.inJustDecodeBounds = true;

                    int height = 0;
                    int width = 0;
                    int rotation = 0;

                    if (ContentResolver.SCHEME_FILE.equals(scheme) ||
                            ContentResolver.SCHEME_CONTENT.equals(scheme) ||
                            ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
                        ContentResolver contentResolver = reactContext.getContentResolver();
                        BitmapFactory.decodeStream(contentResolver.openInputStream(u), null,
                                                                             options);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            exifInterface =
                                    new ExifInterface(contentResolver.openInputStream(u));
                        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
                            exifInterface = new ExifInterface(u.getPath());
                        }
                    } else {
                        URL url = new URL(uri);
                        BitmapFactory.decodeStream(url.openStream(), null, options);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            exifInterface = new ExifInterface(url.openStream());
                        }
                    }

                    height = options.outHeight;
                    width = options.outWidth;

                    if (exifInterface != null) {
                        int orientation =
                                exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
                            rotation = 90;
                        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
                            rotation = 180;
                        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
                            rotation = 270;
                    }

                    WritableMap map = Arguments.createMap();

                    map.putInt("height", height);
                    map.putInt("width", width);
                    map.putInt("rotation", rotation);

                    promise.resolve(map);
                } catch (Exception e) {
                    promise.reject(e);
                }
            }
        }).start();
    }

    @Override
    public String getName() {
        return NAME;
    }
}
