package com.frteddz.bugpedia.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class DropdownMenu {
	public static final int HEADER_HEIGHT = 20;
	private static final int ITEM_HEIGHT = 12;
	private static final int MAX_VISIBLE = 8;

	private final String label;
	private final List<String> options;
	private final boolean searchable;
	private int selected;
	private boolean open;
	private int scroll;
	private boolean dragging;
	private double dragOffset;
	private String search = "";
	private int x;
	private int y;
	private int width;

	public DropdownMenu(String label, List<String> options, int defaultIndex, boolean searchable) {
		this.label = label;
		this.options = List.copyOf(options);
		this.selected = defaultIndex;
		this.searchable = searchable;
	}

	public String selected() {
		return this.options.get(this.selected);
	}

	public boolean isOpen() {
		return this.open;
	}

	public void setBounds(int x, int y, int width) {
		this.x = x;
		this.y = y;
		this.width = width;
	}

	public void toggle() {
		this.open = !this.open;
		this.search = "";
		this.scroll = 0;
	}

	public void close() {
		this.open = false;
		this.search = "";
		this.dragging = false;
	}

	public void stopDrag() {
		this.dragging = false;
	}

	private List<Integer> visible() {
		List<Integer> shown = new ArrayList<>();
		String query = this.search.toLowerCase();
		for (int i = 0; i < this.options.size(); i++) {
			if (query.isEmpty() || this.options.get(i).toLowerCase().contains(query)) {
				shown.add(i);
			}
		}
		return shown;
	}

	private int panelTop() {
		return this.y + HEADER_HEIGHT + 1;
	}

	private int searchHeight() {
		return this.searchable ? 18 : 0;
	}

	private int itemsTop() {
		return this.panelTop() + this.searchHeight();
	}

	private int panelHeight() {
		return this.searchHeight() + Math.min(this.visible().size(), MAX_VISIBLE) * ITEM_HEIGHT + 4;
	}

	private int trackTop() {
		return this.itemsTop();
	}

	private int trackHeight() {
		return Math.min(this.visible().size(), MAX_VISIBLE) * ITEM_HEIGHT;
	}

	private int thumbHeight() {
		int shown = this.visible().size();
		if (shown <= MAX_VISIBLE) {
			return this.trackHeight();
		}
		return Math.max(4, MAX_VISIBLE * this.trackHeight() / shown);
	}

	private int thumbY() {
		int shown = this.visible().size();
		if (shown <= MAX_VISIBLE) {
			return this.trackTop();
		}
		return this.trackTop() + this.scroll * (this.trackHeight() - this.thumbHeight()) / (shown - MAX_VISIBLE);
	}

	private int scrollFor(double mouseY) {
		int shown = this.visible().size();
		int travel = this.trackHeight() - this.thumbHeight();
		if (travel <= 0) {
			return 0;
		}
		int scroll = (int) Math.round((mouseY - this.dragOffset - this.trackTop()) / travel * (shown - MAX_VISIBLE));
		return Math.min(shown - MAX_VISIBLE, Math.max(0, scroll));
	}

	public boolean handleClick(double mouseX, double mouseY) {
		if (mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + HEADER_HEIGHT) {
			this.toggle();
			return true;
		}
		if (!this.open) {
			return false;
		}
		int top = this.panelTop();
		int height = this.panelHeight();
		if (mouseX < this.x || mouseX >= this.x + this.width || mouseY < top || mouseY >= top + height) {
			return false;
		}
		if (this.visible().size() > MAX_VISIBLE && mouseX >= this.x + this.width - 4 && mouseY >= this.trackTop()
				&& mouseY < this.trackTop() + this.trackHeight()) {
			this.dragging = true;
			if (mouseY < this.thumbY() || mouseY >= this.thumbY() + this.thumbHeight()) {
				this.dragOffset = this.thumbHeight() / 2.0;
				this.scroll = this.scrollFor(mouseY);
			} else {
				this.dragOffset = mouseY - this.thumbY();
			}
			return true;
		}
		int row = ((int) mouseY - this.itemsTop()) / ITEM_HEIGHT;
		List<Integer> shown = this.visible();
		int index = this.scroll + row;
		if (row >= 0 && index < shown.size()) {
			this.selected = shown.get(index);
			this.close();
		}
		return true;
	}

	public boolean mouseDragged(double mouseY) {
		if (!this.open || !this.dragging) {
			return false;
		}
		this.scroll = this.scrollFor(mouseY);
		return true;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (!this.open) {
			return false;
		}
		int top = this.panelTop();
		if (mouseX < this.x || mouseX >= this.x + this.width || mouseY < this.y || mouseY >= top + this.panelHeight()) {
			return false;
		}
		List<Integer> shown = this.visible();
		int maxScroll = Math.max(0, shown.size() - MAX_VISIBLE);
		this.scroll = Math.min(maxScroll, Math.max(0, this.scroll + (amount < 0 ? 1 : -1)));
		return true;
	}

	public boolean typeChar(int codepoint) {
		if (!this.open || !this.searchable) {
			return false;
		}
		this.search += Character.toString(codepoint);
		this.scroll = 0;
		return true;
	}

	public boolean backspace() {
		if (!this.open || !this.searchable) {
			return false;
		}
		if (!this.search.isEmpty()) {
			this.search = this.search.substring(0, this.search.length() - 1);
			this.scroll = 0;
		}
		return true;
	}

	public void render(GuiGraphics graphics, Font font, double mouseX, double mouseY) {
		graphics.fill(this.x, this.y, this.x + this.width, this.y + HEADER_HEIGHT, -16777216);
		outline(graphics, this.x, this.y, this.width, HEADER_HEIGHT, 0xFFAAAAAA);
		graphics.drawString(font, this.label + ": " + this.selected(), this.x + 4, this.y + 6, 0xFFFFFFFF, false);
		int chevronX = this.x + this.width - 8;
		int chevronY = this.y + 8;
		graphics.fill(chevronX - 2, chevronY, chevronX - 1, chevronY + 1, 0xFFFFFFFF);
		graphics.fill(chevronX - 1, chevronY + 1, chevronX, chevronY + 2, 0xFFFFFFFF);
		graphics.fill(chevronX, chevronY + 2, chevronX + 1, chevronY + 3, 0xFFFFFFFF);
		graphics.fill(chevronX + 1, chevronY + 1, chevronX + 2, chevronY + 2, 0xFFFFFFFF);
		graphics.fill(chevronX + 2, chevronY, chevronX + 3, chevronY + 1, 0xFFFFFFFF);
		if (!this.open) {
			return;
		}
		List<Integer> shown = this.visible();
		int top = this.panelTop();
		int height = this.panelHeight();
		graphics.fill(this.x, top, this.x + this.width, top + height, -535818224);
		outline(graphics, this.x, top, this.width, height, 0xFFAAAAAA);
		if (this.searchable) {
			graphics.fill(this.x + 2, top + 2, this.x + this.width - 2, top + 16, -16777216);
			graphics.drawString(font, this.search + "_", this.x + 5, top + 5, 0xFFFFFFFF, false);
		}
		int count = Math.min(shown.size(), MAX_VISIBLE);
		for (int i = 0; i < count; i++) {
			int option = shown.get(this.scroll + i);
			int rowY = this.itemsTop() + i * ITEM_HEIGHT;
			boolean hovered = mouseX >= this.x && mouseX < this.x + this.width && mouseY >= rowY && mouseY < rowY + ITEM_HEIGHT;
			if (hovered) {
				graphics.fill(this.x + 1, rowY, this.x + this.width - 1, rowY + ITEM_HEIGHT, -12303292);
			}
			int color = option == this.selected ? 0xFFFFFF55 : 0xFFE0E0E0;
			graphics.drawString(font, this.options.get(option), this.x + 5, rowY + 2, color, false);
		}
		if (shown.size() > MAX_VISIBLE) {
			graphics.fill(this.x + this.width - 4, this.trackTop(), this.x + this.width - 2, this.trackTop() + this.trackHeight(), -14540254);
			graphics.fill(this.x + this.width - 4, this.thumbY(), this.x + this.width - 2, this.thumbY() + this.thumbHeight(), 0xFFE0E0E0);
		}
	}

	private static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + 1, color);
		graphics.fill(x, y + height - 1, x + width, y + height, color);
		graphics.fill(x, y, x + 1, y + height, color);
		graphics.fill(x + width - 1, y, x + width, y + height, color);
	}
}
