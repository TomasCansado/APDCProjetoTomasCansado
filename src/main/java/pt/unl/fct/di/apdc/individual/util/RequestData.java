package pt.unl.fct.di.apdc.individual.util;

public class RequestData {
	private String username;
	private String tokenID;
	
	public RequestData() {
		
	}
	
	public RequestData(String username,String tokenID) {
		this.username=username;
		this.tokenID=tokenID;
	}

	public String getUsername() {
		return username;
	}

	public String getTokenID() {
		return tokenID;
	}
}
