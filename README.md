# MaterialSeekArc


[![CI](https://github.com/simonebortolin/MaterialSeekArc/actions/workflows/ci.yml/badge.svg)](https://github.com/simonebortolin/MaterialSeekArc/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.simonebortolin/MaterialSeekArc)](https://central.sonatype.com/artifact/io.github.simonebortolin/MaterialSeekArc)
[![](https://jitpack.io/v/simonebortolin/MaterialSeekArc.svg)](https://jitpack.io/#simonebortolin/MaterialSeekArc)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg)](https://developer.android.com/about/versions/marshmallow)

Android 3rd party library to make implementing a MaterialSeekArc.
So what the heck is a SeekArc? Essentially it’s a SeekBar that has been wrapped around a circle. It acts like a SeekBar and generally has the same settings. You can even add a Listener to it as you would with a SeekBar. So if its in a circle why not call it a SeekCircle? The answer is that the SeekArc does have a few more setting than the SeekBar one of these is the sweepAngle, which means that the SeekArc doesn’t have to be drawn as a full circle, and can be drawn as a circle, semi-circle  or quarter arc or whatever you like. Supports min SDK 23. 

<img src="art/Screenshot_1.png" alt="" width="200px">
<img src="art/Screenshot_2.png" alt="" width="200px">
<img src="art/Screenshot_3.png" alt="" width="200px">
<img src="art/Screenshot_5.png" alt="" width="200px">

# Using the SeekArc

Using the SeekArc is straightforward. There are a number of settings from sweep angle to rotation that can be set. One of the less obvious settings is the radius or diameter of the SeekArc. When I developed the SeekArc I thought about adding an attribute for defining the diameter of the circle. In the end I decided against this. I thought a more flexible approach would be to allow the SeekArc to expand to the size of  its container layout. This way the SeekArc can grow in size with a layout that has widths or heights of match parent. Also it is still possible to set the SeekArc to a specific size by setting the container layouts width and height to specific dp values. This sizing approach gives the best of both worlds. To further adjust how the arc fits in its container a padding attribute can also be used.

To help with understanding how to use the SeeekArc I’ve put together a sample app with a number of controls that can be used to adjust the attributes of the SeekArc. This is by far the best way to get a good understanding of how to use the SeekArc. From the sample app you’ll see that it is possible to set attributes such as:

* Sweep angle
* Rotation
* Clockwise (which way do you want the progress to increase clockwise/anticlockwise),
* Arc and progress width
* Rounded corners 
* Touch inside enable/disable.
* Enabled - enables/disables touch and the thumbnail 

## Installation

The library is published on Maven Central, add the dependency (remember to check for latest release)

```groovy
    dependencies {
        implementation 'io.github.simonebortolin:MaterialSeekArc:2.2.1'
    }
```

It is also available on [JitPack](https://jitpack.io/#simonebortolin/MaterialSeekArc) as `com.github.simonebortolin:MaterialSeekArc`
(add `maven { url "https://jitpack.io" }` to the repositories in `settings.gradle`) and on
[GitHub Packages](https://github.com/simonebortolin/MaterialSeekArc/packages) as `io.github.simonebortolin:materialseekarc`, which requires a GitHub token to download.

The Javadoc of the latest release is on [GitHub Pages](https://simonebortolin.github.io/MaterialSeekArc/).

Every release also ships the `.aar`, the Javadoc `.jar` and the sample app `.apk`, with their build provenance attestation,
on the [Releases page](https://github.com/simonebortolin/MaterialSeekArc/releases).

## Use

Simply add the View to your layout (There are several custom attributes) 

        <com.simonebortolin.seekarc.MaterialSeekArc
            android:id="@+id/seekArc"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:padding="30dp"
            seekarc:rotation="180"
            seekarc:startAngle="30"
            seekarc:sweepAngle="300"
            seekarc:touchInside="true" />
            
            
# Building & releasing

The project builds with Gradle 9, Android Gradle Plugin 9 and JDK 17+:

```sh
./gradlew :MaterialSeekArc:assembleRelease :app:assembleDebug
```

The [CI workflow](.github/workflows/ci.yml) builds the library and the sample app on every push to `master` and on every pull request. On `master`, when `VERSION_NAME` in `gradle.properties` doesn't match an existing tag, it creates the tag and a GitHub release with the AAR, the sample APK and their checksums. The sample APK is signed with the release key when the `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` and `ANDROID_KEY_PASSWORD` secrets are set; otherwise the debug APK is published. Dependencies and actions are kept up to date by Dependabot.

# Recent changes 

## Version 2.1.0

Build updated to Gradle 9, AGP 9 and Kotlin 2, AndroidX and Material dependencies updated, public CI with automatic releases and Dependabot.

## Version 2.0.0

New version completely material and written in Kotlin. The various parameter names are more faithful to those of the android SeekBar material, much code has been rewritten and more has been converted. The main novelty, however, is the use of the AndroidX libraries; for a more detailed breakdown of the differences, I suggest looking at the commits. 


## Previous Version (< 2.0)

The changelog is available in [this github repository](https://github.com/neild001/SeekArc)
All that is left of the old version is the core and some basic functionality, but the whole logic has completely changed. 

# Some Examples Project

You can see the MaterialSeekArc working in the sample application:


<img src="art/Screenshot_6.png" alt="" width="200px">
<img src="art/Screenshot_7.png" alt="" width="200px">
<img src="art/Screenshot_9.png" alt="" width="200px">


<img src="art/Screenshot_8.png" alt="" width="500px">

# License

```

The MIT License (MIT)

Copyright (c) 2013 Triggertrap Ltd
Author Neil Davies 

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

```
