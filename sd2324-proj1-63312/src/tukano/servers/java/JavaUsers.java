package tukano.servers.java;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.clients.ShortsClientFactory;
import tukano.api.User;
import tukano.api.java.Users;
import tukano.persistence.*;

public class JavaUsers implements Users {

	private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

	@Override
	public Result<String> createUser(User user) {
		Log.info("createUser : " + user);
		// Check if user data is valid
		if(user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null) {
			Log.info("User object invalid.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		
		// Insert user, checking if name already exists
		if( retrieveUser(user.getUserId()) != null ) {
			Log.info("User already exists.");
			return Result.error( ErrorCode.CONFLICT);
		}
		Hibernate.getInstance().persist(user);
		return Result.ok( user.getUserId() );
	}

	@Override
		public Result<User> updateUser(String userId, String pwd, User user) {
			Log.info("updateUser : user = " + userId + "; pwd = " + pwd);

			// Check if user data is valid
			if(userId == null || pwd == null){
				Log.info("Name or Password null.");
				return Result.error( ErrorCode.BAD_REQUEST);
			}
			
			// Check if the user exists
			User userCheck = retrieveUser(userId);

			if( userCheck == null ){
				Log.info("User does not exist");
				return Result.error( ErrorCode.NOT_FOUND );
			}

			// Check if the password is correct
			if( !userCheck.pwd().equals(pwd)){
				Log.info("Wrong Password");
				return Result.error( ErrorCode.FORBIDDEN );
			}

			if( userCheck.getUserId() != user.getUserId() && user.getUserId() != null){
				Log.info("UserId does not match");
				return Result.error( ErrorCode.BAD_REQUEST );
			}
			
			// Valid
			if (user.getPwd() != null) {
				userCheck.setPwd(user.getPwd());
			}
			if (user.getEmail() != null) {
				userCheck.setEmail(user.getEmail());
			}
			if (user.getDisplayName() != null) {
				userCheck.setDisplayName(user.getDisplayName());
			}
			Hibernate.getInstance().update( userCheck );
			return Result.ok( userCheck ); 
		}
	
	@Override
	public Result<User> getUser(String userId, String pwd) {
		Log.info("getUser : user = " + userId + "; pwd = " + pwd);
		
		// Check if user is valid
		if(userId == null || pwd == null) {
			Log.info("Name or Password null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		
		User user = retrieveUser(userId);		
		// Check if user exists 
		if( user == null ) {
			Log.info("User does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		
		//Check if the password is correct
		if( !user.pwd().equals(pwd)) {
			Log.info("Password is incorrect.");
			return Result.error( ErrorCode.FORBIDDEN);
		}
		
		return Result.ok(user);
	}

	@Override
	public Result<Void> getUserIdSv(String userId) {

		// Check if user is valid
		if(userId == null) {
			Log.info("Name null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}

		User user = retrieveUser(userId);	

		// Check if user exists 
		if( user == null ) {
			Log.info("User does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		return Result.ok();	
	}

	@Override
	public Result<User> deleteUser(String userId, String pwd) {

		// Check if user data is valid
		if(userId == null || pwd == null){
			Log.info("Name or Password null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		
		// Check if the user exists
		User user = retrieveUser(userId);

		if( user == null ){
			Log.info("User does not exist");
			return Result.error( ErrorCode.NOT_FOUND);
		}

		// Check if the password is correct
		if( !user.pwd().equals(pwd)){
			Log.info("Wrong Password");
			return Result.error( ErrorCode.FORBIDDEN);
		}
		
		ShortsClientFactory.getClient().deleteShorts(userId);
		Hibernate.getInstance().delete(user);
		return Result.ok(user);
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {

		List<User> users = Hibernate.getInstance().sql("SELECT * FROM User u WHERE LOWER(u.userId) LIKE LOWER" + "('%" + pattern + "%')", User.class);
		List<User> usersNoPwd = new ArrayList<>(users);
		for (User user : usersNoPwd){
			user.setPwd("");
		}
		return Result.ok(usersNoPwd);
	}


	private User retrieveUser(String userId){
		return Hibernate.getInstance().retrieve(User.class, userId);
	}

	
}
