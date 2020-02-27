package fout.custom;

import fout.annoation.FormatOp;
import fout.annoation.FormatOpSubColumn;
import fout.base.FoutColumn;

public class Testcase extends FoutColumn {

	@FormatOp(columnName = "State")
	public String state;

	@FormatOpSubColumn(columnName = "Action", cls = ActionColumn.class)
	public String action;

	@FormatOpSubColumn(columnName = "Goto", cls = GotoColumn.class)
	public String gotoStr;
}
