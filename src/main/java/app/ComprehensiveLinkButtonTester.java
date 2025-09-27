package app;

import com.opencsv.CSVWriter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComprehensiveLinkButtonTester {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static CSVWriter csvWriter;
    private static int testCount = 0;
    private static Set<String> visitedUrls = new HashSet<>();
    private static String baseUrl = "https://www.greenchef.com/";

    public static void main(String[] args) {
        System.out.println("=== Comprehensive Link & Button Testing ===");
        System.out.println("Testing every clickable element on GreenChef website...\n");

        setupDriver();
        setupOutputFiles();

        try {
            // Start from homepage
            navigateToPage(baseUrl);
            
            // Test all elements on current page
            testAllClickableElements();
            
            // Find and test all links
            testAllLinks();
            
            // Find and test all buttons
            testAllButtons();
            
            // Test form elements
            testFormElements();
            
            // Test navigation elements
            testNavigationElements();
            
            System.out.println("\n=== TESTING COMPLETE ===");
            System.out.println("Total elements tested: " + testCount);
            System.out.println("Unique URLs visited: " + visitedUrls.size());
            
        } catch (Exception e) {
            System.err.println("Error during testing: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private static void setupDriver() {
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1280,900");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-extensions");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
        System.out.println("✓ WebDriver initialized successfully");
    }

    private static void setupOutputFiles() {
        try {
            Path outDir = Paths.get("output");
            if (!Files.exists(outDir)) {
                Files.createDirectories(outDir);
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path csvPath = outDir.resolve("comprehensive_link_button_test_" + timestamp + ".csv");
            
            csvWriter = new CSVWriter(new FileWriter(csvPath.toFile()));
            String[] header = {"test_id", "element_type", "action", "element_text", "element_url", 
                              "success", "error_message", "current_url", "timestamp"};
            csvWriter.writeNext(header);
            
            System.out.println("✓ Output file configured: " + csvPath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to setup output files", e);
        }
    }

    private static void navigateToPage(String url) {
        try {
            if (visitedUrls.contains(url)) {
                System.out.println("⏭️  Already visited: " + url);
                return;
            }
            
            System.out.println("🌐 Navigating to: " + url);
            driver.get(url);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            // Handle overlays and popups
            closeOverlays();
            
            visitedUrls.add(url);
            Thread.sleep(2000); // Allow page to settle
            
        } catch (Exception e) {
            System.err.println("❌ Error navigating to " + url + ": " + e.getMessage());
        }
    }

    private static void testAllClickableElements() {
        System.out.println("\n--- Testing All Clickable Elements ---");
        
        // Find all clickable elements
        List<By> clickableSelectors = List.of(
            By.cssSelector("a[href]"),
            By.cssSelector("button"),
            By.cssSelector("input[type='button']"),
            By.cssSelector("input[type='submit']"),
            By.cssSelector("[onclick]"),
            By.cssSelector("[role='button']"),
            By.cssSelector(".btn, .button"),
            By.cssSelector("[data-test*='button']"),
            By.cssSelector("[data-testid*='button']")
        );
        
        Set<String> testedElements = new HashSet<>();
        
        for (By selector : clickableSelectors) {
            try {
                List<WebElement> elements = driver.findElements(selector);
                System.out.println("Found " + elements.size() + " elements with selector: " + selector);
                
                for (WebElement element : elements) {
                    try {
                        if (element.isDisplayed() && element.isEnabled()) {
                            String elementId = getElementIdentifier(element);
                            if (!testedElements.contains(elementId)) {
                                testElement(element);
                                testedElements.add(elementId);
                            }
                        }
                    } catch (StaleElementReferenceException e) {
                        System.out.println("  - Element became stale, skipping");
                    }
                }
            } catch (Exception e) {
                System.out.println("  - Error with selector " + selector + ": " + e.getMessage());
            }
        }
    }

    private static void testAllLinks() {
        System.out.println("\n--- Testing All Links ---");
        
        try {
            List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
            System.out.println("Found " + links.size() + " links to test");
            
            for (int i = 0; i < Math.min(links.size(), 20); i++) { // Limit to 20 links to avoid infinite loops
                WebElement link = links.get(i);
                try {
                    if (link.isDisplayed() && link.isEnabled()) {
                        String href = link.getAttribute("href");
                        String linkText = link.getText().trim();
                        
                        if (href != null && !href.isEmpty() && 
                            (href.startsWith("https://www.greenchef.com") || href.startsWith("/"))) {
                            
                            System.out.println("🔗 Testing link: '" + linkText + "' -> " + href);
                            
                            // Test hover action
                            testHoverAction(link, "Link hover");
                            
                            // Test click if it's a GreenChef internal link
                            if (href.startsWith("https://www.greenchef.com") && !visitedUrls.contains(href)) {
                                testClickAction(link, "Link click", href);
                            }
                        }
                    }
                } catch (StaleElementReferenceException e) {
                    System.out.println("  - Link became stale, skipping");
                } catch (Exception e) {
                    System.out.println("  - Error testing link: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error in link testing: " + e.getMessage());
        }
    }

    private static void testAllButtons() {
        System.out.println("\n--- Testing All Buttons ---");
        
        try {
            List<WebElement> buttons = driver.findElements(By.cssSelector("button, input[type='button'], input[type='submit']"));
            System.out.println("Found " + buttons.size() + " buttons to test");
            
            for (WebElement button : buttons) {
                try {
                    if (button.isDisplayed() && button.isEnabled()) {
                        String buttonText = button.getText().trim();
                        String buttonType = button.getAttribute("type");
                        
                        System.out.println("🔘 Testing button: '" + buttonText + "' (type: " + buttonType + ")");
                        
                        // Test hover action
                        testHoverAction(button, "Button hover");
                        
                        // Test click action (be careful with form submissions)
                        if (isSafeToClick(button)) {
                            testClickAction(button, "Button click", null);
                        } else {
                            System.out.println("  - Skipping click (potentially unsafe)");
                            logTest("button", "click_skipped", buttonText, "", false, "Potentially unsafe to click", driver.getCurrentUrl());
                        }
                    }
                } catch (StaleElementReferenceException e) {
                    System.out.println("  - Button became stale, skipping");
                } catch (Exception e) {
                    System.out.println("  - Error testing button: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error in button testing: " + e.getMessage());
        }
    }

    private static void testFormElements() {
        System.out.println("\n--- Testing Form Elements ---");
        
        try {
            // Test input fields
            List<WebElement> inputs = driver.findElements(By.cssSelector("input[type='text'], input[type='email'], input[type='search'], textarea"));
            
            for (WebElement input : inputs) {
                try {
                    if (input.isDisplayed() && input.isEnabled()) {
                        String inputType = input.getAttribute("type");
                        String placeholder = input.getAttribute("placeholder");
                        
                        System.out.println("📝 Testing input: " + inputType + " (placeholder: " + placeholder + ")");
                        
                        // Test focus
                        input.click();
                        System.out.println("  - Focus successful");
                        
                        // Test text input
                        if ("email".equals(inputType)) {
                            input.clear();
                            input.sendKeys("test@example.com");
                            System.out.println("  - Text input successful");
                            
                            String value = input.getAttribute("value");
                            logTest("input", "sendKeys", placeholder, value, true, "", driver.getCurrentUrl());
                            
                            // Clear the field
                            input.clear();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("  - Error testing input: " + e.getMessage());
                }
            }
            
            // Test select dropdowns
            List<WebElement> selects = driver.findElements(By.cssSelector("select"));
            for (WebElement selectElement : selects) {
                try {
                    if (selectElement.isDisplayed() && selectElement.isEnabled()) {
                        Select select = new Select(selectElement);
                        List<WebElement> options = select.getOptions();
                        
                        System.out.println("📋 Testing dropdown with " + options.size() + " options");
                        
                        if (options.size() > 1) {
                            select.selectByIndex(1);
                            WebElement selectedOption = select.getFirstSelectedOption();
                            System.out.println("  - Selection successful: " + selectedOption.getText());
                            logTest("select", "selectByIndex", "Dropdown", selectedOption.getText(), true, "", driver.getCurrentUrl());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("  - Error testing dropdown: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error in form testing: " + e.getMessage());
        }
    }

    private static void testNavigationElements() {
        System.out.println("\n--- Testing Navigation Elements ---");
        
        try {
            // Test navigation menus
            List<WebElement> navItems = driver.findElements(By.cssSelector("nav a, .nav a, .navigation a, .menu a"));
            
            for (WebElement navItem : navItems) {
                try {
                    if (navItem.isDisplayed() && navItem.isEnabled()) {
                        String navText = navItem.getText().trim();
                        String href = navItem.getAttribute("href");
                        
                        if (!navText.isEmpty() && href != null && href.startsWith("https://www.greenchef.com")) {
                            System.out.println("🧭 Testing navigation: '" + navText + "'");
                            
                            testHoverAction(navItem, "Navigation hover");
                            
                            if (!visitedUrls.contains(href)) {
                                testClickAction(navItem, "Navigation click", href);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("  - Error testing navigation: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error in navigation testing: " + e.getMessage());
        }
    }

    private static void testElement(WebElement element) {
        try {
            String tagName = element.getTagName();
            String text = element.getText().trim();
            String elementId = getElementIdentifier(element);
            
            System.out.println("🧪 Testing " + tagName + ": '" + text + "'");
            
            // Test hover
            testHoverAction(element, tagName + " hover");
            
            // Test click if it's a link or button
            if ("a".equals(tagName) || "button".equals(tagName)) {
                String href = element.getAttribute("href");
                if (isSafeToClick(element)) {
                    testClickAction(element, tagName + " click", href);
                }
            }
            
        } catch (Exception e) {
            System.out.println("  - Error testing element: " + e.getMessage());
        }
    }

    private static void testHoverAction(WebElement element, String actionType) {
        try {
            Actions actions = new Actions(driver);
            actions.moveToElement(element).perform();
            System.out.println("  ✓ Hover successful");
            logTest("hover", actionType, element.getText(), "", true, "", driver.getCurrentUrl());
            Thread.sleep(500);
        } catch (Exception e) {
            System.out.println("  ❌ Hover failed: " + e.getMessage());
            logTest("hover", actionType, element.getText(), "", false, e.getMessage(), driver.getCurrentUrl());
        }
    }

    private static void testClickAction(WebElement element, String actionType, String expectedUrl) {
        try {
            String currentUrl = driver.getCurrentUrl();
            
            // Try regular click first
            try {
                element.click();
                System.out.println("  ✓ Click successful");
                Thread.sleep(2000);
                
                // Check if URL changed
                String newUrl = driver.getCurrentUrl();
                if (!newUrl.equals(currentUrl)) {
                    System.out.println("  📍 URL changed: " + newUrl);
                    navigateToPage(newUrl);
                }
                
                logTest("click", actionType, element.getText(), newUrl, true, "", currentUrl);
                
            } catch (Exception e) {
                // Try JavaScript click
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                    System.out.println("  ✓ JavaScript click successful");
                    Thread.sleep(2000);
                    
                    String newUrl = driver.getCurrentUrl();
                    if (!newUrl.equals(currentUrl)) {
                        System.out.println("  📍 URL changed: " + newUrl);
                        navigateToPage(newUrl);
                    }
                    
                    logTest("click", actionType + " (JS)", element.getText(), newUrl, true, "", currentUrl);
                    
                } catch (Exception jsE) {
                    System.out.println("  ❌ Both click methods failed: " + jsE.getMessage());
                    logTest("click", actionType, element.getText(), "", false, jsE.getMessage(), currentUrl);
                }
            }
            
        } catch (Exception e) {
            System.out.println("  ❌ Click test failed: " + e.getMessage());
            logTest("click", actionType, element.getText(), "", false, e.getMessage(), driver.getCurrentUrl());
        }
    }

    private static boolean isSafeToClick(WebElement element) {
        try {
            String tagName = element.getTagName();
            String className = element.getAttribute("class");
            String id = element.getAttribute("id");
            String text = element.getText().toLowerCase();
            
            // Avoid potentially dangerous elements
            if (text.contains("delete") || text.contains("remove") || text.contains("cancel")) {
                return false;
            }
            
            if (className != null && (className.contains("delete") || className.contains("remove"))) {
                return false;
            }
            
            // Safe to click links and buttons
            return "a".equals(tagName) || "button".equals(tagName);
            
        } catch (Exception e) {
            return false;
        }
    }

    private static String getElementIdentifier(WebElement element) {
        try {
            String id = element.getAttribute("id");
            if (id != null && !id.isEmpty()) {
                return "id:" + id;
            }
            
            String className = element.getAttribute("class");
            if (className != null && !className.isEmpty()) {
                return "class:" + className;
            }
            
            String text = element.getText();
            if (text != null && !text.isEmpty()) {
                return "text:" + text.substring(0, Math.min(20, text.length()));
            }
            
            return "tag:" + element.getTagName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static void closeOverlays() {
        try {
            List<By> overlaySelectors = List.of(
                By.cssSelector("button[aria-label='Close']"),
                By.cssSelector(".modal .close, .modal .close-btn"),
                By.cssSelector(".overlay .close, .overlay .close-btn"),
                By.cssSelector("#onetrust-accept-btn-handler"),
                By.cssSelector(".cookie, .cookies, .cookie-banner button"),
                By.cssSelector("[data-test*='close'], [data-testid*='close']"),
                By.cssSelector(".popup-close, .banner-close")
            );
            
            for (By selector : overlaySelectors) {
                try {
                    List<WebElement> elements = driver.findElements(selector);
                    for (WebElement element : elements) {
                        if (element.isDisplayed() && element.isEnabled()) {
                            element.click();
                            Thread.sleep(1000);
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Continue with next selector
                }
            }
        } catch (Exception e) {
            // Ignore overlay closing errors
        }
    }

    private static void logTest(String elementType, String action, String elementText, String result, boolean success, String error, String currentUrl) {
        try {
            testCount++;
            String[] row = {
                String.valueOf(testCount),
                elementType,
                action,
                elementText.length() > 100 ? elementText.substring(0, 100) + "..." : elementText,
                result.length() > 100 ? result.substring(0, 100) + "..." : result,
                String.valueOf(success),
                error,
                currentUrl,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            };
            csvWriter.writeNext(row);
            csvWriter.flush();
        } catch (Exception e) {
            System.err.println("Error logging test: " + e.getMessage());
        }
    }

    private static void cleanup() {
        try {
            if (csvWriter != null) {
                csvWriter.close();
                System.out.println("✓ CSV file closed successfully");
            }
        } catch (Exception e) {
            System.err.println("Error closing CSV: " + e.getMessage());
        }
        
        if (driver != null) {
            driver.quit();
            System.out.println("✓ WebDriver closed successfully");
        }
    }
}
