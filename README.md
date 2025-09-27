## Selenium Scraper (GreenChef) — Java + Maven

### Prerequisites
- Java 11+
- Maven 3.8+
- Google Chrome installed (WebDriverManager will fetch a matching driver automatically)

### How to build and run
```bash
mvn -q -e -DskipTests clean compile
mvn -q exec:java -Dexec.mainClass="app.Main"
```

Outputs are written to the `output/` directory with a timestamp:
- `scraped_data_YYYYMMDD_HHMMSS.csv`
- `homepage_YYYYMMDD_HHMMSS.html`
- `homepage_YYYYMMDD_HHMMSS.png`

To run headless, open `src/main/java/app/Main.java` and uncomment the `--headless=new` Chrome option.

### Notes
- Default target URL: `https://www.greenchef.com/` (edit `baseUrl` in `Main` if needed).
- CSS selectors in the sample are generic. Inspect the site and replace them with stable selectors for your report.
- If your environment disallows WebDriverManager, manually install ChromeDriver and set:
  ```java
  System.setProperty("webdriver.chrome.driver", "C:/path/to/chromedriver.exe");
  ```



