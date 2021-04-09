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



@Path("/logout")
@Consumes(MediaType.APPLICATION_JSON)
public class LogoutResource {
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG= Logger.getLogger(LoginResource.class.getName());


	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogout(RequestData data) {
		LOG.fine("Logout attempt by user: "+data.getUsername());	
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
		Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.getTokenID());
		Transaction txn=datastore.newTransaction();
		try {
			Entity user=txn.get(userKey);
			Entity token=txn.get(tokenKey);
			if(user==null) {
				LOG.warning("Failed logout attempt for username: "+data.getUsername()+". User doesnt exist.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			if(token==null) {
				LOG.warning("Failed logout attempt for username: "+data.getUsername()+". Token doesnt exist.");
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
			token= Entity.newBuilder(tokenKey).set("tokenID",token.getString("tokenID"))
					.set("token_expiration_date",System.currentTimeMillis())
					.set("token_creation_date",token.getLong("token_creation_date") )
					.set("token_user", token.getString("token_user")).build();
			txn.put(token);
			LOG.info("User Logged Out.Token updated for user:"+data.getUsername());
			txn.commit();
			return Response.ok("{}").build();
		}finally {
			if(txn.isActive()) {
				txn.rollback();
			}
		}
	}
	

}
