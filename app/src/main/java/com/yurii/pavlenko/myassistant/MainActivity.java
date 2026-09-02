package com.yurii.pavlenko.myassistant;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayoutMediator;
import com.yurii.pavlenko.myassistant.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final String[] titles = new String[]{"Tasks", "Weather", "Currency", "Calc"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(titles[position]);
        }).attach();
    }
}