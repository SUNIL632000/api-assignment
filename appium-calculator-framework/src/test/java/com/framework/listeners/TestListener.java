package com.framework.listeners;

import com.framework.base.DriverManager;
import com.framework.utils.ScreenshotUtils;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Captures a screenshot for every test outcome (pass/fail) so the
 * Deliverables requirement of "Screenshots of execution" is satisfied
 * automatically on every run, without any code in the test methods.
 */
public class TestListener implements ITestListener {

    @Override
    public void onTestSuccess(ITestResult result) {
        capture(result, "PASS");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        capture(result, "FAIL");
    }

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("▶ Starting: " + result.getMethod().getMethodName());
    }

    private void capture(ITestResult result, String status) {
        if (DriverManager.getDriver() != null) {
            String path = ScreenshotUtils.capture(
                    DriverManager.getDriver(),
                    status + "_" + result.getMethod().getMethodName());
            System.out.println(status + ": " + result.getMethod().getMethodName()
                    + " | screenshot: " + path);
        }
    }
}
