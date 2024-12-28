package com.valterc.ki2.activities.update;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.valterc.ki2.R;
import com.valterc.ki2.activities.main.MainActivity;
import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.data.update.OngoingUpdateStateInfo;
import com.valterc.ki2.data.update.ReleaseInfo;
import com.valterc.ki2.fragments.IKarooKeyListener;
import com.valterc.ki2.fragments.update.UpdateFragment;

public class UpdateActivity extends AppCompatActivity {

    private UpdateFragment updateFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null &&
                getIntent().getExtras().containsKey(PackageInstaller.EXTRA_SESSION_ID) &&
                !UpdateActivity.class.getSimpleName().equals(getIntent().getExtras().getString(Activity.class.getSimpleName()))) {

            finish();
            Intent intentReturnFromPackageInstaller = getIntent();
            intentReturnFromPackageInstaller.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            intentReturnFromPackageInstaller.putExtra(Activity.class.getSimpleName(), UpdateActivity.class.getSimpleName());
            startActivity(intentReturnFromPackageInstaller);

            return;
        }

        setContentView(R.layout.activity_update);
        ReleaseInfo releaseInfo = getIntent().getParcelableExtra(ReleaseInfo.class.getSimpleName());
        OngoingUpdateStateInfo ongoingUpdateStateInfo = getIntent().getParcelableExtra(OngoingUpdateStateInfo.class.getSimpleName());

        if (releaseInfo != null) {
            updateFragment = UpdateFragment.newInstance(releaseInfo);
        } else if (ongoingUpdateStateInfo != null) {
            updateFragment = UpdateFragment.newInstance(ongoingUpdateStateInfo);
        } else {
            updateFragment = UpdateFragment.newInstance();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, updateFragment)
                .commitNow();

        ExtendedFloatingActionButton buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener((view) -> {
            finish();

            if (ongoingUpdateStateInfo != null) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        if (getIntent().getExtras() != null &&
                getIntent().getExtras().containsKey(PackageInstaller.EXTRA_SESSION_ID) &&
                updateFragment.isUpdateIntent(getIntent())) {
            updateFragment.onUpdateIntent(getIntent());
        } else if (ongoingUpdateStateInfo == null) {
            updateFragment.checkForUpdates();
        }
    }

    @Override
    public void onBackPressed() {
        if (getIntent() != null &&
                getIntent().getExtras() != null &&
                getIntent().getExtras().containsKey(OngoingUpdateStateInfo.class.getSimpleName())) {
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (updateFragment.isUpdateIntent(intent)) {
            updateFragment.onUpdateIntent(intent);
        }
    }

}
