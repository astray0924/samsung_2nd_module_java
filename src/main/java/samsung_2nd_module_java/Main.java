package samsung_2nd_module_java;

import samsung_2nd_module_java.hierarchy.HierarchyModuleRunner;

public class Main {
	private enum Mode {
		SENTIMENT, TARGET, HIERARCHY
	};

	public static void main(String[] args) throws ClassNotFoundException {
		// 옵션 파싱하여 실행 모드(모듈) 및 input 파악
		Mode mode = Mode.HIERARCHY;

		// switch 문으로 실행할 모듈 선택
		SamsungModule module = null;
		switch (mode) {
		case HIERARCHY:
			module = new HierarchyModuleRunner();
			module.run();
			break;
		default:
			break;
		}
	}
}
