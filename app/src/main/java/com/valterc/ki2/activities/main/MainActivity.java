package com.valterc.ki2.activities.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
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

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        if ((currentFocus == null) || !currentFocus.isClickable() && !(currentFocus instanceof Button)) {
            KarooKey karooKey = KarooKey.fromKeyCode(e.getKeyCode());

            if (karooKey != KarooKey.NONE) {
                Adapter<?> viewPagerAdapter = viewPager.getAdapter();
                if (viewPagerAdapter != null) {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + viewPagerAdapter.getItemId(viewPager.getCurrentItem()));

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
        }
        return super.dispatchKeyEvent(e);
    }

}