package tukano.servers.rest;


import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.rest.RestBlobs;
import tukano.servers.java.JavaBlobs;

@Singleton
public class RestBlobsResourse implements RestBlobs{

    final Blobs impl;
    public RestBlobsResourse() {
        this.impl = new JavaBlobs();
    }
    
    @Override
    public void upload(String blobId, byte[] bytes) {
        resultOrThrow( impl.upload(blobId, bytes) );
    }

    @Override
    public byte[] download(String blobId) {
        return resultOrThrow(impl.download(blobId));
    }
	@Override
	public void deleteBlob(String shortId) {
		resultOrThrow(impl.deleteBlob(shortId));
	}
    /**
	 * Given a Result<T>, either returns the value, or throws the JAX-WS Exception
	 * matching the error code...
	 */
	protected <T> T resultOrThrow(Result<T> result) {
		if (result.isOK())
			return result.value();
		else
			throw new WebApplicationException(statusCodeFrom(result));
	}

	/**
	 * Translates a Result<T> to a HTTP Status code
	 */
	private static Status statusCodeFrom(Result<?> result) {
		return switch (result.error()) {
			case CONFLICT -> Status.CONFLICT;
			case NOT_FOUND -> Status.NOT_FOUND;
			case FORBIDDEN -> Status.FORBIDDEN;
			case BAD_REQUEST -> Status.BAD_REQUEST;
			case INTERNAL_ERROR -> Status.INTERNAL_SERVER_ERROR;
			case NOT_IMPLEMENTED -> Status.NOT_IMPLEMENTED;
			case OK -> result.value() == null ? Status.NO_CONTENT : Status.OK;
			default -> Status.INTERNAL_SERVER_ERROR;
		};
	}

	

}
