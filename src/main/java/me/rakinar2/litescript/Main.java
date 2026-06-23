package me.rakinar2.litescript;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import me.rakinar2.litescript.ast.nodes.RootNode;
import me.rakinar2.litescript.frontend.lexer.Lexer;
import me.rakinar2.litescript.frontend.lexer.Token;
import me.rakinar2.litescript.frontend.parser.Parser;
import me.rakinar2.litescript.frontend.SyntaxException;
import me.rakinar2.litescript.interpreter.ExecutionContext;
import me.rakinar2.litescript.interpreter.Interpreter;
import me.rakinar2.litescript.interpreter.InterpreterRuntimeException;
import me.rakinar2.litescript.interpreter.RuntimeValue;
import me.rakinar2.litescript.interpreter.ValueFormatter;

/**
 * Main entry point.
 * 
 * @author Ar Rakin [rakinar2@osndevs.org]
 */
public class Main {
    private static final Interpreter interpreter = new Interpreter();
    private static final ExecutionContext context = interpreter.createDefaultContext();
            
    public static void main(String[] args) throws Exception {        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            for (;;) {
                System.out.print("\033[1;34m[litescript:repl]$\033[0m ");
                System.out.flush();

                String line = reader.readLine();
                
                if (line == null || line.equals(".exit")) {
                    break;
                }
                
                if (line.isEmpty()) {
                    continue;
                }

                try {
                    RuntimeValue value = executeLine(line);
                    System.out.format("\033[2m>\033[0m %s\n", ValueFormatter.format(value));
                }
                catch (SyntaxException exception) {
                    System.err.format("%s:%d:%d: syntax error: %s\n", 
                            exception.getLocation().fileName(),
                            exception.getLocation().lineStart(), 
                            exception.getLocation().columnStart(),
                            exception.getMessage());
                }
                catch (InterpreterRuntimeException exception) {
                    System.err.format("%s:%d:%d: error: %s\n", 
                            exception.getLocation().fileName(),
                            exception.getLocation().lineStart(), 
                            exception.getLocation().columnStart(),
                            exception.getMessage());
                }
            }
        }
    }
    
    public static RuntimeValue executeLine(String line) {
        Lexer lexer = new Lexer("<stdin>", new String[] {line});
        List<Token> tokens = lexer.lex();

        Parser parser = new Parser(tokens, false);
        RootNode rootNode = parser.parseAll();

        return interpreter.interpret(rootNode, context);
    }
}
