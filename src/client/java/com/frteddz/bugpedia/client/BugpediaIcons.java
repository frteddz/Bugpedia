package com.frteddz.bugpedia.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BugpediaIcons {
	public static final ItemStack SEARCH = new ItemStack(Items.SPYGLASS);
	public static final ItemStack REFRESH = new ItemStack(Items.CLOCK);
	public static final ItemStack CLEAR = new ItemStack(Items.BARRIER);
	public static final ItemStack BUG = new ItemStack(Items.SILVERFISH_SPAWN_EGG);

	private BugpediaIcons() {
	}

	public static ItemStack categoryIcon(String category) {
		return switch (category) {
			case "sound" -> new ItemStack(Items.NOTE_BLOCK);
			case "crash" -> new ItemStack(Items.TNT);
			case "gameplay" -> new ItemStack(Items.GRASS_BLOCK);
			case "graphics" -> new ItemStack(Items.GLASS);
			default -> BUG;
		};
	}

	public static void draw(GuiGraphics graphics, ItemStack icon, int x, int y) {
		graphics.renderItem(icon, x, y);
	}
}
