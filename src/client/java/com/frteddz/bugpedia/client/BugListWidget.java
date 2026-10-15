package com.frteddz.bugpedia.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

public class BugListWidget extends ObjectSelectionList<BugListWidget.BugEntry> {
	public BugListWidget(Minecraft client, int width, int height, int y, int itemHeight) {
		super(client, width, height, y, itemHeight);
	}

	@Override
	public int getRowWidth() {
		return this.getWidth() - 14;
	}

	public static class BugEntry extends ObjectSelectionList.Entry<BugEntry> {
		private final Minecraft client;
		private final Bug bug;

		public BugEntry(Minecraft client, Bug bug) {
			this.client = client;
			this.bug = bug;
		}

		public Bug bug() {
			return this.bug;
		}

		@Override
		public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			int iconX = this.getX() + 4;
			int iconY = this.getY() + (this.getHeight() - 16) / 2;
			BugpediaIcons.draw(graphics, BugpediaIcons.categoryIcon(this.bug.category()), iconX, iconY);
			int textX = iconX + 16 + 6;
			int maxWidth = this.getX() + this.getWidth() - 4 - textX;
			graphics.drawString(this.client.font, this.bug.id(), textX, this.getY() + 6, 0xFFFFFFFF, false);
			graphics.drawString(this.client.font, this.trim(this.bug.title(), maxWidth), textX, this.getY() + 20, 0xFFAAAAAA, false);
		}

		private String trim(String text, int maxWidth) {
			if (this.client.font.width(text) <= maxWidth) {
				return text;
			}
			while (!text.isEmpty() && this.client.font.width(text + "...") > maxWidth) {
				text = text.substring(0, text.length() - 1);
			}
			return text + "...";
		}

		@Override
		public Component getNarration() {
			return Component.literal(this.bug.id() + " " + this.bug.title());
		}
	}
}
