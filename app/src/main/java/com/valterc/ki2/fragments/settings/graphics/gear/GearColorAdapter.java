package com.valterc.ki2.fragments.settings.graphics.gear;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;
import com.valterc.ki2.data.connection.ConnectionInfo;
import com.valterc.ki2.data.connection.ConnectionStatus;
import com.valterc.ki2.data.device.BatteryInfo;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.data.preferences.device.DevicePreferencesView;
import com.valterc.ki2.data.shifting.BuzzerType;
import com.valterc.ki2.data.shifting.FrontTeethPattern;
import com.valterc.ki2.data.shifting.RearTeethPattern;
import com.valterc.ki2.data.shifting.ShiftingInfo;
import com.valterc.ki2.data.shifting.ShiftingMode;
import com.valterc.ki2.karoo.Ki2Context;
import com.valterc.ki2.karoo.overlay.view.IOverlayView;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderEntry;
import com.valterc.ki2.karoo.overlay.view.builder.OverlayViewBuilderRegistry;
import com.valterc.ki2.karoo.views.KarooTheme;

import java.util.HashMap;
import java.util.function.Consumer;

import io.hammerhead.sdk.v0.SdkContext;

public class GearColorAdapter extends RecyclerView.Adapter<GearColorViewHolder> {

    private final Context context;
    private final Consumer<String> clickListener;
    private final LayoutInflater layoutInflater;
    private final String[] colorValues;
    private final String[] colorTitles;
    public GearColorAdapter(Context context, Consumer<String> clickListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.layoutInflater = LayoutInflater.from(context);

        this.colorValues = context.getResources().getStringArray(R.array.preference_values_gear_color);
        this.colorTitles = context.getResources().getStringArray(R.array.preference_titles_gear_color);
    }

    @NonNull
    @Override
    public GearColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.view_item_gear_color, parent, false);
        view.setOnClickListener(v -> this.clickListener.accept(colorValues[(int) view.getTag()]));
        return new GearColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GearColorViewHolder holder, int position) {
        HashMap<String, Object> preferences = new HashMap<>();
        preferences.put(context.getString(R.string.preference_gear_color), colorValues[position]);
        PreferencesView preferencesView = new PreferencesView(preferences);

        holder.getRootView().setTag(position);
        holder.getTextViewName().setText(colorTitles[position]);
        holder.getGearsView().setSelectedGearColor(preferencesView.getGearsColor(layoutInflater.getContext(), KarooTheme.UNKNOWN));
    }

    @Override
    public int getItemCount() {
        return colorValues.length;
    }

    public int getItemIndex(String key) {
        for (int i = 0; i < colorValues.length; i++) {
            if (colorValues[i].equals(key)) {
                return i;
            }
        }
        return 0;
    }

}
