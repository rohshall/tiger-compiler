package com.tiger.compiler.frontend.parser;

import com.tiger.compiler.Tuple;
import com.tiger.compiler.frontend.GrammarSymbol;
import com.tiger.compiler.frontend.Token;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ParserTableGenerator
{
    public final static String PRODUCTIONS_FILENAME = "res/ParserProductions.csv";
    public final static String TABLE_FILENAME = "res/ParserTable.csv";

    private static List<ParserProduction> generateParserProductions()
    {
        List<ParserProduction> rules = new ArrayList<>();
        Scanner input = null;

        try
        {
            input = new Scanner(new File(PRODUCTIONS_FILENAME));
        }
        catch(Exception e)
        {
            return null;
        }

        int ruleNumber = 1;

        /***************Each row from here below is a DFA state***************/
        while(input.hasNext())
        {
            String row = input.nextLine();
            Scanner rowScanner = new Scanner(row).useDelimiter(",");

            String lhs = rowScanner.next();
            lhs = lhs.substring(1, lhs.length() - 1);

            NonterminalSymbol lhsSymbol = NonterminalSymbol.valueOf(lhs);

            rowScanner.useDelimiter("[, ]");
            List<GrammarSymbol> rhsSymbols = new ArrayList<>();

            while(rowScanner.hasNext())
            {
                String str = rowScanner.next();

                //TODO: why is empty string ever even coming up?
                //this is a temporary fix to ignore empty strings
                if(str.isEmpty())
                    continue;

                if(str.startsWith("<") && str.endsWith(">"))
                {
                    str = str.substring(1, str.length() - 1);
                    rhsSymbols.add(NonterminalSymbol.valueOf(str));
                }
                else if(str.startsWith("#"))
                {
                    str = str.substring(1, str.length());
                    rhsSymbols.add(SemanticAction.valueOf(str));
                }
                else
                {
                    rhsSymbols.add(Token.valueOf(str));
                }
            }

            rules.add(new ParserProduction(ruleNumber, lhsSymbol, rhsSymbols));

            ruleNumber++;
        }

        input.close();
        return rules;
    }

    public static ParserTable generateParserTable()
    {
        List<ParserProduction> rules = generateParserProductions();
        Map<Tuple<NonterminalSymbol, Token>, ParserProduction> table = new HashMap<>();

        Scanner input = null;

        try
        {
            input = new Scanner(new File(TABLE_FILENAME));
        }
        catch(Exception e)
        {
            return null;
        }

        //this row lists all the tokens
        String row0 = input.nextLine();
        Scanner rowScanner = new Scanner(row0).useDelimiter(",");

        //record which column each token corresponds with
        //so when we see transitions, we know what character class to map them to
        List<Token> tokens = new ArrayList<>();

        while(rowScanner.hasNext())
        {
            String column = rowScanner.next();
            tokens.add(Token.valueOf(column));
        }

        while(input.hasNext())
        {
            String row = input.nextLine();
            rowScanner = new Scanner(row).useDelimiter(",");

            String nt = rowScanner.next();
            nt = nt.substring(1, nt.length() - 1);

            NonterminalSymbol nonterminal = NonterminalSymbol.valueOf(nt);


            int column = 0;

            while(rowScanner.hasNext())
            {
                Token terminal = tokens.get(column);
                Tuple<NonterminalSymbol, Token> key = new Tuple<>(nonterminal, terminal);

                String str = rowScanner.next();

                if(str.equals("E"))
                {
                    table.put(key, ParserProduction.ERROR);
                }
                else
                {
                    int ruleId = Integer.parseInt(str);

                    for(ParserProduction production: rules)
                    {
                        if(production.getId() == ruleId)
                        {
                            table.put(key, production);
                            break;
                        }
                    }
                }
                column++;
            }
        }

        rowScanner.close();
        input.close();
        return new ParserTable(table);
    }
}
