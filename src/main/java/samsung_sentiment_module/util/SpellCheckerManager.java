package samsung_sentiment_module.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import samsung_sentiment_module.hierarchy.OpinionTargetClassifier;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellChecker;

public class SpellCheckerManager {
	private static SpellDictionaryHashMap dictionary = null;
	private static SpellChecker spellChecker = null;
	private static int THRESHOLD = 1;

	static {
		try {
			InputStreamReader reader = new InputStreamReader(
					OpinionTargetClassifier.class
							.getResourceAsStream("/english.0.txt"));
			dictionary = new SpellDictionaryHashMap(reader);

		} catch (IOException e) {
			e.printStackTrace();
		}
		spellChecker = new SpellChecker(dictionary);
	}

	protected static List getAllSuggestion(String word) {
		return spellChecker.getSuggestions(word, THRESHOLD);
	}

	public static String getSuggestion(String word) {
		List suggestions = spellChecker.getSuggestions(word, THRESHOLD);

		if (suggestions.size() > 0) {
			return ((Word) suggestions.get(0)).toString();
		} else {
			return word;
		}
	}
}
