support-library of WebP decode library for Android .

###HOW TO USE

* add to your build.gradle files:

```
dependencies {
    compile 'com.pl:webplibrary:0.2'
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

