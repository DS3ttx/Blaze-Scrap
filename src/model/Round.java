package model;

import java.util.Objects;

public class Round {
	private final int id;
	private int winner;
	
	public Round(int rid) {
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