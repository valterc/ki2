package com.valterc.ki2.activities.devices.details;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.valterc.ki2.R;
import com.valterc.ki2.data.device.DeviceId;
import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.fragments.IKarooKeyListener;
import com.valterc.ki2.fragments.devices.details.DeviceDetailsFragment;
import com.valterc.ki2.data.action.KarooAction;

public class DeviceDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DeviceId deviceId = getIntent().getParcelableExtra(DeviceId.class.getSimpleName());

        if (deviceId == null) {
            throw new IllegalArgumentException("Device Id was not provided to activity");
        }

        setContentView(R.layout.activity_device_details);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, DeviceDetailsFragment.newInstance(deviceId))
                    .commitNow();
        }

        ExtendedFloatingActionButton buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener((view) -> {
            finish();
        });
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