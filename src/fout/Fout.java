package fout;

import fout.annoation.FormatOp;
import fout.annoation.FormatOpSubColumn;
import fout.attr.ColumnAttr;
import fout.attr.FoutColor;
import fout.attr.FoutGravity;
import fout.base.FoutColumn;
import fout.logic.StyleKV;
import logger.Log;

import javax.naming.Name;
import java.awt.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

public class Fout {
	private static final char OuterFrameCorner = '+';
	private static final char OuterFrameLine = '-';
	private static final char ColumnSeparator = '|';

	private static final int QueueMaxSize = 10;

	private int numberOfColumns;
	// 列项类型
	private LinkedList<ColumnAttr> columnList;
	// 数据存放
	private List<String[]> data;

	// 当此位为false时不能修改列项。
	private boolean modifiedColumn = true;
	// 表名
	private String tableName = "";
	// 输入指针，在指针的位置插入数据
	private Point ptrPlace = new Point(0, 0);

	// 子列项范围
	class SubColumnArea {
		// 子列项的名称
		String name;
		// 子列项开始列和结束列
		int startIndex;
		int endIndex;
		// 最后使用时间
		long lastVisitTime;

		Point lastUsePlace;
	}

	// 优先队列，插入子列时的缓存队列
	private PriorityQueue<SubColumnArea> queue;
	// queue的排序方式
	private Comparator<SubColumnArea> subColumnAreaComparator = (t0, t1) -> {
		if (t0.lastVisitTime - t1.lastVisitTime < 0) return 1;
		return 0;
	};

	// 在查找值时，存储该值的坐标
	private Set<Point> specialValueSet;
	private Set<Integer> specialValueSetRow;

	public Fout(Class<? extends FoutColumn> colCls) {
		Annotation[] annotations = getFieldAnnotations(colCls);
		this.columnList = getColumn(annotations, 1, new HashMap<>());
		this.numberOfColumns = columnList.size();
		init();
	}

	public Fout(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
		this.columnList = new LinkedList<>();
		for (int i = 0; i < numberOfColumns; i++) {
			columnList.add(new ColumnAttr());
		}
		init();
	}

	public Fout(ColumnAttr... attrs) {
		this.numberOfColumns = attrs.length;
		this.columnList = new LinkedList<>();
		columnList.addAll(Arrays.asList(attrs));
		init();
	}

	private void init() {
		this.data = new ArrayList<>();
		this.queue = new PriorityQueue<>(5, subColumnAreaComparator);
		this.specialValueSet = new HashSet<>();
		this.specialValueSetRow = new HashSet<>();
	}

	// 添加一个列项
	public Fout addColumn(ColumnAttr columnAttr) {
		if (cantModifiedColumn()) return this;

		columnList.add(columnAttr);
		this.numberOfColumns = columnList.size();
		return this;
	}

	// 指定位置插入列项
	public Fout addColumn(int index, ColumnAttr columnAttr) {
		if (cantModifiedColumn()) return this;

		columnList.add(index, columnAttr);
		this.numberOfColumns = columnList.size();
		return this;
	}

	/**
	 * 如果插入子列项，那么父列项将没有实际意义，所以要删除
	 *
	 * @param columnName    父列项名称
	 * @param subColumnAttr 子列项集
	 * @return
	 */
	public Fout addSubColumn(String columnName, ColumnAttr... subColumnAttr) {
		if (cantModifiedColumn()) return this;

		ListIterator<ColumnAttr> iterator = columnList.listIterator();

		ColumnAttr fatherColumn = null;
		boolean mark = false;
		while (iterator.hasNext()) {
			fatherColumn = iterator.next();
			if (fatherColumn.columnName.equals(columnName)) {
				mark = true;
				break;
			}
		}

		// 当前处于要更改的父列项处，先删除父列项，再添加子列项
		if (mark) {
			iterator.remove();

			for (ColumnAttr attr : subColumnAttr) {
				attr.addFatherColumn(fatherColumn);
				iterator.add(attr);
			}

		} else {
			Log.warning("No parent column found.");
		}

		this.numberOfColumns = columnList.size();
		return this;
	}

