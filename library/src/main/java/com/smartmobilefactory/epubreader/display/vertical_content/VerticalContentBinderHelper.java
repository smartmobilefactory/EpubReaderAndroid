package com.smartmobilefactory.epubreader.display.vertical_content;

import android.animation.Animator;
import android.view.View;
import android.widget.SeekBar;

import java.util.concurrent.TimeUnit;

import com.smartmobilefactory.epubreader.databinding.ItemEpubVerticalContentBinding;
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class VerticalContentBinderHelper {

    public static Disposable bind(ItemEpubVerticalContentBinding binding) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(bindSeekbar(binding));
        compositeDisposable.add(bindProgressBar(binding));

        return compositeDisposable;
    }

    private static Disposable bindProgressBar(ItemEpubVerticalContentBinding binding) {
        return binding.webview.isReady()
                .doOnNext(isReady -> {
                    if (isReady) {
                        binding.seekbar.setVisibility(View.INVISIBLE);
                        binding.progressBar.setVisibility(View.GONE);
                    } else {
                        binding.seekbar.setVisibility(View.GONE);
                        binding.progressBar.setVisibility(View.VISIBLE);
                    }
                }).subscribeWith(new BaseDisposableObserver<>());
    }

    private static Disposable bindSeekbar(ItemEpubVerticalContentBinding binding) {
        binding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (binding.webview.getProgress() == 100) {
                        binding.webview.scrollTo(0, progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return binding.webview.verticalScrollState()
                .doOnDispose(() -> {
                    binding.seekbar.setOnSeekBarChangeListener(null);
                })
                .doOnNext(scrollState -> {
                    if (binding.seekbar.getVisibility() == View.INVISIBLE) {
                        animateSeekbar(binding.seekbar, true);
                    }
                    binding.seekbar.setMax(scrollState.maxTop());
                    binding.seekbar.setProgress(scrollState.top());
                })
                .debounce(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(scrollState -> {
                    if (binding.seekbar.getVisibility() == View.VISIBLE) {
                        animateSeekbar(binding.seekbar, false);
                    }
                })
                .subscribeWith(new BaseDisposableObserver<>());
    }

    private static void animateSeekbar(View seekbar, boolean fadeIn) {
        if (fadeIn) {
            seekbar.setAlpha(0);
            seekbar.setVisibility(View.VISIBLE);
            seekbar.animate().alpha(1).setListener(null).start();
        } else {
            seekbar.animate().alpha(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    seekbar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
    }


}
