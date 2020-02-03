package com.vikingsen.inject.fragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public interface FragmentInjectFactory {
    @NonNull Fragment create();
}
