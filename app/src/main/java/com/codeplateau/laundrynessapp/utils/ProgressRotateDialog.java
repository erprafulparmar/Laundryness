package com.codeplateau.laundrynessapp.utils;

import android.app.Activity;
import android.app.ProgressDialog;

public class ProgressRotateDialog {
    private Activity mContext;
    private ProgressDialog progressDialog;

    public ProgressRotateDialog(Activity _mContext) {
        this.mContext = _mContext;
        progressDialog = new ProgressDialog(mContext);
    }

    public void showProgressDialog() {
        if (progressDialog != null) {
            progressDialog.setMessage("Please wait a moment");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    public void showProgressDialog(String msg) {
        if (progressDialog != null) {
            progressDialog.setMessage(msg);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    public void dismissDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
