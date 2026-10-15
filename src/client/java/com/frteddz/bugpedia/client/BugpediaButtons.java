package com.frteddz.bugpedia.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class BugpediaButtons {
	public static final String LABEL = "Bug-o-pedia";

	private BugpediaButtons() {
	}

	public static Button create(Screen current) {
		return Button.builder(Component.literal(LABEL), pressed -> Minecraft.getInstance().setScreen(new BugpediaScreen(current)))
			.bounds(10, 10, 110, 20)
			.build();
	}
}
