package scrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class BlazeScraper {
	private WebDriver driver;
	private WebDriverWait wait;
	private JavascriptExecutor jsExecutor;
	
	public BlazeScraper() {
		this.driver = new FirefoxDriver();
		this.jsExecutor = (JavascriptExecutor) driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	}
	
	public void openSite() {
		this.driver.get("https://blaze.com/pt/games/double");
	}
	
	public List<List<String>> captureEntries() {
		try {
			return (List<List<String>>) this.jsExecutor.executeScript(Scripts.OBSERVER);
		} catch (Exception e) {
			System.out.println("Erro ao capturar entradas: " + e.getMessage());
			return List.of();
		}
	}
	
	public void disableCapture() {
		this.jsExecutor.executeScript(Scripts.DISABLE_OBSERVER);
	}
	
	public void clearEntries() {
		this.jsExecutor.executeScript(Scripts.CLEAR_ENTRIES);
	}
	
	public String getRouletteState() {
		try {
			WebElement roulette = wait.until(presenceOfElementLocated(By.id("roulette")));
			return roulette.getDomAttribute("class");
		} catch (Exception e) {
			return "undefined";
		}
	}
	
	public String getWinner() {
		try {
			WebElement history = wait.until(presenceOfElementLocated(By.id("roulette-recent")));
			WebElement lastResult = history.findElement(By.className("roulette-tile"));
			WebElement result = lastResult.findElement(By.xpath("./div"));
			return result.getDomAttribute("class").replace("sm-box ", "");
		} catch (Exception e) {
			return "undefined";
		}
	}
	
	public void close() {
		this.driver.close();
	}
}