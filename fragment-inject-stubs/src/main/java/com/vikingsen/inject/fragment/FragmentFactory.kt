package com.vikingsen.inject.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory as AndroidFragmentFactory

class FragmentFactory(
) : AndroidFragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        throw RuntimeException("STUB!")
    }
}