package fout;

import log.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FOut {
	private static final char OuterFrameLeftCorner = '+';
	private static final char OuterFrameLine = '-';
	private static final char ColumnSeparator = '|';

	private int numberOfColumns;
	private List<ColumnAttr> columns;
	private List<List<String>> tables;

	private int skipPlace = 0;

	// 当此属性为0时，不显示content
	private int closeDisplay;

	// 当前放置的位置下标, 添加一行数据后，将此位设置为0
	// add方法根据此位放置数据
	private int placementPosX = 0;
	private int placementPosY = 0;

	public FOut(Class<? extends Column> colCls) {
		this.columns = new ArrayList<>();
		this.tables = new ArrayList<>();
		this.numberOfColumns = colCls.getFields().length;
		this.closeDisplay = numberOfColumns;

		for (Field field : colCls.getFields()) {
			for (Annotation annotation : field.getAnnotations()) {
				FormatOp formatOp = (FormatOp) annotation;
				columns.add(new ColumnAttr(formatOp.name(), formatOp.gravity()));
			}
		}
	}

	public void fout() {
		int widthSum = 0;

		for (ColumnAttr column : columns) {
			if (column.visible) {
				widthSum += column.maxWidth;
			} else widthSum -= 1;
		}

		// 宽度总长 = 每一列的长度 + 分隔符的数量{列数 + 1}
		widthSum += columns.size() + 1;

		// 打印列名
		foutTitle(widthSum);

		// 打印内容
		for (List<String> content : tables) {
			foutData(content);
		}

		// 打印底边框
		foutOuterFrameLine(widthSum);
	}

	public FOut add(Object... data) {
		List<String> rows = addRow(placementPosY);

		int len = data.length;
		int index = 0;

		while (len-- > 0) {
			if (placementPosX >= numberOfColumns) {
				placementPosX = 0;
				placementPosY++;
				rows = addRow(placementPosY);
			}

			// 占位
			if (skipPlace != 0) {
				skipPlace--;
				// 多减一次，需要恢复
				len++;
				rows.add("");
			} else {
				rows.add(String.valueOf(data[index++]));
			}

			placementPosX++;
		}

		return this;
	}

	// 添加一行数据，如果不存在则新增
	private List<String> addRow(int posY) {
		List<String> result;

		if (posY >= tables.size()) {
			result = new ArrayList<>();
			tables.add(result);
		} else result = tables.get(posY);

		return result;
	}

	public FOut addln(Object... data) {
		while (skipPlace != 0) {
			skipPlace--;
			add("");
		}

		placementPosX = 0;
		placementPosY = tables.size() + 1;

		List<String> rows = new ArrayList<>();
		if (data.length > numberOfColumns) {
			Log.warning("The added data length exceeds the number of columns.");
		}

		for (int i = 0; i < data.length && i < numberOfColumns; i++) {
			String str = String.valueOf(data[i]);

			int maxW = ColumnAttr.FixWidth + ColumnAttr.getStringLength(str);
			if (maxW > columns.get(i).maxWidth) columns.get(i).maxWidth = maxW;

			rows.add(str);
		}

		tables.add(rows);

		return this;
	}

	public FOut skipPlace() {
		skipPlace++;
		return this;
	}

	public void setColumnInvisible(int... index) {
		for (int i : index) {
			if (i >= numberOfColumns) {
				Log.warning("setColumnInvisible() index out bounds.");
			}
			columns.get(i).visible = false;
			closeDisplay--;
		}
	}

	public void resetVisible() {
		columns.forEach(item -> item.visible = true);
		closeDisplay = numberOfColumns;
	}

	private void foutTitle(int widthSum) {
		foutOuterFrameLine(widthSum);

		// 输出标题
		List<String> datTitle = new ArrayList<>();
		for (ColumnAttr attr : columns) {
			datTitle.add(attr.columnName);
		}
		foutData(datTitle);
	}

	private void foutData(List<String> content) {
		if (closeDisplay == 0) return;

		System.out.print(ColumnSeparator);

		for (int i = 0; i < columns.size(); i++) {
			ColumnAttr attr = columns.get(i);

			if (!attr.visible) {
				continue;
			}

			String str = "";
			try {
				str = content.get(i);
			} catch (Exception e) {
				foutSpace(attr.maxWidth);
				System.out.print(ColumnSeparator);
				continue;
			}

			switch (attr.gravity) {
				case LEFT:
					leftOutput(str, attr.maxWidth);
					break;
				case CENTER:
					centerOutput(str, attr.maxWidth);
					break;
				case RIGHT:
					rightOutput(str, attr.maxWidth);
					break;
			}
			System.out.print(ColumnSeparator);
		}
		System.out.print('\n');
	}

	private void leftOutput(String str, int width) {
		// 先输出空格
		System.out.print(' ');
		// 再输出内容
		System.out.print(str);
		// 再补充空格
		int len = ColumnAttr.getStringLength(str);

		len = width - len - 1;
		while (len-- > 0) System.out.print(' ');
	}

	private void centerOutput(String str, int width) {
		// 如果不对称，则左少一
		int len = ColumnAttr.getStringLength(str);

		len = width - len;
		int left, right;
		if (len % 2 == 1) {
			left = (len - 1) / 2;
			right = len - left;
		} else {
			left = right = len / 2;
		}

		while (left-- > 0) System.out.print(' ');
		System.out.print(str);
		while (right-- > 0) System.out.print(' ');
	}

	private void rightOutput(String str, int width) {
		// 先补充空格
		int len = ColumnAttr.getStringLength(str);

		len = width - len - 1;
		while (len-- > 0) System.out.print(' ');

		// 再输出内容
		System.out.print(str);

		// 再输出空格
		System.out.print(' ');
	}

	private void foutOuterFrameLine(int widthSum) {
		System.out.print(OuterFrameLeftCorner);
		for (int i = 0; i < widthSum - 2; i++) System.out.print(OuterFrameLine);
		System.out.print(OuterFrameLeftCorner);
		System.out.print('\n');
	}

	private void foutSpace(int width) {
		while (width-- > 0) System.out.print(' ');
	}
}
