package com.example.assignment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MultiSelectionSpinner extends androidx.appcompat.widget.AppCompatSpinner implements DialogInterface.OnMultiChoiceClickListener {
    String[] _items = null;
    boolean[] selection = null;

    ArrayAdapter<String> adapter;

    public MultiSelectionSpinner(Context context) {
        super(context);
        adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
        super.setAdapter(adapter);
    }

    public MultiSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
        super.setAdapter(adapter);
    }

    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (selection != null && which < selection.length) {
            selection[which] = isChecked;
            adapter.clear();
            adapter.add(buildSelectedItemString());
        } else {
            throw new IllegalArgumentException("Argument 'which' is out of bounds.");
        }
    }

    @Override
    public boolean performClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(_items, selection, this);
        builder.show();
        return true;
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException("setAdapter is not supported by MultiSelectSpinner");
    }

    public void setItems(String[] items) {
        _items = items;
        selection = new boolean[_items.length];
        adapter.clear();
        adapter.add(_items[0]);
        Arrays.fill(selection, false);
    }

    public void setItems(List<String> items) {
        _items = items.toArray(new String[items.size()]);
        selection = new boolean[_items.length];
        adapter.clear();
        adapter.add(_items[0]);
        Arrays.fill(selection, false);
    }

    public void setSelection(String[] selectionPass) {
        for (String cell : selectionPass) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].equals(cell)) {
                    selection[j] = true;
                }
            }
        }
    }

    public void setSelection(List<String> selectionPass) {
        for (int i = 0; i < selection.length; i++) {
            selection[i] = false;
        }
        for (String sel : selectionPass) {
            for (int j = 0; j < _items.length; ++j) {
                if (_items[j].equals(sel)) {
                    selection[j] = true;
                }
            }
        }
        adapter.clear();
        adapter.add(buildSelectedItemString());
    }

    public void setSelection(int index) {
        for (int i = 0; i < selection.length; i++) {
            selection[i = i] = false;
        }
        if (index >= 0 && index < selection.length) {
            selection[index] = true;
        } else {
            throw new IllegalArgumentException("Index " + index + " is out of bound!");
        }
        adapter.clear();
        adapter.add(buildSelectedItemString());
    }

    public void setSelection(int[] selectedIndicies) {
        for (int i = 0; i < selection.length; i++) {
            selection[i] = false;
        }
        for (int index : selectedIndicies) {
            if (index >= 0 && index < selection.length) {
                selection[index] = true;
            } else {
                throw new IllegalArgumentException("Index " + index + " is out of bounds!");
            }
        }
        adapter.clear();
        adapter.add(buildSelectedItemString());
    }

    public List<String> getSelectedStrings(){
        List<String> lSelection = new LinkedList<String>();
        for (int i=0; i<_items.length; i++){
            if (selection[i]){
                lSelection.add(_items[i]);
            }
        }
        return lSelection;
    }

    private String buildSelectedItemString(){
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;
        for (int i=0; i<_items.length; ++i){
            if(selection[i]){
                if(foundOne){
                    sb.append("/");
                }
                foundOne = true;
                sb.append(_items[i]);
            }
        }
        return sb.toString();
    }

    public String getSelectedItemsAsString(){
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;
        for (int i=0; i<_items.length; ++i){
            if(selection[i]){
                if(foundOne){
                    sb.append("/");
                }
                foundOne = true;
                sb.append(_items[i]);
            }
        }
        return sb.toString();

    }
}
