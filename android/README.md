# Android-App – Malteser Dienstzettel 1.0.0

Offline-App mit gebündelter PWA-Basis 3.12 für Android ab 8.0 (API 26). Paketname: `de.famulla.malteser`. Die PWA wird über eine ausschließlich lokal beantwortete HTTPS-Adresse geladen. Keine INTERNET-Berechtigung und kein Nachladen der App aus GitHub.

## Installation und Datenübernahme

1. In der bisherigen PWA „Meine Daten sichern“ auswählen und JSON-Datei aufbewahren.
2. APK auf dem Android-Handy öffnen und Installation für die verwendete Dateien-/Browser-App erlauben, falls Android danach fragt.
3. In der neuen App „Sicherung wiederherstellen“ auswählen und die JSON-Datei öffnen.
4. Monat, Einträge und Unterschrift kontrollieren. Die bisherige PWA bis dahin behalten.

PWA und Android-App speichern getrennt. Deinstallation oder „App-Daten löschen“ löscht lokale App-Daten. Vorher immer JSON exportieren. E-Mail funktioniert wie bisher: PDF speichern und selbst als Anhang hinzufügen. Spracheingabe verwendet die auf dem Gerät installierte Android-Spracherkennung; Offline-Verfügbarkeit hängt vom Sprachanbieter ab.

## Enthalten

Dienstkategorien, Bereitschaft 3/24, Einsatzpauschalen, manuelle Stunden, Schulungsdauer, Notizen, Zeichnen/Import/Fotografieren einer Unterschrift, Monatsverlauf und Abschluss, PDF-Vorschau mit Android-Druckdialog, JSON-Import/Export über Android-Dateidialoge, Android-Spracheingabe für Datum und Uhrzeit und Zurück-Navigation.

Monatsübergreifende Schichten werden in beiden Monaten mit ihren anteiligen Stunden berücksichtigt. Abgeschlossene betroffene Monate sperren die gesamte Schicht. Datumskontrolle beim Import verwendet lokale Kalenderdaten statt UTC.

## Build

JDK 17, Android SDK Platform 35 und Build Tools 35.0.0 bereitstellen. `javac` muss im PATH liegen. Es werden keine Drittbibliotheken eingebunden.

```sh
export ANDROID_JAR=/path/to/android-sdk/platforms/android-35/android.jar
export ANDROID_BUILD_TOOLS=/path/to/android-sdk/build-tools/35.0.0
export MALTESER_KEYSTORE=/private/path/malteser-release.jks
export MALTESER_STORE_PASSWORD='private-password'
bash android/build.sh
```

Ergebnis: `android/build/Malteser-Dienstzettel-1.0.0.apk`. Schlüsselalias: `malteser`. Für spätere Updates denselben privaten Signaturschlüssel verwenden und versionCode erhöhen. Signaturschlüssel niemals ins Git-Repository aufnehmen.

## Prüfung und Grenzen

20 automatisierte Funktionstests mit Chromium 131, mobiler Ansicht (393 × 852) und Europe/Berlin bestanden. Abgedeckt: Start, Persistenz, Pauschalen, manuelle Stunden, Schulungen, ungültige Daten/Zeiten, Mitternacht/Monatswechsel, Bearbeiten/Löschen, Monatsabschluss, Backup/Restore, Signatur, Sprachrückgabe und PDF-HTML. Test-PDF auf einer A4-Seite geprüft. Native Brücke wurde in Browsertests simuliert. Java wurde gegen SDK 35 kompiliert, APK-Signatur v2/v3 verifiziert.

Kein Android-Gerät oder Emulator war verfügbar. Kamera, Dateidialoge, Sprachdienst, Android-Druckdialog, Installation und Zurück-Taste müssen noch auf einem Android-Gerät abgenommen werden. Dies ist eine erste Testversion, keine vollständig auf Geräten validierte Veröffentlichung.

```sh
python3 android/prepare_assets.py
PLAYWRIGHT_MODULE=/path/to/node_modules/playwright CHROMIUM_PATH=/path/to/chrome-headless-shell node android/tests/smoke.cjs
```
