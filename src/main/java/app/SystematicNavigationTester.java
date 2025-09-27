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

public class SystematicNavigationTester {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static CSVWriter csvWriter;
    private static int testCount = 0;
    private static Set<String> visitedUrls = new HashSet<>();
    private static String baseUrl = "https://www.greenchef.com/";

    public static void main(String[] args) {
        System.out.println("=== Systematic Navigation & Data Scraping ===");
        System.out.println("Testing specific sections and scraping their data...\n");

        setupDriver();
        setupOutputFiles();

        try {
            // Start from homepage
            navigateToPage(baseUrl);
            
            // Test specific sections systematically
            testSpecificSections();
            
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
            Path csvPath = outDir.resolve("systematic_navigation_test_" + timestamp + ".csv");
            
            csvWriter = new CSVWriter(new FileWriter(csvPath.toFile()));
            String[] header = {"test_id", "section", "element_type", "action", "element_text", "element_url", 
                              "scraped_data", "success", "error_message", "current_url", "timestamp"};
            csvWriter.writeNext(header);
            
            System.out.println("✓ Output file configured: " + csvPath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to setup output files", e);
        }
    }

    private static void testSpecificSections() {
        // Define specific sections to test
        String[] targetSections = {
            "Our Plans", "How it works", "Our values", "Gift cards", "Nutrition guide",
            "Weekly Menu", "Recipes", "Keto", "Plant-based", "Mediterranean"
        };
        
        for (String section : targetSections) {
            System.out.println("\n=== Testing Section: " + section + " ===");
            
            // Go back to homepage first
            navigateToPage(baseUrl);
            
            // Find and test the specific section
            testSection(section);
            
            // Scrape data from current page
            scrapeCurrentPageData(section);
        }
    }

    private static void testSection(String sectionName) {
        try {
            // Look for links containing the section name
            List<WebElement> sectionLinks = driver.findElements(By.cssSelector("a[href]"));
            
            for (WebElement link : sectionLinks) {
                try {
                    String linkText = link.getText().trim().toLowerCase();
                    String href = link.getAttribute("href");
                    
                    if (linkText.contains(sectionName.toLowerCase()) && 
                        href != null && href.startsWith("https://www.greenchef.com")) {
                        
                        System.out.println("🔗 Found section link: '" + link.getText() + "' -> " + href);
                        
                        // Test hover
                        testHoverAction(link, sectionName + " hover");
                        
                        // Test click and navigate
                        if (!visitedUrls.contains(href)) {
                            testClickAndNavigate(link, sectionName + " click", href);
                            
                            // Wait for page to load
                            Thread.sleep(3000);
                            
                            // Test additional elements on this page
                            testPageElements(sectionName);
                            
                            return; // Found and tested the section
                        }
                    }
                } catch (StaleElementReferenceException e) {
                    System.out.println("  - Link became stale, skipping");
                } catch (Exception e) {
                    System.out.println("  - Error testing link: " + e.getMessage());
                }
            }
            
            // If not found as direct link, try alternative approaches
            testAlternativeSectionAccess(sectionName);
            
        } catch (Exception e) {
            System.out.println("❌ Error testing section " + sectionName + ": " + e.getMessage());
        }
    }

    private static void testAlternativeSectionAccess(String sectionName) {
        try {
            // Try to find section in navigation menus
            List<WebElement> navElements = driver.findElements(By.cssSelector("nav a, .nav a, .navigation a, .menu a"));
            
            for (WebElement navElement : navElements) {
                try {
                    String navText = navElement.getText().trim().toLowerCase();
                    
                    if (navText.contains(sectionName.toLowerCase())) {
                        System.out.println("🧭 Found in navigation: '" + navElement.getText() + "'");
                        
                        // Test hover
                        testHoverAction(navElement, sectionName + " nav hover");
                        
                        // Test click
                        String href = navElement.getAttribute("href");
                        if (href != null && !visitedUrls.contains(href)) {
                            testClickAndNavigate(navElement, sectionName + " nav click", href);
                            Thread.sleep(3000);
                            testPageElements(sectionName);
                            return;
                        }
                    }
                } catch (Exception e) {
                    // Continue with next element
                }
            }
            
            // Try to find section in buttons
            List<WebElement> buttons = driver.findElements(By.cssSelector("button"));
            for (WebElement button : buttons) {
                try {
                    String buttonText = button.getText().trim().toLowerCase();
                    
                    if (buttonText.contains(sectionName.toLowerCase())) {
                        System.out.println("🔘 Found as button: '" + button.getText() + "'");
                        
                        testHoverAction(button, sectionName + " button hover");
                        
                        if (isSafeToClick(button)) {
                            testClickAction(button, sectionName + " button click");
                            Thread.sleep(3000);
                            testPageElements(sectionName);
                            return;
                        }
                    }
                } catch (Exception e) {
                    // Continue with next element
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error in alternative section access: " + e.getMessage());
        }
    }

    private static void testPageElements(String sectionName) {
        System.out.println("📄 Testing elements on " + sectionName + " page");
        
        try {
            // Test all clickable elements on current page
            List<WebElement> clickableElements = driver.findElements(By.cssSelector("a[href], button"));
            
            for (int i = 0; i < Math.min(clickableElements.size(), 10); i++) {
                WebElement element = clickableElements.get(i);
                try {
                    if (element.isDisplayed() && element.isEnabled()) {
                        String elementText = element.getText().trim();
                        String tagName = element.getTagName();
                        
                        if (!elementText.isEmpty()) {
                            System.out.println("  🧪 Testing " + tagName + ": '" + elementText + "'");
                            
                            // Test hover
                            testHoverAction(element, sectionName + " page element hover");
                            
                            // Test click if safe
                            if (isSafeToClick(element)) {
                                testClickAction(element, sectionName + " page element click");
                            }
                        }
                    }
                } catch (StaleElementReferenceException e) {
                    System.out.println("    - Element became stale, skipping");
                } catch (Exception e) {
                    System.out.println("    - Error testing element: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error testing page elements: " + e.getMessage());
        }
    }

    private static void scrapeCurrentPageData(String sectionName) {
        System.out.println("📊 Scraping data from " + sectionName + " page");
        
        try {
            // Scrape headings
            List<WebElement> headings = driver.findElements(By.cssSelector("h1, h2, h3, h4"));
            for (WebElement heading : headings) {
                try {
                    String headingText = heading.getText().trim();
                    if (!headingText.isEmpty()) {
                        logScrapedData(sectionName, "heading", heading.getTagName(), headingText, "", headingText, true, "");
                    }
                } catch (Exception e) {
                    // Skip problematic headings
                }
            }
            
            // Scrape paragraphs
            List<WebElement> paragraphs = driver.findElements(By.cssSelector("p"));
            for (WebElement paragraph : paragraphs) {
                try {
                    String paragraphText = paragraph.getText().trim();
                    if (!paragraphText.isEmpty() && paragraphText.length() > 20) {
                        logScrapedData(sectionName, "paragraph", "p", paragraphText, "", paragraphText, true, "");
                    }
                } catch (Exception e) {
                    // Skip problematic paragraphs
                }
            }
            
            // Scrape links
            List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
            for (WebElement link : links) {
                try {
                    String linkText = link.getText().trim();
                    String href = link.getAttribute("href");
                    
                    if (!linkText.isEmpty() && href != null) {
                        logScrapedData(sectionName, "link", "a", linkText, href, linkText, true, "");
                    }
                } catch (Exception e) {
                    // Skip problematic links
                }
            }
            
            // Scrape images
            List<WebElement> images = driver.findElements(By.cssSelector("img"));
            for (WebElement image : images) {
                try {
                    String src = image.getAttribute("src");
                    String alt = image.getAttribute("alt");
                    
                    if (src != null && !src.isEmpty()) {
                        logScrapedData(sectionName, "image", "img", alt != null ? alt : "", src, src, true, "");
                    }
                } catch (Exception e) {
                    // Skip problematic images
                }
            }
            
            // Take screenshot
            takeScreenshot(sectionName.replaceAll("[^a-zA-Z0-9]", "_"));
            
        } catch (Exception e) {
            System.out.println("❌ Error scraping data: " + e.getMessage());
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

    private static void testHoverAction(WebElement element, String actionType) {
        try {
            Actions actions = new Actions(driver);
            actions.moveToElement(element).perform();
            System.out.println("  ✓ Hover successful");
            logTest("hover", actionType, element.getText(), "", "", true, "", driver.getCurrentUrl());
            Thread.sleep(500);
        } catch (Exception e) {
            System.out.println("  ❌ Hover failed: " + e.getMessage());
            logTest("hover", actionType, element.getText(), "", "", false, e.getMessage(), driver.getCurrentUrl());
        }
    }

    private static void testClickAction(WebElement element, String actionType) {
        try {
            String currentUrl = driver.getCurrentUrl();
            
            // Try regular click first
            try {
                element.click();
                System.out.println("  ✓ Click successful");
                Thread.sleep(2000);
                
                String newUrl = driver.getCurrentUrl();
                logTest("click", actionType, element.getText(), newUrl, "", true, "", currentUrl);
                
            } catch (Exception e) {
                // Try JavaScript click
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                    System.out.println("  ✓ JavaScript click successful");
                    Thread.sleep(2000);
                    
                    String newUrl = driver.getCurrentUrl();
                    logTest("click", actionType + " (JS)", element.getText(), newUrl, "", true, "", currentUrl);
                    
                } catch (Exception jsE) {
                    System.out.println("  ❌ Both click methods failed: " + jsE.getMessage());
                    logTest("click", actionType, element.getText(), "", "", false, jsE.getMessage(), driver.getCurrentUrl());
                }
            }
            
        } catch (Exception e) {
            System.out.println("  ❌ Click test failed: " + e.getMessage());
            logTest("click", actionType, element.getText(), "", "", false, e.getMessage(), driver.getCurrentUrl());
        }
    }

    private static void testClickAndNavigate(WebElement element, String actionType, String expectedUrl) {
        try {
            String currentUrl = driver.getCurrentUrl();
            
            // Try regular click first
            try {
                element.click();
                System.out.println("  ✓ Click successful");
                Thread.sleep(3000);
                
                String newUrl = driver.getCurrentUrl();
                if (!newUrl.equals(currentUrl)) {
                    System.out.println("  📍 URL changed: " + newUrl);
                    navigateToPage(newUrl);
                }
                
                logTest("click_navigate", actionType, element.getText(), newUrl, "", true, "", currentUrl);
                
            } catch (Exception e) {
                // Try JavaScript click
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                    System.out.println("  ✓ JavaScript click successful");
                    Thread.sleep(3000);
                    
                    String newUrl = driver.getCurrentUrl();
                    if (!newUrl.equals(currentUrl)) {
                        System.out.println("  📍 URL changed: " + newUrl);
                        navigateToPage(newUrl);
                    }
                    
                    logTest("click_navigate", actionType + " (JS)", element.getText(), newUrl, "", true, "", currentUrl);
                    
                } catch (Exception jsE) {
                    System.out.println("  ❌ Both click methods failed: " + jsE.getMessage());
                    logTest("click_navigate", actionType, element.getText(), "", "", false, jsE.getMessage(), driver.getCurrentUrl());
                }
            }
            
        } catch (Exception e) {
            System.out.println("  ❌ Click and navigate test failed: " + e.getMessage());
            logTest("click_navigate", actionType, element.getText(), "", "", false, e.getMessage(), driver.getCurrentUrl());
        }
    }

    private static boolean isSafeToClick(WebElement element) {
        try {
            String tagName = element.getTagName();
            String className = element.getAttribute("class");
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

    private static void takeScreenshot(String name) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path screenshotPath = Paths.get("output", name + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png");
            Files.copy(screenshot.toPath(), screenshotPath);
            System.out.println("📸 Screenshot saved: " + screenshotPath.getFileName());
        } catch (Exception e) {
            System.out.println("  - Screenshot failed: " + e.getMessage());
        }
    }

    private static void logTest(String elementType, String action, String elementText, String elementUrl, String scrapedData, boolean success, String error, String currentUrl) {
        try {
            testCount++;
            String[] row = {
                String.valueOf(testCount),
                "", // section
                elementType,
                action,
                elementText.length() > 100 ? elementText.substring(0, 100) + "..." : elementText,
                elementUrl.length() > 100 ? elementUrl.substring(0, 100) + "..." : elementUrl,
                scrapedData.length() > 100 ? scrapedData.substring(0, 100) + "..." : scrapedData,
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

    private static void logScrapedData(String section, String elementType, String tagName, String elementText, String elementUrl, String scrapedData, boolean success, String error) {
        try {
            testCount++;
            String[] row = {
                String.valueOf(testCount),
                section,
                elementType,
                "scrape",
                elementText.length() > 100 ? elementText.substring(0, 100) + "..." : elementText,
                elementUrl.length() > 100 ? elementUrl.substring(0, 100) + "..." : elementUrl,
                scrapedData.length() > 100 ? scrapedData.substring(0, 100) + "..." : scrapedData,
                String.valueOf(success),
                error,
                driver.getCurrentUrl(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            };
            csvWriter.writeNext(row);
            csvWriter.flush();
        } catch (Exception e) {
            System.err.println("Error logging scraped data: " + e.getMessage());
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
