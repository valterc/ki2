package com.valterc.ki2.activities.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.valterc.ki2.BuildConfig;
import com.valterc.ki2.R;
import com.valterc.ki2.data.input.KarooKey;
import com.valterc.ki2.data.update.UpdateStateStore;
import com.valterc.ki2.fragments.IKarooKeyListener;
import com.valterc.ki2.fragments.update.overlay.UpdateOverlayFragment;

public class MainActivity extends AppCompatActivity {

    private static final int TIME_MS_CHECK_FOR_UPDATES = 3500;
    private static final int KEY_EVENT_SOURCE_OWN = 0x6667;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private BaseInputConnection baseInputConnection;
    private boolean longPressOngoing;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        baseInputConnection = new BaseInputConnection(findViewById(android.R.id.content), true);

        TextView textViewVersion = findViewById(R.id.textview_main_version);
        if (BuildConfig.DEBUG) {
            textViewVersion.setText(BuildConfig.BUILD_TYPE);
        } else {
            textViewVersion.setText(BuildConfig.VERSION_NAME);
        }

        viewPager = findViewById(R.id.viewpager2_main);
        viewPager.setAdapter(new MainViewPagerAdapter(this));

        TabLayout tabLayout = findViewById(R.id.tabs);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

        ExtendedFloatingActionButton buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener((view) ->
        {
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        });

        if (UpdateStateStore.shouldAutomaticallyCheckForUpdatesInApp(this)) {
            handler.postDelayed(() -> {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.anim_enter_top, R.anim.anim_exit_top, R.anim.anim_enter_top, R.anim.anim_exit_top);
                transaction.replace(R.id.container_update_overlay, UpdateOverlayFragment.newInstance());
                transaction.commitNow();
            }, TIME_MS_CHECK_FOR_UPDATES);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        View currentFocus = getCurrentFocus();
        KarooKey karooKey = KarooKey.fromKeyCode(e.getKeyCode());
        Adapter<?> viewPagerAdapter = viewPager.getAdapter();

        if (e.getSource() == KEY_EVENT_SOURCE_OWN) {
            return super.dispatchKeyEvent(e);
        }

        if (viewPagerAdapter != null) {
            if (handleFocusNavigation(e, karooKey)) {
                return true;
            }

            if (handleKeyEvent(e, currentFocus, karooKey, viewPagerAdapter)) {
                return true;
            }
        }

        return super.dispatchKeyEvent(e);
    }

    private boolean handleFocusNavigation(KeyEvent e, KarooKey karooKey) {
        if (karooKey == KarooKey.TOP_LEFT || karooKey == KarooKey.TOP_RIGHT) {
            if (e.getAction() == KeyEvent.ACTION_DOWN) {
                if (e.getRepeatCount() > 0) {
                    if (karooKey == KarooKey.TOP_LEFT) {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                    } else {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                    }
                    longPressOngoing = true;
                } else {
                    longPressOngoing = false;
                }
            } else if (e.getAction() == KeyEvent.ACTION_UP) {
                if (!longPressOngoing) {
                    KeyEvent ke = KeyEvent.changeAction(e, KeyEvent.ACTION_DOWN);
                    ke.setSource(KEY_EVENT_SOURCE_OWN);
                    handler.post(() -> baseInputConnection.sendKeyEvent(ke));
                }
                longPressOngoing = false;
            }
            return true;
        }
        return false;
    }

    private boolean handleKeyEvent(KeyEvent e, View currentFocus, KarooKey karooKey, Adapter<?> viewPagerAdapter) {
        if (karooKey != KarooKey.INVALID &&
                (karooKey == KarooKey.BOTTOM_LEFT
                        || currentFocus == null
                        || !currentFocus.isClickable())) {

            Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + viewPagerAdapter.getItemId(viewPager.getCurrentItem()));
            if (fragment instanceof IKarooKeyListener) {
                if (e.getAction() == KeyEvent.ACTION_UP && e.getRepeatCount() == 0) {
                    return ((IKarooKeyListener) fragment).onKarooKeyPressed(karooKey);
                }
            }
        }

        return false;
    }

}