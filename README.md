# CBS — CodeBots Scripting Language

CBS is a small, statically-typed, C-inspired scripting language, originally built to let players
program bots in the [CodeBots](https://github.com/alantr7/CodeBots) Minecraft plugin. This
repository contains the language itself: the tokenizer, parser, compiler and a tiny stack/register
based virtual machine that executes it.

This document describes the language grammar and semantics as implemented by this compiler. It is
derived directly from the source (`Tokenizer`, `Parser`, `ParserHelper`, the AST classes, and the
compiler test suite), so every snippet below is either lifted from, or verified against, the
existing unit tests.

## How it fits together

```
source (.txt/.cbs string)
   │  Tokenizer.tokenize(...)
   ▼
tokens (TokenQueue)
   │  Parser.parse(...)
   ▼
AST (functions, global declarations, function signatures)
   │  Compiler.toHumanReadable(...) / ByteCodeCompiler
   ▼
bytecode
   │  ByteCodeCompressor / ByteCodeDecompressor (optional, for storage/transport)
   ▼
Program (low.runtime.Program) + ProgramExecutor
   │  program.run() — one instruction/tick at a time
   ▼
program output / return value
```

A minimal embedding example, based on the project's own test suite:

```java
ModuleRepository repository = new ModuleRepository();
repository.registerModule(new SystemModule());

AST ast = Parser.parse(repository, """
    import system;

    int main() {
      int rand = system.random();
      system.print("Value is: " + rand);
      return rand;
    }
    """);

Program program = new Program(UUID.randomUUID(), Tokenizer.tokenize(Compiler.toHumanReadable(repository, source)), repository);
program.setMode(Program.RUN_UNTIL_END);
program.run();
```

Host applications (like the CodeBots plugin) extend the language by registering `Module`s full of
`ExternalFunction`s — native Java code exposed to scripts as `module.function(...)` calls. See
[Modules & native functions](#modules--native-functions) below.

## Basic syntax

A CBS program is a sequence of `import` statements, global variable declarations and function
definitions. Statements inside a function body end with `;`, except for `if`, `while`, `for`
blocks, which end with `}` (a `for` loop is still followed by a `;` — see the control flow section).

```c
// single-line comments start with //
import system;

int main() {
   system.print("Hello, world!");
   return 0;
}
```

- **Entry point** — execution starts at `int main()`; its return value becomes the program's exit
  code.
- **User-defined functions** are declared like in C — `type name(type param, ...) { ... }` — and
  are called *without* a module prefix: `name(args)`.
- **Native functions**, registered by the host application through a `Module`, are grouped by
  module name and called as `module.function(args)`, after `import module;`. A module can opt into
  being "auto-imported": its functions are then available without an explicit `import` or module
  prefix.
- **Comments** are single-line only (`//`); there is no multi-line comment syntax.

## Types & variables

| Type | Description |
|---|---|
| `int` | Integer. Also used as a boolean: `0` is false, anything else is true in conditions. |
| `float` | Floating point number. Literals: `3.14`, `2f`. |
| `string` | Text between double quotes. Concatenated with `+` (numbers are converted automatically). |
| `void` | Function return type only — no value returned. |

There is no dedicated `bool` type. The `struct` keyword is reserved by the tokenizer but **not
implemented** by the parser — see [Known limitations](#known-limitations).

### Declaration & assignment

```c
int a;             // declaration without an initial value (defaults to 0)
int b = 5;         // declaration with initialization
b = b + 1;         // reassignment
float f = a;       // int -> float: implicit conversion allowed
int g = f;         // float -> int: ERROR, needs an explicit cast
int h = (int) f;   // explicit cast
```

The compiler performs **static type checking** on assignments. The only implicit conversion
allowed is `int → float`; every other type mismatch is a compile-time error. The only explicit
casts supported are `(int)` and `(float)` — there is no cast to/from `string`.

### Global variables & scope

Variables can also be declared outside of any function; they become global and visible from every
function. A local variable with the same name as a global one shadows it inside that function:

```c
import system;

int test = 3;   // global

int main() {
  int test = 4;             // local, shadows the global
  system.print("test = " + test);  // prints 4
}
```

### Arrays

Multi-dimensional, fixed-size arrays are supported. Dimensions must be positive integer literals
(not variables or expressions):

```c
int matrix[5][5];
matrix[1][1] = 3;
return matrix[1][1];
```

Array access must always specify every declared dimension. There is no array literal/initializer
syntax (e.g. `{1, 2, 3}`), and arrays cannot be resized after declaration.

## Operators

| Category | Operators | Notes |
|---|---|---|
| Arithmetic | `+ - * /` | `+` between two strings (or a string and a number) is concatenation |
| Comparison | `== != < <= > >=` | Result is `int` (0/1) |
| Logical | `&& \|\|` | Short-circuit AND/OR over integers |
| Assignment | `=` | Usable as an expression too: `int b = (a = 3);` |
| Increment/decrement | `++ --` | Both prefix (`++a`) and postfix (`a++`) forms |
| Cast | `(int) (float)` | The only explicit conversions available |
| Grouping | `( )` | Explicit precedence in expressions |

Operator precedence, from lowest to highest, as defined by the parser:
`=` → `||` → `&&` → comparisons (`< > == != <= >=`) → `+ -` → `* /`.

## Control flow

```c
if (a > 5) {
  return 48;
} else if (a > 3) {
  return 32;
} else {
  return a;
}

while (i < 5) {
  i = i + 1;
}

do {
  i = i + 1;
} while (i < 5);

for (int i = 0; i < 10; i++) {
  if (i > 50) { break; }
  continue;
};
```

`if`/`else if`/`else`, `while`, `do...while` and `for` are all supported, along with `break` and
`continue` inside loops. Note the trailing `;` after a `for` loop's closing `}` — it is required by
the parser, as shown in the compiler's own test suite.

## Functions

```c
int fact(int n) {
  if (n <= 1) {
    return 1;
  }
  return n * fact(n - 1);
}

int main() {
  return fact(5);
}
```

User-defined functions are typed on both return value and parameters (up to 8 parameters), support
recursion, and are called without a module prefix — unlike native functions, which always require
`module.function(...)`. The type of the value in every `return` statement must exactly match the
function's declared return type.

## Modules & native functions

Host applications extend CBS by registering a `Module` (subclassing
`com.github.alantr7.codebots.cbslang.low.runtime.modules.Module`) and, inside its `setup()` method,
calling `registerFunction(name, new ExternalFunction(this, name, returnType, paramType...) { ... })`
for each native function it wants to expose.

```java
public class SystemModule extends Module {
    public SystemModule() {
        super("system");
    }

    @Override
    public void setup() {
        registerFunction("print", new ExternalFunction(this, "print", DataType.VOID, DataType.STRING) {
            @Override
            public Data handle(Context context) {
                System.out.println(context.getArguments()[0].getValue());
                return null;
            }
        });
    }
}
```

Scripts then call it with `import system; system.print("hi");`. A module can be marked
"auto-import" (`setAutoImport(true)` in its constructor) so its functions are available without an
`import` statement or a module prefix.

### Long-running ("yielding") native functions

A native function does not have to complete in a single call. By setting
`context.setRecall(true)` and returning `null`, a function tells the executor to call it again on
the next tick, and `context.getMemory()[n]` can be used as call-scoped storage that survives
between those re-invocations (e.g. counting elapsed ticks, or remembering that an asynchronous
operation has already been started). This is how host applications implement actions that
logically "block" a script for multiple ticks — like movement or a network request — without
blocking the actual game/server loop.

## Known limitations

A few things worth knowing if you're writing non-trivial scripts, found while reading the
compiler's source:

- **Bitwise operators are incomplete.** The tokenizer and precedence table recognize `&`, `|` and
  `^` as operators, but `Parser#parseOperator` does not translate them into any actual operation
  (it returns `null`) — they are effectively unusable in the current version.
- **`struct` is not implemented.** It is a reserved keyword and the parser recognizes it at the
  top level, but it is simply skipped (`// parse struct` in the source) without producing anything
  — it is not currently possible to define usable struct types.
- **No dedicated boolean type** — conditions just use `int` values (`0` = false).
- **Array dimensions must be literal, positive integers** written directly in the source; they
  cannot be a variable or an expression.
- **Only two explicit casts exist**: `(int)` and `(float)`. There is no cast to/from `string`;
  host applications typically expose helper native functions for that instead (e.g. `to_int`,
  `to_float`, `is_int` in the CodeBots plugin's `lang` module).

## Contributing

This README was written by reading the tokenizer, parser and AST source code plus the existing
`CompilerTest` unit tests, since the project didn't yet have a written language reference. If you
spot an inaccuracy, or the language has moved on since, please open an issue or a PR — ideally
alongside a unit test in `CompilerTest.java` that demonstrates the behavior.
