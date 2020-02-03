package com.example

import javax.inject.Inject

class Greeter
@Inject constructor() {
    fun sayHi() = "Hello, James!"
}