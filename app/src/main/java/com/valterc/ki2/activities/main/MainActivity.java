package com.valterc.ki2.activities.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.valterc.ki2.R;
import com.valterc.ki2.fragments.MainFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager2 viewPager2 = findViewById(R.id.viewpager2_main);
        viewPager2.setAdapter(new MainViewPagerAdapter(this));
    }
}