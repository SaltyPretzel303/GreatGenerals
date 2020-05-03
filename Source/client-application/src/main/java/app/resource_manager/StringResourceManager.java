package app.resource_manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONObject;

import app.form.FormMessage;

public class StringResourceManager {

	static private final String RESOURCE_PREFIX = "lang/";
	static private final String RESOURCE_SUFFIX = "-lang.json";

	// default language
	static private String language = "en";

	static private StringResourceManager instance;

	private JSONObject resources;
	private JSONArray messages;

	// methods
	public static StringResourceManager getInstance() {

		if (StringResourceManager.instance == null)
			StringResourceManager.instance = new StringResourceManager();

		return StringResourceManager.instance;
	}

	private StringResourceManager() {

		this.loadResources();

	}

	private void loadResources() {

		String file_name = StringResourceManager.RESOURCE_PREFIX + StringResourceManager.language
				+ StringResourceManager.RESOURCE_SUFFIX;

		System.out.println("Searching for: " + file_name);

		ClassLoader loader = getClass().getClassLoader();

		InputStream input_stream = loader.getResourceAsStream(file_name);
		InputStreamReader stream_reder = new InputStreamReader(input_stream);
		BufferedReader buff_reader = new BufferedReader(stream_reder);

		StringBuilder content = new StringBuilder();
		String line = "";
		try {

			while ((line = buff_reader.readLine()) != null) {
				content.append(line + "\n");
			}

			buff_reader.close();
			stream_reder.close();
			input_stream.close();

			// transform plain text in JSONObject
			this.resources = new JSONObject(content.toString());

			// extract array of messages from JSONObject
			this.messages = this.resources.getJSONArray("messages");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	static public void setLanguage(String language) {
		// change language
		StringResourceManager.language = language;

		// create new instance with provided language
		StringResourceManager.instance = new StringResourceManager();

	}

	public String getString(String name) {
		return this.resources.getString(name);
	}

	public FormMessage getMessage(String message) {

		JSONObject json_message = null;
		for (int i = 0; i < messages.length(); i++) {
			json_message = this.messages.getJSONObject(i);
			if (json_message.getString("name").equals(message))
				return new FormMessage(json_message.getString("message"), json_message.getString("color"));
		}

		return null;
	}

}
