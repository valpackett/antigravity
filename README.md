# Antigravity

An App.net file manager for Android.

## Building

You need Maven and the following things in `Android SDK Manager`:

- Android SDK Build-tools
- Android 4.0 (API 14)
- Android Support Repository

Then, copy `res/values/credentials.xml.example` to `res/values/credentials.xml`, uncomment
the tags and change the values to your actual App.net API credentials.

Make sure you have the `ANDROID_HOME` env variable set to the SDK path:

```shell
$ export ANDROID_HOME=/usr/local/Cellar/android-sdk/22.2.1
```

Debug build:

```shell
$ mvn clean install
```

Release build (with proguard, signing and zipaligning):

```shell
$ mvn clean install -Prelease -Djarsigner.keystore=~/path/to/keystore \
  -Djarsigner.alias=release \
  -Djarsigner.storepass=storePassword123 \
  -Djarsigner.keypass=keyPassword456
```
