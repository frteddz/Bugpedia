package com.frteddz.bugpedia.client;

import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class BugSync {
	public static final String DATA_URL = "https://raw.githubusercontent.com/frteddz/bugpedia-data/main/bugs.json";
	private static final Logger LOGGER = LoggerFactory.getLogger(BugSync.class);

	private BugSync() {
	}

	public static void sync(Minecraft client, BugStore store) {
		Thread thread = new Thread(() -> refresh(client, store), "Bugpedia sync");
		thread.setDaemon(true);
		thread.start();
	}

	private static void refresh(Minecraft client, BugStore store) {
		String cached = readCache(cacheFile());
		if (cached != null) {
			applyJson(store, cached);
			refreshScreen(client);
		}
		String fresh = download();
		if (fresh != null) {
			writeCache(cacheFile(), fresh);
			applyJson(store, fresh);
		} else if (cached == null) {
			store.setFailed();
		}
		refreshScreen(client);
	}

	private static void applyJson(BugStore store, String json) {
		try {
			List<Bug> bugs = Bug.parseBugs(json);
			store.setLoaded(bugs, Bug.parseUpdated(json));
		} catch (RuntimeException e) {
			LOGGER.warn("bugpedia could not parse bugs.json", e);
			store.setFailed();
		}
	}

	private static String download() {
		try {
			HttpURLConnection connection = (HttpURLConnection) URI.create(DATA_URL).toURL().openConnection();
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);
			try (InputStream in = connection.getInputStream()) {
				return new String(in.readAllBytes(), StandardCharsets.UTF_8);
			} finally {
				connection.disconnect();
			}
		} catch (IOException | RuntimeException e) {
			LOGGER.warn("bugpedia could not download bugs.json", e);
			return null;
		}
	}

	private static Path cacheFile() {
		return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("bugpedia/bugs.json");
	}

	private static String readCache(Path file) {
		try {
			if (Files.isRegularFile(file)) {
				return Files.readString(file, StandardCharsets.UTF_8);
			}
		} catch (IOException | RuntimeException e) {
			LOGGER.warn("bugpedia could not read the bugs cache", e);
		}
		return null;
	}

	private static void writeCache(Path file, String json) {
		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, json, StandardCharsets.UTF_8);
		} catch (IOException | RuntimeException e) {
			LOGGER.warn("bugpedia could not write the bugs cache", e);
		}
	}

	private static void refreshScreen(Minecraft client) {
		client.execute(() -> {
			if (client.screen instanceof BugpediaScreen browser) {
				browser.rebuildEntries();
			}
		});
	}
}
