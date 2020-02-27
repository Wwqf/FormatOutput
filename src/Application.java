import fout.Fout;
import fout.attr.ColumnAttr;
import fout.attr.FoutColor;
import fout.attr.FoutGravity;
import fout.custom.Testcase;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Application {
	public static void main(String[] args) {

		/*
		 * 1. 通过class对象加载列项初始化，适用于可以确定列项时的情况
		 *  Fout fout = new Fout(Testcase.class);
		 *
		 * 2. 通过列项数初始化，此时，每一列将没有列名
		 *  Fout fout = new Fout(3);
		 *
		 * 3. 通过列项类进行初始化
		 *  Fout fout = new Fout(ColumnAttr...);
		 *
		 * 4. 可以添加一个列项，也可以指定其位置
		 *  fout.addColumn(ColumnAttr);
		 *  fout.addColumn(index, ColumnAttr);
		 *
		 * 5. 也可以插入子列项，如果将某一列插入子列项，
		 * 并且传入的实参为2个以上，则该项可有子列项；如果实参为1个或0个，则不插入子列项。
		 *  fout.addSubColumn(String, ColumnAttr...)
		 *
		 * n. 可以设置此表的表名, 也可以不设置
		 *  fout.setTableName(String);
		 */

		long start = System.nanoTime();

		Fout fout = new Fout(Testcase.class);
		fout
				.insert("1")
				.insertln(1, 2)
				.skipPlace()
				.insert(3)
				.insertSubColumn("F", "V1", "V2")
				.insertSubColumn("F", "V3", "V4", "V5")
				.insertlnSubColumn("Goto", "G1", "G2", "G3");
		fout.fout();

		long end = System.nanoTime();
		System.out.println("Time: " + (end - start) / 1000 / 1000 + "ms");


	}
}
