import fout.FOut;

public class Application {
	public static void main(String[] args) {
		FOut out = new FOut(Testcase.class);
		out
			.add("Admin", "Admin", "admin@163.com")
			.addln("Test", "123456")
			.skipPlace().add("pass").add("liu@qq.com")
			.add("user").add(123456)
			.addln("ZARD", "asdfghjkl", "zard@qq.com");


		out.setColumnInvisible(1, 2);
		out.fout();

	}
}
