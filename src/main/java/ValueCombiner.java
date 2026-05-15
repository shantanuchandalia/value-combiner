public class ValueCombiner {
    
    /**
     * Adds two integers with overflow and underflow detection.
     * Used by the API and test report whenever the selected data type is integer.
     *
     * @param a first integer
     * @param b second integer
     * @return sum of a and b
     * @throws ArithmeticException if integer overflow or underflow would occur
     */
    public int add(int a, int b) throws ArithmeticException {
        try {
            // Check for overflow before adding
            if (a > 0 && b > 0 && a > Integer.MAX_VALUE - b) {
                throw new ArithmeticException("Integer overflow: Adding " + a + " and " + b + " exceeds maximum integer value.");
            }
            // Check for underflow before adding
            if (a < 0 && b < 0 && a < Integer.MIN_VALUE - b) {
                throw new ArithmeticException("Integer underflow: Adding " + a + " and " + b + " exceeds minimum integer value.");
            }
            return a + b;
        } catch (ArithmeticException e) {
            System.err.println("Error in integer addition: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Adds two decimal values after rejecting invalid floating-point values.
     * Used by the API and test report whenever the selected data type is decimal.
     *
     * @param a first double
     * @param b second double
     * @return sum of a and b
     * @throws IllegalArgumentException if inputs or the result are NaN or infinite
     */
    public double add(double a, double b) throws IllegalArgumentException {
        try {
            // Check for NaN values
            if (Double.isNaN(a) || Double.isNaN(b)) {
                throw new IllegalArgumentException("Cannot add NaN values. a=" + a + ", b=" + b);
            }
            // Check for infinite values
            if (Double.isInfinite(a) || Double.isInfinite(b)) {
                throw new IllegalArgumentException("Cannot add infinite values. a=" + a + ", b=" + b);
            }
            double result = a + b;
            
            // Check if result is infinite
            if (Double.isInfinite(result)) {
                throw new IllegalArgumentException("Result is infinite: " + a + " + " + b + " = " + result);
            }
            return result;
        } catch (IllegalArgumentException e) {
            System.err.println("Error in double addition: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Concatenates two strings after checking for null values.
     * Used by the API and test report whenever the selected data type is text.
     *
     * @param a first string
     * @param b second string
     * @return concatenated result of a and b
     * @throws NullPointerException if either parameter is null
     */
    public String add(String a, String b) throws NullPointerException {
        try {
            // Check for null values
            if (a == null) {
                throw new NullPointerException("First string parameter is null");
            }
            if (b == null) {
                throw new NullPointerException("Second string parameter is null");
            }
            return a + b;
        } catch (NullPointerException e) {
            System.err.println("Error in string concatenation: " + e.getMessage());
            throw e;
        }
    }
}
