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
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;

/**
 * A base class for the Android-specific barcode handlers. These allow the app to polymorphically
 * suggest the appropriate actions for each data type.
 *
 * This class also contains a bunch of utility methods to take common actions like opening a URL.
 * They could easily be moved into a helper object, but it can't be static because the Activity
 * instance is needed to launch an intent.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public abstract class ResultHandler {

  private static final String TAG = ResultHandler.class.getSimpleName();

  private static final int NO_TYPE = -1;

  private final ParsedResult result;
  private final Activity activity;
  private final Result rawResult;

  ResultHandler(Activity activity, ParsedResult result) {
    this(activity, result, null);
  }

  ResultHandler(Activity activity, ParsedResult result, Result rawResult) {
    this.result = result;
    this.activity = activity;
    this.rawResult = rawResult;
  }

  public final ParsedResult getResult() {
    return result;
  }

  final Activity getActivity() {
    return activity;
  }

  /**
   * Indicates how many buttons the derived class wants shown.
   *
   * @return The integer button count.
   */
  public abstract int getButtonCount();

  /**
   * The text of the nth action button.

  /**
   * Create a possibly styled string for the contents of the current barcode.
   *
   * @return The text to be displayed.
   */
  public CharSequence getDisplayContents() {
    String contents = result.getDisplayResult();
    return contents.replace("\r", "");
  }

  /**
   * A string describing the kind of barcode that was found, e.g. "Found contact info".
   *
   * @return The resource ID of the string.
   */
  public abstract int getDisplayTitle();

  /**
   * A convenience method to get the parsed type. Should not be overridden.
   *
   * @return The parsed type, e.g. URI or ISBN
   */
  public final ParsedResultType getType() {
    return result.getType();
  }

  final void openURL(String url) {
    // Strangely, some Android browsers don't seem to register to handle HTTP:// or HTTPS://.
    // Lower-case these as it should always be OK to lower-case these schemes.
    if (url.startsWith("HTTP://")) {
      url = "http" + url.substring(4);
    } else if (url.startsWith("HTTPS://")) {
      url = "https" + url.substring(5);
    }
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    try {
      launchIntent(intent);
    } catch (ActivityNotFoundException ignored) {
      LogUtils.w(TAG, "Nothing available to handle " + intent);
    }
  }

  /**
   * Like {@link #launchIntent(Intent)} but will tell you if it is not handle-able
   * via {@link ActivityNotFoundException}.
   *
   * @throws ActivityNotFoundException if Intent can't be handled
   */
  final void rawLaunchIntent(Intent intent) {
    if (intent != null) {
      intent.addFlags(Intents.FLAG_NEW_DOC);
      LogUtils.d(TAG, "Launching intent: " + intent + " with extras: " + intent.getExtras());
      activity.startActivity(intent);
    }
  }

  /**
   * Like {@link #rawLaunchIntent(Intent)} but will show a user dialog if nothing is available to handle.
   */
  final void launchIntent(Intent intent) {
    try {
      rawLaunchIntent(intent);
    } catch (ActivityNotFoundException ignored) {
      AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      builder.setTitle(R.string.app_name);
      builder.setMessage(R.string.msg_intent_failed);
      builder.setPositiveButton(R.string.button_ok, null);
      builder.show();
    }
  }

  private static void putExtra(Intent intent, String key, String value) {
    if (value != null && !value.isEmpty()) {
      intent.putExtra(key, value);
    }
  }

  final String fillInCustomSearchURL(String text) {
      return text; // ?
  }

  static String formatPhone(String phoneData) {
    // Just collect the call to a deprecated method in one place
    return PhoneNumberUtils.formatNumber(phoneData);
  }

}
