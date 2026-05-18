package ui.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import ui.base.BaseTest;
import ui.pages.ValueCombinerPage;
import ui.utils.TestData;
import ui.utils.TestData.CalculationData;

public class ValueCombinerCalculationTest extends BaseTest {
    @Test
    public void shouldAddIntegerValues() {
        CalculationData testData = TestData.INTEGER_ADDITION;
        ValueCombinerPage page = new ValueCombinerPage(driver());
        int successCountBefore = page.getSuccessCount();

        String result = page.calculateInteger(testData.inputs());

        Assert.assertEquals(result, testData.expectedResult());
        Assert.assertTrue(page.getSuccessCount() > successCountBefore);
    }

    @Test
    public void shouldAddDecimalValues() {
        CalculationData testData = TestData.DECIMAL_ADDITION;
        ValueCombinerPage page = new ValueCombinerPage(driver());
        int successCountBefore = page.getSuccessCount();

        String result = page.calculateDecimal(testData.inputs());

        Assert.assertEquals(result, testData.expectedResult());
        Assert.assertTrue(page.getSuccessCount() > successCountBefore);
    }

    @Test
    public void shouldConcatenateTextValues() {
        CalculationData testData = TestData.TEXT_CONCATENATION;
        ValueCombinerPage page = new ValueCombinerPage(driver());
        int successCountBefore = page.getSuccessCount();

        String result = page.calculateText(testData.inputs());

        Assert.assertEquals(result, testData.expectedResult());
        Assert.assertTrue(page.getSuccessCount() > successCountBefore);
    }
}
