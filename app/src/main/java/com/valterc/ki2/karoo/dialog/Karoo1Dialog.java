package com.valterc.ki2.karoo.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.valterc.ki2.R;

public class Karoo1Dialog {

    private final AlertDialog dialog;

    public Karoo1Dialog(Context context) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);
        final View viewAlertDialog = factory.inflate(R.layout.dialog_karoo1, null);
        alert.setView(viewAlertDialog);
        alert.setTitle(R.string.text_welcome_to_ki2);
        alert.setPositiveButton(android.R.string.ok, (dialog1, which) -> { });
        dialog = alert.create();
    }

    public void show() {
        dialog.show();
    }
}
