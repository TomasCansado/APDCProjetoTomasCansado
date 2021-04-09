
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

import java.util.logging.Logger;

import pt.unl.fct.di.apdc.individual.util.*;

@Path("/modify")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ExtraProfileResource {
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegistrationV2(ExtraProfileData data) {
		LOG.fine("Attempt to modify user: "+ data.getUsername());

		if (!data.dataValidation()) {
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
		}
		Transaction txn=datastore.newTransaction();
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
		Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.getTokenID());
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
			else {
				String password;
				String email;
				String address;
				String address2;
				String phone;
				String cellphone;
				String localidade;
				String profile;
				if(data.getPassword()==null) {
					password=user.getString("user_pwd");
				}else {password=data.getPassword();}
				if(data.getEmail()==null) {
					email=user.getString("user_email");
				}else {email=data.getEmail();}
				if(data.getAddress()==null) {
					address=user.getString("user_Address");
				}else {address=data.getAddress();}
				if(data.getComplAddress()==null) {
					address2=user.getString("user_Address2");
				}else {address2=data.getComplAddress();}
				if(data.getPhone()==null) {
					phone=user.getString("user_Phone");
				}else {phone=data.getPhone();}
				if(data.getCellphone()==null) {
					cellphone=user.getString("user_Cellphone");
				}else {cellphone=data.getCellphone();}
				if(data.getLocalidade()==null) {
					localidade=user.getString("user_Localidade");
				}else {localidade=data.getLocalidade();}
				if(data.getProfile()==null) {
					profile=user.getString("user_Profile");
				}else {profile=data.getProfile();}
				user = Entity.newBuilder(userKey).set("user_name", data.getUsername())
						.set("user_pwd", DigestUtils.sha512Hex(password))
						.set("user_email", email)
						.set("user_Role", user.getString("user_Role"))
						.set("user_State", user.getString("user_State"))
						.set("user_Address", address)
						.set("user_Address2", address2)
						.set("user_Phone", phone)
						.set("user_Cellphone", cellphone)
						.set("user_Localidade", localidade)
						.set("user_Profile", profile)
						.set("removed", "")
						.build();
				txn.put(user);
				LOG.info("User modified: "+data.getUsername());
				txn.commit();
				return Response.ok("{}").build();

			}
		}finally{
			if(txn.isActive()) {
				txn.rollback();
			}
		}

	}

}