	/**
	 * 插入一个数据
	 *
	 * @return
	 */
	public Fout insert(Object... objects) {
		modifiedColumn = false;

		int len = objects.length;
		int index = 0;
		String[] rows = getPtrPlaceInRow();

		while (len-- > 0) {
			if (ptrPlace.y >= numberOfColumns) {
				ptrPlace.y = 0;
				ptrPlace.translate(1, 0);
				rows = getPtrPlaceInRow();
			}

			rows[ptrPlace.y] = String.valueOf(objects[index++]);
			ptrPlace.translate(0, 1);

		}

		return this;
	}

	private String[] getPtrPlaceInRow() {
		String[] result = null;
		if (ptrPlace.x >= data.size()) {
			result = new String[numberOfColumns];
			data.add(result);
		} else result = data.get(ptrPlace.x);
		return result;
	}

	/**
	 * 插入一行数据
	 *
	 * @param objects
	 * @return
	 */
	public Fout insertln(Object... objects) {
		modifiedColumn = false;

		ptrPlace.y = 0;
		ptrPlace.translate(1, 0);

		// 如果实参个数超过列数，超过部分舍去
		if (objects.length > numberOfColumns) {
			Log.warning("The added data length exceeds the number of columns.");
		}

		// 插入数据，并更新当前列最大长度

		String[] rowData = new String[numberOfColumns];
		for (int i = 0; i < objects.length && i < numberOfColumns; i++) {
			rowData[i] = String.valueOf(objects[i]);
			updateColumnMaxWidth(i, rowData[i]);
			ptrPlace.translate(0, 1);
		}
		this.data.add(rowData);
		return this;
	}

	// 指定子列，针对子列项插入数据，超过的部分舍去
	public Fout insertlnSubColumn(String scName, Object... objects) {

		SubColumnArea area = null;
		for (SubColumnArea sca : queue) {
			if (sca.name.equals(scName)) {
				area = sca;
				break;
			}
		}

		if (area == null) {
			int start = -1, end = 0;
			for (int i = 0; i < columnList.size(); i++) {
				// 查找是否有该子列项
				if (columnList.get(i).findColumnName(scName)) {
					if (start == -1) start = i;
					end = i;
				}
			}

			if (start == -1) {
				Log.warning("Not found sub column: " + scName);
				return this;
			} else {
				if (objects.length > end - start + 1) {
					Log.warning("The added data length exceeds the number of columns.");
				}
			}

			area = new SubColumnArea();
			area.startIndex = start;
			area.endIndex = end;
			area.name = scName;
			area.lastUsePlace = new Point();
			queue.add(area);
		}

		area.lastUsePlace.x = this.data.size();
		String[] rowData = new String[numberOfColumns];
		for (int i = area.startIndex, datIndex = 0; datIndex < objects.length && i <= area.endIndex; i++, datIndex++) {
			rowData[i] = String.valueOf(objects[datIndex]);
			updateColumnMaxWidth(i, rowData[i]);
			area.lastUsePlace.y = i;
		}
		this.data.add(rowData);
		area.lastVisitTime = System.nanoTime();

		// 更新输入指针位置
		area.lastUsePlace.translate(0, 1);
		ptrPlace.setLocation(area.lastUsePlace);
		// 如果超出设置的长度，移除不常用的
		if (queue.size() > QueueMaxSize) {
			queue.poll();
		}

		return this;
	}

