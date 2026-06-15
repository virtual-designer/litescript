package me.rakinar2.litescript;

import java.util.List;
import me.rakinar2.litescript.frontend.lexer.Lexer;
import me.rakinar2.litescript.frontend.lexer.Token;

/**
 * Main entry point.
 * 
 * @author Ar Rakin [rakinar2@osndevs.org]
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Lexer lexer = new Lexer(new String[] {" ( + -) 100342"});
        List<Token> tokens = lexer.lex();
        System.out.println(tokens);
    }
}
