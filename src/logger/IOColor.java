package logger;

public class IOColor extends IOBase {
	public static IOColor BLACK = new IOColor(30);
	public static IOColor RED = new IOColor(31);
	public static IOColor GREEN = new IOColor(32);
	public static IOColor YELLOW = new IOColor(33);
	public static IOColor BLUE = new IOColor(34);
	public static IOColor PURPLE = new IOColor(35);
	public static IOColor DEEP_GREEN = new IOColor(36);
	public static IOColor GRAY = new IOColor(37);

	public int colorCode;
	private IOColor(int colorCode) {
		this.colorCode = colorCode;
	}

	@Override
	public void println() {
		System.out.println();
	}

	@Override
	public void println(Object msg) {
		String stringFormat = "\033[" + colorCode + "m%s\033[0m\n";
		System.out.printf(stringFormat, msg);
	}

	@Override
	public void println(Object tag, Object msg) {
		String stringFormat = "\033[" + colorCode + "m%s: %s\033[0m\n";
		System.out.printf(stringFormat, tag, msg);
	}

	@Override
	public void printlnCallMethod(Object msg) {
		StackTraceElement[] element = Thread.currentThread().getStackTrace();
		String stringFormat = "\033[" + colorCode + "m%s: %s\033[0m\n";
		System.out.printf(stringFormat, element[2], msg);
	}

	@Override
	public void print(Object msg) {
		String stringFormat = "\033[" + colorCode + "m%s\033[0m";
		System.out.printf(stringFormat, msg);
	}

	@Override
	public void print(Object tag, Object msg) {
		String stringFormat = "\033[" + colorCode + "m%s: %s\033[0m";
		System.out.printf(stringFormat, tag, msg);
	}

	@Override
	public void printCallMethod(Object msg) {
		StackTraceElement[] element = Thread.currentThread().getStackTrace();
		String stringFormat = "\033[" + colorCode + "m%s: %s\033[0m";
		System.out.printf(stringFormat, element[2], msg);
	}

	public static IOColor[] values() {
		return new IOColor[]{
				BLACK, RED, GREEN,
				YELLOW, BLUE, PURPLE,
				DEEP_GREEN, GRAY
		};
	}
}
