package com.frteddz.bugpedia.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record Bug(String id, String title, String category, List<String> affected, String fixedIn, String workaround, List<String> loaders,
		String confirmation, String resolution, String created, String updated, String priority, int votes, int comments, int duplicates,
		Map<String, String> links) {
	public static List<Bug> parseBugs(String json) {
		List<Bug> bugs = new ArrayList<>();
		JsonObject root = JsonParser.parseString(json).getAsJsonObject();
		JsonArray array = root.getAsJsonArray("bugs");
		if (array == null) {
			return bugs;
		}
		for (JsonElement element : array) {
			if (element.isJsonObject()) {
				bugs.add(parseBug(element.getAsJsonObject()));
			}
		}
		return bugs;
	}

	public static String parseUpdated(String json) {
		JsonObject root = JsonParser.parseString(json).getAsJsonObject();
		return getString(root, "updated", "");
	}

	private static Bug parseBug(JsonObject object) {
		return new Bug(
			getString(object, "id", "unknown"),
			getString(object, "title", "untitled"),
			getString(object, "category", "other"),
			getStringList(object, "affected"),
			getNullableString(object, "fixedIn"),
			getNullableString(object, "workaround"),
			getStringList(object, "loaders"),
			getString(object, "confirmation", "Unconfirmed"),
			getNullableString(object, "resolution"),
			getString(object, "created", ""),
			getString(object, "updated", ""),
			getString(object, "priority", "Medium"),
			getInt(object, "votes"),
			getInt(object, "comments"),
			getInt(object, "duplicates"),
			getStringMap(object, "links"));
	}

	private static String getString(JsonObject object, String key, String fallback) {
		JsonElement element = object.get(key);
		if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
			return element.getAsString();
		}
		return fallback;
	}

	private static String getNullableString(JsonObject object, String key) {
		JsonElement element = object.get(key);
		if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
			return element.getAsString();
		}
		return null;
	}

	private static int getInt(JsonObject object, String key) {
		JsonElement element = object.get(key);
		if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
			return element.getAsInt();
		}
		return 0;
	}

	private static Map<String, String> getStringMap(JsonObject object, String key) {
		Map<String, String> values = new LinkedHashMap<>();
		JsonElement element = object.get(key);
		if (element != null && element.isJsonObject()) {
			for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
				JsonElement value = entry.getValue();
				if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
					values.put(entry.getKey(), value.getAsString());
				}
			}
		}
		return values;
	}

	private static List<String> getStringList(JsonObject object, String key) {
		List<String> values = new ArrayList<>();
		JsonElement element = object.get(key);
		if (element != null && element.isJsonArray()) {
			for (JsonElement entry : element.getAsJsonArray()) {
				if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
					values.add(entry.getAsString());
				}
			}
		}
		return values;
	}
}
