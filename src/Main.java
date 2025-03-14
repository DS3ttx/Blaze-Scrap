import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.HashSet;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

class BlazeDB {
	private Connection conn;
	
	public BlazeDB() {
		String url = "jdbc:sqlite:blaze_bets.db";
		
		try {
			this.conn = DriverManager.getConnection(url);
			this.conn.setAutoCommit(false);
			
			if (this.conn != null) {
				Statement stmt = this.conn.createStatement();
				
				String sqlCreateTable = """
						CREATE TABLE IF NOT EXISTS round (
						id INTEGER PRIMARY KEY,
						datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
						winner INTEGER NOT NULL
						);
						CREATE TABLE IF NOT EXISTS bet (
						userId TEXT NOT NULL,
						amount REAL NOT NULL,
						color INTEGER NOT NULL,
						roundId INTEGER NOT NULL
						);""";
				// FOREIGN KEY(roundId) REFERENCES round(id) -- retirado para efficiency
				stmt.executeUpdate(sqlCreateTable);
				stmt.close();
			}
			
		} catch (SQLException e) {
			System.out.println("Erro: " + e.getMessage());
		}
	}
	
	public final void rollback() {
		try {
			if (conn != null) {
				conn.rollback();
			}
		} catch (SQLException ex) {
			System.out.println("Erro ao fazer rollback: " + ex.getMessage());
		}
	}
	
	public final void close() {
		try {
			this.conn.close();
		} catch (SQLException e) {
			System.out.println("Erro ao fechar conexão: " + e.getMessage());
		}
	}
	
	public final void saveRound(Round r) {
		String sqlInsert = "INSERT INTO round(id, winner) VALUES (?, ?)";
		
		try (PreparedStatement stmt = this.conn.prepareStatement(sqlInsert)) {
			stmt.setInt(1, r.getId());
			stmt.setInt(2, r.getWinner());
			stmt.executeUpdate();
			this.conn.commit();
		} catch (SQLException e) {
			this.rollback();
			System.out.println("Erro ao inserir novo round: " + e.getMessage());
		}
	}
	
	public final void saveBets(int rid, HashSet<Entry> bets) {
		String sqlInsert = "INSERT INTO bet(userId, amount, color, roundId) VALUES (?, ?, ?, ?)";
		
		try (PreparedStatement stmt = this.conn.prepareStatement(sqlInsert)) {
			
			for (Entry bet : bets) {
				System.out.println(bet.userId + " " + bet.amount + " " + bet.color + " " + rid);
				stmt.setString(1, bet.userId);
				stmt.setFloat(2, bet.amount);
				stmt.setInt(3, bet.color);
				stmt.setInt(4, rid);
				stmt.addBatch();
			}
			
			stmt.executeBatch();
			this.conn.commit();
		} catch (SQLException e) {
			this.rollback();
			System.out.println("Erro ao inserir novo round: " + e.getMessage());
		}
	}
}

class Round {
	private final int id;
	private int winner;
	
	Round(int rid) {
		this.id = rid;
	}
	
	public void setWinner(String winner) {
		if (Objects.equals(winner, "red")) {
			this.winner = 0;
		} else if (Objects.equals(winner, "black")) {
			this.winner = 1;
		} else {
			this.winner = 2; // White
		}
	}
	
	public int getId() {
		return this.id;
	}
	
	public int getWinner() {
		return this.winner;
	}
}

class Entry {
	public String userId;
	public float amount;
	public int color = 0;
	
	Entry(String uid, String betValue, String betColor) {
		this.userId = uid;
		this.amount = Float.parseFloat(betValue);
		
		if (Objects.equals(betColor, " red")) {
			this.color = 0;
		} else if (Objects.equals(betColor, "black")) {
			this.color = 1;
		} else {
			this.color = 2; // White
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		Entry other = (Entry) obj;
		return Objects.equals(this.userId, other.userId) && this.amount == other.amount && this.color == other.color;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.userId, this.color, this.amount);
	}
}

public class Main {
	
	static final String SCRIPT = """
			function noLink(userProfile) {
				if (userProfile) {
					return userProfile.replace('/pt/games/double?modal=profile_new&user_id=', '');
				}
			}
			
			function processEntry(entryData, color) {
				var user = entryData.querySelector('div a.user-profile-link').getAttribute('href');
				return [
					noLink(user),
					entryData.querySelector('div.amount').textContent.replace('R$ ', ''),
					color.replace('roulette-column', '')
				];
			}
			
			const observer = new MutationObserver((mutationsList, observer) => {
				for (let mutation of mutationsList) {
				if (mutation.type === 'childList') {
						mutation.addedNodes.forEach(node => {
							if (node.classList && node.classList.contains('entry')) {
								var color = node.parentNode.parentNode.parentNode.getAttribute('class');
								var user_amount = processEntry(node, color);
			
								if (user_amount[0]) {
									window.entriesData = window.entriesData || [];
									window.entriesData.push(user_amount);
								}
							}
						});
					}
				}
			});
			
			const config = { childList: true, subtree: true };
			observer.observe(document.body, config);
			
			return window.entriesData || [];
			""";
	
	private Main() {
	}
	
	public static void main(String[] args) {
		BlazeDB database = new BlazeDB();
		
		int actualRoundId = 0;
		
		boolean processed = false;
		boolean completed = false;
		
		WebDriver driver = new FirefoxDriver();
		JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		
		driver.get("https://blaze.com/pt/games/double");
		HashSet<Entry> roundBets = new HashSet<>();
		
		while (true) {
			WebElement roulette = wait.until(presenceOfElementLocated(By.id("roulette")));
			String state = roulette.getDomAttribute("class");
			
			Round actualRound = new Round(actualRoundId);
			
			if (Objects.equals(state, "page waiting")) {
				if (processed) {
					jsExecutor.executeScript("window.entriesData = [];");
					System.out.println("Iniciando round: " + actualRoundId);
					processed = false;
					completed = false;
				}
				
				try {
					// Executa o script para capturar as entradas a partir do script JavaScript
					List<List<String>> entries = (List<List<String>>) jsExecutor.executeScript(SCRIPT);
					
					for (List<String> tuple : entries) {
						Entry bet = new Entry(tuple.get(0), tuple.get(1), tuple.get(2));
						roundBets.add(bet);
					}
					
				} catch (org.openqa.selenium.StaleElementReferenceException e) {
					System.out.println("Erro com StaleElement, tentando novamente.");
				}
				
			} else if (Objects.equals(state, "page rolling") && !processed) {
				actualRoundId++;
				processed = true;
				
				System.out.println("Total de apostas: " + roundBets.size());
				database.saveBets(actualRoundId, roundBets);
				roundBets.clear();
				
			} else if (Objects.equals(state, "page complete") && !completed) {
				WebElement history = wait.until(presenceOfElementLocated(By.id("roulette-recent")));
				WebElement lastResult = history.findElement(By.className("roulette-tile"));
				WebElement result = lastResult.findElement(By.xpath("./div"));
				String winner = result.getDomAttribute("class").replace("sm-box ", "");
				
				completed = true;
				actualRound.setWinner(winner);
				database.saveRound(actualRound);
				System.out.println("Vencedor: " + winner + " " + actualRound.getWinner());
			}
		}
		
		//driver.quit();
	}
}
