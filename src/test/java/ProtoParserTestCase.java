import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/** 
 * AsnParser test unit
 * @author foobar@em.boo.jp
 *
 */
public class ProtoParserTestCase {
	/**
	 * constractor 
	 * @throws Exception exception
	 */
	public ProtoParserTestCase() throws Exception {
	}

	/**
	 * main test.
	 */
	@Test
	public void testProto3() {
		File fileList = new File("./src/test/config/proto3");
		checkFile(fileList);
	}
	
	/**
	 * sub test.
	 */
	@Test
	public void testProto2() {
		File fileList = new File("./src/test/config/proto2");
		checkFile(fileList);
	}
	
	protected void checkFile(File fileList) {
		for (File file : fileList.listFiles()) {
			ProtoParser parser;
			try (InputStream is = new FileInputStream(file)) {
				parser = new ProtoParser(is);
				//ASTInput input = 
				parser.Input();
			} catch (TokenMgrError|IOException e) {
				System.out.println("target=" + file.toString());
				System.out.println(e.getClass().getName());
				System.out.println(e.getMessage());
				fail(e.getClass().getName());
			} catch (ParseException e) {
				System.out.println("target=" + file.toString());
				System.out.println(e.getClass().getName());
				System.out.println(e.getMessage());
				e.printStackTrace();
				fail(e.getClass().getName());
			}
		}
	}
}
