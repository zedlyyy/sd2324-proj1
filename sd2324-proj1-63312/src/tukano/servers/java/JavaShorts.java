package tukano.servers.java;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import tukano.api.Follow;
import tukano.api.Short;
import tukano.api.Likes;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.clients.*;
import tukano.discovery.Discovery;
import tukano.persistence.*;
import java.util.UUID;
import tukano.api.java.Result.ErrorCode;
import java.util.Comparator;
import java.util.Collections;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;


public class JavaShorts implements Shorts{

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    @Override
    public Result<Short> createShort(String userId, String password) {
        Log.info("createShort : " + userId);

        if(userId == null || password == null) {
			Log.info("User object invalid.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
        ErrorCode error = UsersClientFactory.getClient().getUser(userId, password).error();
        //User does not exist
        if( error == ErrorCode.NOT_FOUND ){
            Log.info("User does not exist");
            return Result.error( ErrorCode.NOT_FOUND );
        };
        //Passwords do not match
        if( error == ErrorCode.FORBIDDEN ){
            Log.info("Passwords do not match");
            return Result.error( ErrorCode.FORBIDDEN );
        }
        //Timeout
        if( error != ErrorCode.OK){
            return Result.error(error);
        }
        
        //Valid

        //Criação do ShorId e da TimeStamp
        String shortId = UUID.randomUUID().toString();
        long timestampEmMilis = System.currentTimeMillis();

        //Escolha random de um servidor de blobs existente
        URI[] blobsList = Discovery.getInstance().knownUrisOf("blobs", 1);
        int nrBlobs = blobsList.length;
        Random random = new Random();
        int blobNr = random.nextInt(nrBlobs - 0) + 0;
        String blobURI = blobsList[blobNr].toString() + "/blobs/" + shortId;
        //Construção do novo Short colecação dele na BD
        Short newShort = new Short(shortId, userId, blobURI, timestampEmMilis, 0);
        Hibernate.getInstance().persist(newShort);
        return Result.ok(newShort);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        // Check if user data is valid
		if(shortId == null || password == null){
			Log.info("ShortId or Password null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		
		// Check if the user exists
		Short s = retrieveShort(shortId);

		if( s == null ){
			Log.info("Short does not exist");
			return Result.error( ErrorCode.NOT_FOUND);
		}

		// Check if the password is correct
        ErrorCode error = UsersClientFactory.getClient().getUser(s.getOwnerId(), password).error();
		if(error == ErrorCode.FORBIDDEN ){
            Log.info("Passwords do not match");
            return Result.error( ErrorCode.FORBIDDEN );
        }

        //Timeout
        if( error != ErrorCode.OK){
            return Result.error(error);
        }

        //Apagar todas as relações de Likes, likes que foram dados ao short
        List<Likes> shortLikes = Hibernate.getInstance().sql("SELECT * FROM Likes l WHERE l.shortId LIKE" + "('%" + shortId + "%')", Likes.class);
        
        for(Likes elem : shortLikes){
            Hibernate.getInstance().delete(elem);
        }

        try {
            clearShorBlob(shortId);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
		Hibernate.getInstance().delete(s);
		return Result.ok();
    }

    @Override
    public Result<Short> getShort(String shortId) {
        Log.info("getShort : short = " + shortId);
			
		Short s = retrieveShort(shortId);		
		// Check if short exists 
		if( s == null ) {
			Log.info("Short does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}

		return Result.ok(s);
    }

    @Override
    public Result<List<String>> getShorts(String userId) {

        ErrorCode error = UsersClientFactory.getClient().getUserIdSv(userId).error();
        //User does not exist
        if( error == ErrorCode.NOT_FOUND ){
            Log.info("User does not exist");
            return Result.error( ErrorCode.NOT_FOUND );
        };
        //Timeout
        if( error != ErrorCode.OK){
            return Result.error(error);
        }
        List<String> shorts = Hibernate.getInstance().sql("SELECT s.shortId FROM Short s WHERE LOWER(s.ownerId) LIKE LOWER" + "('%" + userId + "%')", String.class);
		
        return Result.ok(shorts);
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {

        ErrorCode error1 = UsersClientFactory.getClient().getUserIdSv(userId1).error() ;
        ErrorCode error2 = UsersClientFactory.getClient().getUserIdSv(userId1).error() ;
        //One of the Users don't exist
        if( error1== ErrorCode.NOT_FOUND ||
            error2 == ErrorCode.NOT_FOUND){
            Log.info("One of the Users does not exist");
            return Result.error( ErrorCode.NOT_FOUND );
        }

        ErrorCode error3 = UsersClientFactory.getClient().getUser(userId1, password).error();
        //The password of the userId1 does not match 
        if(UsersClientFactory.getClient().getUser(userId1, password).error() == ErrorCode.FORBIDDEN ){
            Log.info("Passwords do not match");
            return Result.error( ErrorCode.FORBIDDEN );
        }

        //Timeout
        if ( error1 != ErrorCode.OK){
            return Result.error(error1);
        }
        if ( error2 != ErrorCode.OK){
            return Result.error(error2);
        }
        if ( error3 != ErrorCode.OK){
            return Result.error(error3);
        }

        List<Follow> following = Hibernate.getInstance().sql("SELECT * FROM Follow f WHERE f.userFollowing LIKE " + "('%" + userId1 + "%')" + " AND f.UserFollowed LIKE " + "('%" + userId2 + "%')", Follow.class);
        if(!following.isEmpty() && isFollowing){
            Log.info("The user already follows");
             return Result.error( ErrorCode.CONFLICT );
        }
        if(following.isEmpty() && !isFollowing){
            Log.info("The user does not follow ");
            return Result.ok();
        }
        if(following.isEmpty() && isFollowing){
            Follow newFollower = new Follow(userId1, userId2);
            Hibernate.getInstance().persist(newFollower);
            return Result.ok();
        }
        if(!following.isEmpty() && !isFollowing){
            Hibernate.getInstance().delete(following.get(0));
            return Result.ok();
        }
        
        return Result.ok();
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        
        ErrorCode error1 = UsersClientFactory.getClient().getUserIdSv(userId).error() ;
        ErrorCode error2 = UsersClientFactory.getClient().getUser(userId, password).error();

        //User doesn't exist
        if( error1 == ErrorCode.NOT_FOUND){
            Log.info("User does not exist");
            return Result.error( ErrorCode.NOT_FOUND );
        }

        //The password of the userId1 does not match 
        if(error2 == ErrorCode.FORBIDDEN ){
            Log.info("Passwords do not match");
            return Result.error( ErrorCode.FORBIDDEN );
        }
    
        //Timeout
        if ( error1 != ErrorCode.OK){
            return Result.error(error1);
        }
        if ( error2 != ErrorCode.OK){
            return Result.error(error2);
        }

        List<String> followers = Hibernate.getInstance().sql("SELECT f.userFollowing FROM Follow f WHERE f.userFollowed LIKE " + "('%" + userId + "%')", String.class);
        return Result.ok(followers);
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        
        if(shortId == null || userId == null || password == null){
			Log.info("Name/short/passwword null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
        Short s = retrieveShort(shortId);		
		// Check if short exists 
		if( s == null ) {
			Log.info("Short does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}

        ErrorCode error = UsersClientFactory.getClient().getUser(userId, password).error() ;
        //The password of the userId does not match 
        if(error == ErrorCode.FORBIDDEN ){
            Log.info("Passwords do not match");
            return Result.error( ErrorCode.FORBIDDEN );
        }
        //Timeout
        if ( error != ErrorCode.OK){
            return Result.error(error);
        }

        List<Likes> likes = Hibernate.getInstance().sql("SELECT * FROM Likes l WHERE l.shortId LIKE " + "('%" + shortId + "%')" + " AND l.userId LIKE " + "('%" + userId + "%')", Likes.class);
        if(!likes.isEmpty() && isLiked){
            Log.info("The like already exists");
             return Result.error( ErrorCode.CONFLICT );
        }

        if(likes.isEmpty() && !isLiked){
            Log.info("The user didn't like this post");
            return Result.error( ErrorCode.NOT_FOUND);
        }

        if(likes.isEmpty() && isLiked){
            //Novo like
            Short shortOwner = retrieveShort(shortId);
            Likes newLike = new Likes(shortId, shortOwner.getOwnerId(), userId);
            Hibernate.getInstance().persist(newLike);

            //Update do número total de likes
            Short shortToUpdate = retrieveShort(shortId);
            shortToUpdate.setTotalLikes(shortToUpdate.getTotalLikes() + 1);
            Hibernate.getInstance().update(shortToUpdate);

            return Result.ok();
        }

        if(!likes.isEmpty() && !isLiked){
            Hibernate.getInstance().delete(likes.get(0));

            //Update do número total de likes
            Short shortToUpdate = retrieveShort(shortId);
            shortToUpdate.setTotalLikes(shortToUpdate.getTotalLikes() - 1);
            Hibernate.getInstance().update(shortToUpdate);

            return Result.ok();
        }
        return Result.ok();
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        //User doesn't exist
        Short shortLikes = retrieveShort(shortId);

        if( shortLikes == null){
            Log.info("Short does not exist");
            return Result.error( ErrorCode.NOT_FOUND );
        }

        ErrorCode error = UsersClientFactory.getClient().getUser(shortLikes.getOwnerId(), password).error();
        //The password of the ownerId does not match 
        if(error == ErrorCode.FORBIDDEN ){
            Log.info("Passwords do not match");
            return Result.error( ErrorCode.FORBIDDEN );
        }
        //Timeout
        if ( error != ErrorCode.OK){
            return Result.error(error);
        }

        List<String> usersLiked = Hibernate.getInstance().sql("SELECT l.userId FROM Likes l WHERE l.shortId LIKE " + "('%" + shortId + "%')", String.class);
        return Result.ok(usersLiked);
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {

        ErrorCode error = UsersClientFactory.getClient().getUserIdSv(userId).error();

        if( error == ErrorCode.NOT_FOUND){
            Log.info("One of the Users does not exist");
            return Result.error( ErrorCode.NOT_FOUND );
        }

        ErrorCode error1 = UsersClientFactory.getClient().getUser(userId, password).error();

        //The password of the userId1 does not match 
        if(error1 == ErrorCode.FORBIDDEN ){
            Log.info("Passwords do not match");
            return Result.error( ErrorCode.FORBIDDEN );
        }

        //Adicionar todos os shorts dos users que o userId segue
        List<String> follows = Hibernate.getInstance().sql("SELECT f.userFollowed FROM Follow f WHERE f.userFollowing LIKE " + "('%" + userId + "%')", String.class);
        follows.add(userId); //Adicionoar o próprio user para obter os shorts dele
        
        List<Short> feedShortsAll = new ArrayList<>();
        List<String> feedShortsAllId = new ArrayList<>();
        List<Short> feedShortsUser = new ArrayList<>();
    
        for(String s : follows){
            List<String> feedShortsId = Hibernate.getInstance().sql("SELECT s.shortId FROM Short s WHERE LOWER(s.ownerId) LIKE LOWER" + "('%" + s + "%')", String.class);
            
            for(String shorts : feedShortsId){
                feedShortsUser.add(retrieveShort(shorts));
            }
            feedShortsAll.addAll(feedShortsUser);

        }

        //Código gerado com chatGPT
        Comparator<Short> comparador = Comparator.comparing(Short::getTimestamp).reversed();
        Collections.sort(feedShortsAll, comparador);

        for(Short s : feedShortsAll){
            if(!feedShortsAllId.contains(s.getShortId())){
                feedShortsAllId.add(s.getShortId());
            }  
        }

        return Result.ok(feedShortsAllId);
    }

    @Override
    public Result<Void> deleteShorts(String userId) {
    
        //Eleminar todas as relações de follow
            //Users que o userId segue
        List<Follow> userIdFol = Hibernate.getInstance().sql("SELECT * FROM Follow f WHERE f.userFollowing LIKE " + "('%" + userId + "%')", Follow.class);
        for(Follow elem : userIdFol){
            Hibernate.getInstance().delete(elem);
        }
            //Users que seguem o userId
        List<Follow> folUserId = Hibernate.getInstance().sql("SELECT * FROM Follow f WHERE f.userFollowed LIKE " + "('%" + userId + "%')", Follow.class);
        for(Follow elem : folUserId){
            Hibernate.getInstance().delete(elem);
        }
        //Eleminar todos os likes dos shorts do user
            //Shorts que o user deu like (retirar 1 valor do total likes também)
        List<Likes> userIdLikes = Hibernate.getInstance().sql("SELECT * FROM Likes l WHERE l.userId LIKE " + "('%" + userId + "%')", Likes.class);
        for(Likes elem : userIdLikes){

            //Atualizar o número de likes dos shorts que o userId deu like
            Short shortToDislike = retrieveShort(elem.getShortLiked());
            shortToDislike.setTotalLikes(shortToDislike.getTotalLikes() - 1);
            Hibernate.getInstance().update(shortToDislike);

            //Remover a relação de "likes" entre o userId e os shorts que deu like
            Hibernate.getInstance().delete(elem);

        }     
            //Likes que foram dados aos shorts do user
        List<Likes> likedShortsUser = Hibernate.getInstance().sql("SELECT * FROM Likes l WHERE l.ownerId LIKE " + "('%" + userId + "%')", Likes.class);
        for(Likes elem : likedShortsUser){
            Hibernate.getInstance().delete(elem);
        }

        //Eleminar todos os shorts do User e buscar todos as relações "Likes" me que um short do userId foi liked
        List<Short> shortsOfUser = Hibernate.getInstance().sql("SELECT * FROM Short s WHERE s.ownerId LIKE " + "('%" + userId + "%')", Short.class);   
        
        for(Short elem : shortsOfUser){
            try {
                clearShorBlob(elem.getShortId());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            Hibernate.getInstance().delete(elem);
        }

        return Result.ok();
    }

    private Short retrieveShort( String shortId){
        return Hibernate.getInstance().retrieve(Short.class, shortId);
    }

    private void clearShorBlob(String shortId) throws URISyntaxException{
        Short shortToDelete = retrieveShort(shortId);
        BlobsClientFactory.getClient(shortToDelete.getBlobUrl()).deleteBlob(shortId);

    }

}
