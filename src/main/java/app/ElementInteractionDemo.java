package app;

import com.opencsv.CSVWriter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
import java.util.List;

public class ElementInteractionDemo {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static CSVWriter csvWriter;
    private static int interactionCount = 0;

    public static void main(String[] args) {
        System.out.println("=== Selenium Element Interaction Demonstration ===");
        System.out.println("Demonstrating various Selenium commands and element interactions...\n");

        setupDriver();
        setupOutputFiles();

        try {
            // Demonstrate various element interactions
            demonstrateBasicNavigation();
            demonstrateTextInteractions();
            demonstrateLinkInteractions();
            demonstrateButtonInteractions();
            demonstrateFormInteractions();
            demonstrateAdvancedInteractions();
            
            System.out.println("\n=== DEMONSTRATION COMPLETE ===");
            System.out.println("Total interactions demonstrated: " + interactionCount);
            
        } catch (Exception e) {
            System.err.println("Error during demonstration: " + e.getMessage());
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
        // Remove headless for demonstration purposes
        
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
            Path csvPath = outDir.resolve("element_interactions_" + timestamp + ".csv");
            
            csvWriter = new CSVWriter(new FileWriter(csvPath.toFile()));
            String[] header = {"interaction_id", "interaction_type", "element_type", "action", 
                              "element_text", "element_value", "success", "url", "timestamp"};
            csvWriter.writeNext(header);
            
            System.out.println("✓ Output files configured: " + csvPath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to setup output files", e);
        }
    }

    private static void demonstrateBasicNavigation() {
        System.out.println("\n--- Basic Navigation Commands ---");
        
        // Navigate to website
        logInteraction("Navigation", "URL", "get()", "Navigating to GreenChef", "", true);
        driver.get("https://www.greenchef.com/");
        System.out.println("✓ driver.get() - Navigated to GreenChef homepage");
        
        // Wait for page to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        System.out.println("✓ WebDriverWait - Page loaded successfully");
        
        // Get page title and URL
        String title = driver.getTitle();
        String currentUrl = driver.getCurrentUrl();
        logInteraction("Navigation", "Page", "getTitle()", title, currentUrl, true);
        System.out.println("✓ getTitle(): " + title);
        System.out.println("✓ getCurrentUrl(): " + currentUrl);
        
        // Take screenshot
        takeScreenshot("navigation_demo");
    }

    private static void demonstrateTextInteractions() {
        System.out.println("\n--- Text Element Interactions ---");
        
        try {
            // Find and interact with text elements
            List<WebElement> headings = driver.findElements(By.cssSelector("h1, h2, h3"));
            
            for (int i = 0; i < Math.min(5, headings.size()); i++) {
                WebElement heading = headings.get(i);
                try {
                    String tagName = heading.getTagName();
                    String text = heading.getText();
                    boolean isDisplayed = heading.isDisplayed();
                    
                    logInteraction("Text", tagName, "getText()", text, "", isDisplayed);
                    System.out.println("✓ " + tagName + ".getText(): " + text.substring(0, Math.min(50, text.length())) + "...");
                    
                    // Demonstrate getAttribute()
                    String className = heading.getAttribute("class");
                    if (className != null && !className.isEmpty()) {
                        System.out.println("  - getAttribute('class'): " + className);
                    }
                    
                } catch (StaleElementReferenceException e) {
                    System.out.println("  - Element became stale, skipping");
                }
            }
        } catch (Exception e) {
            System.out.println("  - Error finding text elements: " + e.getMessage());
        }
    }

    private static void demonstrateLinkInteractions() {
        System.out.println("\n--- Link Element Interactions ---");
        
        try {
            // Find links
            List<WebElement> links = driver.findElements(By.cssSelector("a[href]"));
            
            for (int i = 0; i < Math.min(3, links.size()); i++) {
                WebElement link = links.get(i);
                try {
                    String href = link.getAttribute("href");
                    String linkText = link.getText();
                    boolean isEnabled = link.isEnabled();
                    
                    if (href != null && href.startsWith("https://www.greenchef.com") && !linkText.isEmpty()) {
                        logInteraction("Link", "anchor", "getAttribute('href')", linkText, href, true);
                        System.out.println("✓ Link found - Text: '" + linkText + "' -> " + href);
                        
                        // Demonstrate hover action
                        try {
                            org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
                            actions.moveToElement(link).perform();
                            System.out.println("  - Actions.moveToElement() - Hover performed");
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            System.out.println("  - Hover action failed: " + e.getMessage());
                        }
                    }
                } catch (StaleElementReferenceException e) {
                    System.out.println("  - Link became stale, skipping");
                }
            }
        } catch (Exception e) {
            System.out.println("  - Error finding links: " + e.getMessage());
        }
    }

    private static void demonstrateButtonInteractions() {
        System.out.println("\n--- Button Element Interactions ---");
        
        try {
            // Find buttons
            List<WebElement> buttons = driver.findElements(By.cssSelector("button, input[type='button'], input[type='submit']"));
            
            for (int i = 0; i < Math.min(3, buttons.size()); i++) {
                WebElement button = buttons.get(i);
                try {
                    String buttonText = button.getText();
                    String buttonType = button.getAttribute("type");
                    boolean isEnabled = button.isEnabled();
                    boolean isDisplayed = button.isDisplayed();
                    
                    logInteraction("Button", "button", "isEnabled()", buttonText, buttonType, isEnabled);
                    System.out.println("✓ Button - Text: '" + buttonText + "', Type: " + buttonType + 
                                     ", Enabled: " + isEnabled + ", Displayed: " + isDisplayed);
                    
                    // Demonstrate click if safe
                    if (isEnabled && isDisplayed && (buttonText.toLowerCase().contains("menu") || 
                                                   buttonText.toLowerCase().contains("view") ||
                                                   buttonText.toLowerCase().contains("show"))) {
                        try {
                            button.click();
                            System.out.println("  - click() performed successfully");
                            Thread.sleep(2000);
                            logInteraction("Button", "button", "click()", buttonText, "", true);
                            break; // Only click one button to avoid navigation issues
                        } catch (Exception e) {
                            System.out.println("  - Click failed: " + e.getMessage());
                        }
                    }
                } catch (StaleElementReferenceException e) {
                    System.out.println("  - Button became stale, skipping");
                }
            }
        } catch (Exception e) {
            System.out.println("  - Error finding buttons: " + e.getMessage());
        }
    }

    private static void demonstrateFormInteractions() {
        System.out.println("\n--- Form Element Interactions ---");
        
        try {
            // Look for input fields
            List<WebElement> inputs = driver.findElements(By.cssSelector("input[type='text'], input[type='email'], input[type='search'], textarea"));
            
            for (WebElement input : inputs) {
                try {
                    String inputType = input.getAttribute("type");
                    String placeholder = input.getAttribute("placeholder");
                    boolean isEnabled = input.isEnabled();
                    
                    if (isEnabled && input.isDisplayed()) {
                        System.out.println("✓ Input field - Type: " + inputType + ", Placeholder: " + placeholder);
                        
                        // Demonstrate sendKeys()
                        String testText = "test@example.com";
                        if ("email".equals(inputType)) {
                            input.clear();
                            input.sendKeys(testText);
                            System.out.println("  - sendKeys() performed: " + testText);
                            
                            String value = input.getAttribute("value");
                            System.out.println("  - getAttribute('value'): " + value);
                            
                            logInteraction("Form", "input", "sendKeys()", placeholder, value, true);
                            
                            // Clear the field
                            input.clear();
                            System.out.println("  - clear() performed");
                            break; // Only interact with one form field
                        }
                    }
                } catch (StaleElementReferenceException e) {
                    System.out.println("  - Input became stale, skipping");
                } catch (Exception e) {
                    System.out.println("  - Error interacting with input: " + e.getMessage());
                }
            }
            
            // Look for select dropdowns
            List<WebElement> selects = driver.findElements(By.cssSelector("select"));
            for (WebElement selectElement : selects) {
                try {
                    if (selectElement.isDisplayed() && selectElement.isEnabled()) {
                        Select select = new Select(selectElement);
                        List<WebElement> options = select.getOptions();
                        
                        System.out.println("✓ Dropdown found with " + options.size() + " options");
                        logInteraction("Form", "select", "getOptions()", "Dropdown", String.valueOf(options.size()), true);
                        
                        if (options.size() > 1) {
                            select.selectByIndex(1);
                            System.out.println("  - selectByIndex(1) performed");
                            
                            WebElement selectedOption = select.getFirstSelectedOption();
                            System.out.println("  - Selected option: " + selectedOption.getText());
                        }
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("  - Error with dropdown: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.out.println("  - Error in form interactions: " + e.getMessage());
        }
    }

    private static void demonstrateAdvancedInteractions() {
        System.out.println("\n--- Advanced Selenium Interactions ---");
        
        try {
            // Demonstrate JavaScript execution
            Long scrollHeight = (Long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
            System.out.println("✓ JavaScriptExecutor - Page scroll height: " + scrollHeight);
            logInteraction("JavaScript", "document", "executeScript()", "scrollHeight", scrollHeight.toString(), true);
            
            // Demonstrate scrolling
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 500)");
            System.out.println("✓ JavaScript scroll performed");
            Thread.sleep(1000);
            
            // Demonstrate finding elements by different locators
            demonstrateElementLocators();
            
            // Demonstrate explicit waits
            demonstrateWaits();
            
            // Take final screenshot
            takeScreenshot("advanced_demo");
            
        } catch (Exception e) {
            System.out.println("  - Error in advanced interactions: " + e.getMessage());
        }
    }

    private static void demonstrateElementLocators() {
        System.out.println("\n--- Element Locator Strategies ---");
        
        // By ID
        try {
            List<WebElement> elementsById = driver.findElements(By.id("main"));
            System.out.println("✓ By.id() - Found " + elementsById.size() + " elements");
        } catch (Exception e) {
            System.out.println("✓ By.id() - No elements found with id 'main'");
        }
        
        // By Class Name
        try {
            List<WebElement> elementsByClass = driver.findElements(By.className("container"));
            System.out.println("✓ By.className() - Found " + elementsByClass.size() + " elements");
        } catch (Exception e) {
            System.out.println("✓ By.className() - No elements found with class 'container'");
        }
        
        // By Tag Name
        try {
            List<WebElement> divs = driver.findElements(By.tagName("div"));
            System.out.println("✓ By.tagName() - Found " + divs.size() + " div elements");
        } catch (Exception e) {
            System.out.println("✓ By.tagName() - Error finding divs");
        }
        
        // By CSS Selector
        try {
            List<WebElement> elementsByCSS = driver.findElements(By.cssSelector("a[href]"));
            System.out.println("✓ By.cssSelector() - Found " + elementsByCSS.size() + " links");
        } catch (Exception e) {
            System.out.println("✓ By.cssSelector() - Error finding links");
        }
        
        // By XPath
        try {
            List<WebElement> elementsByXPath = driver.findElements(By.xpath("//img"));
            System.out.println("✓ By.xpath() - Found " + elementsByXPath.size() + " images");
        } catch (Exception e) {
            System.out.println("✓ By.xpath() - Error finding images");
        }
    }

    private static void demonstrateWaits() {
        System.out.println("\n--- Wait Strategies ---");
        
        try {
            // Explicit wait for element presence
            WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            System.out.println("✓ ExpectedConditions.presenceOfElementLocated() - Body element found");
            
            // Explicit wait for element visibility
            try {
                WebElement visibleElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1, h2")));
                System.out.println("✓ ExpectedConditions.visibilityOfElementLocated() - Heading visible");
            } catch (Exception e) {
                System.out.println("✓ ExpectedConditions.visibilityOfElementLocated() - No visible heading found");
            }
            
            // Implicit wait demonstration (already set by WebDriverWait)
            System.out.println("✓ Implicit waits configured via WebDriverWait");
            
        } catch (Exception e) {
            System.out.println("  - Error demonstrating waits: " + e.getMessage());
        }
    }

    private static void logInteraction(String type, String elementType, String action, String text, String value, boolean success) {
        try {
            interactionCount++;
            String[] row = {
                String.valueOf(interactionCount),
                type,
                elementType,
                action,
                text.length() > 100 ? text.substring(0, 100) + "..." : text,
                value.length() > 100 ? value.substring(0, 100) + "..." : value,
                String.valueOf(success),
                driver.getCurrentUrl(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            };
            csvWriter.writeNext(row);
            csvWriter.flush();
        } catch (Exception e) {
            System.err.println("Error logging interaction: " + e.getMessage());
        }
    }

    private static void takeScreenshot(String name) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path screenshotPath = Paths.get("output", name + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png");
            Files.copy(screenshot.toPath(), screenshotPath);
            System.out.println("✓ Screenshot saved: " + screenshotPath.getFileName());
        } catch (Exception e) {
            System.out.println("  - Screenshot failed: " + e.getMessage());
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

