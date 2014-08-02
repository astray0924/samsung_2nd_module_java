package samsung_2nd_module_java;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellChecker;

public class SpellCheckerManager {
	private static SpellDictionaryHashMap dictionary = null;
	private static SpellChecker spellChecker = null;
	private static int THRESHOLD = 1;

	static {
		try {
//			dictionary = new SpellDictionaryHashMap(new File(
//					"dictionary/english.txt"));
			dictionary = new SpellDictionaryHashMap(new File(
					"resources/dictionary/english.0.txt"));

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

	// public static void main(String[] args) {
	// System.out.println(SpellCheckerManager.getAllSuggestion("verr"));
	// }
}
