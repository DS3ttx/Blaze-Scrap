import db.BlazeDB;
import model.Round;
import model.Entry;
import java.util.List;
import java.util.Objects;
import java.util.HashSet;
import scrapper.BlazeScraper;


public class Main {
	public static void main(String[] args) {
		BlazeDB database = new BlazeDB();
		BlazeScraper scrapper = new BlazeScraper();
		HashSet<Entry> roundBets = new HashSet<>();
		
		int actualRoundId = 0;
		boolean processed = false;
		boolean completed = false;
		
		scrapper.openSite();
		
		while (true) {
			String state = scrapper.getRouletteState();
			Round actualRound = new Round(actualRoundId);
			
			if (Objects.equals(state, "page waiting")) {
				if (processed) {
					processed = false;
					completed = false;
					scrapper.clearEntries();
					System.out.println("Iniciando round: " + actualRoundId);
				}
				
				try {
					List<List<String>> entries = scrapper.captureEntries();
					
					for (List<String> tuple : entries) {
						Entry bet = new Entry(tuple.get(0), tuple.get(1), tuple.get(2));
						roundBets.add(bet);
					}
					
				} catch (org.openqa.selenium.StaleElementReferenceException e) {
					System.out.println("Erro com StaleElement.");
				}
				
			} else if (Objects.equals(state, "page rolling") && !processed) {
				actualRoundId++;
				processed = true;
				
				System.out.println("Total de apostas: " + roundBets.size());
				database.saveBets(actualRoundId, roundBets);
				scrapper.disableCapture();
				roundBets.clear();
				
			} else if (Objects.equals(state, "page complete") && !completed) {
				String winner = scrapper.getWinner();
				actualRound.setWinner(winner);
				database.saveRound(actualRound);
				completed = true;
				
				System.out.println("Vencedor: " + winner + " " + actualRound.getWinner());
			}
		}
		
		//driver.quit();
	}
}
