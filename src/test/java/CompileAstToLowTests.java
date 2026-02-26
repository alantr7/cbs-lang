import com.github.alantr7.codebots.cbslang.high.compiler.Compiler;
import com.github.alantr7.codebots.cbslang.high.parser.TokenQueue;
import com.github.alantr7.codebots.cbslang.high.parser.ast.AST;
import com.github.alantr7.codebots.cbslang.high.parser.ast.expressions.*;
import com.github.alantr7.codebots.cbslang.high.parser.ast.objects.*;
import com.github.alantr7.codebots.cbslang.high.parser.ast.statements.*;
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

public class CompileAstToLowTests {

    Compiler compiler = new Compiler(new AST());

    @Test
    public void compileComplexExpression() {
        compiler.compileExpression(new Logical(new Operand[] {
          new Logical(new Operand[] {
            new Arithmetic(new Operand[] {
              new Literal(Literal.INT, 5),
              new Literal(Literal.INT, 0),
              Operator.MUL,
            }),
            Operator.OR,
            new Literal(Literal.INT, 3),
          }),
          Operator.OR,
          new Logical(new Operand[] {
            new Literal(Literal.INT, 2),
            Operator.AND,
            new Literal(Literal.INT, 0),
          }),
        }));
        compiler.append("pop");
    }

    @Test
    public void compileVariableDeclare() {
        compiler.compileVariableDeclare(new Variable(Primitive.INT, false, 0, 1));
    }

    @Test
    public void compileVariableAssign() {
        Variable variable = new Variable(Primitive.INT, true, 0, 1);
        compiler.compileVariableDeclare(variable);
        compiler.compileExpression(new Assign(
          variable,
          new Operand[0],
          new Arithmetic(new Operand[]{
            new Literal(Literal.INT, 5),
            new Literal(Literal.INT, 3),
            Operator.ADD,
          })
        ));
    }

    @Test
    public void compileVariableUseInExpression() {
        Variable a = new Variable(Primitive.INT, true, 0, 1);
        compiler.compileVariableDeclare(a);
        compiler.compileExpression(new Assign(a, new Operand[0], new Literal(Literal.INT, 8)));
        compiler.append("pop");

        compiler.compileExpression(new Arithmetic(new Operand[]{
          new Literal(Literal.INT, 7),
          new Access(a, new Operand[0]),
          Operator.ADD
        }));
        compiler.append("pop [0]");
    }

    @Test
    public void compileFunctionCall() {
        FunctionSignature random = new FunctionSignature("system", "random", Primitive.INT, new Type[0]);
        FunctionSignature print = new FunctionSignature("system", "print", Primitive.INT, new Type[]{ Primitive.INT });
        compiler.ast.signatures.add(random);
        compiler.ast.signatures.add(print);
        compiler.compileSignatures();
        compiler.compileExpression(new Call(print, new Operand[][]{
          {
            new Call(random, new Operand[0][]),
          }
        }));
        compiler.append("pop");
    }

    @Test
    public void compileFunction() {
        FunctionSignature print = new FunctionSignature("system", "print", Primitive.INT, new Type[] { Primitive.STRING });
        compiler.ast.signatures.add(print);
        compiler.compileSignatures();

        FunctionSignature addSignature = new FunctionSignature(null, "add", Primitive.INT, new Type[] { Primitive.INT, Primitive.INT });
        Function add = new Function(addSignature, new Statement[] {
          new Ret(new Arithmetic(new Operand[]{
            new Access(new Variable(Primitive.INT, false, -2, 1), new Operand[0]),
            new Access(new Variable(Primitive.INT, false, -1, 1), new Operand[0]),
            Operator.ADD,
          }), 0)
        });

        FunctionSignature mainSignature = new FunctionSignature(null, "main", Primitive.INT, new Type[0]);
        Function main = new Function(mainSignature, new Statement[] {
          new Call(print, new Operand[][]{
            {
              new Call(addSignature, new Operand[][]{
                {
                  new Literal(Literal.INT, 5)
                },
                {
                  new Literal(Literal.INT, 3)
                }
              }),
            }
          }),
          new Ret(new Literal(Literal.INT, 0), 0),
        });

        compiler.compileFunction(main);
        compiler.compileFunction(add);
    }

    @Test
    public void compileFloats() {
        Float fl1 = -5.0f;
        compiler.ast.constants.add(new TokenQueue.Constant(Primitive.FLOAT, fl1));
        compiler.compileConstant("flt", fl1);

        compiler.compileExpression(new Arithmetic(new Operand[]{
          new Literal(Literal.FLOAT, fl1),
          new Literal(Literal.INT, -4),
          Operator.ADD,
        }));
    }

    @Test
    public void compileStrings() {
        String hello = "hello";
        compiler.ast.constants.add(new TokenQueue.Constant(Primitive.STRING, hello));
        compiler.compileConstant("str", hello);

        FunctionSignature print = new FunctionSignature("system", "print", Primitive.INT, new Type[] { Primitive.STRING });
        compiler.ast.signatures.add(print);

        compiler.compileSignatures();

        compiler.compileStatement(new Call(print, new Operand[][]{{
              new Access(new Variable(Primitive.STRING, true, 0, 1), new Operand[0])
        }}));
    }

    @Test
    public void compileArray() {
        Variable array = new Variable(Primitive.INT, true, 0, 10);
        compiler.compileVariableDeclare(array);

        compiler.compileExpression(new Assign(array, new Operand[]{ new Literal(Literal.INT, 3) }, new Literal(Literal.INT, 3)));
        compiler.append("pop");
    }

