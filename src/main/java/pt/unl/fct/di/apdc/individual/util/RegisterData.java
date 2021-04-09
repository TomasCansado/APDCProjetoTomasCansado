package pt.unl.fct.di.apdc.individual.util;

public class RegisterData {
	
	private String username;
	private String password;
	private String pwConfirmation;
	private String email;
	
	public RegisterData() {
		
	}
	public RegisterData(String username, String pw, String pwCon, String email) {
		this.username=username;
		this.password=pw;
		this.pwConfirmation=pwCon;
		this.email=email;
	}
	
	public boolean validRegistration() {
		if(username==null || password==null || pwConfirmation==null || !pwValidation() || !emailValidation()) {
			return false;
		}
		return true;
		
	}
	
	//returns true if pw is valid
	public boolean pwValidation() {
		return password.equals(pwConfirmation) && password.length()>=7;
	}
	
	//returns true if email is valid
	public boolean emailValidation() {
		if(!email.contains("@")||!email.contains(".")) {
			return false;
		}
		String dns = email.substring(email.lastIndexOf("."));
		String emailName=email.substring(0, email.lastIndexOf("@"));
		String emailBetween= email.substring(email.lastIndexOf("@"), email.lastIndexOf("."));
		return !dns.contains("@") && !emailName.isEmpty() && !emailBetween.isEmpty();
	}
	
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}

	public String getPwConfirmation() {
		return pwConfirmation;
	}

}
