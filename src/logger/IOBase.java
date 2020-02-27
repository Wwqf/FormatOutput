package logger;

public abstract class IOBase {
	public abstract void println();
	public abstract void println(Object msg);
	public abstract void println(Object tag, Object msg);
	public abstract void printlnCallMethod(Object msg);

	public abstract void print(Object msg);
	public abstract void print(Object tag, Object msg);
	public abstract void printCallMethod(Object msg);
}
