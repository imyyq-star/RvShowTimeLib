package com.imyyq.rvshowtime;

import androidx.annotation.NonNull;

public class ItemEntity implements RvInterface {
    public String text;

    public ItemEntity(String text) {
        this.text = text;
    }

    @NonNull
    @Override
    public String toString() {
        return "ItemEntity{" +
                "text='" + text + '\'' +
                '}';
    }
}
