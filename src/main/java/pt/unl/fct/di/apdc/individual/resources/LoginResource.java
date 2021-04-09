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



@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
public class LoginResource {
	private final Gson g = new Gson();
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG= Logger.getLogger(LoginResource.class.getName());


	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON +";charset=utf-8")
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by user: "+data.getUsername());
		
		Key userKey =datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
		Transaction txn=datastore.newTransaction();
		try {
			Entity user=txn.get(userKey);
			if(user==null) {
				LOG.warning("Failed login attempt for username: "+data.getUsername());
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			if(user.getString("removed").equals("removed")) {
				LOG.warning("Failed login attempt for username: "+data.getUsername()+". User has been removed.");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			if(user.getString("user_State").equals("disabled")) {
				LOG.warning("Failed logout attempt for username: "+data.getUsername()+". User is disabled");
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
			String hashedPWD=(String) user.getString("user_pwd");
			if(hashedPWD.equals(DigestUtils.sha512Hex(data.getPassword()))) {
				AuthToken token= new AuthToken(data.getUsername());
				LOG.info("User "+data.getUsername()+" logged in sucessfully.");
				Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(token.getTokenID());
				user = Entity.newBuilder(tokenKey).set("tokenID",token.getTokenID())
						.set("token_expiration_date",token.getExpirationData())
						.set("token_creation_date",token.getCreationData() )
						.set("token_user", token.getUsername()).build();
				txn.add(user);
				LOG.info("Token registered "+data.getUsername());
				txn.commit();
				return Response.ok(g.toJson(token)).build();
			}else {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).build();
			}
		}finally {
			if(txn.isActive()) {
				txn.rollback();
			}
		}
	}
	

}
