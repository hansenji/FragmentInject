package com.example.inject

import androidx.fragment.app.FragmentFactory
import com.vikingsen.inject.fragment.FragmentInjectionFactory
import com.vikingsen.inject.fragment.FragmentModule
import dagger.Binds
import dagger.Module

@FragmentModule
@Module(includes = [FragmentInject_AssistModule::class])
abstract class AssistModule {

    @Binds
    abstract fun bindFragmentFactory(fragmentInjectionFactory: FragmentInjectionFactory): FragmentFactory
}