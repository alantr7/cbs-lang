import com.github.alantr7.codebots.cbslang.exceptions.ParserException;
import com.github.alantr7.codebots.cbslang.high.parser.Parser;
import com.github.alantr7.codebots.cbslang.high.parser.ast.AST;
import org.junit.After;
import org.junit.Test;

public class ParserTest {

    AST ast;

    @Test
    public void testEmptyFunction() throws ParserException {
        ast = Parser.parse("""
          int main() {}
          """);
    }

    @Test
    public void testFunctionParameters() throws ParserException {
        ast = Parser.parse("""
          int add(int a, int b) {}
          """);
    }

    @After
    public void showResults() {
        System.out.println(ast);
    }

}
