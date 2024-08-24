package com.valterc.ki2.fragments.settings.graphics.gear;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.valterc.ki2.R;
import com.valterc.ki2.data.preferences.PreferencesView;
import com.valterc.ki2.karoo.views.KarooTheme;

import java.util.HashMap;
import java.util.function.Consumer;

public class AccentColorAdapter extends RecyclerView.Adapter<AccentColorViewHolder> {

    private final Context context;
    private final Consumer<String> clickListener;
    private final LayoutInflater layoutInflater;
    private final String[] colorValues;
    private final String[] colorTitles;
    public AccentColorAdapter(Context context, Consumer<String> clickListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.layoutInflater = LayoutInflater.from(context);

        this.colorValues = context.getResources().getStringArray(R.array.preference_values_accent_color);
        this.colorTitles = context.getResources().getStringArray(R.array.preference_titles_accent_color);
    }

    @NonNull
    @Override
    public AccentColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.view_item_accent_color, parent, false);
        view.setOnClickListener(v -> this.clickListener.accept(colorValues[(int) view.getTag()]));
        return new AccentColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccentColorViewHolder holder, int position) {
        HashMap<String, Object> preferences = new HashMap<>();
        preferences.put(context.getString(R.string.preference_accent_color), colorValues[position]);
        PreferencesView preferencesView = new PreferencesView(preferences);

        holder.getRootView().setTag(position);
        holder.getTextViewName().setText(colorTitles[position]);
        holder.getGearsView().setSelectedGearColor(preferencesView.getAccentColor(layoutInflater.getContext(), KarooTheme.UNKNOWN));
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
