package com.smartmobilefactory.epubreader.sample;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.smartmobilefactory.epubreader.EpubView;
import com.smartmobilefactory.epubreader.model.Epub;
import com.smartmobilefactory.epubreader.sample.databinding.TableOfContentsItemBinding;
import com.smartmobilefactory.epubreader.utils.BaseDisposableObserver;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import nl.siegmann.epublib.domain.TOCReference;

public class TableOfContentsAdapter extends RecyclerView.Adapter<TableOfContentsAdapter.VH> {

    private int currentTocPosition = -1;

    private final List<TOCReference> tableOfContents = new ArrayList<>();
    private Epub epub;

    private PublishSubject<Integer> jumpToChapter = PublishSubject.create();

    public void bindToEpubView(EpubView epubView) {
        epubView.currentChapter()
                .filter(integer -> epub != null)
                .filter(integer -> epub.equals(epubView.getEpub()))
                .doOnNext(chapter -> {
                    currentTocPosition = epub.getTocPositionForSpinePosition(chapter);
                    notifyDataSetChanged();
                })
                .subscribe(new BaseDisposableObserver<>());
    }

    public void setEpub(Epub epub) {
        this.epub = epub;
        tableOfContents.clear();
        fillToc(epub.getBook().getTableOfContents().getTocReferences());

        notifyDataSetChanged();
    }

    private void fillToc(List<TOCReference> tocReferences) {
        for (TOCReference tocReference : tocReferences) {
            tableOfContents.add(tocReference);
            fillToc(tocReference.getChildren());
        }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(TableOfContentsItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        TOCReference tocReference = tableOfContents.get(position);
        String title = tocReference.getTitle();
        if (title == null) {
            title = "Chapter " + (position + 1);
        }
        holder.binding.chapterTitle.setText(title);

        if (position == currentTocPosition) {
            holder.binding.chapterTitle.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.binding.chapterTitle.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            // search correct chapter (spine position) for toc entry
            int spinePosition = epub.getSpinePositionForTocReference(tocReference);
            if (spinePosition >= 0) {
                jumpToChapter.onNext(spinePosition);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (epub == null) {
            return 0;
        }
        return tableOfContents.size();
    }

    public Observable<Integer> jumpToChapter() {
        return jumpToChapter;
    }

    static class VH extends RecyclerView.ViewHolder {

        TableOfContentsItemBinding binding;

        public VH(TableOfContentsItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
