package com.frteddz.bugpedia.client;

import net.minecraft.ChatFormatting;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BugpediaScreen extends Screen {
	private final Screen parent;
	private EditBox searchBox;
	private Button refreshButton;
	private Button clearButton;
	private DropdownMenu confirmationMenu;
	private DropdownMenu resolutionMenu;
	private DropdownMenu versionMenu;
	private DropdownMenu sortMenu;
	private BugListWidget bugList;
	private BugDetailsWidget details;
	private Bug selectedBug;
	private int ticks;
	private int listLeft;
	private int listWidth;
	private int listTop;
	private int listHeight;

	public BugpediaScreen(Screen parent) {
		super(Component.literal("Bug-o-pedia"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int searchY = 30;
		int filtersY = 54;
		this.listTop = 78;
		int doneY = this.height - 28;
		this.listHeight = doneY - 4 - this.listTop;
		int middle = (int) (this.width * 0.55);
		this.listLeft = 10;
		this.listWidth = middle - this.listLeft;
		int detailsLeft = middle + 5;
		int detailsWidth = this.width - 10 - detailsLeft;

		this.searchBox = new EditBox(this.font, 32, searchY, 100, 20, Component.literal("Search"));
		this.searchBox.setHint(Component.literal("Search bugs..."));
		this.searchBox.setResponder(value -> this.rebuildEntries());
		int listEnd = this.listLeft + this.listWidth;
		this.clearButton = Button.builder(Component.literal(""), pressed -> this.searchBox.setValue(""))
			.bounds(listEnd - 20, searchY, 20, 20)
			.tooltip(Tooltip.create(Component.literal("Clear search")))
			.build();
		this.refreshButton = Button.builder(Component.literal(""), pressed -> BugSync.sync(this.minecraft, BugpediaClient.STORE))
			.bounds(listEnd - 42, searchY, 20, 20)
			.tooltip(Tooltip.create(Component.literal("Download the latest bugs")))
			.build();
		this.searchBox.setWidth(listEnd - 44 - 32);
		this.addRenderableWidget(this.searchBox);
		this.addRenderableWidget(this.refreshButton);
		this.addRenderableWidget(this.clearButton);

		List<String> versions = new ArrayList<>();
		versions.add("Any version");
		for (String version : List.of("26.3", "26.2", "26.1.2", "26.1.1", "26.1",
				"1.21.11", "1.21.10", "1.21.9", "1.21.8", "1.21.7", "1.21.6",
				"1.21.5", "1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21")) {
			versions.add(version);
		}
		this.confirmationMenu = new DropdownMenu("Confirmation",
			List.of("Any confirmation", "Unconfirmed", "Plausible", "Community Consensus", "Confirmed"), 0, false);
		this.resolutionMenu = new DropdownMenu("Resolution",
			List.of("Any resolution", "Unresolved", "Awaiting response", "Cannot reproduce", "Done",
				"Duplicate", "Fixed", "Incomplete", "Invalid", "Won't fix", "Work as intended"), 0, false);
		this.versionMenu = new DropdownMenu("Version", versions, 0, true);
		this.sortMenu = new DropdownMenu("Sort by",
			List.of("Created", "Updated", "Resolved", "Priority", "Votes", "Comments", "Duplicates"), 0, false);
		int filterWidth = (this.width - 20 - 12) / 4;
		this.confirmationMenu.setBounds(10, filtersY, filterWidth);
		this.resolutionMenu.setBounds(14 + filterWidth, filtersY, filterWidth);
		this.versionMenu.setBounds(18 + filterWidth * 2, filtersY, filterWidth);
		this.sortMenu.setBounds(22 + filterWidth * 3, filtersY, this.width - 32 - filterWidth * 3);

		this.bugList = new BugListWidget(this.minecraft, this.listWidth, this.listHeight, this.listTop, 36);
		this.bugList.setX(this.listLeft);
		this.addRenderableWidget(this.bugList);
		this.details = new BugDetailsWidget(this.minecraft, detailsWidth, this.listHeight, this.listTop, 12);
		this.details.setX(detailsLeft);
		this.addRenderableWidget(this.details);

		this.addRenderableWidget(Button.builder(Component.literal("Done"), pressed -> this.minecraft.setScreen(this.parent))
			.bounds(this.width / 2 - 100, doneY, 200, 20)
			.build());

		this.rebuildEntries();
		if (BugpediaClient.STORE.bugs().isEmpty()) {
			BugSync.sync(this.minecraft, BugpediaClient.STORE);
		}
		this.setInitialFocus(this.searchBox);
	}

	public void rebuildEntries() {
		BugStore store = BugpediaClient.STORE;
		String query = this.searchBox.getValue().trim().toLowerCase();
		String version = this.versionMenu.selected();
		String confirmation = this.confirmationMenu.selected();
		String resolution = this.resolutionMenu.selected();
		List<BugListWidget.BugEntry> entries = new ArrayList<>();
		for (Bug bug : store.bugs()) {
			if (!version.equals("Any version") && !bug.affected().contains(version)) {
				continue;
			}
			if (!confirmation.equals("Any confirmation") && !bug.confirmation().equals(confirmation)) {
				continue;
			}
			if (!resolution.equals("Any resolution") && !matchesResolution(bug, resolution)) {
				continue;
			}
			if (!query.isEmpty() && !bug.id().toLowerCase().contains(query) && !bug.title().toLowerCase().contains(query)) {
				continue;
			}
			entries.add(new BugListWidget.BugEntry(this.minecraft, bug));
		}
		Comparator<BugListWidget.BugEntry> order = this.comparator();
		entries.sort(order);
		this.bugList.replaceEntries(entries);
		this.bugList.setSelected(null);
		this.selectedBug = null;
		this.details.setText("Select a bug to see details.", this.detailsWidth());
	}

	private static boolean matchesResolution(Bug bug, String resolution) {
		if (resolution.equals("Unresolved")) {
			return bug.resolution() == null;
		}
		return resolution.equals(bug.resolution());
	}

	private Comparator<BugListWidget.BugEntry> comparator() {
		Comparator<Bug> byBug = switch (this.sortMenu.selected()) {
			case "Updated" -> Comparator.comparing(Bug::updated, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
			case "Resolved" -> Comparator.comparing(Bug::fixedIn, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
			case "Priority" -> Comparator.comparingInt((Bug bug) -> priorityRank(bug.priority())).reversed();
			case "Votes" -> Comparator.comparingInt(Bug::votes).reversed();
			case "Comments" -> Comparator.comparingInt(Bug::comments).reversed();
			case "Duplicates" -> Comparator.comparingInt(Bug::duplicates).reversed();
			default -> Comparator.comparing(Bug::created, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
		};
		return Comparator.comparing(BugListWidget.BugEntry::bug, byBug);
	}

	private static int priorityRank(String priority) {
		return switch (priority) {
			case "Highest" -> 5;
			case "High" -> 4;
			case "Medium" -> 3;
			case "Low" -> 2;
			case "Lowest" -> 1;
			default -> 0;
		};
	}

	private String statusText() {
		BugStore store = BugpediaClient.STORE;
		if (store.loading() && store.bugs().isEmpty()) {
			return "Loading bugs...";
		}
		if (store.failed() && store.bugs().isEmpty()) {
			return "Could not load bugs.";
		}
		return "No bugs match.";
	}

	private void showDetails(Bug bug) {
		String fixed = bug.fixedIn() == null ? "Still open" : bug.fixedIn();
		String workaround = bug.workaround() == null ? "None known" : bug.workaround();
		String affected = bug.affected().isEmpty() ? "Unknown versions" : String.join(", ", bug.affected());
		List<BugDetailsWidget.DetailLine> paragraphs = new ArrayList<>();
		paragraphs.add(BugDetailsWidget.DetailLine.text(Component.literal(bug.title()).withStyle(ChatFormatting.BOLD)));
		paragraphs.add(BugDetailsWidget.DetailLine.text(Component.empty()));
		paragraphs.add(BugDetailsWidget.DetailLine.text(label("ID", bug.id())));
		paragraphs.add(BugDetailsWidget.DetailLine.text(label("Category", bug.category())));
		paragraphs.add(BugDetailsWidget.DetailLine.text(label("Confirmation", bug.confirmation())));
		paragraphs.add(BugDetailsWidget.DetailLine.text(label("Resolution", bug.resolution() == null ? "Unresolved" : bug.resolution())));
		paragraphs.add(BugDetailsWidget.DetailLine.text(label("Affects", affected)));
		paragraphs.add(BugDetailsWidget.DetailLine.text(label("Fixed in", fixed)));
		paragraphs.add(BugDetailsWidget.DetailLine.text(Component.empty()));
		paragraphs.add(BugDetailsWidget.DetailLine.text(Component.literal("Workaround").withStyle(ChatFormatting.YELLOW)));
		paragraphs.add(BugDetailsWidget.DetailLine.text(Component.literal(workaround)));
		if (!bug.links().isEmpty()) {
			paragraphs.add(BugDetailsWidget.DetailLine.text(Component.empty()));
			paragraphs.add(BugDetailsWidget.DetailLine.text(Component.literal("Links").withStyle(ChatFormatting.YELLOW)));
			for (Map.Entry<String, String> link : bug.links().entrySet()) {
				paragraphs.add(BugDetailsWidget.DetailLine.link(link.getKey(), link.getValue()));
			}
		}
		this.details.setContent(paragraphs, this.detailsWidth());
	}

	private int detailsWidth() {
		return this.details.getWidth() - 44;
	}

	private static Component label(String name, String value) {
		return Component.literal(name + ": ").withStyle(ChatFormatting.YELLOW)
			.append(Component.literal(value).withStyle(ChatFormatting.WHITE));
	}

	private List<DropdownMenu> menus() {
		return List.of(this.confirmationMenu, this.resolutionMenu, this.versionMenu, this.sortMenu);
	}

	private void closeMenus() {
		for (DropdownMenu menu : this.menus()) {
			menu.close();
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		for (DropdownMenu menu : this.menus()) {
			if (menu.handleClick(event.x(), event.y())) {
				if (menu.isOpen()) {
					for (DropdownMenu other : this.menus()) {
						if (other != menu) {
							other.close();
						}
					}
				}
				BugpediaSounds.click();
				this.rebuildEntries();
				return true;
			}
		}
		this.closeMenus();
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public void tick() {
		super.tick();
		BugListWidget.BugEntry selected = this.bugList.getSelected();
		if (selected != null && selected.bug() != this.selectedBug) {
			this.selectedBug = selected.bug();
			BugpediaSounds.click();
			this.showDetails(selected.bug());
		}
		if (++this.ticks >= 7200) {
			this.ticks = 0;
			BugSync.sync(this.minecraft, BugpediaClient.STORE);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		for (DropdownMenu menu : this.menus()) {
			if (menu.mouseScrolled(mouseX, mouseY, scrollY)) {
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double offsetX, double offsetY) {
		for (DropdownMenu menu : this.menus()) {
			if (menu.mouseDragged(event.y())) {
				return true;
			}
		}
		return super.mouseDragged(event, offsetX, offsetY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		for (DropdownMenu menu : this.menus()) {
			menu.stopDrag();
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
			for (DropdownMenu menu : this.menus()) {
				if (menu.isOpen()) {
					this.closeMenus();
					return true;
				}
			}
		} else if (event.key() == GLFW.GLFW_KEY_BACKSPACE && this.versionMenu.isOpen()) {
			return this.versionMenu.backspace();
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (this.versionMenu.isOpen() && this.versionMenu.typeChar(event.codepoint())) {
			return true;
		}
		return super.charTyped(event);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		graphics.drawCenteredString(this.font, "Bug-o-pedia", this.width / 2, 10, 0xFFFFFFFF);
		BugpediaIcons.draw(graphics, BugpediaIcons.SEARCH, 11, 32);
		BugpediaIcons.draw(graphics, BugpediaIcons.REFRESH, this.refreshButton.getX() + 2, this.refreshButton.getY() + 2);
		BugpediaIcons.draw(graphics, BugpediaIcons.CLEAR, this.clearButton.getX() + 2, this.clearButton.getY() + 2);
		if (this.bugList.children().isEmpty()) {
			graphics.drawCenteredString(this.font, this.statusText(), this.listLeft + this.listWidth / 2, this.listTop + this.listHeight / 2 - 4, 0xFFAAAAAA);
		}
		for (DropdownMenu menu : this.menus()) {
			menu.render(graphics, this.font, mouseX, mouseY);
		}
		String version = FabricLoader.getInstance().getModContainer("bugpedia")
			.map(container -> container.getMetadata().getVersion().getFriendlyString())
			.orElse("");
		graphics.drawString(this.font, version, this.width - this.font.width(version) - 6, this.height - 14, 0xFFFFFFFF, false);
		BugStore store = BugpediaClient.STORE;
		if (!store.bugs().isEmpty()) {
			String freshness = "Updated " + store.updated() + " - " + store.bugs().size() + (store.bugs().size() == 1 ? " bug" : " bugs");
			graphics.drawString(this.font, freshness, 6, this.height - 14, 0xFFAAAAAA, false);
		}
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}
}
