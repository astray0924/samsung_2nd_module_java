package samsung_2nd_module_java;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

public class Main {
	public static void main(String[] args) {
		FeatureExtractor extractor = new FeatureExtractor();
		try {
			extractor.extract();

			// 결과값 수집
			Map<String, Multiset<String>> np_context = extractor.getContexts();
			ImmutableSet<Entry<String>> tokens = extractor.getTokens();
			ImmutableSet<Entry<String>> NPs = extractor.getNPs();

			// 디버깅
			System.out.println(NPs);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
