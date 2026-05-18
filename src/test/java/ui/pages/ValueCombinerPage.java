package ui.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ui.utils.WaitUtil;

public class ValueCombinerPage {
    private final WebDriver driver;

    @FindBy(id = "integerTypeBtn")
    private WebElement integerTypeButton;

    @FindBy(id = "doubleTypeBtn")
    private WebElement decimalTypeButton;

    @FindBy(id = "stringTypeBtn")
    private WebElement textTypeButton;

    @FindBy(id = "addBtn")
    private WebElement addButton;

    @FindBy(id = "calculateBtn")
    private WebElement calculateButton;

    @FindBy(id = "resultContainer")
    private WebElement resultContainer;

    @FindBy(id = "resultValue")
    private WebElement resultValue;

    @FindBy(id = "passCount")
    private WebElement successCount;

    @FindBy(id = "exceptionCount")
    private WebElement exceptionCount;

    public ValueCombinerPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        WaitUtil.untilVisible(driver, calculateButton);
    }

    public void selectIntegerType() {
        WaitUtil.untilClickable(driver, integerTypeButton).click();
    }

    public void selectDecimalType() {
        WaitUtil.untilClickable(driver, decimalTypeButton).click();
    }

    public void selectTextType() {
        WaitUtil.untilClickable(driver, textTypeButton).click();
    }

    public void enterValues(String... values) {
        ensureInputCount(values.length);
        List<WebElement> inputs = inputFields();
        for (int i = 0; i < values.length; i++) {
            WebElement input = inputs.get(i);
            input.clear();
            input.sendKeys(values[i]);
        }
    }

    public void clickCalculate() {
        WaitUtil.untilClickable(driver, calculateButton).click();
    }

    public String getResultText() {
        WaitUtil.until(driver, ExpectedConditions.attributeContains(resultContainer, "class", "show"));
        return resultValue.getText();
    }

    public String calculateInteger(String... values) {
        selectIntegerType();
        enterValues(values);
        clickCalculate();
        return getResultText();
    }

    public String calculateDecimal(String... values) {
        selectDecimalType();
        enterValues(values);
        clickCalculate();
        return getResultText();
    }

    public String calculateText(String... values) {
        selectTextType();
        enterValues(values);
        clickCalculate();
        return getResultText();
    }

    public int getSuccessCount() {
        return Integer.parseInt(successCount.getText());
    }

    public int getExceptionCount() {
        return Integer.parseInt(exceptionCount.getText());
    }

    private void ensureInputCount(int expectedCount) {
        if (expectedCount < 2 || expectedCount > 4) {
            throw new IllegalArgumentException("UI supports between 2 and 4 inputs.");
        }

        while (inputFields().size() < expectedCount) {
            WaitUtil.untilClickable(driver, addButton).click();
        }
    }

    private List<WebElement> inputFields() {
        return driver.findElements(By.cssSelector(".input-value"));
    }
}
