/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sunyy.qrcode.mylibrary;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Map;

import camera.OpenCamera;

import static com.sunyy.qrcode.mylibrary.QRFragment.DECODE;
import static com.sunyy.qrcode.mylibrary.QRFragment.DECODE_FAILED;
import static com.sunyy.qrcode.mylibrary.QRFragment.DECODE_SUCCEEDED;
import static com.sunyy.qrcode.mylibrary.QRFragment.QUIT;
import static com.sunyy.qrcode.mylibrary.QRFragment.ZOOM_CHANGE;

final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final QRFragment mFragment;
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;
    private Map<DecodeHintType, Object> mHints;

    DecodeHandler(QRFragment fragment, Map<DecodeHintType, Object> hints) {
        mHints = new Hashtable<>();
        mHints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        mHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(mHints);
        this.mFragment = fragment;
    }

    @Override
    public void handleMessage(Message message) {
        if (message == null || !running) {
            return;
        }
        switch (message.what) {
            case DECODE:
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case QUIT:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        long start = System.currentTimeMillis();
        Result rawResult = null;
        PlanarYUVLuminanceSource source = mFragment.getCameraManager().buildLuminanceSource(data, width, height);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }

        Handler handler = mFragment.getHandler();
        if (rawResult != null) {
            // Don't log the barcode contents for security.
            long end = System.currentTimeMillis();
            LogUtils.d(TAG, "Found barcode in " + (end - start) + " ms");
            if (handler != null) {
                Rect rect = mFragment.getCameraManager().getFramingRect();
                if (rect != null) {
                    Camera camera = mFragment.getCameraManager().getOpenCamera().getCamera();
                    Camera.Parameters parameters = camera.getParameters();
                    int maxZoom = parameters.getMaxZoom();
                    int zoom = parameters.getZoom();
                    if (parameters.isZoomSupported()) {
                        ResultPoint[] points = rawResult.getResultPoints();
                        float point1X = points[0].getX();
                        float point1Y = points[0].getY();
                        float point2X = points[1].getX();
                        float point2Y = points[1].getY();
                        int len = (int) Math.sqrt(Math.pow(point1X - point2X, 2) + Math.pow(point1Y - point2Y, 2));
                        if (len <= rect.width() / 4) {
                            if (zoom == 0) {
                                zoom = maxZoom * 4;
                            } else {
                                zoom = zoom + 10;
                            }
                            if (zoom > maxZoom) {
                                zoom = maxZoom;
                            }
                            parameters.setZoom(zoom);
                            camera.setParameters(parameters);
                            Message message = Message.obtain(handler, DECODE_FAILED);
                            message.sendToTarget();
                        } else {
                            Message message = Message.obtain(handler, DECODE_SUCCEEDED, rawResult);
                            Bundle bundle = new Bundle();
                            bundleThumbnail(source, bundle);
                            message.setData(bundle);
                            message.sendToTarget();
                        }
                    } else {
                        Message message = Message.obtain(handler, DECODE_SUCCEEDED, rawResult);
                        Bundle bundle = new Bundle();
                        bundleThumbnail(source, bundle);
                        message.setData(bundle);
                        message.sendToTarget();
                    }
                } else {
                    Message message = Message.obtain(handler, DECODE_SUCCEEDED, rawResult);
                    Bundle bundle = new Bundle();
                    bundleThumbnail(source, bundle);
                    message.setData(bundle);
                    message.sendToTarget();
                }
            }
        } else {
            if (handler != null) {
                Message message = Message.obtain(handler, QRFragment.DECODE_FAILED);
                message.sendToTarget();
            }
        }
    }

    private static void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray());
        bundle.putFloat(DecodeThread.BARCODE_SCALED_FACTOR, (float) width / source.getWidth());
    }

}
