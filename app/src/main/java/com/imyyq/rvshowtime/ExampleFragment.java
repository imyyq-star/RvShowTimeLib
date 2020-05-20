package com.imyyq.rvshowtime;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.imyyq.rvshowtime.databinding.FragmentExampleBinding;
import com.imyyq.showtime.RvShowTimeInterface;

public class ExampleFragment extends Fragment implements RvShowTimeInterface {
    private FragmentExampleBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExampleBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.i("ExampleFragment", "commonLog - onActivityCreated: "+getFragmentManager());
        RvUtil.initRv(getFragmentManager(), this, binding.rv);
    }
}
