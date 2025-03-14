package model;

import java.util.Objects;

public class Entry {
	public String userId;
	public float amount;
	public int color = 0;
	
	public Entry(String uid, String betValue, String betColor) {
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