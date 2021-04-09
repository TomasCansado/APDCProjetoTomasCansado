
package pt.unl.fct.di.apdc.individual.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;


import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import java.util.logging.Logger;

import pt.unl.fct.di.apdc.individual.util.AuthToken;
import pt.unl.fct.di.apdc.individual.util.LoginData;
import pt.unl.fct.di.apdc.individual.util.RequestData;
import pt.unl.fct.di.apdc.individual.util.RoleChangeData;



@Path("/statec")
@Consumes(MediaType.APPLICATION_JSON)
public class ChangeStateResource {
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG= Logger.getLogger(LoginResource.class.getName());


	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doChange(RequestData data) {
		LOG.fine("State change attempt by user: "+data.getUsername());

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
		Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.getTokenID());
		Transaction txn=datastore.newTransaction();
		try {
			Entity user=txn.get(userKey);
			Entity token=txn.get(tokenKey);
			if(user==null) {
				LOG.warning("Failed state change attempt for username: "+data.getUsername()+". User doesnt exist.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}if(token==null) {
				LOG.warning("Failed state change attempt for username: "+data.getUsername()+". Token doesnt exist.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			Key changerKey =datastore.newKeyFactory().setKind("User").newKey(token.getString("token_user"));
			Entity changer=txn.get(changerKey);
			if(!token.getString("token_user").equals(data.getUsername())) {
				if(changer.getString("user_Role").equals("user")||
						(changer.getString("user_Role").equals("gbo")&&!user.getString("user_Role").equals("user"))
						||changer.getString("user_Role").equals("ga")&&(user.getString("user_Role").equals("su")||user.getString("user_Role").equals("ga"))) {
					LOG.warning("Failed state change attempt for username: "+data.getUsername()+". User doesnt correspond to token");
					txn.rollback();
					return Response.status(Status.FORBIDDEN).build();
				}
			}
			if(changer.getString("removed").equals("removed")) {
				LOG.warning("Failed state change attempt for username: "+data.getUsername()+". User was removed");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			if(changer.getString("user_State").equals("disabled")) {
				LOG.warning("Failed state change attempt for username: "+data.getUsername()+". User was disabled");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			if(token.getLong("token_expiration_date")<System.currentTimeMillis()) {
				LOG.warning("Failed state change attempt for username: "+data.getUsername()+". Token has expired.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			String newState;
			if(user.getString("user_State").equals("disabled")) {
				newState="enabled";
			}else {
				newState="disabled";
			}
			user = Entity.newBuilder(userKey).set("user_name", user.getString("user_name"))
					.set("user_pwd", user.getString("user_pwd"))
					.set("user_email", user.getString("user_email"))
					.set("user_Role", user.getString("user_Role"))
					.set("user_State", newState)
					.set("user_Address", user.getString("user_Address"))
					.set("user_Address2", user.getString("user_Address2"))
					.set("user_Phone", user.getString("user_Phone"))
					.set("user_Cellphone", user.getString("user_Cellphone"))
					.set("user_Localidade", user.getString("user_Localidade"))
					.set("user_Profile", user.getString("user_Profile"))
					.set("removed", user.getString("removed"))
					.build();
			txn.put(user);
			LOG.info("User state modified "+data.getUsername());
			txn.commit();
			return Response.ok("{}").build();
		}finally {
			if(txn.isActive()) {
				txn.rollback();
			}
		}
	}


}


