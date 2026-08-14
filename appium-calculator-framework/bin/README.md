# Android Calculator – Appium + Java + TestNG Automation Framework

A small mobile automation framework that drives the built-in Android
Calculator app (`com.google.android.calculator`) using **Appium**,
**Java**, and **TestNG**, built with the Page Object Model (POM).

## 1. What this covers

| # | Scenario | Test method | File |
|---|----------|-------------|------|
| 1 | Basic calculation `10 + 20 = 30` | `testBasicCalculation` | `CalculatorTests.java` |
| 2 | Multiple operations `100-25=75`, `5×6=30`, `100÷4=25` | `testSubtraction`, `testMultiplication`, `testDivision` | `CalculatorTests.java` |
| 3 | Negative scenario – divide by zero | `testDivisionByZero` | `CalculatorTests.java` |
| 4 | Clear functionality | `testClearFunctionality` | `CalculatorTests.java` |

Each test method is independent (fresh Appium session per test via
`@BeforeMethod`/`@AfterMethod` in `BaseTest`), so failures don't cascade
and tests can run in any order or in parallel.

## 2. Project structure

```
appium-calculator-framework/
├── pom.xml                         # Maven deps: appium-java-client, selenium, testng, extentreports
├── testng.xml                      # TestNG suite definition (entry point for the run)
├── README.md
├── screenshots/                    # Auto-generated screenshots (pass + fail) from every run
└── src/test/
    ├── resources/
    │   └── config.properties       # Device/app capabilities — edit this, not the code
    └── java/com/framework/
        ├── base/
        │   ├── DriverManager.java  # Creates/quits the AndroidDriver (ThreadLocal-safe)
        │   └── BaseTest.java       # @BeforeMethod/@AfterMethod session lifecycle
        ├── pages/
        │   └── CalculatorPage.java # Page Object: all locators + calculator actions live here
        ├── listeners/
        │   └── TestListener.java   # TestNG ITestListener — auto screenshot on pass/fail
        ├── utils/
        │   ├── ConfigReader.java   # Loads config.properties
        │   └── ScreenshotUtils.java
        └── tests/
            └── CalculatorTests.java # The 6 @Test methods (4 required scenarios)
```

### Why this architecture

- **Page Object Model** – `CalculatorPage` is the only class that knows
  about locators (resource-ids) and low-level Appium calls
  (`findElement`, `click`, waits). Test methods read like plain English
  (`calculator.performOperation(10, "+", 20)`), so a locator change
  (e.g. switching from the Google Calculator to the AOSP calculator)
  is a one-file change.
- **Config-driven capabilities** – device name, platform version,
  app package/activity, and the Appium server URL all live in
  `config.properties`, not hardcoded in Java, so the same code runs
  against an emulator, a physical device, or BrowserStack by editing
  one file.
- **ThreadLocal DriverManager** – makes the suite safe to run in
  parallel (`testng.xml` → `parallel="methods"`) without sessions
  colliding, even though the current suite runs sequentially by default.
- **Listener-based screenshots** – screenshots are captured centrally
  in `TestListener` for every pass/fail, instead of littering each test
  method with screenshot calls. This guarantees evidence exists even
  for tests written later without extra effort.
- **Explicit waits, no `Thread.sleep`** – `WebDriverWait` +
  `ExpectedConditions` throughout, so the suite is fast and not flaky
  on slower emulators/CI.

## 3. Prerequisites

- Java JDK 11+
- Maven 3.8+
- Node.js + Appium server: `npm install -g appium`
- Appium UiAutomator2 driver: `appium driver install uiautomator2`
- Android SDK + either:
  - **Android Studio emulator** with an image that includes Google
    Play/Google APIs (so the Google Calculator app is pre-installed), or
  - a **physical Android device** with USB debugging enabled, or
  - a **BrowserStack App Automate** account (see §6 below)

Verify your setup:
```bash
appium-doctor --android
```

## 4. Setup

```bash
git clone <your-repo-url>
cd appium-calculator-framework
mvn clean install -DskipTests
```

Edit `src/test/resources/config.properties` to match your environment:

```properties
deviceName=emulator-5554          # `adb devices` to find yours
platformVersion=14                # your emulator/device Android version
appPackage=com.google.android.calculator
appActivity=com.android.calculator2.Calculator
```

