package tukano.clients;
import tukano.clients.rest.*;
import tukano.discovery.Discovery;

import java.net.URI;

import tukano.api.java.Users;

public class UsersClientFactory {

    public static Users getClient() {
        URI serverURI[] = Discovery.getInstance().knownUrisOf("users", 1);
        return new RestUsersClient( serverURI[0] );

 }
}
