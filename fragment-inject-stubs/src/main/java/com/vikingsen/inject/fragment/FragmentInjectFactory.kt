package com.vikingsen.inject.fragment

import androidx.fragment.app.Fragment

interface FragmentInjectFactory {
    fun create(): Fragment
}