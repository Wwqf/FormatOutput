package fout.custom;

import fout.annoation.FormatOp;
import fout.base.FoutColumn;

public class ActionColumn extends FoutColumn {

	@FormatOp(columnName = "id")
	public int id;

	@FormatOp(columnName = "*")
	public int star;

	@FormatOp(columnName = "+")
	public int plus;
}
