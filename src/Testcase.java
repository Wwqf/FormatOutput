import fout.Column;
import fout.FormatOp;
import fout.FormatOpGravity;
import log.IOColor;

public class Testcase extends Column {

	@FormatOp(name = "username", gravity = FormatOpGravity.LEFT)
	public String username;

	@FormatOp(name = "password", gravity = FormatOpGravity.CENTER)
	public String password;

	@FormatOp(name = "mail", gravity = FormatOpGravity.RIGHT)
	public String mail;
}
