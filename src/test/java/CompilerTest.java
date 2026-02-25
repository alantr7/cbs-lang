import com.github.alantr7.codebots.cbslang.exceptions.ParserException;
import com.github.alantr7.codebots.cbslang.high.compiler.Compiler;
import com.github.alantr7.codebots.cbslang.high.parser.Parser;
import com.github.alantr7.codebots.cbslang.low.runtime.Program;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.ModuleRepository;
import com.github.alantr7.codebots.cbslang.low.runtime.modules.standard.SystemModule;
import com.github.alantr7.codebots.cbslang.low.tokenizer.Tokenizer;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CompilerTest {

    Compiler compiler;

    @Test
    public void testEmptyFunction() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int main() {}
          """));
        compiler.experimentalCompile();
    }

    @Test
    public void testFunctionParameters() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int add(int a, int b) {}
          int sub(int a, int b) {}
          """));
        compiler.experimentalCompile();
    }

    @Test
    public void testFunctionWithVariableDeclare() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int add(int a, int b) {
            int c;
            int d;
          }
          """));
        compiler.experimentalCompile();
    }

    @Test
    public void testFunctionWithSimpleExpression() throws ParserException {
        compiler = new Compiler(Parser.parse("""
          int add(int a, int b) {
            2+(5+5)*2;
          }
          """));
        compiler.experimentalCompile();
    }

    @After
    public void showResults() throws Exception {
        ModuleRepository repository = new ModuleRepository();
        repository.registerModule(new SystemModule());

        String[][] tokenized = Tokenizer.tokenize(compiler.getOutput());
        Program program = new Program(tokenized, repository);
        program.execute();

        System.out.println(compiler.getOutput());
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
