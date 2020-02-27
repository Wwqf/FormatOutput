package fout.attr;

import fout.attr.FoutGravity;

import java.util.HashMap;
import java.util.Map;

public class ColumnAttr {
	public static final int FixWidth = 8;

	// 列名
	public String columnName;
	// 每一列的重力方向，有居左、居中、居右三种显示风格
	public FoutGravity gravity;
	// 前景色
	public int foreColor;
	// 背景色
	public int backColor;

	// 当前列最大宽度 = 固定宽度（多余） + 字符串的宽度
	public int maxWidth;
	// 这一列是否显示
	public boolean visible = true;
	// 相当于几级标题
	public int level = 1;
	// 是否有父列项
	public boolean hasFatherColumn = false;
	// 父列项的名称
	private Map<Integer, String> fatherColumns = null;

	public ColumnAttr() {
		this("", FoutGravity.CENTER, FoutColor.fore_black, FoutColor.back_default);
	}

	public ColumnAttr(String columnName) {
		this(columnName, FoutGravity.CENTER, FoutColor.fore_black, FoutColor.back_default);
	}

	public ColumnAttr(String columnName, FoutGravity gravity) {
		this(columnName, gravity, FoutColor.fore_black, FoutColor.back_default);
	}

	public ColumnAttr(String columnName, FoutGravity gravity, int foreColor) {
		this(columnName, gravity, foreColor, FoutColor.back_default);
	}

	public ColumnAttr(String columnName, FoutGravity gravity, int foreColor, int backColor) {
		this.columnName = columnName;
		this.gravity = gravity;
		this.foreColor = foreColor;
		this.backColor = backColor;
		this.maxWidth = FixWidth + getStringLength(columnName);
	}

	// 有这个方法是为了简便含中文的字符串
	public static int getStringLength(String str) {
		return str.length();
	}

	/**
	 * 指定列名，快速创建一组列
	 * @param columnName 列名集
	 * @return
	 */
	public static ColumnAttr[] qCreate(String... columnName) {
		ColumnAttr[] result = new ColumnAttr[columnName.length];
		for (int i = 0; i < columnName.length; i++) {
			result[i] = new ColumnAttr(columnName[i]);
		}
		return result;
	}

	/**
	 * 添加父列项，并将父列项的父列项的父列项.... 添加进来
	 * @param fatherColumn 父列项
	 */
	public void addFatherColumn(ColumnAttr fatherColumn) {
		if (fatherColumns == null) fatherColumns = new HashMap<>();

		hasFatherColumn = true;
		level = fatherColumn.level + 1;
		fatherColumns.put(fatherColumn.level, fatherColumn.columnName);
		if (fatherColumn.fatherColumns != null)
			fatherColumns.putAll(fatherColumn.fatherColumns);
	}

	public void addFatherColumn(Map<Integer, String> fatherCol) {
		if (fatherColumns == null) fatherColumns = new HashMap<>();
		if (fatherCol == null || fatherCol.isEmpty()) return ;

		hasFatherColumn = true;

		int depthLevel = 0;
		for (int dl : fatherCol.keySet()) {
			if (dl > depthLevel) depthLevel = dl;
		}

		level = depthLevel + 1;
		fatherColumns.put(depthLevel, fatherCol.get(depthLevel));
		fatherColumns.putAll(fatherCol);
	}

	public String getFatherName(int level) {
		if (fatherColumns == null) return null;
		return fatherColumns.get(level);
	}

	public boolean findColumnName(String cn) {
		if (cn.equals(columnName)) return true;

		if (fatherColumns == null) return false;

		for (String value : fatherColumns.values()) {
			if (value.equals(cn)) return true;
		}
		return false;
	}
}
