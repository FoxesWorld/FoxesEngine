package org.foxesworld.engine.fileLoader;

import com.google.gson.Gson;
import org.foxesworld.engine.Engine;
import org.foxesworld.engine.utils.HTTP.HTTPrequest;
import org.foxesworld.engine.utils.HTTP.HttpParam;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;


public class FileFetcher extends HTTPrequest {

    @HttpParam
    private final  String sysRequest = "loadFiles";
    @HttpParam
    private String version, client, platform;

    public FileFetcher(Engine engine) {
        super(engine, "POST");
    }

    public CompletableFuture<FileAttributes[]> fetchDownloadList(String client, String version, int platform) {
        this.version = version;
        this.client = client;
        this.platform = String.valueOf(platform);

        CompletableFuture<FileAttributes[]> future = new CompletableFuture<>();

        this.sendAsync(Collections.emptyMap(), response -> {
            FileAttributes[] fileAttributes = new Gson().fromJson(String.valueOf(response), FileAttributes[].class);
            future.complete(fileAttributes);
        }, future::completeExceptionally);

        return future;
    }

}
