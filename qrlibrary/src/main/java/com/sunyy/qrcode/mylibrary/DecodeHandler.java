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

    private QRFragment mFragment;
    private QRCommonFragment mCommonFragment;
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;
    private Map<DecodeHintType, Object> mHints;
    private int mChange = 1;

    DecodeHandler(QRFragment fragment, Map<DecodeHintType, Object> hints) {
        mHints = new Hashtable<>();
        mHints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        mHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(mHints);
        this.mFragment = fragment;
    }

    DecodeHandler(QRCommonFragment fragment, Map<DecodeHintType, Object> hints) {
        mHints = new Hashtable<>();
        mHints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        mHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(mHints);
        this.mCommonFragment = fragment;
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
            case ZOOM_CHANGE:
                Camera camera = (Camera) message.obj;
                Camera.Parameters parameters = camera.getParameters();
                int zoom = message.arg1;
                int maxZoom = message.arg2;
                mChange++;
                zoom += mChange;
                parameters.setZoom(zoom);
                camera.setParameters(parameters);
                if (zoom < maxZoom / 4) {
                    Message message1 = new Message();
                    message1.what = ZOOM_CHANGE;
                    message1.arg1 = zoom;
                    message1.arg2 = maxZoom;
                    message1.obj = camera;
                    sendMessageDelayed(message1, 40);
                } else {
                    Message message1;
                    if (mFragment != null) {
                        message1 = Message.obtain(mFragment.getHandler(), DECODE_FAILED);
                    } else {
                        message1 = Message.obtain(mCommonFragment.getHandler(), DECODE_FAILED);
                    }
                    message1.sendToTarget();
                }

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
        PlanarYUVLuminanceSource source;
        if(this.mFragment != null) {
            source = this.mFragment.getCameraManager().buildLuminanceSource(data, width, height);
        } else {
            source = this.mCommonFragment.getCameraManager().buildLuminanceSource(data, width, height);
        }

        if(source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            try {
                rawResult = this.multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException var26) {
                ;
            } finally {
                this.multiFormatReader.reset();
            }
        }

        Handler handler;
        if(this.mFragment != null) {
            handler = this.mFragment.getHandler();
        } else {
            handler = this.mCommonFragment.getHandler();
        }

        if(rawResult != null) {
            long end = System.currentTimeMillis();
            LogUtils.d(TAG, "Found barcode in " + (end - start) + " ms");
            if(handler != null) {
                Rect rect;
                if(this.mFragment != null) {
                    rect = this.mFragment.getCameraManager().getFramingRect();
                } else {
                    rect = this.mCommonFragment.getCameraManager().getFramingRect();
                }

                if(rect != null) {
                    Camera camera;
                    if(this.mFragment != null) {
                        camera = this.mFragment.getCameraManager().getOpenCamera().getCamera();
                    } else {
                        camera = this.mCommonFragment.getCameraManager().getOpenCamera().getCamera();
                    }

                    Camera.Parameters parameters = camera.getParameters();
                    int maxZoom = parameters.getMaxZoom();
                    int zoom = parameters.getZoom();
                    if(parameters.isZoomSupported()) {
                        ResultPoint[] points = rawResult.getResultPoints();
                        float pointY = points[0].getX();
                        float pointX = points[0].getY();
                        float point2Y = points[2].getX();
                        float point2X = points[2].getY();
                        int len = Math.max((int)Math.abs(pointX - point2X), (int)Math.abs(pointY - point2Y));
                        Message message;
                        if(len <= rect.width() / 4 && isInRect(pointX, pointY, point2X, point2Y, rect)) {
                            ++zoom;
                            this.mChange = 1;
                            if(zoom > maxZoom) {
                                zoom = maxZoom;
                            }

                            if(zoom < maxZoom / 4) {
                                message = new Message();
                                message.what = ZOOM_CHANGE;
                                message.arg1 = zoom;
                                message.arg2 = maxZoom;
                                message.obj = camera;
                                this.sendMessageDelayed(message, 100L);
                            } else {
                                parameters.setZoom(zoom);
                                camera.setParameters(parameters);
                                message = Message.obtain(handler, DECODE_FAILED);
                                message.sendToTarget();
                            }
                        } else {
                            message = Message.obtain(handler, DECODE_SUCCEEDED, rawResult);
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
        } else if(handler != null) {
            Message message = Message.obtain(handler, DECODE_FAILED);
            message.sendToTarget();
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

    private static boolean isInRect(float x1, float y1, float x2, float y2, Rect rect) {
        if (rect.left < x1 && x1 < rect.right &&
                rect.left < x2 && x2 < rect.right &&
                rect.top < y1 && y1 < rect.bottom &&
                rect.top < y2 && y2 < rect.bottom) {
            return true;
        }
        return false;
    }

}
