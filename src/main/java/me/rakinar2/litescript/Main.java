package me.rakinar2.litescript;

import java.util.List;
import me.rakinar2.litescript.ast.nodes.ExpressionStatementNode;
import me.rakinar2.litescript.ast.nodes.LiteralExpressionNode;
import me.rakinar2.litescript.ast.nodes.RootNode;
import me.rakinar2.litescript.frontend.lexer.Lexer;
import me.rakinar2.litescript.frontend.lexer.LexicalAnalysisException;
import me.rakinar2.litescript.frontend.lexer.Token;
import me.rakinar2.litescript.frontend.parser.Parser;
import me.rakinar2.litescript.frontend.parser.ParserException;

/**
 * Main entry point.
 * 
 * @author Ar Rakin [rakinar2@osndevs.org]
 */
public class Main {
    public static void main(String[] args) throws Exception {
        try {
            Lexer lexer = new Lexer("<mem>", new String[] {"273"});
            List<Token> tokens = lexer.lex();
            System.out.println(tokens);

            Parser parser = new Parser(tokens);
            RootNode rootNode = parser.parseAll();

            System.out.println(rootNode);
            System.out.println(rootNode.statements.get(0));

            final var stmt = rootNode.statements.get(0);

            if (stmt instanceof ExpressionStatementNode s) {
                if (s.expression instanceof LiteralExpressionNode l) {
                    System.out.println(l.value);
                }
            }
        }
        catch (LexicalAnalysisException exception) {
            System.err.format("%s:%d:%d: error: %s\n", 
                    exception.location.fileName(),
                    exception.location.lineStart(), 
                    exception.location.lineEnd(),
                    exception.getMessage());
            System.exit(1);
        }
        catch (ParserException exception) {
            System.err.format("%s:%d:%d: error: %s\n", 
                    exception.location.fileName(),
                    exception.location.lineStart(), 
                    exception.location.lineEnd(),
                    exception.getMessage());
            System.exit(1);
        }
    }
}
