package tukano.clients.rest;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.GenericType;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.User;
import tukano.api.java.Users;
import tukano.api.rest.RestUsers;


public class RestUsersClient extends RestClient implements Users {


	final WebTarget target;
	
	public RestUsersClient( URI serverURI ) {
		super( serverURI );
		target = client.target( serverURI ).path( RestUsers.PATH );
	}
		
	@Override
	public Result<String> createUser(User user) {
		return super.reTry(() -> clt_createUser(user));
	}
	private Result<String> clt_createUser(User user){
		Response r = target.request()
				.post(Entity.entity(user, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, String.class);
	}

	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		return super.reTry(() -> clt_updateUser(userId, password, user));
	}
	private Result<User> clt_updateUser(String userId, String password, User user){
		Response r = target.path( userId )
				.queryParam(RestUsers.PWD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(user, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, User.class);
	}

	@Override
	public Result<User> getUser(String name, String pwd) {
		return super.reTry(() -> clt_getUser(name, pwd));
	}
	private Result<User> clt_getUser(String name, String pwd){
		Response r = target.path( name )
				.queryParam(RestUsers.PWD, pwd).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		return super.toJavaResult(r, User.class);
	}


	@Override
	public Result<Void> getUserIdSv(String userId){		
		return super.reTry(() -> clt_getUserIdSv(userId));		
	}
	private Result<Void> clt_getUserIdSv(String userId){
		Response r = target.path(userId).request()
					.accept(MediaType.APPLICATION_JSON)
					.post(Entity.entity(userId, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, Void.class);
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		return super.reTry(() -> clt_searchUsers(pattern));		
	}
	private Result<List<User>> clt_searchUsers(String pattern){
		Response r = target.request()
					.accept(MediaType.APPLICATION_JSON)
					.get();
		return super.toJavaResult(r, new GenericType<List<User>>() {});
	}

	@Override
	public Result<User> deleteUser(String userId, String password) {
		return super.reTry(() -> clt_deleteUser(userId, password));
	}
	private Result<User> clt_deleteUser(String userId, String password){
		Response r = target.path( userId )
				.queryParam(RestUsers.PWD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.delete();
		return super.toJavaResult(r, User.class);
	}

	
	public static ErrorCode getErrorCodeFrom(int status) {
		return switch (status) {
		case 200, 209 -> ErrorCode.OK;
		case 409 -> ErrorCode.CONFLICT;
		case 403 -> ErrorCode.FORBIDDEN;
		case 404 -> ErrorCode.NOT_FOUND;
		case 400 -> ErrorCode.BAD_REQUEST;
		case 500 -> ErrorCode.INTERNAL_ERROR;
		case 501 -> ErrorCode.NOT_IMPLEMENTED;
		default -> ErrorCode.INTERNAL_ERROR;
		};
	}
}
