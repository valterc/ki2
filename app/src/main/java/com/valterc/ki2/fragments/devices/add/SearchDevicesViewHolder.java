package com.valterc.ki2.fragments.devices.add;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;

public class SearchDevicesViewHolder extends RecyclerView.ViewHolder {

    private final ImageView imageViewIcon;
    private final TextView textViewName;
    private final Button buttonAdd;

    public SearchDevicesViewHolder(@NonNull View itemView) {
        super(itemView);

        imageViewIcon = itemView.findViewById(R.id.imageview_add_device_item_icon);
        textViewName = itemView.findViewById(R.id.textview_add_device_item_name);
        buttonAdd = itemView.findViewById(R.id.button_add_device_item_add);
    }

    public ImageView getImageViewIcon() {
        return imageViewIcon;
    }

    public TextView getTextViewName() {
        return textViewName;
    }

    public Button getButtonAdd() {
        return buttonAdd;
    }
}
