package logger;

public class Log {

	/**
	 * 四种级别的输出
	 *  debug为测试级, 输出颜色为绿色
	 *  info为正常级, 输出颜色为蓝色
	 *  warning为警告级, 输出颜色为黄色
	 *  error为异常级, 输出颜色为红色
	 *
	 *  If there is one parameter, it is 'msg', and the function call stack
	 *  is output when output.
	 *  If there are two parameters, the first parameter is 'tag' and
	 *  the second parameter is 'msg'.
	 */

	public static void debug(Object ...message) {
		int colorCode = IOColor.GREEN.colorCode;
		output(colorCode, message);
	}

	public static void info(Object ...message) {
		int colorCode = IOColor.BLUE.colorCode;
		output(colorCode, message);
	}

	public static void warning(Object ...message) {
		int colorCode = IOColor.YELLOW.colorCode;
		output(colorCode, message);
	}

	public static void error(Object ...message) {
		int colorCode = IOColor.RED.colorCode;
		output(colorCode, message);
		System.exit(-1);
	}

	private static void output(int colorCode, Object[] message) {
		StackTraceElement[] element = Thread.currentThread().getStackTrace();
		if (message.length == 1) {
			String stringFormat = "\033[" + colorCode + "m%s: %s\033[0m\n";
			System.out.printf(stringFormat, element[4], message[0]);
		} else if (message.length == 2) {
			String stringFormat = "\033[" + colorCode + "m%s %s: %s\033[0m\n";
			System.out.printf(stringFormat, element[4], message[0], message[1]);
		}
	}
}
