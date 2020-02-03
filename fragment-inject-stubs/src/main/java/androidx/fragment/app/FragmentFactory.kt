package androidx.fragment.app

abstract class FragmentFactory() {

    abstract fun instantiate(classLoader: ClassLoader, className: String): Fragment
}