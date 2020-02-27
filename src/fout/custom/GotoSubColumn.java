package fout.custom;

import fout.annoation.FormatOp;
import fout.base.FoutColumn;

public class GotoSubColumn extends FoutColumn {

	@FormatOp(columnName = "V")
	public int V;

	@FormatOp(columnName = "S")
	public int S;
}
