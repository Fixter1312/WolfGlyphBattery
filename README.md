# Wolf Glyph Battery — APK z wilkiem 25×25

Ten projekt to **najprostsza możliwa** apka na Androida, która rysuje wilka na **macierzy 25×25** (1 = biały, 0 = czarny) i **odkrywa** go od dołu do góry w **takim procencie**, jaki jest **poziom baterii** telefonu. Na środku jest też napis „XX%”.

> Uwaga: to jest standardowa apka Android. Działa na Nothing Phone 3 jak zwykły program. Jeśli chcesz później zintegrować to z systemem Glyph (tyły obudowy), najpierw ogarnij ten projekt, a potem możesz dołożyć SDK Nothing (GDK) — to osobny temat.

---

## Jak to zbudować **lokalnie** (klik po kliku, jak dla totalnego laika)

1. **Zainstaluj Android Studio** (Arctic Fox lub nowsze). Włącz podczas instalacji Android SDK, Platform-Tools i emulator (opcjonalnie).
2. **Pobierz to repo** (albo rozpakuj ZIPa, którego Ci dałem).
3. Otwórz Android Studio → **Open** → wskaż folder `WolfGlyphBattery`.
4. Poczekaj aż się zrobi **Gradle sync** (na dole pasek postępu). Nic nie klikaj.
5. Podłącz telefon kablem USB (włącz **Opcje programistyczne** → **Debugowanie USB**).
6. W Android Studio kliknij **Run ▶** (górny pasek). Wybierz swój telefon. Po chwili apk się zainstaluje.
7. Odpal apkę: zobaczysz wilka, a napis na środku pokazuje **procent baterii**. Wilk jest odkryty od dołu do wysokości odpowiadającej procentowi.

---

## Jak zbudować **APK na GitHub (Actions)** i pobrać gotowy plik

1. Zrób konto na GitHub i **utwórz nowe repozytorium** (Public lub Private).
2. Skopiuj **całą** zawartość folderu `WolfGlyphBattery` do tego repo i zrób **commit + push**.
3. Wejdź w **Actions** → przy pierwszym wejściu potwierdź uruchomienie.
4. W repo w katalogu `.github/workflows/` już jest plik `android.yml`. On:
   - instaluje JDK,
   - pobiera Android SDK,
   - buduje **Debug APK**,
   - wrzuca wynik jako **artifact**.
5. Po zakończeniu joba wejdź w **Actions** → kliknij najnowszy workflow → **Artifacts** → pobierz `wolf-apk` → w środku masz `app-debug.apk`.

**Zero magii, wszystko klika się samo.**

---

## Pliki, które Cię obchodzą

- `app/src/main/java/com/example/wolfglyphbattery/GlyphMatrixView.kt` — rysuje wilka z tablicy 25×25 i „maskuje” go procentem baterii (odkrywa od dołu).
- `app/src/main/java/com/example/wolfglyphbattery/MainActivity.kt` — czyta poziom baterii i podaje procent do widoku.
- `app/src/main/res/layout/activity_main.xml` — prosty layout (widok + napis).
- `.github/workflows/android.yml` — przepis na budowanie APK w Actions.

---

## Co ewentualnie zmienić

- **Kolory**: w `GlyphMatrixView.kt` jest farba biała i przygaszona (`dimPaint`). Zmienisz na własne.
- **Sposób odkrywania**: teraz liczona jest liczba widocznych **wierszy** (25 × procent). Jeżeli chcesz bardziej dokładnie *piksel po pikselu*, możesz policzyć piksele i przycinać rysowanie co do prostokąta — ale wizualnie to i tak 1:1 wierszami.
- **Integracja z Nothing Glyph SDK**: gdybyś chciał to świecić na LED-ach z tyłu, potrzebne jest oficjalne SDK Nothing (tzw. GDK). Wtedy zamiast rysować na ekranie, wysyłasz pattern do kontrolera Glyph. To wymaga osobnych uprawnień i dokumentacji.

Powodzenia! 🐺🔋