package db;

import java.sql.*;
import model.Round;
import model.Entry;
import java.util.HashSet;

public class BlazeDB {
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