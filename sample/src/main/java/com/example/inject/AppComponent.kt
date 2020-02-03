package com.example.inject

import com.example.MainActivity
import dagger.Component

@Component(modules = [AssistModule::class])
interface AppComponent {
    fun inject(target: MainActivity)
}