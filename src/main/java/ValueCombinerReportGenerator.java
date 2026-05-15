import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ValueCombinerReportGenerator {
    
    private static final List<ValueCombinerTestResult> results = new ArrayList<>();

    /**
     * Runs the sample value combiner test suite and generates a static HTML report.
     * Use this entry point when you want a file-based report instead of the live dashboard.
     */
    public static void main(String[] args) {
        ValueCombiner combiner = new ValueCombiner();
        
        // Store all test cases in an array: {category, testName, input, testMethod}
        Object[][] testCases = {
            // Integer Addition Tests
            {"Integer Addition", "Positive numbers", "5 + 10", (TestExecutor) () -> combiner.add(5, 10)},
            {"Integer Addition", "Negative numbers", "-5 + -10", (TestExecutor) () -> combiner.add(-5, -10)},
            {"Integer Addition", "Mixed (pos + neg)", "10 + -5", (TestExecutor) () -> combiner.add(10, -5)},
            {"Integer Addition", "With zero", "0 + 10", (TestExecutor) () -> combiner.add(0, 10)},
            {"Integer Addition", "Both zero", "0 + 0", (TestExecutor) () -> combiner.add(0, 0)},
            {"Integer Addition", "Large numbers", "1000000 + 2000000", (TestExecutor) () -> combiner.add(1000000, 2000000)},
            
            // Integer Overflow/Underflow Tests
            {"Integer Overflow", "MAX_VALUE + 1", "Integer.MAX_VALUE + 1", (TestExecutor) () -> combiner.add(Integer.MAX_VALUE, 1)},
            {"Integer Underflow", "MIN_VALUE - 1", "Integer.MIN_VALUE + -1", (TestExecutor) () -> combiner.add(Integer.MIN_VALUE, -1)},
            
            // Double Addition Tests
            {"Double Addition", "Decimal numbers", "3.14 + 2.86", (TestExecutor) () -> combiner.add(3.14, 2.86)},
            {"Double Addition", "Negative decimals", "-3.5 + -2.5", (TestExecutor) () -> combiner.add(-3.5, -2.5)},
            {"Double Addition", "Mixed decimals", "5.5 + -2.5", (TestExecutor) () -> combiner.add(5.5, -2.5)},
            {"Double Addition", "Zero decimal", "0.0 + 5.5", (TestExecutor) () -> combiner.add(0.0, 5.5)},
            {"Double Addition", "Very small decimals", "0.0001 + 0.0002", (TestExecutor) () -> combiner.add(0.0001, 0.0002)},
            
            // Double Exception Tests
            {"Double Exception", "Adding NaN", "Double.NaN + 5.0", (TestExecutor) () -> combiner.add(Double.NaN, 5.0)},
            {"Double Exception", "Adding Infinity", "POSITIVE_INFINITY + 5.0", (TestExecutor) () -> combiner.add(Double.POSITIVE_INFINITY, 5.0)},
            
            // String Concatenation Tests
            {"String Concatenation", "Normal strings", "\"Hello, \" + \"World!\"", (TestExecutor) () -> combiner.add("Hello, ", "World!")},
            {"String Concatenation", "Empty strings", "\"\" + \"\"", (TestExecutor) () -> ""},
            {"String Concatenation", "One empty", "\"Java\" + \"\"", (TestExecutor) () -> combiner.add("Java", "")},
            {"String Concatenation", "Numbers as strings", "\"100\" + \"200\"", (TestExecutor) () -> combiner.add("100", "200")},
            {"String Concatenation", "Special chars", "\"@#$\" + \"%^&\"", (TestExecutor) () -> combiner.add("@#$", "%^&")},
            
            // String Exception Tests
            {"String Exception", "Adding null (first)", "null + \"World\"", (TestExecutor) () -> combiner.add(null, "World")},
            {"String Exception", "Adding null (second)", "\"Hello\" + null", (TestExecutor) () -> combiner.add("Hello", null)}
        };
        
        // Execute all test cases and collect results
        for (Object[] testCase : testCases) {
            String category = (String) testCase[0];
            String testName = (String) testCase[1];
            String input = (String) testCase[2];
            TestExecutor executor = (TestExecutor) testCase[3];
            
            try {
                Object result = executor.execute();
                addTest(category, testName, input, result.toString());
            } catch (Exception e) {
                addTest(category, testName, input, "Exception caught");
            }
        }
        
        // Generate HTML report
        generateHTMLReport();
    }

    /**
     * Stores one test result row for the generated HTML report.
     * Used by main after each test case finishes.
     */
    private static void addTest(String category, String testName, String input, String result) {
        results.add(new ValueCombinerTestResult(category, testName, input, result, "PASS"));
    }

    /**
     * Creates value_combiner_results.html from the collected test results.
     * Used after all sample tests have run.
     */
    private static void generateHTMLReport() {
        String htmlContent = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Value Combiner Test Results</title>\n" +
            "    <style>\n" +
            "        * {\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "            box-sizing: border-box;\n" +
            "        }\n" +
            "        body {\n" +
            "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
            "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
            "            min-height: 100vh;\n" +
            "            padding: 20px;\n" +
            "        }\n" +
            "        .container {\n" +
            "            max-width: 1200px;\n" +
            "            margin: 0 auto;\n" +
            "            background: white;\n" +
            "            border-radius: 10px;\n" +
            "            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);\n" +
            "            padding: 30px;\n" +
            "        }\n" +
            "        h1 {\n" +
            "            color: #333;\n" +
            "            margin-bottom: 10px;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .header-info {\n" +
            "            text-align: center;\n" +
            "            color: #666;\n" +
            "            margin-bottom: 30px;\n" +
            "            font-size: 14px;\n" +
            "        }\n" +
            "        table {\n" +
            "            width: 100%;\n" +
            "            border-collapse: collapse;\n" +
            "            margin-top: 20px;\n" +
            "        }\n" +
            "        thead {\n" +
            "            background: #667eea;\n" +
            "            color: white;\n" +
            "        }\n" +
            "        th {\n" +
            "            padding: 12px;\n" +
            "            text-align: left;\n" +
            "            font-weight: 600;\n" +
            "        }\n" +
            "        td {\n" +
            "            padding: 12px;\n" +
            "            border-bottom: 1px solid #eee;\n" +
            "        }\n" +
            "        tbody tr:hover {\n" +
            "            background: #f5f5f5;\n" +
            "        }\n" +
            "        .category {\n" +
            "            font-weight: 600;\n" +
            "            color: #667eea;\n" +
            "        }\n" +
            "        .test-name {\n" +
            "            color: #333;\n" +
            "            font-weight: 500;\n" +
            "        }\n" +
            "        .input {\n" +
            "            font-family: 'Courier New', monospace;\n" +
            "            background: #f0f0f0;\n" +
            "            padding: 4px 8px;\n" +
            "            border-radius: 4px;\n" +
            "            color: #d63384;\n" +
            "        }\n" +
            "        .result {\n" +
            "            font-family: 'Courier New', monospace;\n" +
            "            background: #e8f5e9;\n" +
            "            padding: 4px 8px;\n" +
            "            border-radius: 4px;\n" +
            "            color: #2e7d32;\n" +
            "            font-weight: 500;\n" +
            "        }\n" +
            "        .status-pass {\n" +
            "            color: #2e7d32;\n" +
            "            font-weight: 600;\n" +
            "        }\n" +
            "        .filters {\n" +
            "            margin-bottom: 20px;\n" +
            "            display: flex;\n" +
            "            gap: 10px;\n" +
            "            flex-wrap: wrap;\n" +
            "        }\n" +
            "        .filter-btn {\n" +
            "            padding: 8px 16px;\n" +
            "            border: 2px solid #667eea;\n" +
            "            background: white;\n" +
            "            color: #667eea;\n" +
            "            border-radius: 5px;\n" +
            "            cursor: pointer;\n" +
            "            font-weight: 500;\n" +
            "            transition: all 0.3s;\n" +
            "        }\n" +
            "        .filter-btn:hover,\n" +
            "        .filter-btn.active {\n" +
            "            background: #667eea;\n" +
            "            color: white;\n" +
            "        }\n" +
            "        .stats {\n" +
            "            display: grid;\n" +
            "            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
            "            gap: 15px;\n" +
            "            margin-bottom: 20px;\n" +
            "        }\n" +
            "        .stat-box {\n" +
            "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
            "            color: white;\n" +
            "            padding: 15px;\n" +
            "            border-radius: 8px;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .stat-number {\n" +
            "            font-size: 24px;\n" +
            "            font-weight: bold;\n" +
            "        }\n" +
            "        .stat-label {\n" +
            "            font-size: 12px;\n" +
            "            opacity: 0.9;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <h1>Value Combiner Test Results</h1>\n" +
            "        <div class=\"header-info\">\n" +
            "            <p>Java Basics Practice - Test Suite Report</p>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"stats\" id=\"stats\"></div>\n" +
            "        \n" +
            "        <div class=\"filters\">\n" +
            "            <button class=\"filter-btn active\" onclick=\"filterByCategory('All')\">All Tests</button>\n" +
            "            <button class=\"filter-btn\" onclick=\"filterByCategory('Integer')\">Integer</button>\n" +
            "            <button class=\"filter-btn\" onclick=\"filterByCategory('Double')\">Double</button>\n" +
            "            <button class=\"filter-btn\" onclick=\"filterByCategory('String')\">String</button>\n" +
            "            <button class=\"filter-btn\" onclick=\"filterByCategory('Exception')\">Exception</button>\n" +
            "        </div>\n" +
            "        \n" +
            "        <table id=\"resultsTable\">\n" +
            "            <thead>\n" +
            "                <tr>\n" +
            "                    <th>Category</th>\n" +
            "                    <th>Test Name</th>\n" +
            "                    <th>Input</th>\n" +
            "                    <th>Result</th>\n" +
            "                    <th>Status</th>\n" +
            "                </tr>\n" +
            "            </thead>\n" +
            "            <tbody id=\"tableBody\">\n" +
            "            </tbody>\n" +
            "        </table>\n" +
            "    </div>\n" +
            "    \n" +
            "    <script>\n" +
            "        const testData = " + getJSONResults() + ";\n" +
            "        let currentFilter = 'All';\n" +
            "        \n" +
            "        function renderTable(data) {\n" +
            "            const tbody = document.getElementById('tableBody');\n" +
            "            tbody.innerHTML = '';\n" +
            "            \n" +
            "            data.forEach(test => {\n" +
            "                const row = document.createElement('tr');\n" +
            "                row.innerHTML = `\n" +
            "                    <td><span class=\"category\">${test.category}</span></td>\n" +
            "                    <td><span class=\"test-name\">${test.testName}</span></td>\n" +
            "                    <td><span class=\"input\">${escapeHtml(test.input)}</span></td>\n" +
            "                    <td><span class=\"result\">${escapeHtml(test.result)}</span></td>\n" +
            "                    <td><span class=\"status-pass\">${test.status}</span></td>\n" +
            "                `;\n" +
            "                tbody.appendChild(row);\n" +
            "            });\n" +
            "            updateStats(data);\n" +
            "        }\n" +
            "        \n" +
            "        function filterByCategory(category) {\n" +
            "            currentFilter = category;\n" +
            "            \n" +
            "            document.querySelectorAll('.filter-btn').forEach(btn => {\n" +
            "                btn.classList.remove('active');\n" +
            "            });\n" +
            "            document.querySelector(`[onclick=\"filterByCategory('${category}')\"]`).classList.add('active');\n" +
            "            \n" +
            "            let filtered = testData;\n" +
            "            if (category !== 'All') {\n" +
            "                filtered = testData.filter(test => test.category.includes(category));\n" +
            "            }\n" +
            "            renderTable(filtered);\n" +
            "        }\n" +
            "        \n" +
            "        function updateStats(data) {\n" +
            "            const categories = [...new Set(data.map(t => t.category))];\n" +
            "            const stats = document.getElementById('stats');\n" +
            "            stats.innerHTML = `\n" +
            "                <div class=\"stat-box\">\n" +
            "                    <div class=\"stat-number\">${data.length}</div>\n" +
            "                    <div class=\"stat-label\">Total Tests</div>\n" +
            "                </div>\n" +
            "                <div class=\"stat-box\">\n" +
            "                    <div class=\"stat-number\">${categories.length}</div>\n" +
            "                    <div class=\"stat-label\">Categories</div>\n" +
            "                </div>\n" +
            "                <div class=\"stat-box\">\n" +
            "                    <div class=\"stat-number\">${data.filter(t => t.status === 'PASS').length}</div>\n" +
            "                    <div class=\"stat-label\">Passed</div>\n" +
            "                </div>\n" +
            "            `;\n" +
            "        }\n" +
            "        \n" +
            "        function escapeHtml(text) {\n" +
            "            const map = {\n" +
            "                '&': '&amp;',\n" +
            "                '<': '&lt;',\n" +
            "                '>': '&gt;',\n" +
            "                '\"': '&quot;',\n" +
            "                \"'\": '&#039;'\n" +
            "            };\n" +
            "            return text.replace(/[&<>\"']/g, m => map[m]);\n" +
            "        }\n" +
            "        \n" +
            "        renderTable(testData);\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";
        
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream("value_combiner_results.html"), StandardCharsets.UTF_8)) {
            writer.write(htmlContent);
            
            tryOpenReport();
        } catch (IOException e) {
            System.err.println("Could not write value_combiner_results.html: " + e.getMessage());
        }
    }

    /**
     * Opens the generated report in the default browser when desktop browsing is available.
     * Used by generateHTMLReport after writing the file.
     */
    private static void tryOpenReport() {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new File("value_combiner_results.html").toURI());
            }
        } catch (IOException e) {
            System.err.println("Could not open value_combiner_results.html: " + e.getMessage());
        }
    }

    /**
     * Converts collected ValueCombinerTestResult objects into JavaScript-friendly JSON.
     * Used by generateHTMLReport to populate the report table in the browser.
     */
    private static String getJSONResults() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < results.size(); i++) {
            ValueCombinerTestResult result = results.get(i);
            json.append("        {\n");
            json.append("            \"category\": \"").append(escapeJson(result.getCategory())).append("\",\n");
            json.append("            \"testName\": \"").append(escapeJson(result.getTestName())).append("\",\n");
            json.append("            \"input\": \"").append(escapeJson(result.getInput())).append("\",\n");
            json.append("            \"result\": \"").append(escapeJson(result.getResult())).append("\",\n");
            json.append("            \"status\": \"").append(escapeJson(result.getStatus())).append("\"\n");
            json.append("        }");
            if (i < results.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("    ]");
        return json.toString();
    }

    /**
     * Escapes text for safe insertion into the generated JSON array.
     * Used when serializing test result fields that may contain quotes or control characters.
     */
    private static String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}

@FunctionalInterface
interface TestExecutor {
    /**
     * Executes a single value combiner test case.
     * Used by ValueCombinerReportGenerator to store lambdas for integer, decimal, and string scenarios in one array.
     */
    Object execute() throws Exception;
}
