import java.io.InputStream;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Simulate a PDA to evaluate a series of postfix expressions provided by a lexer.
 * The constructor argument is the lexer of type Lexer. A single line is evaluated and its
 * value is printed. Expression values can also be assigned to variables for later use.
 * If no variable is explicitly assigned, then the default variable "it" is assigned
 * the value of the most recently evaluated expression.
 *
 * @author Jacob Peterson (peter2js)
 */
public class Evaluator {

   /**
    * Run the desk calculator.
    */
   public static void main(String[] args) {
      Evaluator evaluator = new Evaluator(new Lexer(System.in));
      evaluator.run();
   }

   private Lexer lexer; // providing a stream of tokens
   private LinkedList<Double> stack; // operands
   private HashMap<String, Double> symbols; // symbol table for variables
   private String target; // variable assigned the latest expression value

   public Evaluator(Lexer lexer) {
      this.lexer = lexer;
      stack = new LinkedList<>();
      symbols = new HashMap<>();
      target = "it";
   }

   /**
    * Evaluate a single line of input, which should be a complete expression
    * optionally assigned to a variable; if no variable is assigned to, then
    * the result is assigned to "it". In any case, return the value of the
    * expression, or null if there was some sort of error.
    */
   public Double evaluate() {

      // initializers
      // assignVal : is like a temporary this.target String for each line input
      int token = 0;
      String assignVal = target;
      if (!symbols.containsKey(assignVal))
         symbols.put (assignVal, 0.0);

      do {

         token = lexer.nextToken();

         // if token is a variable
         if (token == 12) {

            String variable = getVariable();

            // check if the following token is an assignment variable
            // if it is its the new assignVal
            // if its not it should be treated like a number
            // if its undefined (cant be treated like a number) theres a problem.
            if (lexer.nextToken() == 8) {

               // set assignVal to the new variable, put that in the hashmap
               // set its value to 0.0.
               // by the end of the line the new value will be put in
               assignVal = variable;
               symbols.put(variable, 0.0);
               char c = ' ';
               while (c != '=') {
                  c = lexer.nextChar();
               }
            } else if (symbols.containsKey(variable)) {
               // treat the variable like a number.
               stack.add (0, symbols.get(variable));
            } else {
               error ("Invalid use of Variable.\n" +
                     "Variable must be assigned a Number before use.");
               return null;
            }

         }

         // if token is a number
         if (token == 11) {
            stack.add(0, getNumber());
         }

         // if token is a operation (+,-,*,/)
         if (token > 2 && token < 7) {

            // to perform an operation at least 2 numbers/variables are required
            // to be on the stack.
            // otherwise there are to many operations compared to the number of
            // numbers/variables
            if (stack.size() < 2) {

               // this is sort of unnessisary but its nice to know what operation
               // the program was at when it fucked up
               String msg = "2 Numbers/Variables required to perform ";
               switch (token) {
                  case 3:
                     msg += "Addition.";
                     break;
                  case 4:
                     msg += "Subtraction.";
                     break;
                  case 5:
                     msg += "Multiplication.";
                     break;
                  case 6:
                     msg += "Division.";
                     break;
               }

               msg += "\n  " + stack.size() + " Numbers/Variables were found.";
               error(msg);
               return null;
            }

            // get the top two doubles off the stack, perform operation, then
            // put it back into the stack
            double v1 = stack.get(0);
            stack.remove(0);
            double v2 = stack.get(0);
            stack.remove(0);

            lexer.nextChar();
            double result = 0.0;

            // perform operation based on the token
            switch (token) {
               case 3:
                  result = v2 + v1;
                  break;
               case 4:
                  result = v2 - v1;
                  break;
               case 5:
                  result = v2 * v1;
                  break;
               case 6:
                  result = v2 / v1;
                  break;
            }

            // throw it back on the stack
            stack.add(0, result);
         }

         // if ~ then make the number at the top of the stack negative
         if (token == 7) {

            // ensure that there is at least one variable for the minus operation
            // to negate
            if (stack.size() == 0) {
               error ("1 Number/Variable required to perform negation.\n" +
                     "  0 Numers/Variables were found.");
               return null;
            }

            // get the double off the stack, negate it, then put it back
            double v = stack.get(0);
            stack.remove(0);
            v = v * -1;
            stack.add(0, v);
            lexer.nextChar();
         }

         // if at the end of the line there are still stuff in the stack,
         // something went very wrong.
         if (token == 9) {
            
            // There must only be one double still in the stack by the end of a line
            // otherwise the number of numbers/variables was to high compared
            // to the number of operations
            if (stack.size() != 1) {

               error ("Not Enough Operations for the number of Numbers/Variables");
               return null;
            }
            
            // put the last double in the stack to whatever is stored into 
            // the assigned variable
            symbols.replace (assignVal, stack.get(0));
            stack.remove(0);
         }

         if (token == 100) {
            error ("Bad Token Value was Inputed");
            return null;
         }

      // if current line = "" then the program has run its course. 
      } while (token != 9);

      lexer.nextChar();
      return symbols.get(assignVal);

   } // evaluate

