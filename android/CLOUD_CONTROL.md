# TK_SABR cloud control 1.4.0

Owner: long-press the app title, then sign in with the owner Firebase email/password. No Google sign-in and no login required for public users. Authorization is enforced by Firestore rules, not by hiding the UI.

Reads /appConfig/public. Owner can save enabled, title, subtitle, footer, maintenance, announcement, theme (0..2), motion, themeNames, and compressed background/logo. No private data belongs in this public document. Existing deployed owner-UID rules are required. A fresh server snapshot is required on startup. Existing APKs before 1.4.0 must update once; offline devices cannot receive live changes. Disabling replaces the analyzer with maintenance view and cancels its composition.

Notifications: user taps enable notifications and grants permission; subscribes to tk_sabr_public. Send to that topic via Firebase Messaging console. Sending from the in-app owner panel is NOT implemented: it requires a trusted backend with authorization and FCM credentials, never embedded in the APK. Announcements are foreground in-app messages, not push notifications.

Launcher icon changes and arbitrary executable behavior still require a new APK. Uploaded images affect only the internal logo/background. Custom theme images and names are supported; there are three animation algorithms, not arbitrary user-uploaded executable shaders.

Verification needed: owner login, unauthorized write rejection, changes on a second device, disable/re-enable, image updates, notification permission/background delivery, cold-start and public account scan. Build success is not runtime verification.
