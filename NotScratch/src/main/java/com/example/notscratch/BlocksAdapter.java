package com.example.notscratch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.notscratch.R;
import com.example.notscratch.Block;
import java.util.List;

public class BlocksAdapter extends RecyclerView.Adapter<BlocksAdapter.BlockViewHolder> {
    private final List<Block> blocks;
    private final OnBlockInteractionListener interactionListener;

    public interface OnBlockInteractionListener {
        void onBlockMoved(int from, int to);
        void onBlockEdited(int position);
        void onBlockDeleted(int position);
    }

    public BlocksAdapter(List<Block> blocks, OnBlockInteractionListener interactionListener) {
        this.blocks = blocks;
        this.interactionListener = interactionListener;
    }

    @NonNull
    @Override
    public BlockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_block, parent, false);
        return new BlockViewHolder(view, interactionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockViewHolder holder, int position) {
        Block block = blocks.get(position);
        holder.bind(block);
    }

    @Override
    public int getItemCount() {
        return blocks.size();
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= blocks.size() ||
                toPosition < 0 || toPosition >= blocks.size()) {
            return;
        }

        Block movedItem = blocks.remove(fromPosition);
        blocks.add(toPosition, movedItem);
        notifyItemMoved(fromPosition, toPosition);

        if (interactionListener != null) {
            interactionListener.onBlockMoved(fromPosition, toPosition);
        }
    }

    public void removeItem(int position) {
        if (position < 0 || position >= blocks.size()) {
            return;
        }

        blocks.remove(position);
        notifyItemRemoved(position);

        if (interactionListener != null) {
            interactionListener.onBlockDeleted(position);
        }
    }

    static class BlockViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvBlockType;
        private final TextView tvBlockText;
        private final ImageButton btnDelete;

        BlockViewHolder(View itemView, OnBlockInteractionListener listener) {
            super(itemView);
            tvBlockType = itemView.findViewById(R.id.tvBlockType);
            tvBlockText = itemView.findViewById(R.id.tvBlockText);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            setupListeners(listener);
        }

        void bind(Block block) {
            tvBlockType.setText(block.getType().getDisplayName());
            tvBlockText.setText(block.getCode());

            if (block.getDescription() != null && !block.getDescription().isEmpty()) {
                tvBlockType.append(": " + block.getDescription());
            }
        }

        private void setupListeners(OnBlockInteractionListener listener) {
            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBlockDeleted(position);
                }
            });

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBlockEdited(position);
                }
            });
        }
    }
}