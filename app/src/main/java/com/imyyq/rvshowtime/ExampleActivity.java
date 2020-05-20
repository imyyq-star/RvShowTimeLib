package com.imyyq.rvshowtime;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.imyyq.rvshowtime.databinding.ActivityExampleBinding;
import com.imyyq.showtime.RvShowTimeInterface;

public class ExampleActivity extends AppCompatActivity implements RvShowTimeInterface {
    private ActivityExampleBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExampleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        RvUtil.initRv(this, binding.rv);
    }
}
