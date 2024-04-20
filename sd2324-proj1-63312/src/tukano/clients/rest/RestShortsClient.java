package tukano.clients.rest;

import java.util.List;
import java.net.URI;

import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;

import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tukano.api.java.Result.ErrorCode;
import tukano.api.rest.RestShorts;

public class RestShortsClient extends RestClient implements Shorts{


	final WebTarget target;

    public RestShortsClient( URI serverURI ) {
		super(serverURI);
		target = client.target( serverURI ).path( RestShorts.PATH );
	}

    @Override
    public Result<Short> createShort(String userId, String password) {
        return super.reTry(() -> clt_createShort(userId, password));
    }
    private Result<Short> clt_createShort(String userId, String password){
        Response r = target.path( userId )
                .queryParam(RestShorts.PWD, password).request()
                .accept(MediaType.APPLICATION_JSON)
                .post(null);
        return super.toJavaResult(r, Short.class);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        return super.reTry(() -> clt_deleteShort(shortId, password));
    }
    private Result<Void> clt_deleteShort(String shortId, String password){
        Response r = target.path( shortId )
                    .queryParam(RestShorts.PWD, password).request()
                    .accept(MediaType.APPLICATION_JSON)
                    .delete();
        return super.toJavaResult(r, Void.class);
    }

    @Override
    public Result<Short> getShort(String shortId) {
        return super.reTry(() -> clt_getShort(shortId));
	
    }
    private Result<Short> clt_getShort(String shortId){
        Response r = target.path( shortId ).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
        return super.toJavaResult(r, Short.class);
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        return super.reTry(() -> clt_getShorts(userId));	
    }
    private Result<List<String>> clt_getShorts(String userId){
        Response r = target.path( userId ).request()
					.accept(MediaType.APPLICATION_JSON)
					.get();
        return super.toJavaResult(r, new GenericType<List<String>>() {});
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        return super.reTry(() -> clt_follow(userId1, userId2, isFollowing, password));               
    }
    private Result<Void> clt_follow(String userId1, String userId2, boolean isFollowing, String password){
        Response r = target.path(userId1).path(userId2)
                    .queryParam(RestShorts.PWD, password).request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(isFollowing, MediaType.APPLICATION_JSON));
        return super.toJavaResult(r, Void.class);
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        return super.reTry(() -> clt_followers(userId, password));		
    }
    private Result<List<String>> clt_followers(String userId, String password){
        Response r = target.path(userId)
                    .queryParam(RestShorts.PWD, password).request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();
        return super.toJavaResult(r, new GenericType<List<String>>() {});
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        return super.reTry(() -> clt_like(shortId, userId, isLiked, password)); 
    }
    private Result<Void> clt_like(String shortId, String userId, boolean isLiked, String password){
        Response r = target.path(shortId).path(userId)
                    .queryParam(RestShorts.PWD, password).request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(isLiked, MediaType.APPLICATION_JSON));
        return super.toJavaResult(r, Void.class);
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        return super.reTry(() -> clt_likes(shortId, password));			
    }
    private Result<List<String>> clt_likes(String shortId, String password){
        Response r = target.path(shortId)
                    .queryParam(RestShorts.PWD, password).request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();
        return super.toJavaResult(r, new GenericType<List<String>>() {});
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        return super.reTry(() -> clt_getFeed(userId, password));	
    }
    private Result<List<String>> clt_getFeed(String userId, String password){
        Response r = target.path(userId)
                    .queryParam(RestShorts.PWD, password).request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();
        return super.toJavaResult(r, new GenericType<List<String>>() {});
    }

    @Override
    public Result<Void> deleteShorts(String userId) {
        return super.reTry(() -> clt_deleteShorts(userId));
    }
    private Result<Void> clt_deleteShorts(String userId) {
        Response r = target.request()
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(userId, MediaType.APPLICATION_JSON));
        return super.toJavaResult(r, Void.class);
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
