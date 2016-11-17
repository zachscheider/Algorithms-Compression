/*************************************************************************
 *  Compilation:  javac MyLZW.java
 *  Execution:    java MyLZW - < input.txt   (compress)
 *  Execution:    java MyLZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/

import java.lang.*;
import java.util.*;

public class MyLZW {
    private static final int MIN_WIDTH = 9;
    private static final int MAX_WIDTH = 16;
    private static int R = 256;        // number of input chars
    private static int W = MIN_WIDTH;         // codeword width4
    private static int L = (int) Math.pow(2, W);       // number of codewords = 2^W
    private static char mode = 'n';
    private static int uncompressed = 0, compressed = 0;
    private static double prevRatio = 1.0, ratio = 0.0;
    private static boolean firstMonitor = true;

    public static void compress() { 
        int strPos = 0;
        //Write mode to file as char
        BinaryStdOut.write(mode);

        //String input = BinaryStdIn.readString();
        StringBuilder input = new StringBuilder(BinaryStdIn.readString());
        TST<Integer> st = new TST<Integer>();
        System.err.println("Initializing codebook");
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF

        System.err.println("Beginning compression...");
        while (input.length() > strPos) {
            String s = st.longestPrefixOf(input, strPos);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();

            if(mode == 'm'){
                uncompressed += t * 8;
                compressed += W;
            }

            if(code >= L){
                if(!increaseWidth()){
                    boolean resetCodebook = false;
                    if(mode == 'n');
                    else if(mode == 'r'){
                        resetCodebook = true;
                    }else if(mode == 'm'){
                        ratio = uncompressed / (double) compressed;
                        if(firstMonitor){
                            prevRatio = ratio;
                            firstMonitor = false;
                        }
                        else if(prevRatio / ratio > 1.1){
                            resetCodebook = true;
                            firstMonitor = true;
                            System.err.printf("The ratio has degraded. It was: %.2f / %.2f = %.2f\n", prevRatio, ratio, prevRatio/ratio);
                        }
                    }
                    if(resetCodebook){
                        System.err.println("Codebook reset\nStill working...");
                        W = MIN_WIDTH;
                        L = (int) Math.pow(2, W);
                        st = new TST<Integer>();
                        for (int i = 0; i < R; i++)
                            st.put("" + (char) i, i);

                        code = R+1;
                    }
                }
            }
            if ((strPos + t) < input.length() && code < L)    // Add s to symbol table.
                st.put(input.substring(strPos, strPos + t + 1), code++);
            strPos += t;
            //input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 

    public static void expand() {
        // reads the mode from the file
        mode = BinaryStdIn.readChar();

        ArrayList<String> st = new ArrayList<String>(L);
        int i; // next available codeword value

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st.add("" + (char) i);
        st.add("");
        i++;                        // (unused) lookahead for EOF

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st.get(codeword);

        while (true) {

            if(mode == 'm'){
                uncompressed += val.length() * 8;
                compressed += W;
            }

            if(i >= L){
                if(!increaseWidth()){
                    boolean resetCodebook = false;
                    if(mode == 'n');
                    else if(mode == 'r'){
                        resetCodebook = true;
                    }else if(mode == 'm'){
                        ratio = uncompressed / (double) compressed;
                        if(firstMonitor){
                            prevRatio = ratio;
                            firstMonitor = false;
                        }
                        else if(prevRatio / ratio > 1.1){
                            resetCodebook = true;
                            firstMonitor = true;
                            System.err.printf("The ratio has degraded. It was: %.2f / %.2f = %.2f\n", prevRatio, ratio, prevRatio/ratio);
                        }
                    }
                    if(resetCodebook){
                        W = MIN_WIDTH;
                        L = (int) Math.pow(2, W);
                        st = new ArrayList<String>(L);
                        for (i = 0; i < R; i++)
                            st.add("" + (char) i);
                        st.add("");
                        i++; 
                    }
                }
            }

            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s;
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            else s = st.get(codeword);
            if (i < L){
                st.add(val + s.charAt(0));
                i++;
            }
            val = s;
        }
        BinaryStdOut.close();
    }

    private static boolean increaseWidth(){
        if(W < MAX_WIDTH){
            W++;
            L = (int) Math.pow(2, W);
            return true;
        }else{
            return false;
        }
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) {
            if(args[1].equals("n") || args[1].equals("r") || args[1].equals("m")){
                mode = args[1].charAt(0);
                compress();
            }
            else throw new IllegalArgumentException("Please only use \'n\', \'r\', or \'m\' ");
        }
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}