package com.yurii.pavlenko.myassistant.scan.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.ItemScanBinding;
import com.yurii.pavlenko.myassistant.scan.model.ScanDocumentSummary;

import java.text.DateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class ScanListAdapter extends ListAdapter<ScanDocumentSummary, ScanListAdapter.DocumentViewHolder> {

    private static final DiffUtil.ItemCallback<ScanDocumentSummary> DIFF = new DiffUtil.ItemCallback<ScanDocumentSummary>() {
        @Override
        public boolean areItemsTheSame(@NonNull ScanDocumentSummary oldItem, @NonNull ScanDocumentSummary newItem) {
            return oldItem.document.getId() == newItem.document.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ScanDocumentSummary oldItem, @NonNull ScanDocumentSummary newItem) {
            return oldItem.document.getTitle().equals(newItem.document.getTitle())
                    && oldItem.pageCount == newItem.pageCount
                    && String.valueOf(oldItem.coverPath).equals(String.valueOf(newItem.coverPath));
        }
    };

    private final ScanThumbnailLoader thumbnailLoader;
    private final Consumer<ScanDocumentSummary> onItemClick;

    public ScanListAdapter(ScanThumbnailLoader thumbnailLoader, Consumer<ScanDocumentSummary> onItemClick) {
        super(DIFF);
        this.thumbnailLoader = thumbnailLoader;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DocumentViewHolder(ItemScanBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class DocumentViewHolder extends RecyclerView.ViewHolder {

        private final ItemScanBinding binding;

        DocumentViewHolder(ItemScanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ScanDocumentSummary item) {
            binding.scanTitle.setText(item.document.getTitle());
            binding.scanDetails.setText(String.format("%s · %s",
                    binding.getRoot().getResources().getQuantityString(R.plurals.scan_pages, item.pageCount, item.pageCount),
                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(item.document.getCreatedAt()))));
            thumbnailLoader.load(binding.scanThumbnail, item.coverPath);
            binding.getRoot().setOnClickListener(v -> onItemClick.accept(item));
        }
    }
}
