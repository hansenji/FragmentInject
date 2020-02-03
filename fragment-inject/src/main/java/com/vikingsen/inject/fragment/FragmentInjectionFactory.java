package com.vikingsen.inject.fragment;

import android.util.Log;

import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;

public class FragmentInjectionFactory extends FragmentFactory {
    private static final String TAG = "FragmentInjection";

    private final Map<Class<?>, FragmentInjectFactory> factories;

    @Inject
    public FragmentInjectionFactory(@NonNull Map<Class<?>, FragmentInjectFactory> factories) {
        if (factories == null) throw new NullPointerException("factories == null");
        this.factories = factories;
    }

    @NonNull
    @Override
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
        Class fragmentClass = loadFragmentClass(classLoader, className);
        FragmentInjectFactory factory = factories.get(fragmentClass);
        if (factory != null) {
            try {
                return factory.create();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return createFragmentAsFallback(classLoader, className);
        }
    }

    private Fragment createFragmentAsFallback(ClassLoader classLoader, String className) {
        Log.w(TAG, "No provider found for class: " + className + ". Using default constructor");
        try {
            return super.instantiate(classLoader, className);
        } catch (Exception e) {
            throw new RuntimeException("Make sure injectable fragment is annotated with @FragmentInject", e);
        }
    }
}
