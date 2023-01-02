package com.valterc.ki2.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.valterc.ki2.R;
import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.fragments.IKarooKeyListener;

public class SettingsHostFragment extends Fragment implements IKarooKeyListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_host, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public boolean onKarooKeyPressed(KarooKey karooKey) {
        if (karooKey == KarooKey.BACK) {
            return getChildFragmentManager().popBackStackImmediate();
        }

        return false;
    }

}
