cs282
=====

Principles of Operating Systems II

This project started as a class project for CS282 at Vanderbilt University, Nashville Tennessee USA.



MOTIVATION
==========

The goals of the project are to demonstrate best practices for collaborative android development.
The emphasis for this example differs from most android samples in that it 
considers less human interaction concerns and more system concerns.

Although these projects are developed with eclipse the official builds make use of maven.
Demonstrating how to test system components such as services and content providers
is given special attention.


POSIX
=====

The following should work for Linux and MacOS.


ENVIRONMENT
-----------

As this is a maven project you will need to install maven.
As it is an android project you will need to install the android development kit.
So that the android-maven-plugin can find the needed components of
the android SDK a few environment parameters will need to be set.
Presuming the android SDK is install in /opt/android/

ANDROID_SDK=/opt/android/android-sdk-linux
ANDROID_NDK=/opt/android/android-ndk
ANDROID_HOME=/opt/android/android-sdk-linux
ANDROID_NDK_HOME=/opt/android/android-ndk-r8b

BUILD
-----

mvn clean install

RUN
---

pushd assignment-6
mvn android:deploy android:run
popd

TEST
---

pushd assignment-6-test
mvn install
popd



WINDOWS
=======

The following should work for modern Microsoft systems.
This works but I have not written the documentation yet.