	// 指定子列，针对子列项一个一个插入数据
	public Fout insertSubColumn(String scName, Object... objects) {

		SubColumnArea area = null;
		for (SubColumnArea sca : queue) {
			if (sca.name.equals(scName)) {
				area = sca;
				break;
			}
		}

		if (area == null) {
			int start = -1, end = 0;
			for (int i = 0; i < columnList.size(); i++) {
				// 查找是否有该子列项
				if (columnList.get(i).findColumnName(scName)) {
					if (start == -1) start = i;
					end = i;
				}
			}

			if (start == -1) {
				Log.warning("Not found sub column: " + scName);
				return this;
			} else {
				if (objects.length > end - start + 1) {
					Log.warning("The added data length exceeds the number of columns.");
				}
			}

			area = new SubColumnArea();
			area.startIndex = start;
			area.endIndex = end;
			area.name = scName;
			area.lastUsePlace = new Point(-1, -1);
			queue.add(area);
		}

		String[] rows = null;
		if (area.lastUsePlace.x == -1) {
			rows = this.data.get(this.data.size() - 1);
			area.lastUsePlace.x = this.data.size() - 1;
			area.lastUsePlace.y = area.startIndex;
		} else rows = data.get(area.lastUsePlace.x);

		for (int i = 0; i < objects.length; i++) {
			if (area.lastUsePlace.y > area.endIndex) {
				area.lastUsePlace.y = area.startIndex;
				area.lastUsePlace.translate(1, 0);

				rows = new String[numberOfColumns];
				data.add(rows);
			}

			rows[area.lastUsePlace.y] = String.valueOf(objects[i]);
			updateColumnMaxWidth(i, rows[area.lastUsePlace.y]);
			area.lastUsePlace.translate(0, 1);
		}

		area.lastVisitTime = System.nanoTime();
		// 更新输入指针位置
		ptrPlace.setLocation(area.lastUsePlace);
		// 如果超出设置的长度，移除不常用的
		if (queue.size() > QueueMaxSize) {
			queue.poll();
		}

		return this;
	}

	public Fout skipPlace() {
		if (ptrPlace.y + 1 >= numberOfColumns) {
			ptrPlace.y = 0;
			ptrPlace.translate(1, 0);
			// 添加一层
			getPtrPlaceInRow();
		} else ptrPlace.translate(0, 1);
		return this;
	}

	private void updateColumnMaxWidth(int colId, String str) {
		int maxW = ColumnAttr.FixWidth + ColumnAttr.getStringLength(str);
		if (maxW > columnList.get(colId).maxWidth) columnList.get(colId).maxWidth = maxW;
	}

	/**
	 * 查找值
	 *
	 * @param value
	 */
	public void findValue(String value) {
		for (int i = 0; i < data.size(); i++) {
			String[] rows = data.get(i);
			for (int j = 0; j < rows.length; j++) {
				if (rows[j] == null) continue;

				if (rows[j].equals(value)) {
					specialValueSet.add(new Point(i, j));
					specialValueSetRow.add(i);
				}
			}
		}
	}

	/**
	 * 对外接口，打印数据
	 */
	public void fout() {
		int widthSum = 0;

		for (ColumnAttr column : columnList) {
			if (column.visible) {
				widthSum += column.maxWidth;
			} else widthSum -= 1;
		}

		// 宽度总长 = 每一列的长度 + 分隔符的数量{列数 + 1}
		widthSum += columnList.size() + 1;

		foutOuterFrame(OuterFrameCorner, widthSum);
		if (!tableName.equals(""))
			foutTableName(widthSum);
		foutColumnName(widthSum);

		for (int i = 0; i < data.size(); i++) {
			foutData(i, data.get(i));
		}
		foutOuterFrame(OuterFrameCorner, widthSum);
	}

	/**
	 * 输出表名
	 *
	 * @param widthSum
	 */
	private void foutTableName(int widthSum) {
		System.out.print('|');
		foutOrdinaryUnit(widthSum - 2, tableName, FoutGravity.CENTER);
		System.out.print('|');
		System.out.print('\n');
		foutOuterFrame(ColumnSeparator, widthSum);
	}

