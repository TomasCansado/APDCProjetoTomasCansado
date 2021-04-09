package pt.unl.fct.di.apdc.individual.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtraProfileData extends RequestData {
	private String profile;
	private String phone;
	private String cellphone;
	private String address;
	private String complAddress;
	private String localidade;
	private String password;
	private String email;

	public ExtraProfileData() {

	}

	public ExtraProfileData(String username, String token,String password, String email  ,String state,String phone,String cellphone,String address,String complAddress,String localidade) {
		super(username,token);
		this.password=password;
		this.email=email;
		this.profile=state;
		this.phone=phone;
		this.cellphone=cellphone;
		this.address=address;
		this.complAddress=complAddress;
		this.localidade=localidade;
	}

	public boolean dataValidation() {
		return pwValidation()&& emailValidation()&&profileValidation() && phoneValidation() && cellphoneValidation()&& localidadeValidation() ;
	}

	//returns true if pw is valid
		public boolean pwValidation() {
			if(password==null) {
				return true;
			}
			return password.length()>=7;
		}
		
		//returns true if email is valid
		public boolean emailValidation() {
			if(email==null) {
				return true;
			}
			if(!email.contains("@")||!email.contains(".")) {
				return false;
			}
			String dns = email.substring(email.lastIndexOf("."));
			String emailName=email.substring(0, email.lastIndexOf("@"));
			String emailBetween= email.substring(email.lastIndexOf("@"), email.lastIndexOf("."));
			return !dns.contains("@") && !emailName.isEmpty() && !emailBetween.isEmpty();
		}
	
	//retorna true se for valido
	private boolean profileValidation() {
		if(profile==null) {
			return true;
		}
		return profile.equals("Publico")||profile.equals("Privado");
	}

	//retorna true se o telefone for valido
	private boolean cellphoneValidation() {
		if (cellphone!=null) {
			String regexTelMovel = "([+]351\\s)?[9][0-9]{8}";
			Pattern telMovelPattern = Pattern.compile(regexTelMovel);
			Matcher telMovelMatcher = telMovelPattern.matcher(cellphone);
			if (!telMovelMatcher.matches()) {
				return false;
			}	
		}
		return true;
	}

	private boolean phoneValidation() {
		if(phone!=null) {
			String regexTelFixo =  "([+]351\\s)?[2][0-9]{8}";
			Pattern telFixoPattern = Pattern.compile(regexTelFixo);
			Matcher telFixoMatcher = telFixoPattern.matcher(phone);
			if (!telFixoMatcher.matches())
				return false;		
		}
		return true;
	}
	
	private boolean localidadeValidation() {
		if(localidade!=null) {
			if(localidade.length()<14) {
				return false;
			}
		}
		return true;
	}
	
	public String getProfile() {
		return profile;
	}

	public String getPhone() {
		return phone;
	}

	public String getCellphone() {
		return cellphone;
	}

	public String getAddress() {
		return address;
	}

	public String getComplAddress() {
		return complAddress;
	}

	public String getLocalidade() {
		return localidade;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}
}
