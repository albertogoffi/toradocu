package org.toradocu.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonInst {
	
	private final static Gson gson;

	static {
		gson = new GsonBuilder().setPrettyPrinting().create();
	}

	private GsonInst() {}
	
	public static Gson gson() {
		return gson;
	}
}
