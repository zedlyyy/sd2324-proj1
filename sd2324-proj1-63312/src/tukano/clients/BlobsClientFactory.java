package tukano.clients;

import tukano.clients.rest.*;

import java.net.URI;
import java.net.URISyntaxException;

import tukano.api.java.Blobs;

public class BlobsClientFactory {

    public static Blobs getClient(String uri) throws URISyntaxException {
        URI uriFromString = new URI(uri);
        return new RestBlobsClient( uriFromString );
    }
}
