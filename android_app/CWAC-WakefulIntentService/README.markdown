CWAC Wakeful: Staying Awake At Work
===================================

The recommended pattern for Android's equivalent to cron
jobs and Windows scheduled tasks is to use `AlarmManager`.
This works well when coupled with an `IntentService`, as the
service will do its work on a background thread and shut down
when there is no more work to do.

There's one small problem: `IntentService` does nothing to keep
the device awake. If the alarm was a `WAKEUP` variant, the phone
will only stay awake on its own while the `BroadcastReceiver`
handling the alarm is in its `onReceive()` method. Otherwise,
the phone may fall back asleep.

`WakefulIntentService` attempts to combat this by combining
the ease of `IntentService` with a partial `WakeLock`.

This is [available as a JAR file](https://github.com/commonsguy/downloads).
The project itself is set up as an Android library project,
in case you wish to use the source code in that fashion.

**NOTE**: `WakefulIntentService` v0.4.0 and newer requires Android 2.0+, so it
can take advantage of `onStartCommand()` for better handling of
crashed services. Use earlier versions of `WakefulIntentService` if
you wish to try to use it on older versions of Android, though this
is not supported.

Basic Usage
-----------
Any component that wants to send work to a
`WakefulIntentService` subclass needs to call either:

`WakefulIntentService.sendWakefulWork(context, MyService.class);`

(where `MyService.class` is the `WakefulIntentService` subclass)

or:

`WakefulIntentService.sendWakefulWork(context, intentOfWork);`

(where `intentOfWork` is an `Intent` that will be used to call
`startService()` on your `WakefulIntentService` subclass)

Implementations of `WakefulIntentService` must override
`doWakefulWork()` instead of `onHandleIntent()`. `doWakefulWork()`
will be processed within the bounds of a `WakeLock`. Otherwise,
the semantics of `doWakefulWork()` are identical to `onHandleIntent()`.
`doWakefulWork()` will be passed the `Intent` supplied to
`sendWakefulWork()` (or an `Intent` created by the `sendWakefulWork()`
method, depending on which flavor of that method you use).

And that's it. `WakefulIntentService` handles the rest.

NOTE: this only works with local services. You have no means
of accessing the static `WakeLock` of a remote service.

NOTE #2: Your application must hold the `WAKE_LOCK` permission.

NOTE #3: If you get an "`WakeLock` under-locked" exception, make sure
that you are not starting your service by some means other than
`sendWakefulWork()`.

Alarm Usage
-----------
If you want to slightly simplify your use of `WakefulIntentService`
in conjunction with `AlarmManager`, you can do the following:

First, implement your `WakefulIntentService` and `doWakefulWork()`
as described above.

Next, create a class implementing the `WakefulIntentService.AlarmListener`
interface. This class needs to have a no-argument public constructor
in addition to the interface method implementations. One method
is `scheduleAlarms()`, where you are passed in an `AlarmManager`,
a `PendingIntent`, and a `Context`, and your mission is to schedule
your alarms using the supplied `PendingIntent`. You also implement
`sendWakefulWork()`, which is passed a `Context`, and is where
you call `sendWakefulWork()` upon your `WakefulIntentService`
implementation. And, you need to implement `getMaxAge()`, which
should return the time in milliseconds after which, if we have
not seen an alarm go off, we should assume that the alarms were
canceled (e.g., application was force-stopped by the user), and
should reschedule them.

Then, create an XML metadata file where you identify the class
that implements `WakefulIntentService.AlarmListener` from the
previous step, akin to:

    <WakefulIntentService
      listener="com.commonsware.cwac.wakeful.demo.AppListener"
    />

Next, register `com.commonsware.cwac.wakeful.AlarmReceiver`
as a `<receiver>` in your manifest, set to respond to
`ACTION_BOOT_COMPLETED` broadcasts, and with a `com.commonsware.cwac.wakeful`
`<meta-data>` element pointing to the XML resource from 
the previous step, akin to:

    <receiver android:name="com.commonsware.cwac.wakeful.AlarmReceiver">
      <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED"/>
      </intent-filter>

      <meta-data
        android:name="com.commonsware.cwac.wakeful"
        android:resource="@xml/wakeful"/>
    </receiver>

Also, add the `RECEIVE_BOOT_COMPLETED` permission to your manifest.

Finally, when you wish to manually set up the alarms (e.g., on
first run of your app), create an instance of your `AlarmListener`
and call `scheduleAlarms()` on the `WakefulIntentService`
class, passing in the `AlarmListener` and a `Context` (e.g.,
the activity that is trying to set up the alarms). If you are only
scheduling alarms using the single provided `PendingIntent`, you
can also call `cancelAlarms()` on the `WakefulIntentService` class
to cancel any outstanding alarms.

For production use, ProGuard may rename your `AlarmListener`
class, which will foul up access to your metadata. To stop this
from happening, you
[will need to add a `-keep` line to your ProGuard configuration file](http://developer.android.com/guide/developing/tools/proguard.html#configuring)
(e.g., `proguard.cfg`) to stop ProGuard from renaming it.

Over time, this portion of the framework will be expanded
further to help consolidate a good usage pattern for
managing alarms.

Additional documentation can be found in the "AlarmManager: Making the Services Run On
Time" section of [this free excerpt](http://commonsware.com/AdvAndroid/wakeful.pdf)
from [The Busy Coder's Guide to Advanced Android Development](http://commonsware.com/AdvAndroid).

Dependencies
------------
None.

This project should work on API Level 7 and higher, except for any portions that
may be noted otherwise in this document. Please report bugs if you find features
that do not work on API Level 7 and are not noted as requiring a higher version.

Version
-------
This is version v0.6.0 of this module, meaning it is proving
to be surprisingly popular.

Demo
----
In the `demo/` project directory and `com.commonsware.cwac.wakeful.demo` package you will find
an `AppListener`, which is an implementation of `AlarmListener`,
and `AppService`, which
extends `WakefulIntentService`. `AppService` pretends to do some work in a background
thread. All of this is set up via a `DemoActivity` (required
to move the application out of the "stopped" state on Android 3.1+),
and if needed on a reboot.

Note that when you build the JAR via `ant jar`, the sample
activity is not included, nor any resources -- only the
compiled classes for the actual library are put into the JAR.

License
-------
The code in this project is licensed under the Apache
Software License 2.0, per the terms of the included LICENSE
file.

Questions
---------
If you have questions regarding the use of this code, please post a question
on [StackOverflow](http://stackoverflow.com/questions/ask) tagged with `commonsware` and `android`. Be sure to indicate
what CWAC module you are having issues with, and be sure to include source code 
and stack traces if you are encountering crashes.

If you have encountered what is clearly a bug, please post an [issue](https://github.com/commonsguy/cwac-wakeful/issues). Be certain to include complete steps
for reproducing the issue.

Do not ask for help via Twitter.
Release Notes
-------------
- v0.6.2: added more fail-safes around `WakeLock` acquisition and release
- v0.6.1: replaced `AlarmListener` `Log` lines with `RuntimeExceptions`
- v0.6.0: added `cancelAlarms()` to `WakefulIntentService`
- v0.5.1: semi-automatically handle canceled alarms (e.g., app force-stopped)
- v0.5.0: added the `AlarmListener` portion of the framework
- v0.4.5: completed switch to `Application` as the `Context` for the `WakeLock`
- v0.4.4: switched to `Application` as the `Context` for the `WakeLock`
- v0.4.3: added better recovery from an `Intent` redelivery condition
- v0.4.2: added `volatile` keyword to static `WakeLock` for better double-checked locking implementation
- v0.4.1: added `setIntentRedelivery()` call, nuked extraneous permissions check
- v0.4.0: switched to `onStartCommand()`, requiring Android 2.0+ (API level 5 or higher)
- v0.3.0: converted to Android library project, added test for `WAKE_LOCK` permission

