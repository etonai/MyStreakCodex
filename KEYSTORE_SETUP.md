# MyStreak Release Keystore Setup Guide

This guide explains how to set up the release keystore for signing your MyStreak APK for Google Play Store submission.

## Step 1: Generate Release Keystore

Run this command to generate your release keystore:

```bash
keytool -genkey -v -keystore mystreak-release.keystore -alias mystreak -keyalg RSA -keysize 2048 -validity 10000
```

You will be prompted to enter:
- Keystore password (choose a strong password)
- Key password (can be same as keystore password)
- Your name/organization details

**IMPORTANT**:
- Keep the keystore file and passwords SECURE
- Back up the keystore file - if you lose it, you cannot update your app on Google Play
- Never commit the keystore file to git

## Step 2: Store Keystore Securely

1. Move the keystore file to a secure location outside your project directory
2. Note the full path to the keystore file

## Step 3: Create keystore.properties File

Create a file called `keystore.properties` in your project root (same level as build.gradle.kts) with:

```properties
storeFile=/path/to/your/mystreak-release.keystore
storePassword=your_keystore_password
keyAlias=mystreak
keyPassword=your_key_password
```

**IMPORTANT**: Add `keystore.properties` to your `.gitignore` to keep credentials secure.

## Step 4: Update .gitignore

Add these lines to your `.gitignore` file:

```
# Keystore files
keystore.properties
*.keystore
*.jks
```

## Step 5: Enable Release Signing

In `app/build.gradle.kts`, uncomment the keystore configuration code in the `signingConfigs` section.

## Step 6: Build Release APK

Once configured, build your release APK with:

```bash
./gradlew assembleRelease
```

The signed APK will be in: `app/build/outputs/apk/release/MyStreak_1.0.0.apk`

## Verification

Verify your APK is properly signed:

```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/MyStreak_1.0.0.apk
```

## Google Play Store Upload

Upload the signed APK to Google Play Console under "App releases" → "Production" → "Create new release".

---

**Security Reminder**: Never share your keystore file or passwords. Store them securely and create backups.