> If your emulator only ships the plain AOSP calculator (no Google
> Play), switch to:
> ```properties
> appPackage=com.android.calculator2
> appActivity=com.android.calculator2.Calculator
> ```
> No other code changes are needed — `CalculatorPage` reads the
> package name from a single constant.

## 5. Running the tests

**1. Start an emulator or connect a device**
```bash
emulator -avd Pixel_6_API_34   # or plug in / connect your device
adb devices                    # confirm it shows up
```

**2. Start the Appium server** (separate terminal)
```bash
appium
```

**3. Run the suite**
```bash
mvn clean test
```
This picks up `testng.xml` (configured in `pom.xml` via the
`maven-surefire-plugin`) and runs all 6 test methods.

To run a single test class or method:
```bash
mvn test -Dtest=CalculatorTests#testDivisionByZero
```

## 6. Running on BrowserStack (optional)

Replace the capability setup in `DriverManager.initDriver()` with
BrowserStack's `bstack:options` (userName/accessKey from your account),
point `appium.server.url` in `config.properties` to
`https://hub.browserstack.com/wd/hub`, and use BrowserStack's own
Calculator sample app id or the device's pre-installed calculator
package. Everything else (Page Object, tests, listener) stays the same
— this is the benefit of keeping capabilities out of the test code.

## 7. Test execution report & screenshots

- **Screenshots**: every test run automatically saves a PNG to
  `screenshots/` named `<PASS|FAIL>_<testMethodName>_<timestamp>.png`
  (see `TestListener` + `ScreenshotUtils`). This gives visual evidence
  for every scenario, not just failures.
- **HTML/XML report**: TestNG generates its native report under
  `test-output/index.html` and `test-output/emailable-report.html`
  after every `mvn test` run — open either in a browser for a
  pass/fail summary with timings.
- (Optional extension) `extentreports` is already a declared Maven
  dependency if you want to wire up a richer branded HTML report — a
  minimal `ExtentSparkReporter` hook can be added to `TestListener`.

## 8. Design decisions & trade-offs (for the "explain and justify" requirement)

- **Google Calculator locators over AOSP**: most modern Android Studio
  emulator images (Google APIs / Google Play) ship the Google
  Calculator, so its resource-ids were used as the default. The
  package/activity is fully config-driven, so switching targets is a
  properties-file edit, not a rewrite.
- **`noReset=true`**: keeps app state/session light between runs since
  the Calculator app has no login/data to reset; speeds up local runs.
  Set to `false` if you want a guaranteed clean app state per session.
- **Negative scenario assertion**: rather than asserting on a specific
  crash/non-crash boolean, the test asserts that a `"Can't divide by
  zero"`-style message is surfaced via `UiSelector.textContains`,
  because that's the actual, user-visible contract of the Calculator
  app when dividing by zero (it doesn't crash — it shows an inline
  error). This is closer to real-world negative testing than just
  checking "app didn't close."
- **Clear vs Delete**: the Google Calculator UI reuses one button id
  for both "delete last digit" (before `=`) and "clear all" (after
  `=`), toggling by state. `CalculatorPage.clear()` handles both cases
  defensively (tries the all-clear button first, falls back to
  repeated backspace) so the same method works regardless of when it's
  called in a test.
- **Per-test fresh session**: trades a little speed for full test
  isolation — no scenario can leak calculator state into the next one,
  which matters most for the divide-by-zero and clear tests.

## 9. A note on this deliverable

This project was scaffolded with AI assistance (Claude) to accelerate
writing boilerplate (POM structure, Maven config, listener plumbing).
The architecture, locator strategy, and test logic follow standard
Appium/TestNG conventions and are meant to be read, understood, and
modified directly — every design decision above is something a
reviewer can be asked to justify in person. Because this was generated
outside of an environment with a live Android emulator/Appium server,
the suite has **not** been executed here; before submitting, run
`mvn clean test` locally against your emulator/device per §5, confirm
all 6 scenarios pass, and commit the resulting `screenshots/` and
`test-output/` (or delete/replace with your own real run's artifacts)
so the deliverables reflect an actual execution, not just code that
compiles.
