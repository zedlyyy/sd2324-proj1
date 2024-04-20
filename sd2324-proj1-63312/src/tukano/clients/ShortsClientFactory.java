package tukano.clients;

import tukano.clients.rest.*;
import tukano.discovery.Discovery;

import java.net.URI;

import tukano.api.java.Shorts;

public class ShortsClientFactory {

    public static Shorts getClient() {
        URI serverURI[] = Discovery.getInstance().knownUrisOf("shorts", 1);
        return new RestShortsClient( serverURI[0] );

 }
}
