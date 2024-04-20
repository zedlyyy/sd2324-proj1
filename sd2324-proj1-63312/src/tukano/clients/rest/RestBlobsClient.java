package tukano.clients.rest;

import tukano.api.java.Blobs;
import tukano.api.java.Result;

import java.net.URI;

import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tukano.api.java.Result.ErrorCode;
import tukano.api.rest.RestBlobs;

public class RestBlobsClient extends RestClient implements Blobs{


	final WebTarget target;

    public RestBlobsClient( URI serverURI ) {
		super( serverURI );
		target = client.target( serverURI ).path( RestBlobs.PATH );
	}

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        return super.reTry(() -> clt_upload(blobId, bytes));
    }
	private Result<Void> clt_upload(String blobId, byte[] bytes) {
		Response r = target.path(blobId).request()
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM));
        return super.toJavaResult(r, Void.class);
	}

    @Override
    public Result<byte[]> download(String blobId) {
        return super.reTry(() -> clt_download(blobId));
    }
	private Result<byte[]> clt_download(String blobId) {
		Response r = target.path( blobId ).request()
					.accept(MediaType.APPLICATION_OCTET_STREAM)
					.get();
		return super.toJavaResult(r, byte[].class);
	}

    @Override
	public Result<Void> deleteBlob(String shortId) {
		return super.reTry(() -> clt_deleteBlob(shortId));
    }
	private Result<Void> clt_deleteBlob(String shortId) {
		Response r = target.request()
					.accept(MediaType.APPLICATION_OCTET_STREAM)
					.post(Entity.entity(shortId, MediaType.APPLICATION_JSON));
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
