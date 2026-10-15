package com.frteddz.bugpedia.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class BugDetailsWidget extends ObjectSelectionList<BugDetailsWidget.LineEntry> {
	public BugDetailsWidget(Minecraft client, int width, int height, int y, int itemHeight) {
		super(client, width, height, y, itemHeight);
	}

	@Override
	public int getRowWidth() {
		return this.getWidth() - 14;
	}

	public void setText(String text, int wrapWidth) {
		this.setContent(List.of(DetailLine.text(Component.literal(text))), wrapWidth);
	}

	public void setLines(List<Component> paragraphs, int wrapWidth) {
		List<DetailLine> lines = new ArrayList<>();
		for (Component paragraph : paragraphs) {
			lines.add(DetailLine.text(paragraph));
		}
		this.setContent(lines, wrapWidth);
	}

	public void setContent(List<DetailLine> paragraphs, int wrapWidth) {
		List<LineEntry> lines = new ArrayList<>();
		for (DetailLine paragraph : paragraphs) {
			List<FormattedCharSequence> wrapped = this.minecraft.font.split(paragraph.text(), wrapWidth);
			if (wrapped.isEmpty()) {
				lines.add(new LineEntry(null, null));
			}
			for (FormattedCharSequence line : wrapped) {
				lines.add(new LineEntry(line, paragraph.url()));
			}
		}
		this.replaceEntries(lines);
	}

	public record DetailLine(Component text, String url) {
		public static DetailLine text(Component line) {
			return new DetailLine(line, null);
		}

		public static DetailLine link(String label, String url) {
			return new DetailLine(Component.literal(label).withStyle(ChatFormatting.BLUE, ChatFormatting.UNDERLINE), url);
		}
	}

	public static class LineEntry extends ObjectSelectionList.Entry<LineEntry> {
		private final FormattedCharSequence line;
		private final String url;

		public LineEntry(FormattedCharSequence line, String url) {
			this.line = line;
			this.url = url;
		}

		@Override
		public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			if (this.line != null) {
				graphics.drawString(Minecraft.getInstance().font, this.line, this.getX() + 4, this.getY() + 2, 0xFFFFFFFF, false);
			}
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
			if (this.url != null) {
				Minecraft client = Minecraft.getInstance();
				Screen parent = client.screen;
				client.setScreen(new ConfirmLinkScreen(confirmed -> {
					if (confirmed) {
						Util.getPlatform().openUri(this.url);
					}
					client.setScreen(parent);
				}, this.url, true));
				return true;
			}
			return false;
		}

		@Override
		public Component getNarration() {
			return Component.literal("");
		}
	}
}
