package com.ibm.rtc.automation.examples.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class TokenReplacer {
	@SuppressWarnings("rawtypes")
	public static String replaceTokens(String text,
			Map<String, String> replacements) {
		Set propertySet = replacements.entrySet();
		for (Object o : propertySet) {
			Map.Entry entry = (Map.Entry) o;
			text = text.replaceAll((String) (entry.getKey()),
					(String) (entry.getValue()));
		}

		return text;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, String> propertiesToMap(Properties props) {
		return new HashMap<String, String>((Map) props);
	}
}