    @Test
    public void compileMatrix() {
        Variable array = new Variable(Primitive.INT, true, 0, new int[] { 3, 3 });
        compiler.compileVariableDeclare(array);

        FunctionSignature return3sig = new FunctionSignature(null, "getNum", Primitive.INT, new Type[0]);
        compiler.compileExpression(new Assign(array, new Operand[]{
          new Call(return3sig, new Operand[0][]),
          new Arithmetic(new Operand[] {
            new Literal(Literal.INT, 2),
            new Literal(Literal.INT, 1),
            Operator.MUL,
          }),
        }, new Literal(Literal.INT, 300)));
        compiler.append("pop\n");
        compiler.append("exit\n");

        Function return3 = new Function(return3sig, new Statement[]{
          new Ret(new Literal(Literal.INT, 2), 0)
        });

        compiler.compileFunction(return3);
    }

    @Test
    public void compileMatrixAccess() {
        Variable array = new Variable(Primitive.INT, true, 0, new int[] { 3, 3 });
        compiler.compileVariableDeclare(array);

        compiler.compileExpression(new Assign(array, new Operand[]{
          new Literal(Literal.INT, 1),
          new Literal(Literal.INT, 0),
        }, new Literal(Literal.INT, 300)));
        compiler.append("pop\n");

        // access the nth element
        compiler.compileExpression(new Access(array, new Operand[] {
          new Literal(Literal.INT, 1),
          new Literal(Literal.INT, 0),
        }));
        compiler.append("pop rax\n");
    }

    @Test
    public void compileCompare() {
        compiler.compileExpression(new Compare(new Literal(Literal.INT, 5), Compare.LESS_EQUALS, new Literal(Literal.INT, 10)));
        compiler.append("pop");
    }

    @Test
    public void compileIfCompare() {
        FunctionSignature print = new FunctionSignature("system", "print", Primitive.INT, new Type[] { Primitive.STRING });
        compiler.ast.signatures.add(print);

        compiler.compileSignatures();

        compiler.compileStatement(new If(new Compare(new Literal(Literal.INT, 5), Compare.GREATER_EQUALS, new Literal(Literal.INT, 10)), new Statement[]{
          new Call(print, new Operand[][]{
            {
              new Literal(Literal.INT, 100)
            }}),
        }, new If(new Compare(new Literal(Literal.INT, 5), Compare.LESS_THAN, new Literal(Literal.INT, 5)), new Statement[]{
          new Call(print, new Operand[][]{
            {
              new Literal(Literal.INT, 50)
            }}),
        }, new If(null, new Statement[]{
          new Call(print, new Operand[][]{
            {
              new Literal(Literal.INT, 20)
            }}),
        }, null))));
    }

    @Test
    public void compileWhileLoop() {
        compiler.append("push 0\n");
        compiler.compileStatement(new While(new Compare(
          new Access(new Variable(Primitive.INT, true, 0, 1), new Operand[]{ new Literal(Literal.INT, 0) }),
          Compare.LESS_THAN,
          new Literal(Literal.INT, 150)
        ), new Statement[]{
          new Assign(
            new Variable(Primitive.INT, true, 0, 1),
            new Operand[]{ new Literal(Literal.INT, 0) },
            new Arithmetic(new Operand[]{
              new Access(new Variable(Primitive.INT, true, 0, 1), new Operand[]{ new Literal(Literal.INT, 0) }),
              new Literal(Literal.INT, 5),
              Operator.ADD
            })
          )
        }));
    }

    // int i = 0
    // do {
    //   i++
    // } while (i < 5)
    @Test
    public void compileDoWhileLoop() {
        compiler.append("push 0\n");
        compiler.compileStatement(new While(new Compare(
          new Access(new Variable(Primitive.INT, true, 0, 1), new Operand[]{ new Literal(Literal.INT, 0) }),
          Compare.LESS_THAN,
          new Literal(Literal.INT, 5)
        ), new Statement[]{
          new Assign(
            new Variable(Primitive.INT, true, 0, 1),
            new Operand[]{ new Literal(Literal.INT, 0) },
            new Arithmetic(new Operand[]{
              new Access(new Variable(Primitive.INT, true, 0, 1), new Operand[]{ new Literal(Literal.INT, 0) }),
              new Literal(Literal.INT, 1),
              Operator.ADD
            })
          )
        }, true));
    }

    @Test
    public void compileVariableDeclare1() {
        compiler.compileVariableDeclare(new Declare(Primitive.INT, null, new int[] { 1 }));
    }

    @Test
    public void compileForLoop() {
        FunctionSignature print = new FunctionSignature("system", "print", Primitive.INT, new Type[] { Primitive.INT });
        compiler.ast.signatures.add(print);
        compiler.compileSignatures();

        compiler.compileStatement(new For(
          new Declare(Primitive.INT, new Literal(Literal.INT, 0), new int[] { 1 }),
          new Compare(
            new Access(new Variable(Primitive.INT, true, 0, 1), new Operand[]{ new Literal(Literal.INT, 0) }),
            Compare.LESS_THAN,
            new Literal(Literal.INT, 50)
          ),
          new Assign(
            new Variable(Primitive.INT, true, 0, 1),
            new Operand[]{ new Literal(Literal.INT, 0) },
            new Arithmetic(new Operand[]{
              new Access(new Variable(Primitive.INT, true, 0, 1), new Operand[]{ new Literal(Literal.INT, 0) }),
              new Literal(Literal.INT, 1),
              Operator.ADD
            })
          ),
          new Statement[]{
            new Call(print, new Operand[][]{
              {
                new Access(new Variable(Primitive.INT, true, 0, 1), new Operand[]{new Literal(Literal.INT, 0)}),
              }
            })
          }
        ));
    }

    @After
    public void dump() throws Exception {
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
