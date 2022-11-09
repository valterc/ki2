package com.valterc.ki2.activities.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.valterc.ki2.BuildConfig;
import com.valterc.ki2.R;
import com.valterc.ki2.fragments.IKarooKeyListener;
import com.valterc.ki2.data.input.KarooKey;

public class MainActivity extends AppCompatActivity {

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
        buttonBack.setOnClickListener((view) -> finish());
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