package com.framework.tests;

import com.framework.base.BaseTest;
import com.framework.pages.CalculatorPage;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.framework.listeners.TestListener;

/**
 * Automates the 4 required scenarios against the Android Calculator app:
 *   1. Basic calculation
 *   2. Multiple operations
 *   3. Negative scenario (divide by zero)
 *   4. Clear functionality
 */
@Listeners(TestListener.class)
public class CalculatorTests extends BaseTest {

    @Test(description = "Verify 10 + 20 = 30", priority = 1)
    public void testBasicCalculation() {
        CalculatorPage calculator = new CalculatorPage(driver);

        calculator.performOperation(10, "+", 20);

        String result = calculator.getResultText();
        Assert.assertEquals(result, "30",
                "Basic calculation 10 + 20 did not produce the expected result");
    }

    @Test(description = "Verify 100 - 25 = 75", priority = 2)
    public void testSubtraction() {
        CalculatorPage calculator = new CalculatorPage(driver);

        calculator.performOperation(100, "-", 25);

        Assert.assertEquals(calculator.getResultText(), "75",
                "Subtraction 100 - 25 did not produce the expected result");
    }

    @Test(description = "Verify 5 x 6 = 30", priority = 3)
    public void testMultiplication() {
        CalculatorPage calculator = new CalculatorPage(driver);

        calculator.performOperation(5, "*", 6);

        Assert.assertEquals(calculator.getResultText(), "30",
                "Multiplication 5 x 6 did not produce the expected result");
    }

    @Test(description = "Verify 100 / 4 = 25", priority = 4)
    public void testDivision() {
        CalculatorPage calculator = new CalculatorPage(driver);

        calculator.performOperation(100, "/", 4);

        Assert.assertEquals(calculator.getResultText(), "25",
                "Division 100 / 4 did not produce the expected result");
    }

    @Test(description = "Negative scenario: division by zero should be handled gracefully, not crash the app",
          priority = 5)
    public void testDivisionByZero() {
        CalculatorPage calculator = new CalculatorPage(driver);

        calculator.enterNumber(5)
                  .tapDivide()
                  .enterNumber(0)
                  .tapEquals();

        Assert.assertTrue(calculator.isErrorDisplayed(),
                "Expected the app to show a 'Can't divide by zero' style error message");
    }

    @Test(description = "Clear functionality resets the display", priority = 6)
    public void testClearFunctionality() {
        CalculatorPage calculator = new CalculatorPage(driver);

        calculator.performOperation(12, "+", 8);
        Assert.assertEquals(calculator.getResultText(), "20",
                "Precondition failed: 12 + 8 should equal 20 before clearing");

        calculator.clear();

        Assert.assertTrue(calculator.isDisplayCleared(),
                "Display was not cleared/reset after pressing Clear");
    }
}
