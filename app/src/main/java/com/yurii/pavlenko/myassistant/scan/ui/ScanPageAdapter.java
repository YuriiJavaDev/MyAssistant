package com.yurii.pavlenko.myassistant.scan.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.ItemScanPageBinding;
import com.yurii.pavlenko.myassistant.scan.model.ScanPage;

import java.util.function.Consumer;

public class ScanPageAdapter extends ListAdapter<ScanPage, ScanPageAdapter.PageViewHolder> {

    private static final DiffUtil.ItemCallback<ScanPage> DIFF = new DiffUtil.ItemCallback<ScanPage>() {
        @Override
        public boolean areItemsTheSame(@NonNull ScanPage oldItem, @NonNull ScanPage newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ScanPage oldItem, @NonNull ScanPage newItem) {
            return oldItem.getPosition() == newItem.getPosition()
                    && oldItem.getImagePath().equals(newItem.getImagePath());
        }
    };

    private final ScanThumbnailLoader thumbnailLoader;
    private final Consumer<ScanPage> onItemClick;

    public ScanPageAdapter(ScanThumbnailLoader thumbnailLoader, Consumer<ScanPage> onItemClick) {
        super(DIFF);
        this.thumbnailLoader = thumbnailLoader;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PageViewHolder(ItemScanPageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    class PageViewHolder extends RecyclerView.ViewHolder {

        private final ItemScanPageBinding binding;

        PageViewHolder(ItemScanPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ScanPage page, int index) {
            binding.pageLabel.setText(binding.getRoot().getContext().getString(R.string.scan_page_label, index + 1));
            Context context = binding.getRoot().getContext();
            binding.pageDetails.setText(context.getString(R.string.scan_page_details_format,
                    context.getString(PaperFormatText.of(page.resolvePaperFormat())), page.getWidth(), page.getHeight()));
            thumbnailLoader.load(binding.pageThumbnail, page.getImagePath());
            binding.getRoot().setOnClickListener(v -> onItemClick.accept(page));
        }
    }
}
