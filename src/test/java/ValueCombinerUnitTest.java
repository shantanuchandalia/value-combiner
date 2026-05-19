import org.testng.Assert;
import org.testng.annotations.Test;

public class ValueCombinerUnitTest {
    private final ValueCombiner combiner = new ValueCombiner();

    @Test
    public void shouldAddIntegers() {
        Assert.assertEquals(combiner.add(2, 3), 5);
    }

    @Test(expectedExceptions = ArithmeticException.class)
    public void shouldRejectIntegerOverflow() {
        combiner.add(Integer.MAX_VALUE, 1);
    }

    @Test
    public void shouldAddDecimals() {
        Assert.assertEquals(combiner.add(2.5, 3.25), 5.75);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldRejectInvalidDecimalValues() {
        combiner.add(Double.NaN, 2.0);
    }

    @Test
    public void shouldConcatenateText() {
        Assert.assertEquals(combiner.add("Hello ", "World"), "Hello World");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldRejectNullText() {
        combiner.add(null, "World");
    }
}
