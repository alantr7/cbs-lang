import com.github.alantr7.codebots.cbslang.exceptions.ParserException;
import com.github.alantr7.codebots.cbslang.high.parser.Parser;
import com.github.alantr7.codebots.cbslang.high.parser.ast.AST;
import org.junit.Test;

public class ParserTest {

    @Test
    public void testEmptyFunction() throws ParserException {
        AST ast = Parser.parse("""
          int main() {}
          """);
    }

}
