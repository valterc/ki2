package com.valterc.ki2.activities.devices.gearing;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.valterc.ki2.R;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.action.KarooAction;
import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.data.preferences.device.DevicePreferences;
import com.valterc.ki2.fragments.IKarooKeyListener;
import com.valterc.ki2.fragments.devices.gearing.DeviceGearingFragment;

public class DeviceGearingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DeviceId deviceId = getIntent().getParcelableExtra(DeviceId.class.getSimpleName());
        if (deviceId == null) {
            throw new IllegalArgumentException("Device Id was not provided to activity");
        }

        setContentView(R.layout.activity_device_gearing);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, DeviceGearingFragment.newInstance(deviceId))
                    .commitNow();
        }

        ExtendedFloatingActionButton buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(view -> onBackPressed());

        ImageView imageViewIcon = findViewById(R.id.imageview_device_gearing_icon);
        switch (deviceId.getDeviceType()) {
            case SHIMANO_SHIFTING:
                imageViewIcon.setImageResource(R.drawable.ic_di2);
                break;

            case SHIMANO_EBIKE:
                imageViewIcon.setImageResource(R.drawable.ic_steps);
                break;

            case MOCK_SHIFTING:
                imageViewIcon.setImageResource(R.drawable.ic_mock);
                break;

            case UNKNOWN:
            default:
                imageViewIcon.setImageResource(R.drawable.ic_memory);
                break;
        }

        TextView textViewName = findViewById(R.id.textview_device_gearing_name);
        textViewName.setText(new DevicePreferences(this, deviceId).getName());
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        View view = getCurrentFocus();

        if ((view == null) || !view.isClickable() && !(view instanceof Button)) {
            KarooKey karooKey = KarooKey.fromKeyCode(e.getKeyCode());

            if (karooKey != KarooKey.INVALID) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);

                if (fragment instanceof IKarooKeyListener) {
                    if (e.getAction() == KeyEvent.ACTION_UP && e.getRepeatCount() == 0) {
                        boolean result = ((IKarooKeyListener) fragment).onKarooKeyPressed(karooKey);
                        if (result) {
                            return true;
                        }
                    }
                }
            }
        }
        return super.dispatchKeyEvent(e);
    }

}