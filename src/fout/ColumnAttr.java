package fout;

public class ColumnAttr {
	public static final int FixWidth = 8;

	// 列名
	public String columnName;
	// 每一列的重力方向，有居左、居中、居右三种显示风格
	public FormatOpGravity gravity;
	// 当前列最大宽度 = 固定宽度（多余） + 字符串的宽度
	public int maxWidth;
	// 这一列是否显示
	public boolean visible = true;

	public ColumnAttr(String columnName, FormatOpGravity gravity) {
		this.columnName = columnName;
		this.gravity = gravity;
		this.maxWidth = FixWidth + getStringLength(columnName);
	}

	// 有这个方法是为了简便含中文的字符串
	public static int getStringLength(String str) {
		return str.length();
	}
}
