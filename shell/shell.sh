#!/bin/sh
./gradlew qrlibrary:assembleRelease
rm -rf ~/AndroidRCTeacher/libs/qrlibrary-release.aar
cp ~/QRCode/qrlibrary/build/outputs/aar/qrlibrary-release.aar ~/AndroidRCTeacher/libs/qrlibrary-release.aar

echo "done, QRCode/qrlibrary/build/outputs/aar/qrlibrary-release.aar"