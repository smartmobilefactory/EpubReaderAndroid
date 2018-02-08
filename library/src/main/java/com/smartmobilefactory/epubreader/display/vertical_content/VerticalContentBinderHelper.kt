package com.smartmobilefactory.epubreader.display.vertical_content

import android.animation.Animator
import android.view.View
import android.widget.SeekBar

import java.util.concurrent.TimeUnit

import com.smartmobilefactory.epubreader.display.binding.ItemEpubVerticalContentBinding
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

internal object VerticalContentBinderHelper {

    fun bind(binding: ItemEpubVerticalContentBinding): Disposable {
        val compositeDisposable = CompositeDisposable()

        compositeDisposable.add(bindSeekbar(binding))
        compositeDisposable.add(bindProgressBar(binding))

        return compositeDisposable
    }

    private fun bindProgressBar(binding: ItemEpubVerticalContentBinding): Disposable {
        return binding.webview.isReady()
                .doOnNext { isReady ->
                    if (isReady) {
                        binding.seekbar.visibility = View.INVISIBLE
                        binding.progressBar.visibility = View.GONE
                    } else {
                        binding.seekbar.visibility = View.GONE
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }.subscribeWith(BaseDisposableObserver())
    }

    private fun bindSeekbar(binding: ItemEpubVerticalContentBinding): Disposable {
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (binding.webview.progress == 100) {
                        binding.webview.scrollTo(0, progress)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        return binding.webview.verticalScrollState()
                .doOnDispose { binding.seekbar.setOnSeekBarChangeListener(null) }
                .doOnNext { scrollState ->
                    if (binding.seekbar.visibility == View.INVISIBLE) {
                        animateSeekbar(binding.seekbar, true)
                    }
                    binding.seekbar.max = scrollState.maxTop
                    binding.seekbar.progress = scrollState.top
                }
                .debounce(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    if (binding.seekbar.visibility == View.VISIBLE) {
                        animateSeekbar(binding.seekbar, false)
                    }
                }
                .subscribeWith(BaseDisposableObserver())
    }

    private fun animateSeekbar(seekbar: View, fadeIn: Boolean) {
        if (fadeIn) {
            seekbar.alpha = 0f
            seekbar.visibility = View.VISIBLE
            seekbar.animate().alpha(1f).setListener(null).start()
        } else {
            seekbar.animate().alpha(0f).setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                    seekbar.visibility = View.INVISIBLE
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
        }
    }


}
