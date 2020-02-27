package fout.logic;

import fout.attr.ColumnAttr;
import javafx.beans.binding.SetExpression;

import java.lang.reflect.Array;
import java.util.*;

/*
 * 样式键值对
 *  当id = -1时输出横线分隔符, 当id = 0时输出空格, 当id = -2时输出连续空格, 当id > 0时根据level输出列名
 */
public class StyleKV {
	public int id;
	public String name;

	public StyleKV(int id) {
		this.id = id;
	}

	public StyleKV(int id, String name) {
		this.id = id;
		this.name = name;
	}

	// 具有相同父列项分为一组, 以level 1划分
	public static final class Group {
		public Map<Integer, ColumnAttr> col;
		public int depLev;

		public Group() {
			col = new HashMap<>();
			depLev = 0;
		}

		public Group(Map<Integer, ColumnAttr> col, int depLev) {
			this.col = col;
			this.depLev = depLev;
		}

	}

	public static List<Group> groupCategory = new ArrayList<>();

	/**
	 * 获取列名布局，根据列名布局表绘制
	 * @param columnList
	 * @param depthLevel
	 * @return
	 */
	public static StyleKV[][] getStyleKVS(Object[] columnList, int depthLevel) {
		StyleKV[][] kvs = new StyleKV[depthLevel][columnList.length];
		groupCategory = new ArrayList<>();
		// 划分组

		for (int i = 0; i < columnList.length; i++) {
			ColumnAttr attr = (ColumnAttr) columnList[i];

			if (attr.level > 1) {
				// 有父列项时，为有相同父列项的元素划分组
				Group group = new Group();

				if (attr.level > group.depLev) group.depLev = attr.level;
				group.col.put(i, attr);

				String fatherCol = attr.getFatherName(1);
				for (++i; i < columnList.length; i++) {
					attr = (ColumnAttr) columnList[i];
					String currentFatherCol = attr.getFatherName(1);
					if (currentFatherCol == null) break;

					if (currentFatherCol.equals(fatherCol)) {
						if (attr.level > group.depLev) group.depLev = attr.level;
						group.col.put(i, attr);
					} else break;
				}
				groupCategory.add(group);
				i--;
			} else {
				// level为1时，有单个组

				Group group = new Group();
				group.depLev = 1;
				group.col.put(i, attr);
				groupCategory.add(group);
			}
		}

		// 遍历组设置
		for (Group group : groupCategory) {
			if (group.depLev == 1) {
				group.col.forEach((key, value) -> {
					for (int i = 0; i < (depthLevel - 1) / 2; i++) {
						kvs[i][key] = kvs[depthLevel - i - 1][key] = new StyleKV(0);
					}
					kvs[(depthLevel) / 2][key] = new StyleKV(1, value.columnName);
				});
			} else {
				group.col.forEach((key, value) -> {
					// key is column_id, value is columnAttr
					if (value.level != group.depLev) {
						//一个level留一个空行
						// 从下往上放
						int diff = group.depLev - value.level;
						int bottomIndex = depthLevel - 1;
						for (int i = 0; i < diff; i++) {
							bottomIndex = depthLevel - i - 1;
							kvs[bottomIndex][key] = new StyleKV(0);
						}
						// 放置当前attr
						bottomIndex--;
						kvs[bottomIndex][key] = new StyleKV(value.level, value.columnName);

						// 再放diff个空行
						for (int i = 0; i < diff; i++) {
							bottomIndex--;
							kvs[bottomIndex][key] = new StyleKV(0);
						}

						// 放置父列项
						bottomIndex--;
						int fl = value.level - 1;
						while (bottomIndex >= 0) {
							kvs[bottomIndex][key] = new StyleKV(-1);
							bottomIndex--;
							kvs[bottomIndex][key] = new StyleKV(fl, value.getFatherName(fl));
							bottomIndex--;
							fl--;
						}

					} else {
						// 填补空行数
						int startIndex = (depthLevel - (group.depLev + group.depLev - 1)) / 2;
						for (int i = 0; i < startIndex; i++) {
							kvs[i][key] = new StyleKV(-2);
							kvs[depthLevel - i - 1][key] = new StyleKV(0);
						}

						for (int fl = 1; fl < group.depLev; fl++) {
							kvs[startIndex][key] = new StyleKV(fl, value.getFatherName(fl));
							startIndex++;
							kvs[startIndex][key] = new StyleKV(-1);
							startIndex++;
						}
						kvs[startIndex][key] = new StyleKV(group.depLev, value.columnName);
					}

				});
			}
		}

//		for (int i = 0; i < kvs.length; i++) {
//			for (int j = 0; j < kvs[i].length; j++) {
//				if (kvs[i][j] == null) System.out.print("-2" + "\t");
//				else System.out.print(kvs[i][j].id + "\t");
//			}
//			System.out.println();
//		}

		return kvs;
	}

}
