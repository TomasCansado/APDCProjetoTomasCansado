
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



@Path("/rolec")
@Consumes(MediaType.APPLICATION_JSON)
public class ChangeRoleResource {
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG= Logger.getLogger(LoginResource.class.getName());


	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doChange(RoleChangeData data) {
		LOG.fine("Logout attempt by user: "+data.getUsername());

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
		Key targetKey = datastore.newKeyFactory().setKind("User").newKey(data.getTarget());
		Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.getTokenID());
		Transaction txn=datastore.newTransaction();
		try {
			Entity user=txn.get(userKey);
			Entity token=txn.get(tokenKey);
			Entity target=txn.get(targetKey);
			if(user==null) {
				LOG.warning("Failed role change attempt for username: "+data.getUsername()+". User doesnt exist.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}if(target==null) {
				LOG.warning("Failed role change attempt for username: "+data.getUsername()+". Target doesnt exist.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			if(token==null) {
				LOG.warning("Failed role attempt for username: "+data.getUsername()+". Token doesnt exist.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			if(!token.getString("token_user").equals(data.getUsername())) {
				LOG.warning("Failed logout attempt for username: "+data.getUsername()+". User doesnt correspond to token");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			if(user.getString("removed").equals("removed")) {
				LOG.warning("Failed logout attempt for username: "+data.getUsername()+". User was removed");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			if(user.getString("user_State").equals("disabled")) {
				LOG.warning("Failed logout attempt for username: "+data.getUsername()+". User was disabled");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			if(token.getLong("token_expiration_date")<System.currentTimeMillis()) {
				LOG.warning("Failed logout attempt for username: "+data.getUsername()+". Token has expired.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			if(user.getString("user_Role").equals("user")||user.getString("user_Role").equals("gbo")) {
				LOG.warning("Failed change role attempt for username: "+data.getTarget()+". User doesnt have access to this functionality.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			if(user.getString("user_Role").equals("ga")&&!(data.getNewRole().equals("gbo")||data.getNewRole().equals("user"))){
				LOG.warning("Failed change role attempt for username: "+data.getTarget()+". User doesnt have access to this functionality.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			target = Entity.newBuilder(targetKey).set("user_name", target.getString("user_name"))
					.set("user_pwd", target.getString("user_pwd"))
					.set("user_email", target.getString("user_email"))
					.set("user_Role", data.getNewRole())
					.set("user_State", target.getString("user_State"))
					.set("user_Address", target.getString("user_Address"))
					.set("user_Address2", target.getString("user_Address2"))
					.set("user_Phone", target.getString("user_Phone"))
					.set("user_Cellphone", target.getString("user_Cellphone"))
					.set("user_Localidade", target.getString("user_Localidade"))
					.set("user_Profile", target.getString("user_Profile"))
					.set("removed", target.getString("removed"))
					.build();
			txn.put(target);
			LOG.info("User role modified "+data.getTarget());
			txn.commit();
			return Response.ok("{}").build();
		}finally {
			if(txn.isActive()) {
				txn.rollback();
			}
		}
	}


}