   /**
    * Will loop through a variable that is being pointed to by the lexer.
    * This method should record and return this variable but leave the lexer pointing
    * at whatever the next token after the variable will be.
    *
    * @return the variable string
    */
   private String getVariable() {

      String variable = "";
      char c;
      int token;
      boolean valid = false;
      do {

         // retrieve the next char and token
         c = lexer.nextChar();
         lexer.unread();
         token = lexer.nextToken();
         lexer.nextChar();

         // if the char is still apart of the variable add it and continue
         valid = (c != ' ') && (token == 11 || token == 12);
         if (valid) variable += c;
      } while (valid);

      lexer.unread();
      return variable;
   }

   /**
    * Should loop over a Number that is being pointed to by the lexer.
    * If no . present itll make the int a double regardless.
    * in most cases after reading the number will ensure the next char pointed to
    * by the lexer is the one after the number.
    */
   private double getNumber() {

      String number = "";
      char c;
      int token;
      boolean valid = false;

      do {

         // retrieves the next char and token
         c = lexer.nextChar();
         lexer.unread();
         token = lexer.nextToken();
         lexer.nextChar();

         // if char is still apart of the number add it and continue
         valid = (c != ' ') && (token == 11 || c == '.');
         if (valid) number += c;
      } while (valid);

      lexer.unread();
      return Double.parseDouble(number);
   }

   /**
    * Run evaluate on each line of input and print the result forever.
    */
   public void run() {
      while (true) {
         Double value = evaluate();
         if (value == null)
            System.out.println("no value");
         else
            System.out.println(value);
      }
   }

   /**
    * Print an error message, display the offending line with the current
    * location marked, and flush the lexer in preparation for the next line.
    *
    * @param msg what to print as an error indication
    */
   private void error(String msg) {
      System.out.println(msg);
      String line = lexer.getCurrentLine();
      int index = lexer.getCurrentChar();
      System.out.print(line);
      for (int i = 1; i < index; i++) System.out.print(' ');
      System.out.println("^");
      lexer.flush();
   }

   ////////////////////////////////
   ///////// Lexer Class //////////

   /**
   * Read terminal input and convert it to a token type, and also record the text
   * of each token. Whitespace is skipped. The input comes from stdin, and each line
   * is prompted for.
   */
   public static class Lexer {

      // language token codes
      public static final int ADD_OP      = 3;
      public static final int SUBTRACT_OP = 4;
      public static final int MULTIPLY_OP = 5;
      public static final int DIVIDE_OP   = 6;
      public static final int MINUS_OP    = 7;
      public static final int ASSIGN_OP   = 8;
      public static final int EOL         = 9;
      public static final int NUMBER      = 11;
      public static final int VARIABLE    = 12;
      public static final int BAD_TOKEN   = 100;

      private Scanner input;     // for reading lines from stdin
      private String line;       // next input line
      private int index;         // current character in this line
      private String text;       // text of the current token

      public Lexer(InputStream in) {
         input = new Scanner(in);
         line = "";
         index = 0;
         text = "";
      }

      /**
       * Fetch the next character from the terminal. If the current line is
       * exhausted, then prompt the user and wait for input. If end-of-file occurs,
       * then exit the program.
       */
      private char nextChar() {
         if (index == line.length()) {
            System.out.print(">> ");
            if (input.hasNextLine()) {
               line = input.nextLine() + "\n";
               index = 0;
            } else {
               System.out.println("\nBye");
               System.exit(0);
            }
         }
         char ch = line.charAt(index);
         index++;
         return ch;
      }

      /**
       * Put the last character back on the input line.
       */
      private void unread() { index -= 1; }

      /**
       * Return the next token from the terminal.
       */
      public int nextToken() {

         // initializers
         char nextChar = ' ';
         int token = BAD_TOKEN;
         
         // loops through until we have a char worth putting a token on
         // this allows spaces and brackets into the line
         while (nextChar == ' ' || nextChar == '(' || nextChar == ')') {
            nextChar = nextChar();
         }

         index--;

         // assigns token value based on the char that was read in nextChar
         if (nextChar == '+') {
            token = ADD_OP;
         } else if (nextChar == '-') {
            token = SUBTRACT_OP;
         } else if (nextChar == '*') {
            token = MULTIPLY_OP;
         } else if (nextChar == '/') {
            token = DIVIDE_OP;
         } else if (nextChar == '~') {
            token = MINUS_OP;
         } else if (nextChar == '=') {
            token = ASSIGN_OP;
         } else if (nextChar == '\n') {
            token = EOL;
         } else if (nextChar >= '0' && nextChar <= '9') {
            token = NUMBER;
         } else if ((nextChar >= 'A' && nextChar <= 'Z')
                     || (nextChar >= 'a' && nextChar <= 'z')) {
            token = VARIABLE;
         } 

         return token;

      } // nextToken

      /**
       * Return the current line for error messages.
       */
      public String getCurrentLine() { return line; }

      /**
       * Return the current character index for error messages.
       */
      public int getCurrentChar() { return index; }

      /**
       * /** Return the text of the current token.
       */
      public String getText() { return text; }

      /**
       * Clear the current line after an error
       */
      public void flush() { index = line.length(); }

   } // Lexer

} // Evaluator
