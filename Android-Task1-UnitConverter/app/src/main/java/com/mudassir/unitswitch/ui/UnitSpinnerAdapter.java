package com.mudassir.unitswitch.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mudassir.unitswitch.R;
import com.mudassir.unitswitch.logic.UnitModel;

import java.util.List;

/**
 * Custom ArrayAdapter for displaying UnitModel items in Spinners
 * with unit name and symbol formatted cleanly.
 */
public class UnitSpinnerAdapter extends ArrayAdapter<UnitModel> {

    private final LayoutInflater inflater;

    public UnitSpinnerAdapter(@NonNull Context context, @NonNull List<UnitModel> units) {
        super(context, R.layout.item_spinner, units);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent, R.layout.item_spinner);
    }

    @NonNull
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent, R.layout.item_spinner_dropdown);
    }

    @NonNull
    private View createView(int position, @Nullable View convertView, @NonNull ViewGroup parent, int layoutRes) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(layoutRes, parent, false);
        }

        TextView textView = view.findViewById(android.R.id.text1);
        UnitModel item = getItem(position);
        if (item != null && textView != null) {
            String name = getContext().getString(item.getNameResId());
            String formatted = name + " (" + item.getSymbol() + ")";
            textView.setText(formatted);
        }

        return view;
    }
}
