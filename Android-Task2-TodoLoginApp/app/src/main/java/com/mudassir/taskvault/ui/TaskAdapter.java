package com.mudassir.taskvault.ui;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.mudassir.taskvault.R;
import com.mudassir.taskvault.data.Task;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * RecyclerView adapter for displaying tasks with dynamic strikethrough,
 * checkbox state toggling, and delete triggers.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskStatusChangeListener {
        void onStatusChanged(Task task, boolean isCompleted);
    }

    public interface OnTaskDeleteClickListener {
        void onDeleteClicked(Task task);
    }

    private final List<Task> tasks = new ArrayList<>();
    private final OnTaskStatusChangeListener statusChangeListener;
    private final OnTaskDeleteClickListener deleteClickListener;
    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

    public TaskAdapter(OnTaskStatusChangeListener statusChangeListener,
                       OnTaskDeleteClickListener deleteClickListener) {
        this.statusChangeListener = statusChangeListener;
        this.deleteClickListener = deleteClickListener;
    }

    public void setTasks(List<Task> newTasks) {
        this.tasks.clear();
        if (newTasks != null) {
            this.tasks.addAll(newTasks);
        }
        notifyDataSetChanged();
    }

    public Task getTaskAt(int position) {
        if (position >= 0 && position < tasks.size()) {
            return tasks.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        Context context = holder.itemView.getContext();

        // 1. Task Title & Completion Styling
        holder.tvTitle.setText(task.getTitle());
        if (task.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_completed));
            holder.cardTask.setAlpha(0.72f);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            holder.cardTask.setAlpha(1.0f);
        }

        // 2. Task Notes
        if (task.getNotes() != null && !task.getNotes().trim().isEmpty()) {
            holder.tvNotes.setText(task.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        // 3. Created Date
        String formattedDate = dateFormat.format(new Date(task.getCreatedAt()));
        holder.tvDate.setText(formattedDate);

        // 4. CheckBox with listener detached during recycling
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(task.isCompleted());
        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (statusChangeListener != null) {
                statusChangeListener.onStatusChanged(task, isChecked);
            }
        });

        // 5. Delete Button
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClicked(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardTask;
        final CheckBox cbCompleted;
        final TextView tvTitle;
        final TextView tvNotes;
        final TextView tvDate;
        final ImageButton btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTask = itemView.findViewById(R.id.cardTask);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvNotes = itemView.findViewById(R.id.tvTaskNotes);
            tvDate = itemView.findViewById(R.id.tvTaskDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
