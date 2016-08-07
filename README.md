support-library of WebP library for Android .

###HOW TO USE

* add to your build.gradle files:

```
dependencies {
    compile 'com.pl:webplibrary:0.1'
}
```

* add to your build.gradle to assign abi:

```
android {    
    defaultConfig {
        ndk {
            abiFilter "armeabi"
        }
    }
}
```

* use **com.pl.webplibrary.BitmapFactory** instead of **android.graphics.BitmapFactory**

