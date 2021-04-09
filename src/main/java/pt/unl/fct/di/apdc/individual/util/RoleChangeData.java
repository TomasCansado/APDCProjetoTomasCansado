package pt.unl.fct.di.apdc.individual.util;

public class RoleChangeData extends RequestData {
	private String target;
	private String newRole;
	
	public RoleChangeData() {
		
	}
	
	public RoleChangeData(String username,String token, String target, String newRole) {
		super(username,token);
		this.target=target;
		this.newRole=newRole;
	}
	
	public boolean dataValidation() {
		return newRole.equals("USER")||newRole.equals("GA")||newRole.equals("GBO"); 
	}
	
	public String getTarget() {
		return target;
	}

	public String getNewRole() {
		return newRole;
	}
	
}
