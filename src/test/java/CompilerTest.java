import com.github.alantr7.codebots.cbslang.exceptions.ParserException;
import com.github.alantr7.codebots.cbslang.high.compiler.Compiler;
import com.github.alantr7.codebots.cbslang.high.parser.Parser;
import com.github.alantr7.codebots.cbslang.low.runtime.Program;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ModuleRepository;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.standard.SystemModule;
import com.github.alantr7.codebots.cbslang.low.tokenizer.Tokenizer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CompilerTest {

    Compiler compiler;

    ModuleRepository repository = new ModuleRepository();

    @Before
    public void loadRepository() {
        repository.registerModule(new SystemModule());
    }

    @Test
    public void testEmptyFunction() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int main() {}
          """));
    }

    @Test
    public void testFunctionParameters() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int add(int a, int b) {}
          int sub(int a, int b) {}
          """));
    }

    @Test
    public void testFunctionWithVariableDeclare() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int add(int a, int b) {
            int c;
            int d;
          }
          """));
    }

    @Test
    public void testFunctionWithSimpleExpression() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int add(int a, int b) {
            2+(5+5)*2;
          }
          """));
    }

    @Test
    public void testFunctionWithVariableAssign() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int main() {
            int a;
            int b;
            int c;
            int d;
            a = 5;
            b = 3;
            c = 2;
            
            return a + b * c;
          }
          """));
    }

    @Test
    public void testFunctionWithReturn() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int main(int a) {
            return 5;
            a = 3;
            int b;
            b= 2;
          }
          """));
    }

    @Test
    public void testFunctionWithExpressionAccessingAVariable() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int main() {
            int a = 5;
            int b = a;
            
            return b * 3;
          }
          """));
    }

    @Test
    public void testFunctionWithExpressionCallingParameterlessFunction() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int get_num2() {
            return 5;
          }
          
          int get_num() {
            return get_num2() * 3;
          }
          
          int main() {
            return get_num() * 2;
          }
          """));
    }

    @Test
    public void testFunctionWithExpressionCallingFunctionWithParameters() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int add(int a, int b) {
            return a + b;
          }
          
          int main() {
            int c = add(add(5, 2) * add(2, add(1, 2)), add(7, 7) / add(1, 1));
            return c;
          }
          """));
    }

    @Test
    public void testFunctionWithLogicalExpressions() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int calc() {
            return 10 * (2 + (7 - 3) * 2 - 7);
          }
          int main() {
            return (0 && calc()) || calc();
          }
          """));
    }

    @Test
    public void testFunctionWithComparisonExpressions() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int main() {
            int a = 3;
            return a < 5;
          }
          """));
    }

    @Test
    public void testFunctionWithIf() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int main() {
            int a = 3;
            if (a < 5) {
              return 48;
            }
            return 56;
          }
          """));
    }

    @Test
    public void testFunctionWithIfElseIf() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int main() {
            int a = 3;
            if (a > 5) {
              return 48;
            } else if (a > 3) {
              return 32;
            } else {
              return a;
            }
          }
          """));
    }

    @Test
    public void testFactorial() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int fact(int num) {
            if (num <= 1) {
              return 1;
            }
            return num * fact(num - 1);
          }
          
          int main() {
            return fact(5);
          }
          """));
    }

    @Test
    public void testVariableCleanup() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int test(int e) {
            int a = 3;
            int b = 5;
            int c = 8;
            int d = 11;
            
            return e;
          }
          int main() {
            return test(test(test(12)));
          }
          """));
    }

    @Test
    public void testWhileLoop() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int main() {
            int i = 0;
            while (1) {
              if (i > 80) {
                return i;
              }
              
              i = i + 1;
            }
            return i;
          }
          """));
    }

    @Test
    public void testDoWhileLoop() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int main() {
            int i = 0;
            do {
              i = i + 1;
            } while (i < 5);
            return i;
          }
          """));
    }

    @Test
    public void testForLoop() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int main() {
            int a = 0;
            for (int i = 0; i < 10; i++) {
              a = a + 2;
            };
            return a;
          }
          """));
    }

    @Test
    public void testAssignAsExpression() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int main() {
            int a;
            int b = (a = 3);
            
            return b;
          }
          """));
    }

    @Test
    public void testUnaryOperators() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int main() {
            int a = 1;
            int b = ++a;
            return b;
          }
          """));
    }

    @Test
    public void testImport() throws ParserException {
        compiler = new Compiler(Parser.parse(repository, """
          import system;
          int main() {
            int rand = system.random();
            system.print(rand);
            
            return rand;
          }
          """));
    }

    @Test
    public void testString() throws ParserException {
        compiler = new Compiler(Parser.parse(repository, """
          import system;
          
          string hey() {
            return "Hello";
          }
          
          string main() {
            string name = "Alan";
            return hey() + name;
          }
          """));
    }

    @Test
    public void testEbpIncorrections1() throws ParserException {
        compiler = new Compiler(Parser.parse(repository, """
          int get_num() {
            return 4;
          }
          
          int main() {
            int a = get_num();
            int c = 2;
            return (a * get_num()) / c;
          }
          """));
    }

    @Test
    public void testEbpIncorrections2() throws ParserException {
        compiler = new Compiler(Parser.parse(repository, """
          int square(int num) {
            return num * num;
          }
          
          int main() {
            int a = square(25);
            return a;
          }
          """));
    }

    @Test
    public void testEbpIncorrections3() throws ParserException {
        compiler = new Compiler(Parser.parse(repository, """
          import system;
          int inner(int x) {
              int y = x + 1;
              return y;
          }
          
          int outer(int a) {
              int local = a * 2;
              inner(a);
          
              return local;
          }
          
          int main() {
              return outer(5);
          }
          """));
    }

    @Test
    public void testStrings() throws ParserException {
        compiler = new Compiler(Parser.parse(repository, """
          string get_name() {
            return "Alan";
          }
          string main() {
            string greet = "Hello " + get_name() + "!";
            return greet;
          }
          """));
    }

    @Test
    public void testTypeCheck() throws ParserException {
        compiler = new Compiler(Parser.parse(repository, """
          int main() {
            float a = 3;
            return (int) a;
          }
          """));
    }

    @After
    public void showResults() throws Exception {
        compiler.experimentalCompile();

        String[][] tokenized = Tokenizer.tokenize(compiler.getOutput());
        Program program = new Program(tokenized, repository);
        System.out.println(compiler.getOutput());

        program.execute();
        program.getState().dump();

        Files.writeString(new File("./output.txt").toPath(), compiler.getOutput());

        AtomicInteger idx = new AtomicInteger();
        String memory = Arrays.stream(program.getState().getMEMORY()).map((data) -> {
            if (data == null)
                return idx.getAndIncrement() + "\t(empty)";

            return idx.getAndIncrement() + "\t" + data.getDataType().getTypeName() + "\t" + data.getValue();
        }).collect(Collectors.joining("\n"));
        Files.writeString(new File("./memory.txt").toPath(), memory);
    }

}
