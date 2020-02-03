package androidx.fragment.app

import androidx.fragment.app.Fragment

abstract class FragmentFactory() {

    abstract fun instantiate(classLoader: ClassLoader, className: String): Fragment
}