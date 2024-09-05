package org.foxesworld.engine.discord;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import org.foxesworld.engine.Engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Discord implements DiscordListener {

    private final String iconKey;
    private final DiscordRPC lib;
    private final DiscordRichPresence presence;
    private final ExecutorService rpcExecutorService = Executors.newSingleThreadExecutor();
    private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);

    public Discord(Engine engine, String iconKey) {
        this.iconKey = iconKey;
        String applicationId = engine.getEngineData().getAppId();
        lib = DiscordRPC.INSTANCE;
        String steamId = "";
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.ready = (user) -> {
            //Instance.setUserLogin(User.username);
        };
        lib.Discord_Initialize(applicationId, handlers, true, steamId);
        presence = new DiscordRichPresence();
        lib.Discord_UpdatePresence(presence);
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }
    @Override
    public void discordRpcStart(String state, String details, String detailsIcon) {
        presence.startTimestamp = System.currentTimeMillis() / 1000;
        presence.details = details;
        presence.state = state;
        presence.largeImageKey = iconKey;
        presence.smallImageKey = detailsIcon;
        presence.smallImageText = detailsIcon;
        lib.Discord_UpdatePresence(presence);

        rpcExecutorService.submit(() -> {
            while (!shutdownRequested.get() && !Thread.currentThread().isInterrupted()) {
                lib.Discord_RunCallbacks();
                //try {Thread.sleep(500); } catch (InterruptedException e) {}
            }
        });
    }

    private void shutdown() {
        shutdownRequested.set(true);
        lib.Discord_Shutdown();
        rpcExecutorService.shutdown();
        Thread.currentThread().interrupt();
    }
    @Override
    public DiscordRPC getDiscordLib() {
        return lib;
    }
}
