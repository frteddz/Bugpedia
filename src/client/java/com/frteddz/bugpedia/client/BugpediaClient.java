package com.frteddz.bugpedia.client;

import net.fabricmc.api.ClientModInitializer;

public class BugpediaClient implements ClientModInitializer {
	public static final BugStore STORE = new BugStore();

	@Override
	public void onInitializeClient() {
	}
}
