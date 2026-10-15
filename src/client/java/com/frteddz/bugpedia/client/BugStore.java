package com.frteddz.bugpedia.client;

import java.util.List;

public class BugStore {
	private volatile List<Bug> bugs = List.of();
	private volatile String updated = "";
	private volatile boolean loading = true;
	private volatile boolean failed;

	public List<Bug> bugs() {
		return this.bugs;
	}

	public String updated() {
		return this.updated;
	}

	public boolean loading() {
		return this.loading;
	}

	public boolean failed() {
		return this.failed;
	}

	public void setLoaded(List<Bug> bugs, String updated) {
		this.bugs = List.copyOf(bugs);
		this.updated = updated;
		this.loading = false;
		this.failed = false;
	}

	public void setFailed() {
		this.loading = false;
		this.failed = true;
	}
}