	/**
	 * 输出列名
	 *
	 * @param widthSum
	 */
	private void foutColumnName(int widthSum) {
		// 获取子项深度
		int depthLevel = checkDepthLevel();
		depthLevel += (depthLevel - 1);

		var attrs = columnList.toArray();
		StyleKV[][] kvs = StyleKV.getStyleKVS(attrs, depthLevel);

		for (int i = 0; i < kvs.length; i++) {
			System.out.print(ColumnSeparator);
			for (int j = 0; j < kvs[i].length; j++) {
				int width = ((ColumnAttr) attrs[j]).maxWidth;

				if (kvs[i][j].id == 0) {
					// 输出空格
					repeatOutputChar(' ', width);
				} else if (kvs[i][j].id == -1) {
					// 输出横线分隔符
					for (++j; j < kvs[i].length; j++) {
						if (kvs[i][j].id == -1) {
							width += ((ColumnAttr) attrs[j]).maxWidth + 1;
						} else break;
					}
					j--;
					repeatOutputChar(OuterFrameLine, width);
				} else if (kvs[i][j].id == -2) {
					// 输出连续空格
					for (++j; j < kvs[i].length; j++) {
						if (kvs[i][j].id == -2) {
							width += ((ColumnAttr) attrs[j]).maxWidth + 1;
						} else break;
					}
					j--;
					repeatOutputChar(' ', width);
				} else {
					// 输出列名
					String name = kvs[i][j].name;
					for (++j; j < kvs[i].length; j++) {
						if (kvs[i][j].name == null) break;

						if (kvs[i][j].name.equals(name)) {
							width += ((ColumnAttr) attrs[j]).maxWidth + 1;
						} else break;
					}

					j--;
					foutOrdinaryUnit(width, name, FoutGravity.CENTER);
				}

				System.out.print(ColumnSeparator);
			}
			System.out.println();
		}

		foutOuterFrame(ColumnSeparator, widthSum);
	}

	/**
	 * 输出数据
	 *
	 * @param content 一行数据
	 */
	private void foutData(int row, String[] content) {
		System.out.print(ColumnSeparator);
		for (int i = 0; i < columnList.size(); i++) {
			ColumnAttr attr = columnList.get(i);

			if (!attr.visible) continue;

			String opStr = "";
			if (specialValueSetRow.contains(row) && specialValueSet.contains(new Point(row, i))) {
				// 特殊值输出
				if (content[i] != null) opStr = content[i];
				foutColorUnit(attr.maxWidth, opStr, attr.gravity, FoutColor.fore_red, FoutColor.back_default);
			} else if (attr.foreColor == FoutColor.fore_black && attr.backColor == FoutColor.back_default) {
				// 普通输出
				if (content[i] != null) opStr = content[i];
				foutOrdinaryUnit(attr.maxWidth, opStr, attr.gravity);
			} else {
				// 颜色输出
				if (content[i] != null) opStr = content[i];
				foutColorUnit(attr.maxWidth, opStr, attr);
			}
			System.out.print(ColumnSeparator);
		}
		System.out.print('\n');
	}

	private void foutOuterFrame(char separator, int widthSum) {
		System.out.print(separator);
		for (int i = 0; i < widthSum - 2; i++) System.out.print(OuterFrameLine);
		System.out.print(separator);
		System.out.print('\n');
	}

	/**
	 * 输出一个单位
	 *
	 * @param width   单位宽
	 * @param content 输出内容
	 * @param gravity 输出位置
	 */
	private void foutOrdinaryUnit(int width, String content, FoutGravity gravity) {
		int[] interval = calculationInterval(width, content, gravity);

		while (interval[0]-- > 0) System.out.print(' ');
		System.out.print(content);
		while (interval[1]-- > 0) System.out.print(' ');
	}

	/**
	 * 输出一个带有颜色的单位
	 *
	 * @param width
	 * @param content
	 * @param attr
	 */
	private void foutColorUnit(int width, String content, ColumnAttr attr) {
		foutColorUnit(width, content, attr.gravity, attr.foreColor, attr.backColor);
	}

