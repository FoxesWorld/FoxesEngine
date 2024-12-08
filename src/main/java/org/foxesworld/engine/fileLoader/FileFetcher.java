package org.foxesworld.engine.fileLoader;

import com.google.gson.Gson;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.utils.HTTP.HTTPrequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class FileFetcher {

    private final HTTPrequest postRequest;

    public FileFetcher(Engine engine) {
        this.postRequest = engine.getPOSTrequest();
    }

    public CompletableFuture<FileAttributes[]> fetchDownloadList(String client, String version, int platform) {
        Map<String, Object> request = new HashMap<>();
        request.put("sysRequest", "loadFiles");
        request.put("version", version);
        request.put("client", client);
        request.put("platform", String.valueOf(platform));

        CompletableFuture<FileAttributes[]> future = new CompletableFuture<>();

        postRequest.sendAsync(request, response -> {
            FileAttributes[] fileAttributes = new Gson().fromJson(String.valueOf(response), FileAttributes[].class);
            future.complete(fileAttributes);
        }, future::completeExceptionally);

        return future;
    }
}
