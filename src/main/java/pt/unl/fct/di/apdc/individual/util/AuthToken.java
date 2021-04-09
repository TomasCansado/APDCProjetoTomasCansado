package pt.unl.fct.di.apdc.individual.util;
import java.util.UUID;

public class AuthToken {
	private static final long EXPIRATION_TIME = 1000*60*60*2; //2h
	private String username;
	private String tokenID;
	private long creationData;
	private long expirationData;
	
	public AuthToken() {
		
	}
	
	public AuthToken(String username) {
		this.username = username;
		this.tokenID = UUID.randomUUID().toString();
		this.creationData = System.currentTimeMillis();
		this.expirationData = this.creationData + AuthToken.EXPIRATION_TIME;
	}

	public String getUsername() {
		return username;
	}

	public String getTokenID() {
		return tokenID;
	}

	public long getExpirationData() {
		return expirationData;
	}
	
	public long getCreationData() {
		return creationData;
	}
}