package com.smartmobilefactory.epubreader.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import java.io.IOException;

import com.smartmobilefactory.epubreader.EpubScrollDirection;
import com.smartmobilefactory.epubreader.model.EpubFactory;
import com.smartmobilefactory.epubreader.model.EpubFont;
import com.smartmobilefactory.epubreader.model.EpubLocation;
import com.smartmobilefactory.epubreader.sample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        initSettingsContainer();

        ChapterJavaScriptBridge bridge = new ChapterJavaScriptBridge();
        binding.epubView.getSettings().setJavascriptBridge(bridge);
        binding.epubView.getSettings().setCustomChapterScript(bridge.getCustomChapterScripts());
        binding.epubView.getSettings().setFont(EpubFont.fromFontFamiliy("Monospace"));
        try {
            binding.epubView.setScrollDirection(EpubScrollDirection.HORIZONTAL_WITH_VERTICAL_CONTENT);
            binding.epubView.setEpub(EpubFactory.fromUri(this, "file:///android_asset/private/example.epub"), EpubLocation.fromChapter(10));
        } catch (IOException e) {
            e.printStackTrace();
        }

        observeEpub();
    }

    private void initToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu);

        binding.toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_settings:
                    if (binding.settings.getVisibility() == View.VISIBLE) {
                        binding.settings.setVisibility(View.GONE);
                    } else {
                        binding.settings.setVisibility(View.VISIBLE);
                    }
                    return true;
                default:
                    return false;
            }
        });
    }

    private void initSettingsContainer() {
        binding.textSizeSeekbar.setMax(30);
        binding.textSizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.epubView.getSettings().setFontSizeSp(progress + 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.monospace.setOnClickListener(v -> {
            binding.epubView.getSettings().setFont(EpubFont.fromFontFamiliy("Monospace"));
        });

        binding.serif.setOnClickListener(v -> {
            binding.epubView.getSettings().setFont(EpubFont.fromFontFamiliy("Serif"));
        });

        binding.sanSerif.setOnClickListener(v -> {
            binding.epubView.getSettings().setFont(EpubFont.fromFontFamiliy("Sans Serif"));
        });
    }

    private void observeEpub() {
        binding.epubView.currentLocation()
                .doOnNext(xPathLocation -> {
                    Log.d(TAG, "CurrentLocation: " + xPathLocation);
                }).subscribe();
    }
}
