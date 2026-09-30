# Khata (Android, Kotlin + Jetpack Compose + Room)

Offline-first ledger app. No backend, no cloud, no permissions.

## Get Khata.apk (no Android Studio needed)
1. Create a new empty GitHub repository.
2. Upload the contents of this folder to it (keep the `.github` folder and `khata-release.jks`).
   Easiest: unzip, then on GitHub use "Add file > Upload files" and drag everything in.
3. Open the **Actions** tab > "Build Khata APK" (it starts automatically on push, or press "Run workflow").
4. When the run is green (about 4-6 minutes), open it and download the **Khata-apk** artifact.
   Unzip it to get `Khata.apk`, copy to your phone and install (allow "install unknown apps" when asked).

## Or build locally
Install Android Studio (or JDK 17 + Android SDK 34), open this folder, then:
`Build > Build APK(s)`, or run `gradle :app:assembleRelease`.
Output: `app/build/outputs/apk/release/app-release.apk`

## Notes
- Money is stored as whole paise (Long), never floating point.
- Balance = Total Received - Total Given (positive = Receivable, negative = Payable).
- The APK is signed with the bundled `khata-release.jks` so future builds install as updates over old ones.
  This keystore is for personal use; create your own before publishing to the Play Store.
