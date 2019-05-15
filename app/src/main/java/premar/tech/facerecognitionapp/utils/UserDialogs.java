package premar.tech.facerecognitionapp.utils;

import android.app.Activity;
import android.graphics.Color;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UserDialogs {

    private static SweetAlertDialog pDialog;
    private static SweetAlertDialog alertDialog;

    public static SweetAlertDialog getProgressDialog(final Activity context, final String message,
                                                      final String hexColor) {

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor(hexColor));
                pDialog.setTitleText(message);
                pDialog.setCancelable(false);
            }
        });
        return pDialog;
    }

    public static SweetAlertDialog getProgressDialog(Activity context, String message) {
        return getProgressDialog( context,  message,  "#A5DC86");
    }

    public static SweetAlertDialog showProgressDialog(final Activity context, final String message,
                                                      final String hexColor) {

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pDialog = getProgressDialog(context, message, hexColor);
                pDialog.show();
            }
        });
        return pDialog;
    }

    public static SweetAlertDialog showProgressDialog(Activity context, String message) {
        return showProgressDialog( context,  message,  "#A5DC86");
    }



    public static void hideProgressDialog(final Activity context) {

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (pDialog != null && pDialog.isShowing()) {
                    pDialog.dismiss();
                }
            }
        });
    }

    public static SweetAlertDialog getAlertDialog(final Activity context, final String message,
                                                     final String hexColor) {

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
                alertDialog.setTitleText(message);
                alertDialog.setConfirmText("Dismiss");
                alertDialog.setCancelable(false);
            }
        });
        return alertDialog;
    }

    public static SweetAlertDialog getAlertDialog(Activity context, String message) {
        return getAlertDialog( context,  message,  "#A5DC86");
    }

    public static SweetAlertDialog showAlertDialog(final Activity context, final String message,
                                                      final String hexColor) {

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog = getAlertDialog(context, message, hexColor);
                alertDialog.show();
            }
        });
        return alertDialog;
    }

    public static SweetAlertDialog showAlertDialog(Activity context, String message) {
        return showAlertDialog( context,  message,  "#A5DC86");
    }

    public static void hideAlertDialog(final Activity context) {

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            }
        });
    }
}