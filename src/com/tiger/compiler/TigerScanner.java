package com.tiger.compiler;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;

public class TigerScanner
{
	private List<DfaState> dfaTable;
	private int lineNum;
	private int charNum;
	private final String FILENAME = "test/test1.tiger";
    private FileInputStream inputStream;

	private List<Character> charList;
	
	public TigerScanner() {
		dfaTable = DfaTableGenerator.generateDfa();
		lineNum = 0;
		charNum = 0;
		charList = new ArrayList<Character>();

		try {
			FileInputStream fileInput = new FileInputStream(FILENAME);
			int r;
			while ((r = fileInput.read()) != -1) {
				char c = (char) r;
				charList.add(c);
			}

			fileInput.close();
		} catch(Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

        charList.add('\0'); //add null character at end to denote EOF
	}

	public Tuple<Token, String> nextToken() {
		
		StringBuilder sb = new StringBuilder("");
		DfaState currState = dfaTable.get(0);
		DfaState lastState = dfaTable.get(0);

        //first thing's first, check for EOF.
        //return null in case of EOF
        //do it outside of the loop, because EOF probably occurs
        //after valid token, which needs to detect EOF to know
        //to end the token
        if (charList.get(charNum) == '\0') {
            return new Tuple<Token, String>(Token.EOF, "EOF");
        }

		do {

            //should never happen, since we check for EOF
            //MIGHT happen in an un-closed block comment (since EOF will be considered an OTHER,
            //which is allowed in block comments)
            if(charNum >= charList.size())
				return new Tuple<Token, String>(Token.EOF, "EOF");

			char character = charList.get(charNum);

			charNum++;

			CharClass charClass = CharClass.classOf(character);

			lastState = currState;
			currState = currState.next(charClass);

			sb.append(character);
		} while(!currState.isError());
		charNum--;
		
		sb.deleteCharAt(sb.length() - 1);
		String tokenString = sb.toString();
		
		Token token = Token.classOf(lastState, tokenString);

        //skip over whitespace and block comments and return the next token
        if(token == Token.WHITESPACE || token == Token.BLOCKCOMMENT)
            return nextToken();

		return new Tuple<Token, String>(token, tokenString);
	}

}