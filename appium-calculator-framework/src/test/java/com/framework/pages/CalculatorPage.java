package com.framework.pages;

import com.framework.utils.ConfigReader;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for the Google Calculator app (com.google.android.calculator).
 *
 * Resource-id locators target the app's default id scheme. If you are
 * running against the AOSP calculator (com.android.calculator2) instead,
 * only the locator constants below need to change - no test code changes.
 */
public class CalculatorPage {

    private final AndroidDriver driver;
    private final WebDriverWait wait;

    // ---- Locators -------------------------------------------------------
    private static final String PKG = "com.google.android.calculator";

    private final By digitButton(int digit) {
        return By.id(PKG + ":id/digit_" + digit);
    }

    private final By ADD_BUTTON = By.id(PKG + ":id/op_add");
    private final By SUBTRACT_BUTTON = By.id(PKG + ":id/op_sub");
    private final By MULTIPLY_BUTTON = By.id(PKG + ":id/op_mul");
    private final By DIVIDE_BUTTON = By.id(PKG + ":id/op_div");
    private final By EQUALS_BUTTON = By.id(PKG + ":id/eq");

    // "Clear" (long-press style clear-all) and "Delete" (backspace) share
    // the same button id in the Google Calculator UI; the label/behaviour
    // toggles based on state. We use the formula field + delete presses,
    // plus an explicit clear-all fallback for robustness.
    private final By CLEAR_ALL_BUTTON = By.id(PKG + ":id/clr");
    private final By DELETE_BUTTON = By.id(PKG + ":id/del");

    private final By FORMULA_FIELD = By.id(PKG + ":id/formula");
    private final By RESULT_FINAL = By.id(PKG + ":id/result_final");
    private final By RESULT_PREVIEW = By.id(PKG + ":id/result_preview");
    private final By ERROR_MESSAGE = AppiumBy.androidUIAutomator(
            "new UiSelector().textContains(\"Can't divide by zero\")");

    public CalculatorPage(AndroidDriver driver) {
        this.driver = driver;
        int waitSeconds = ConfigReader.getInt("explicitWaitSeconds");
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
    }

    // ---- Actions ----------------------------------------------------------

    public CalculatorPage tapDigit(int digit) {
        wait.until(ExpectedConditions.elementToBeClickable(digitButton(digit))).click();
        return this;
    }

    /** Taps each digit of a (possibly multi-digit) number in sequence. */
    public CalculatorPage enterNumber(int number) {
        for (char c : String.valueOf(Math.abs(number)).toCharArray()) {
            tapDigit(Character.getNumericValue(c));
        }
        return this;
    }

    public CalculatorPage tapAdd() {
        wait.until(ExpectedConditions.elementToBeClickable(ADD_BUTTON)).click();
        return this;
    }

    public CalculatorPage tapSubtract() {
        wait.until(ExpectedConditions.elementToBeClickable(SUBTRACT_BUTTON)).click();
        return this;
    }

    public CalculatorPage tapMultiply() {
        wait.until(ExpectedConditions.elementToBeClickable(MULTIPLY_BUTTON)).click();
        return this;
    }

    public CalculatorPage tapDivide() {
        wait.until(ExpectedConditions.elementToBeClickable(DIVIDE_BUTTON)).click();
        return this;
    }

    public CalculatorPage tapEquals() {
        wait.until(ExpectedConditions.elementToBeClickable(EQUALS_BUTTON)).click();
        return this;
    }

    /** Clears the current expression/result back to an empty display. */
    public CalculatorPage clear() {
        List<WebElement> clearAll = driver.findElements(CLEAR_ALL_BUTTON);
        if (!clearAll.isEmpty() && clearAll.get(0).isDisplayed()) {
            clearAll.get(0).click();
            return this;
        }
        // Fall back to repeatedly pressing delete until the formula is empty.
        List<WebElement> deleteBtn = driver.findElements(DELETE_BUTTON);
        int safetyLimit = 30;
        while (!deleteBtn.isEmpty() && !getFormulaText().isEmpty() && safetyLimit-- > 0) {
            deleteBtn.get(0).click();
            deleteBtn = driver.findElements(DELETE_BUTTON);
        }
        return this;
    }

    /** Performs a full simple binary operation: number1 <op> number2 = */
    public CalculatorPage performOperation(int number1, String operator, int number2) {
        enterNumber(number1);
        switch (operator) {

        case "+":
            tapAdd();
            break;

        case "-":
            tapSubtract();
            break;

        case "*":
        case "x":
        case "×":
            tapMultiply();
            break;

        case "/":
        case "÷":
            tapDivide();
            break;

        default:
            throw new IllegalArgumentException(
                    "Unsupported operator: " + operator
       );
        }
        enterNumber(number2);
        tapEquals();
        return this;
    }

    // ---- Getters / Assertions helpers -------------------------------------

    public String getResultText() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(RESULT_FINAL));
        return driver.findElement(RESULT_FINAL).getText().trim();
    }

    public String getFormulaText() {
        List<WebElement> formula = driver.findElements(FORMULA_FIELD);
        return formula.isEmpty() ? "" : formula.get(0).getText().trim();
    }

    public boolean isErrorDisplayed() {
        return !driver.findElements(ERROR_MESSAGE).isEmpty();
    }

    public boolean isDisplayCleared() {
        String formula = getFormulaText();
        return formula.isEmpty() || formula.equals("0");
    }
}
