/*
 * Copyright (C) 2008 ZXing authors
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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;

import java.util.Collection;
import java.util.Map;

import camera.CameraManager;

import static com.sunyy.qrcode.mylibrary.QRFragment.DECODE;
import static com.sunyy.qrcode.mylibrary.QRFragment.DECODE_FAILED;
import static com.sunyy.qrcode.mylibrary.QRFragment.DECODE_SUCCEEDED;
import static com.sunyy.qrcode.mylibrary.QRFragment.QUIT;
import static com.sunyy.qrcode.mylibrary.QRFragment.RESTART_PREVIEW;
import static com.sunyy.qrcode.mylibrary.QRFragment.RETURN_SCAN_RESULT;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {

  private static final String TAG = CaptureActivityHandler.class.getSimpleName();

  private final Activity activity;
  private QRFragment mFragment = null;
  private QRCommonFragment mCommonFragment = null;
  private final DecodeThread decodeThread;
  private State state;
  private final CameraManager cameraManager;

  private enum State {
    PREVIEW,
    SUCCESS,
    DONE
  }

  CaptureActivityHandler(Activity activity,
                         QRFragment fragment,
                         Collection<BarcodeFormat> decodeFormats,
                         Map<DecodeHintType,?> baseHints,
                         String characterSet,
                         CameraManager cameraManager) {
    this.activity = activity;
    this.mFragment = fragment;
    decodeThread = new DecodeThread(fragment, decodeFormats, baseHints, characterSet,
        new ViewfinderResultPointCallback(mFragment.getViewfinderView()));
    decodeThread.start();
    state = State.SUCCESS;

    // Start ourselves capturing previews and decoding.
    this.cameraManager = cameraManager;
    cameraManager.startPreview();
    restartPreviewAndDecode();
  }

  CaptureActivityHandler(Activity activity,
                         QRCommonFragment fragment,
                         Collection<BarcodeFormat> decodeFormats,
                         Map<DecodeHintType,?> baseHints,
                         String characterSet,
                         CameraManager cameraManager) {
    this.activity = activity;
    this.mCommonFragment = fragment;
    decodeThread = new DecodeThread(fragment, decodeFormats, baseHints, characterSet,
            new ViewfinderResultPointCallback(mCommonFragment.getViewfinderView()));
    decodeThread.start();
    state = State.SUCCESS;

    // Start ourselves capturing previews and decoding.
    this.cameraManager = cameraManager;
    cameraManager.startPreview();
    restartPreviewAndDecode();
  }

  @Override
  public void handleMessage(Message message) {
    switch (message.what) {
      case RESTART_PREVIEW:
        restartPreviewAndDecode();
        break;
      case DECODE_SUCCEEDED:
        this.state = State.SUCCESS;
        Bundle bundle = message.getData();
        Bitmap barcode = null;
        float scaleFactor = 1.0F;
        if(bundle != null) {
          byte[] compressedBitmap = bundle.getByteArray("barcode_bitmap");
          if(compressedBitmap != null) {
            barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, (BitmapFactory.Options)null);
            barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
          }

          scaleFactor = bundle.getFloat("barcode_scaled_factor");
        }

        if(this.mFragment != null) {
          this.mFragment.handleDecode((Result)message.obj, barcode, scaleFactor);
        } else if(this.mCommonFragment != null) {
          this.mCommonFragment.handleDecode((Result)message.obj, barcode, scaleFactor);
        }

        break;
      case DECODE_FAILED:
        // We're decoding as fast as possible, so when one decode fails, start another.
        state = State.PREVIEW;
        cameraManager.requestPreviewFrame(decodeThread.getHandler(), DECODE);
        break;
      case RETURN_SCAN_RESULT:
        activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
        activity.finish();
        break;
//      case R.id.launch_product_query:
//        String url = (String) message.obj;
//
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.addFlags(Intents.FLAG_NEW_DOC);
//        intent.setData(Uri.parse(url));
//
//        ResolveInfo resolveInfo =
//            activity.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
//        String browserPackageName = null;
//        if (resolveInfo != null && resolveInfo.activityInfo != null) {
//          browserPackageName = resolveInfo.activityInfo.packageName;
//          LogUtils.d(TAG, "Using browser in package " + browserPackageName);
//        }
//
//        // Needed for default Android browser / Chrome only apparently
//        if (browserPackageName != null) {
//          switch (browserPackageName) {
//            case "com.android.browser":
//            case "com.android.chrome":
//              intent.setPackage(browserPackageName);
//              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//              intent.putExtra(Browser.EXTRA_APPLICATION_ID, browserPackageName);
//              break;
//          }
//        }
//
//        try {
//          activity.startActivity(intent);
//        } catch (ActivityNotFoundException ignored) {
//          LogUtils.w(TAG, "Can't find anything to handle VIEW of URI " + url);
//        }
//        break;
    }
  }

  public void quitSynchronously() {
    state = State.DONE;
    cameraManager.stopPreview();
    Message quit = Message.obtain(decodeThread.getHandler(), QUIT);
    quit.sendToTarget();
    try {
      // Wait at most half a second; should be enough time, and onPause() will timeout quickly
      decodeThread.join(500L);
    } catch (InterruptedException e) {
      // continue
    }

    // Be absolutely sure we don't send any queued up messages
    removeMessages(DECODE_SUCCEEDED);
    removeMessages(DECODE_FAILED);
  }

  private void restartPreviewAndDecode() {
    if (state == State.SUCCESS) {
      state = State.PREVIEW;
      cameraManager.requestPreviewFrame(decodeThread.getHandler(), DECODE);
      if (mFragment != null) {
        mFragment.drawViewfinder();
      } else if (mCommonFragment != null) {
        mCommonFragment.drawViewfinder();
      }

    }
  }

}
