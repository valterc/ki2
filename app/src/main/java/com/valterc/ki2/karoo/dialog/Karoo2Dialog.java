package com.valterc.ki2.karoo.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.valterc.ki2.R;

public class Karoo2Dialog {

    private final AlertDialog dialog;

    public Karoo2Dialog(Activity activity){
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        LayoutInflater factory = LayoutInflater.from(activity);
        final View viewAlertDialog = factory.inflate(R.layout.dialog_karoo2, null);
        alert.setView(viewAlertDialog);
        alert.setTitle(R.string.text_not_compatible);
        alert.setPositiveButton(R.string.text_close, (dialog1, which) -> activity.finish());
        alert.setCancelable(false);
        dialog = alert.create();
    }

    public void show(){
        dialog.show();
    }

}
