package tukano.servers.java;

import tukano.api.java.Blobs;
import tukano.api.java.Result;

import java.util.logging.Logger;

import tukano.clients.*;
import tukano.api.java.Result.ErrorCode;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class JavaBlobs implements Blobs{

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        String filePath = "/" + blobId + ".txt";
        //BlobId and ShortId are the same. If short doesn't exist neither does Blob
        if(ShortsClientFactory.getClient().getShort(blobId).error() == ErrorCode.NOT_FOUND){
            Log.info("BlobId not valid");
            return Result.error( ErrorCode.FORBIDDEN );
        };

        File file = new File(filePath);

        if(!file.exists()){
            //Criar ficheiro
            criarOuSubstituirFicheiro(file, bytes);
        } else {
            if(!Arrays.equals(bytes, lerBytesDoArquivo(filePath))){
                Log.info("Bytes don't match");
                return Result.error( ErrorCode.CONFLICT );
            }else {
                //Overide ficheiro
                criarOuSubstituirFicheiro(file,bytes);
            }
        }
        return Result.ok();
    }

    @Override
    public Result<byte[]> download(String blobId) {

        //Blob's short does not exist, meaning, the blob does not exist
        if(ShortsClientFactory.getClient().getShort(blobId).error() == ErrorCode.NOT_FOUND){
            Log.info("User does not exist");
            return Result.error( ErrorCode.NOT_FOUND );
        };

        String filePath = "/" + blobId + ".txt";
        byte[] downloadedBytes = lerBytesDoArquivo(filePath);
        return Result.ok(downloadedBytes);
    }

    //Gerado parcialmente com chatGPT
    private static byte[] lerBytesDoArquivo(String filePath) {
        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
            byte[] bytes = fis.readAllBytes();
            return bytes;
        } catch (IOException e) {
            System.err.println("Erro ao ler os bytes do arquivo: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Result<Void> deleteBlob(String shortId){
        File fileTODelete = new File("/" + shortId + ".txt");
        if(fileTODelete.exists()){
            fileTODelete.delete();
        } else {
            Log.info("Short's blob does not exist");
            return Result.ok();
        }
        return Result.ok();    
    }
    private static void criarOuSubstituirFicheiro(File file, byte[] bytes){
        try (FileOutputStream fos = new FileOutputStream(file, false)) {
            fos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
