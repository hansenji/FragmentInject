Constructor Injection for Fragments
=======================================

This library is a temporary stop gap until the new android injection frameworks is provided for Dagger2

FragmentInject supports [Dagger2](https://google.github.io/dagger/) and **Androidx Fragments only**.

Download
--------
```groovy
implementation 'com.vikingsen.inject:fragment-inject:1.0.0'
annotationProcessor 'com.vikingsen.inject:fragment-inject-processor:1.0.0' // or `kapt` for Kotlin
```

For Snapshots include the following repository:
```groovy
repositories {
    // ...
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
}
```

License
=======

    Copyright 2020 Jordan Hansen

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
