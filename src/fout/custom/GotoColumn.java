package fout.custom;

import fout.annoation.FormatOp;
import fout.annoation.FormatOpSubColumn;
import fout.base.FoutColumn;

public class GotoColumn extends FoutColumn {

	@FormatOp(columnName = "E")
	public int E;

	@FormatOp(columnName = "T")
	public int T;

	@FormatOpSubColumn(columnName = "F", cls = GotoSubColumn.class)
	public int F;
}
