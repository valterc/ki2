package com.valterc.ki2.activities.devices;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.valterc.ki2.R;
import com.valterc.ki2.fragments.IKarooKeyListener;
import com.valterc.ki2.fragments.devices.add.AddDeviceFragment;
import com.valterc.ki2.karoo.input.KarooKey;

public class AddDeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, AddDeviceFragment.newInstance())
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

            if (karooKey != KarooKey.NONE) {
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