	private void foutColorUnit(int width, String content, FoutGravity gravity, int foreColor, int backColor) {
		int[] interval = calculationInterval(width, content, gravity);

		while (interval[0]-- > 0) System.out.print(' ');
		String stringFormat = "\033[" + backColor + ";" + foreColor + "m%s\033[0m";
		System.out.printf(stringFormat, content);
		while (interval[1]-- > 0) System.out.print(' ');
	}

	private int[] calculationInterval(int width, String content, FoutGravity gravity) {
		int len = ColumnAttr.getStringLength(content);
		len = width - len;

		int left = 0, right = 0;

		switch (gravity) {
			case LEFT:
				left = 1;
				right = len - left;
				break;
			case CENTER:
				if (len % 2 == 1) {
					left = (len - 1) / 2;
					right = len - left;
				} else {
					left = right = len / 2;
				}
				break;
			case RIGHT:
				right = 1;
				left = len - right;
				break;
		}

		return new int[]{left, right};
	}

	private void repeatOutputChar(char c, int count) {
		while (count-- > 0) System.out.print(c);
	}

	private int checkDepthLevel() {
		// 2. 检查深度，最深level序号是几
		int depthLevel = 1;

		for (ColumnAttr attr : columnList) {
			if (attr.level > depthLevel) depthLevel = attr.level;
		}

		return depthLevel;
	}

	/**
	 * 获取自定义类的属性的注解值
	 *
	 * @param cls 自定义类
	 * @return 属性注解数组
	 */
	private Annotation[] getFieldAnnotations(Class<? extends FoutColumn> cls) {
		int resultLength = cls.getFields().length;
		Annotation[] result = new Annotation[resultLength];

		int index = 0;
		for (Field field : cls.getFields()) {
			result[index++] = field.getAnnotations()[0];
		}
		return result;
	}

	/**
	 * 根据注解值，获取每一列的设置属性
	 *
	 * @param annotations 属性注解数组
	 * @return 列属性列表
	 */
	private LinkedList<ColumnAttr> getColumn(Annotation[] annotations, int level, Map<Integer, String> fatherCol) {
		LinkedList<ColumnAttr> columnList = new LinkedList<>();

		for (Annotation annotation : annotations) {
			if (annotation instanceof FormatOp) {
				FormatOp formatOp = (FormatOp) annotation;
				ColumnAttr attr = new ColumnAttr(formatOp.columnName(),
						formatOp.gravity(), formatOp.foreColor(), formatOp.backColor());

				attr.addFatherColumn(fatherCol);
				columnList.add(attr);
			} else if (annotation instanceof FormatOpSubColumn) {
				FormatOpSubColumn subColumn = (FormatOpSubColumn) annotation;
				fatherCol.put(level, subColumn.columnName());

				LinkedList<ColumnAttr> rec = getColumn(getFieldAnnotations(subColumn.cls()), level + 1, fatherCol);
				columnList.addAll(rec);

			}
		}

		return columnList;
	}

	/**
	 * 插入数据后，就不能修改列项
	 *
	 * @return 能修改返回false，不能修改返回true
	 */
	private boolean cantModifiedColumn() {
		if (!modifiedColumn) {
			Log.warning("Column items cannot be modified.");
		}
		return !modifiedColumn;
	}

	/**
	 * 修改列颜色, 如果为0则不设置
	 *
	 * @param columnName 列名
	 * @param foreColor  前景色
	 * @param backColor  背景色
	 */
	public void changeColumnColor(String columnName, int foreColor, int backColor) {
		for (ColumnAttr attr : columnList) {
			if (attr.columnName.equals(columnName)) {
				if (foreColor != 0) attr.foreColor = foreColor;
				if (backColor != 0) attr.backColor = backColor;
			}
		}
	}

	public int getNumberOfColumns() {
		return numberOfColumns;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public ColumnAttr getColumn(String columnName) {
		for (ColumnAttr attr : columnList) {
			if (attr.columnName.equals(columnName)) return attr;
		}
		return null;
	}

	// 移动位置
	public void movePtr(int x, int y) {
		ptrPlace.move(x, y);
	}
}
