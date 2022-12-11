package com.valterc.ki2.fragments.update;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.valterc.ki2.R;

public class UpdateTutorialDialog {

    private final Runnable cancelListener;
    private final Runnable completeListener;
    private final AlertDialog dialog;
    private int step;

    private final View viewTutorial1;
    private final View viewTutorial2;
    private final View viewTutorial3;
    private final View viewTutorial4;

    public UpdateTutorialDialog(Context context, Runnable onCancel, Runnable onComplete) {
        this.cancelListener = onCancel;
        this.completeListener = onComplete;

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);
        final View viewAlertDialog = factory.inflate(R.layout.dialog_update_tutorial, null);
        viewTutorial1 = viewAlertDialog.findViewById(R.id.linearlayout_update_dialog_tutorial_1);
        viewTutorial2 = viewAlertDialog.findViewById(R.id.linearlayout_update_dialog_tutorial_2);
        viewTutorial3 = viewAlertDialog.findViewById(R.id.linearlayout_update_dialog_tutorial_3);
        viewTutorial4 = viewAlertDialog.findViewById(R.id.linearlayout_update_dialog_tutorial_4);
        alert.setView(viewAlertDialog);
        alert.setTitle(R.string.text_update_tutorial);
        alert.setNegativeButton(R.string.text_cancel, (dialog1, which) -> {});
        alert.setPositiveButton(R.string.text_next, (dialog1, which) -> {});
        dialog = alert.create();
    }

    public void show() {
        dialog.show();
        step = 1;
        showStep1();

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(this::onCancelListener);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(this::onNextListener);
    }

    private void onCancelListener(View view) {
        switch (step) {
            case 1:
                dialog.cancel();
                if (cancelListener != null) {
                    cancelListener.run();
                }
                return;

            case 2:
                showStep1();
                break;

            case 3:
                showStep2();
                break;

            case 4:
                showStep3();
                break;
        }

        step--;
    }

    private void onNextListener(View view) {
        switch (step) {
            case 1:
                showStep2();
                break;

            case 2:
                showStep3();
                break;

            case 3:
                showStep4();
                break;

            case 4:
                dialog.dismiss();
                if (completeListener != null) {
                    completeListener.run();
                }
                break;
        }

        step++;
    }

    private void showStep1() {
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText(R.string.text_cancel);
        viewTutorial1.setVisibility(View.VISIBLE);
        viewTutorial2.setVisibility(View.GONE);
        viewTutorial3.setVisibility(View.GONE);
        viewTutorial4.setVisibility(View.GONE);
    }

    private void showStep2() {
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText(R.string.text_back);
        viewTutorial1.setVisibility(View.GONE);
        viewTutorial2.setVisibility(View.VISIBLE);
        viewTutorial3.setVisibility(View.GONE);
        viewTutorial4.setVisibility(View.GONE);
    }

    private void showStep3() {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(R.string.text_next);
        viewTutorial1.setVisibility(View.GONE);
        viewTutorial2.setVisibility(View.GONE);
        viewTutorial3.setVisibility(View.VISIBLE);
        viewTutorial4.setVisibility(View.GONE);
    }

    private void showStep4() {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(R.string.text_start_update);
        viewTutorial1.setVisibility(View.GONE);
        viewTutorial2.setVisibility(View.GONE);
        viewTutorial3.setVisibility(View.GONE);
        viewTutorial4.setVisibility(View.VISIBLE);
    }

}
