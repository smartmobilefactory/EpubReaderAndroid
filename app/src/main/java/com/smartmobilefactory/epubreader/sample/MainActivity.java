package com.smartmobilefactory.epubreader.sample;

import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.SeekBar;

import com.smartmobilefactory.epubreader.EpubScrollDirection;
import com.smartmobilefactory.epubreader.model.Epub;
import com.smartmobilefactory.epubreader.model.EpubFont;
import com.smartmobilefactory.epubreader.model.EpubLocation;
import com.smartmobilefactory.epubreader.sample.databinding.ActivityMainBinding;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static Single<Epub> epubSingle;

    private static final String TAG = MainActivity.class.getSimpleName();

    private TableOfContentsAdapter tableOfContentsAdapter;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableStrictMode();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        }

        initToolbar();
        initSettingsContainer();

        ChapterJavaScriptBridge bridge = new ChapterJavaScriptBridge();
        binding.epubView.getSettings().setJavascriptBridge(bridge);
        binding.epubView.getSettings().setCustomChapterScript(bridge.getCustomChapterScripts());
        binding.epubView.getSettings().setFont(EpubFont.fromFontFamily("Monospace"));
        binding.epubView.setScrollDirection(EpubScrollDirection.HORIZONTAL_WITH_VERTICAL_CONTENT);

        tableOfContentsAdapter = new TableOfContentsAdapter();
        tableOfContentsAdapter.bindToEpubView(binding.epubView);
        binding.contentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.contentsRecyclerView.setAdapter(tableOfContentsAdapter);

        tableOfContentsAdapter.jumpToChapter()
                .doOnNext(chapter -> {
                    binding.drawerLayout.closeDrawer(Gravity.START);
                    binding.epubView.gotoLocation(EpubLocation.fromChapter(chapter));
                })
                .subscribe();

        loadEpub().doOnSuccess(epub -> {
            binding.epubView.setEpub(epub);
            tableOfContentsAdapter.setEpub(epub);
            if (savedInstanceState == null) {
                binding.epubView.gotoLocation(EpubLocation.fromChapter(10));
            }
        }).subscribe();

        observeEpub();
    }

    Single<Epub> loadEpub() {
        if (epubSingle == null) {
            Application application = getApplication();
            epubSingle = Single.fromCallable(() -> Epub.fromUri(application, "file:///android_asset/The Silver Chair.epub"))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .cache();
        }
        return epubSingle;
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

        binding.toolbar.setNavigationOnClickListener(v -> {
            binding.drawerLayout.openDrawer(Gravity.START);
        });
    }

    private void initSettingsContainer() {

        // TEXT SIZE

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

        // DISPLAY FONT
        binding.diplomata.setOnClickListener(v -> {
            binding.epubView.getSettings().setFont(EpubFont.fromUri("DiplomataSC", "file:///android_asset/fonts/Diplomata_SC/DiplomataSC-Regular.ttf"));
        });

        binding.monospace.setOnClickListener(v -> {
            binding.epubView.getSettings().setFont(EpubFont.fromFontFamily("Monospace"));
        });

        binding.serif.setOnClickListener(v -> {
            binding.epubView.getSettings().setFont(EpubFont.fromFontFamily("Serif"));
        });

        binding.sanSerif.setOnClickListener(v -> {
            binding.epubView.getSettings().setFont(EpubFont.fromFontFamily("Sans Serif"));
        });

        // DISPLAY STRATEGY

        binding.horizontalVerticalContent.setOnClickListener(v -> {
            binding.epubView.setScrollDirection(EpubScrollDirection.HORIZONTAL_WITH_VERTICAL_CONTENT);
        });

        binding.verticalVerticalContent.setOnClickListener(v -> {
            binding.epubView.setScrollDirection(EpubScrollDirection.VERTICAL_WITH_VERTICAL_CONTENT);
        });

        binding.singleChapterVertical.setOnClickListener(v -> {
            binding.epubView.setScrollDirection(EpubScrollDirection.SINGLE_CHAPTER_VERTICAL);
        });

    }

    private void observeEpub() {
        binding.epubView.currentLocation()
                .doOnNext(xPathLocation -> {
                    Log.d(TAG, "CurrentLocation: " + xPathLocation);
                }).subscribe();

        binding.epubView.currentChapter()
                .doOnNext(chapter -> {
                    Log.d(TAG, "CurrentChapter: " + chapter);
                }).subscribe();
    }

    private void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
    }

}
