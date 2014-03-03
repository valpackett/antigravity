# Antigravity

Antigravity lets you share images (like photos or screenshots) and any other files quickly and securely to anyone in the world.

Upload a file with Antigravity and you'll get a short link you can share on any social network or through email or an instant messaging system. But there's more! Antigravity is powered by App.net and supports a lot of its features. App.net is your passport to great applications like this one. Besides using App.net for storage, you can post files – even already uploaded files – to your App.net microblogging feed as links or as embedded images. You can also delete uploaded files.

[Get it on the Google Play Store](https://play.google.com/store/apps/details?id=com.floatboth.antigravity) | [App.net Directory page](https://directory.app.net/app/303/antigravity/)

## Building

You need the following things in `Android SDK Manager`:

- Android SDK Build-tools
- Android 4.0 (API 14)
- Android Support Repository

Then, copy `res/values/credentials.xml.example` to `res/values/credentials.xml`, uncomment
the tags and change the values to your actual App.net API credentials.

Make sure you have the `ANDROID_HOME` env variable set to the SDK path:

```shell
$ export ANDROID_HOME=/usr/local/Cellar/android-sdk/22.3
```

Make a debug build and install via adb:

```shell
$ ./gradlew installDebug
```

Release build (with proguard, signing and zipaligning):

You'll need to copy `release-signing.properties.example` to `release-signing.properties`
and change the values to your actual values.

```shell
$ ./gradlew assembleRelease
```

## License

    Antigravity - a file manager for App.net
    Copyright (C) 2013-2014  Greg V <floatboth@me.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see [http://www.gnu.org/licenses/].

