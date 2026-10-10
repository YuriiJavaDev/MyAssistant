package com.yurii.pavlenko.myassistant.scan.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.yurii.pavlenko.myassistant.databinding.ItemScanBinding;
import com.yurii.pavlenko.myassistant.scan.model.ScanItem;

import java.text.DateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class ScanListAdapter extends ListAdapter<ScanItem, ScanListAdapter.ScanViewHolder> {

    private static final DiffUtil.ItemCallback<ScanItem> DIFF = new DiffUtil.ItemCallback<ScanItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull ScanItem oldItem, @NonNull ScanItem newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ScanItem oldItem, @NonNull ScanItem newItem) {
            return oldItem.getTitle().equals(newItem.getTitle());
        }
    };

    private final ScanThumbnailLoader thumbnailLoader;
    private final Consumer<ScanItem> onItemClick;

    public ScanListAdapter(ScanThumbnailLoader thumbnailLoader, Consumer<ScanItem> onItemClick) {
        super(DIFF);
        this.thumbnailLoader = thumbnailLoader;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScanViewHolder(ItemScanBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ScanViewHolder extends RecyclerView.ViewHolder {

        private final ItemScanBinding binding;

        ScanViewHolder(ItemScanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ScanItem item) {
            binding.scanTitle.setText(item.getTitle());
            binding.scanDetails.setText(String.format("%s · %s · %d×%d",
                    item.resolvePaperFormat().getLabel(),
                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(item.getCreatedAt())),
                    item.getWidth(), item.getHeight()));
            thumbnailLoader.load(binding.scanThumbnail, item.getImagePath());
            binding.getRoot().setOnClickListener(v -> onItemClick.accept(item));
        }
    }
}
