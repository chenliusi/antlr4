/*
 [The "BSD license"]
  Copyright (c) 2011 Terence Parr
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions
  are met:

  1. Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
  3. The name of the author may not be used to endorse or promote products
     derived from this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.antlr.v4.test;

import org.antlr.v4.Tool;
import org.antlr.v4.automata.ParserATNFactory;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.tool.DOTGenerator;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.antlr.v4.tool.Rule;
import org.junit.Test;

import java.util.List;

	// NOTICE: TOKENS IN LEXER, PARSER MUST BE SAME OR TOKEN TYPE MISMATCH
	// NOTICE: TOKENS IN LEXER, PARSER MUST BE SAME OR TOKEN TYPE MISMATCH
	// NOTICE: TOKENS IN LEXER, PARSER MUST BE SAME OR TOKEN TYPE MISMATCH

public class TestATNParserPrediction extends BaseTest {
	@Test public void testAorB() throws Exception {
		LexerGrammar lg = new LexerGrammar(
			"lexer grammar L;\n" +
			"A : 'a' ;\n" +
			"B : 'b' ;\n" +
			"C : 'c' ;\n");
		Grammar g = new Grammar(
			"parser grammar T;\n"+
			"a : A | B ;");
		int decision = 0;
		checkPredictedAlt(lg, g, decision, "a", 1);
		checkPredictedAlt(lg, g, decision, "b", 2);

		// After matching these inputs for decision, what is DFA after each prediction?
		String[] inputs = {
			"a",
			"b",
			"a"
		};
		String[] dfa = {
			"s0-'a'->:s1=>1\n",

			"s0-'a'->:s1=>1\n" +
			"s0-'b'->:s2=>2\n",

			"s0-'a'->:s1=>1\n" + // don't change after it works
			"s0-'b'->:s2=>2\n",
		};
		checkDFAConstruction(lg, g, decision, inputs, dfa);
	}

	@Test public void testEmptyInput() throws Exception {
		LexerGrammar lg = new LexerGrammar(
			"lexer grammar L;\n" +
			"A : 'a' ;\n" +
			"B : 'b' ;\n" +
			"C : 'c' ;\n");
		Grammar g = new Grammar(
			"parser grammar T;\n"+
			"a : A | ;");
		int decision = 0;
		checkPredictedAlt(lg, g, decision, "a", 1);
		checkPredictedAlt(lg, g, decision, "", 2);

		// After matching these inputs for decision, what is DFA after each prediction?
		String[] inputs = {
			"a",
			"",
		};
		String[] dfa = {
			"s0-'a'->:s1=>1\n",

			"s0-EOF->:s2=>2\n" +
			"s0-'a'->:s1=>1\n",
		};
		checkDFAConstruction(lg, g, decision, inputs, dfa);
	}

	@Test public void testPEGAchillesHeel() throws Exception {
		LexerGrammar lg = new LexerGrammar(
			"lexer grammar L;\n" +
			"A : 'a' ;\n" +
			"B : 'b' ;\n" +
			"C : 'c' ;\n");
		Grammar g = new Grammar(
			"parser grammar T;\n"+
			"a : A | A B ;");
		checkPredictedAlt(lg, g, 0, "a", 1);
		checkPredictedAlt(lg, g, 0, "ab", 2);
		checkPredictedAlt(lg, g, 0, "abc", 2);

		String[] inputs = {
			"a",
			"ab",
			"abc"
		};
		String[] dfa = {
			"s0-'a'->s1\n" +
			"s1-EOF->:s2=>1\n",

			"s0-'a'->s1\n" +
			"s1-EOF->:s2=>1\n" +
			"s1-'b'->:s3=>2\n",

			"s0-'a'->s1\n" +
			"s1-EOF->:s2=>1\n" +
			"s1-'b'->:s3=>2\n"
		};
		checkDFAConstruction(lg, g, 0, inputs, dfa);
	}

	@Test public void testRuleRefxory() throws Exception {
		LexerGrammar lg = new LexerGrammar(
			"lexer grammar L;\n" +
			"A : 'a' ;\n" +
			"B : 'b' ;\n" +
			"C : 'c' ;\n");
		Grammar g = new Grammar(
			"parser grammar T;\n"+
			"a : x | y ;\n" +
			"x : A ;\n" +
			"y : B ;\n");
		int decision = 0;
		checkPredictedAlt(lg, g, decision, "a", 1);
		checkPredictedAlt(lg, g, decision, "b", 2);

		// After matching these inputs for decision, what is DFA after each prediction?
		String[] inputs = {
			"a",
			"b",
			"a"
		};
		String[] dfa = {
			"s0-'a'->:s1=>1\n",

			"s0-'a'->:s1=>1\n" +
			"s0-'b'->:s2=>2\n",

			"s0-'a'->:s1=>1\n" + // don't change after it works
			"s0-'b'->:s2=>2\n",
		};
		checkDFAConstruction(lg, g, decision, inputs, dfa);
	}

	@Test public void testOptionalRuleChasesGlobalFollow() throws Exception {
		LexerGrammar lg = new LexerGrammar(
			"lexer grammar L;\n" +
			"A : 'a' ;\n" +
			"B : 'b' ;\n" +
			"C : 'c' ;\n");
		Grammar g = new Grammar(
			"parser grammar T;\n"+
			"tokens {A;B;C;}\n" +
			"a : x B ;\n" +
			"b : x C ;\n" +
			"x : A | ;\n");
		int decision = 0;
		checkPredictedAlt(lg, g, decision, "a", 1);
		checkPredictedAlt(lg, g, decision, "b", 2);
		checkPredictedAlt(lg, g, decision, "c", 2);

		// After matching these inputs for decision, what is DFA after each prediction?
		String[] inputs = {
			"a",
			"b",
			"c",
			"c",
		};
		String[] dfa = {
			"s0-'a'->:s1=>1\n",

			"s0-'a'->:s1=>1\n" +
			"s0-'b'->:s2=>2\n",

			"s0-'a'->:s1=>1\n" +
			"s0-'b'->:s2=>2\n" +
			"s0-'c'->:s3=>2\n",

			"s0-'a'->:s1=>1\n" +
			"s0-'b'->:s2=>2\n" +
			"s0-'c'->:s3=>2\n",
		};
		checkDFAConstruction(lg, g, decision, inputs, dfa);
	}

	@Test public void testLL1Ambig() throws Exception {
		LexerGrammar lg = new LexerGrammar(
			"lexer grammar L;\n" +
			"A : 'a' ;\n" +
			"B : 'b' ;\n" +
			"C : 'c' ;\n");
		Grammar g = new Grammar(
			"parser grammar T;\n"+
			"a : A | A | A B ;");
		int decision = 0;
		checkPredictedAlt(lg, g, decision, "a", 1);
		checkPredictedAlt(lg, g, decision, "ab", 3);

		// After matching these inputs for decision, what is DFA after each prediction?
		String[] inputs = {
			"a",
			"ab",
			"ab"
		};
		String[] dfa = {
			"s0-'a'->s1\n" +
			"s1-EOF->:s2=>1\n",

			"s0-'a'->s1\n" +
			"s1-EOF->:s2=>1\n" +
			"s1-'b'->:s3=>3\n",

			"s0-'a'->s1\n" +
			"s1-EOF->:s2=>1\n" +
			"s1-'b'->:s3=>3\n",
		};
		checkDFAConstruction(lg, g, decision, inputs, dfa);
	}

	@Test public void testLL2Ambig() throws Exception {
		LexerGrammar lg = new LexerGrammar(
			"lexer grammar L;\n" +
			"A : 'a' ;\n" +
			"B : 'b' ;\n" +
			"C : 'c' ;\n");
		Grammar g = new Grammar(
			"parser grammar T;\n"+
			"a : A B | A B | A B C ;");
		int decision = 0;
		checkPredictedAlt(lg, g, decision, "ab", 1);
		checkPredictedAlt(lg, g, decision, "abc", 3);

		// After matching these inputs for decision, what is DFA after each prediction?
		String[] inputs = {
			"ab",
			"abc",
			"ab"
		};
		String[] dfa = {
			"s0-'a'->s1\n" +
			"s1-'b'->s2\n" +
			"s2-EOF->:s3=>1\n",

			"s0-'a'->s1\n" +
			"s1-'b'->s2\n" +
			"s2-EOF->:s3=>1\n" +
			"s2-'c'->:s4=>3\n",

			"s0-'a'->s1\n" +
			"s1-'b'->s2\n" +
			"s2-EOF->:s3=>1\n" +
			"s2-'c'->:s4=>3\n",
		};
		checkDFAConstruction(lg, g, decision, inputs, dfa);
	}

	@Test public void testFullLLContextParse() throws Exception {
		LexerGrammar lg = new LexerGrammar(
			"lexer grammar L;\n" +
			"A : 'a' ;\n" +
			"B : 'b' ;\n" +
			"C : 'c' ;\n");
		// AB predicted in both alts of e but in diff contexts.
		Grammar g = new Grammar(
			"parser grammar T;\n"+
			"tokens {A;B;C;}\n" +
			"a : e B ;\n" +
			"b : e A B ;\n" +
			"e : A | ;\n"); // TODO: try with three alts

		ATN lexatn = createATN(lg);
		LexerATNSimulator lexInterp = new LexerATNSimulator(lexatn);

		semanticProcess(lg);
		g.importVocab(lg);
		semanticProcess(g);

		ParserATNFactory f = new ParserATNFactory(g);
		ATN atn = f.createATN();

		RuleStartState aStart = atn.ruleToStartState[g.getRule("a").index];
		RuleStartState bStart = atn.ruleToStartState[g.getRule("b").index];
		RuleStartState eStart = atn.ruleToStartState[g.getRule("e").index];
		ATNState a_e_invoke = aStart.transition(0).target; //
		ATNState b_e_invoke = bStart.transition(0).target; //
		ParserRuleContext a_ctx = new ParserRuleContext(null, -1, a_e_invoke.stateNumber);
		ParserRuleContext b_ctx = new ParserRuleContext(null, -1, b_e_invoke.stateNumber);
		ParserRuleContext a_e_ctx = new ParserRuleContext(a_ctx, a_e_invoke.stateNumber, bStart.stateNumber);
		ParserRuleContext b_e_ctx = new ParserRuleContext(b_ctx, b_e_invoke.stateNumber, bStart.stateNumber);

		ParserATNSimulator interp = new ParserATNSimulator(atn);
		interp.setContextSensitive(true);
		List<Integer> types = getTokenTypesViaATN("ab", lexInterp);
		System.out.println(types);
		TokenStream input = new IntTokenStream(types);
		int alt = interp.adaptivePredict(input, 0, b_e_ctx);
		assertEquals(alt, 2);
		DFA dfa = interp.decisionToDFA[0];
		String expecting =
			"s0-'a'->s1\n" +
			"s1-'b'->s2\n" +
			"s2-EOF->:s3@{[10]=2}\n";
		assertEquals(expecting, dfa.toString(g.getTokenDisplayNames()));

		alt = interp.adaptivePredict(input, 0, b_e_ctx); // cached
		assertEquals(alt, 2);
		expecting =
			"s0-'a'->s1\n" +
			"s1-'b'->s2\n" +
			"s2-EOF->:s3@{[10]=2}\n";
		assertEquals(expecting, dfa.toString(g.getTokenDisplayNames()));

		alt = interp.adaptivePredict(input, 0, a_e_ctx); // forces new context-sens ATN match
		assertEquals(alt, 1);
		expecting =
			"s0-'a'->s1\n" +
			"s1-'b'->s2\n" +
			"s2-EOF->:s3@{[10]=2, [6]=1}\n";
		assertEquals(expecting, dfa.toString(g.getTokenDisplayNames()));

		alt = interp.adaptivePredict(input, 0, b_e_ctx); // cached
		assertEquals(alt, 2);
		expecting =
			"s0-'a'->s1\n" +
			"s1-'b'->s2\n" +
			"s2-EOF->:s3@{[10]=2, [6]=1}\n";
		assertEquals(expecting, dfa.toString(g.getTokenDisplayNames()));

		alt = interp.adaptivePredict(input, 0, a_e_ctx); // cached
		assertEquals(alt, 1);
		expecting =
			"s0-'a'->s1\n" +
			"s1-'b'->s2\n" +
			"s2-EOF->:s3@{[10]=2, [6]=1}\n";
		assertEquals(expecting, dfa.toString(g.getTokenDisplayNames()));

		types = getTokenTypesViaATN("b", lexInterp);
		System.out.println(types);
		input = new IntTokenStream(types);
		alt = interp.adaptivePredict(input, 0, null); // ctx irrelevant
		assertEquals(alt, 2);
		expecting =
			"s0-'a'->s1\n" +
			"s0-'b'->:s4=>2\n" +
			"s1-'b'->s2\n" +
			"s2-EOF->:s3@{[10]=2, [6]=1}\n";
		assertEquals(expecting, dfa.toString(g.getTokenDisplayNames()));

		types = getTokenTypesViaATN("aab", lexInterp);
		System.out.println(types);
		input = new IntTokenStream(types);
		alt = interp.adaptivePredict(input, 0, null);
		assertEquals(alt, 1);
		expecting =
			"s0-'a'->s1\n" +
			"s0-'b'->:s4=>2\n" +
			"s1-'a'->:s5=>1\n" +
			"s1-'b'->s2\n" +
			"s2-EOF->:s3@{[10]=2, [6]=1}\n";
		assertEquals(expecting, dfa.toString(g.getTokenDisplayNames()));
	}

	@Test public void testRecursiveLeftPrefix() throws Exception {
		LexerGrammar lg = new LexerGrammar(
			"lexer grammar L;\n" +
			"A : 'a' ;\n" +
			"B : 'b' ;\n" +
			"C : 'c' ;\n" +
			"LP : '(' ;\n" +
			"RP : ')' ;\n" +
			"INT : '0'..'9'+ ;\n"
		);
		Grammar g = new Grammar(
			"parser grammar T;\n"+
			"tokens {A;B;C;LP;RP;INT;}\n" +
			"a : e B | e C ;\n" +
			"e : LP e RP\n" +
			"  | INT\n" +
			"  ;");
		int decision = 0;
		checkPredictedAlt(lg, g, decision, "34b", 1);
		checkPredictedAlt(lg, g, decision, "34c", 2);
		checkPredictedAlt(lg, g, decision, "((34))b", 1);
		checkPredictedAlt(lg, g, decision, "((34))c", 2);

		// After matching these inputs for decision, what is DFA after each prediction?
		String[] inputs = {
			"34b",
			"34c",
			"((34))b",
			"((34))c"
		};
		String[] dfa = {
			"s0-INT->s1\n" +
			"s1-'b'->:s2=>1\n",

			"s0-INT->s1\n" +
			"s1-'b'->:s2=>1\n" +
			"s1-'c'->:s3=>2\n",

			"s0-'('->s4\n" +
			"s0-INT->s1\n" +
			"s1-'b'->:s2=>1\n" +
			"s1-'c'->:s3=>2\n" +
			"s4-'('->s5\n" +
			"s5-INT->s6\n" +
			"s6-')'->s7\n" +
			"s7-')'->s1\n",

			"s0-'('->s4\n" +
			"s0-INT->s1\n" +
			"s1-'b'->:s2=>1\n" +
			"s1-'c'->:s3=>2\n" +
			"s4-'('->s5\n" +
			"s5-INT->s6\n" +
			"s6-')'->s7\n" +
			"s7-')'->s1\n",
		};
		checkDFAConstruction(lg, g, decision, inputs, dfa);
	}

	@Test public void testRecursiveLeftPrefixWithAorABIssue() throws Exception {
		LexerGrammar lg = new LexerGrammar(
			"lexer grammar L;\n" +
			"A : 'a' ;\n" +
			"B : 'b' ;\n" +
			"C : 'c' ;\n" +
			"LP : '(' ;\n" +
			"RP : ')' ;\n" +
			"INT : '0'..'9'+ ;\n"
		);
		Grammar g = new Grammar(
			"parser grammar T;\n"+
			"tokens {A;B;C;LP;RP;INT;}\n" +
			"a : e A | e A B ;\n" +
			"e : LP e RP\n" +
			"  | INT\n" +
			"  ;");
		int decision = 0;
		checkPredictedAlt(lg, g, decision, "34a", 1);
		checkPredictedAlt(lg, g, decision, "34ab", 2); // PEG would miss this one!
		checkPredictedAlt(lg, g, decision, "((34))a", 1);
		checkPredictedAlt(lg, g, decision, "((34))ab", 2);

		// After matching these inputs for decision, what is DFA after each prediction?
		String[] inputs = {
			"34a",
			"34ab",
			"((34))a",
			"((34))ab",
		};
		String[] dfa = {
			"s0-INT->s1\n" +
			"s1-'a'->s2\n" +
			"s2-EOF->:s3=>1\n",

			"s0-INT->s1\n" +
			"s1-'a'->s2\n" +
			"s2-EOF->:s3=>1\n" +
			"s2-'b'->:s4=>2\n",

			"s0-'('->s5\n" +
			"s0-INT->s1\n" +
			"s1-'a'->s2\n" +
			"s2-EOF->:s3=>1\n" +
			"s2-'b'->:s4=>2\n" +
			"s5-'('->s6\n" +
			"s6-INT->s7\n" +
			"s7-')'->s8\n" +
			"s8-')'->s1\n",

			"s0-'('->s5\n" +
			"s0-INT->s1\n" +
			"s1-'a'->s2\n" +
			"s2-EOF->:s3=>1\n" +
			"s2-'b'->:s4=>2\n" +
			"s5-'('->s6\n" +
			"s6-INT->s7\n" +
			"s7-')'->s8\n" +
			"s8-')'->s1\n",
		};
		checkDFAConstruction(lg, g, decision, inputs, dfa);
	}

	/** first check that the ATN predicts right alt.
	 *  Then check adaptive prediction.
	 */
	public void checkPredictedAlt(LexerGrammar lg, Grammar g, int decision,
								  String inputString, int expectedAlt)
	{
		Tool.internalOption_ShowATNConfigsInDFA = true;
		ATN lexatn = createATN(lg);
		LexerATNSimulator lexInterp = new LexerATNSimulator(lexatn);
		List<Integer> types = getTokenTypesViaATN(inputString, lexInterp);
		System.out.println(types);

		semanticProcess(lg);
		g.importVocab(lg);
		semanticProcess(g);

		ParserATNFactory f = new ParserATNFactory(g);
		ATN atn = f.createATN();

		DOTGenerator dot = new DOTGenerator(g);

		Rule r = g.getRule("a");
		if ( r!=null) System.out.println(dot.getDOT(atn.ruleToStartState[r.index]));
		r = g.getRule("b");
		if ( r!=null) System.out.println(dot.getDOT(atn.ruleToStartState[r.index]));
		r = g.getRule("e");
		if ( r!=null) System.out.println(dot.getDOT(atn.ruleToStartState[r.index]));
		r = g.getRule("ifstat");
		if ( r!=null) System.out.println(dot.getDOT(atn.ruleToStartState[r.index]));
		r = g.getRule("block");
		if ( r!=null) System.out.println(dot.getDOT(atn.ruleToStartState[r.index]));

		// Check ATN prediction
		ParserATNSimulator interp = new ParserATNSimulator(atn);
		TokenStream input = new IntTokenStream(types);
		ATNState startState = atn.decisionToState.get(decision);
		DFA dfa = new DFA(startState);
		int alt = interp.predictATN(dfa, input, ParserRuleContext.EMPTY, false);

		System.out.println(dot.getDOT(dfa, false));

		assertEquals(expectedAlt, alt);

		// Check adaptive prediction
		input.seek(0);
		alt = interp.adaptivePredict(input, decision, null);
		assertEquals(expectedAlt, alt);
		// run 2x; first time creates DFA in atn
		input.seek(0);
		alt = interp.adaptivePredict(input, decision, null);
		assertEquals(expectedAlt, alt);
	}

	public DFA getDFA(LexerGrammar lg, Grammar g, String ruleName,
					  String inputString, ParserRuleContext ctx)
	{
		Tool.internalOption_ShowATNConfigsInDFA = true;
		ATN lexatn = createATN(lg);
		LexerATNSimulator lexInterp = new LexerATNSimulator(lexatn);

		semanticProcess(lg);
		g.importVocab(lg);
		semanticProcess(g);

		ParserATNFactory f = new ParserATNFactory(g);
		ATN atn = f.createATN();

//		DOTGenerator dot = new DOTGenerator(g);
//		System.out.println(dot.getDOT(atn.ruleToStartState.get(g.getRule("a"))));
//		System.out.println(dot.getDOT(atn.ruleToStartState.get(g.getRule("b"))));
//		System.out.println(dot.getDOT(atn.ruleToStartState.get(g.getRule("e"))));

		ParserATNSimulator interp = new ParserATNSimulator(atn);
		List<Integer> types = getTokenTypesViaATN(inputString, lexInterp);
		System.out.println(types);
		TokenStream input = new IntTokenStream(types);
		try {
			ATNState startState = atn.decisionToState.get(0);
			DFA dfa = new DFA(startState);
//			Rule r = g.getRule(ruleName);
			//ATNState startState = atn.ruleToStartState.get(r);
			interp.predictATN(dfa, input, ctx, false);
		}
		catch (NoViableAltException nvae) {
			nvae.printStackTrace(System.err);
		}
		return null;
	}

	public void checkDFAConstruction(LexerGrammar lg, Grammar g, int decision,
									 String[] inputString, String[] dfaString)
	{
//		Tool.internalOption_ShowATNConfigsInDFA = true;
		ATN lexatn = createATN(lg);
		LexerATNSimulator lexInterp = new LexerATNSimulator(lexatn);

		semanticProcess(lg);
		g.importVocab(lg);
		semanticProcess(g);

		ParserATNFactory f = new ParserATNFactory(g);
		ATN atn = f.createATN();

		ParserATNSimulator interp = new ParserATNSimulator(atn);
		for (int i=0; i<inputString.length; i++) {
			// Check DFA
			List<Integer> types = getTokenTypesViaATN(inputString[i], lexInterp);
			System.out.println(types);
			TokenStream input = new IntTokenStream(types);
			try {
				interp.adaptivePredict(input, decision, ParserRuleContext.EMPTY);
			}
			catch (NoViableAltException nvae) {
				nvae.printStackTrace(System.err);
			}
			DFA dfa = interp.decisionToDFA[decision];
			assertEquals(dfaString[i], dfa.toString(g.getTokenDisplayNames()));
		}
	}
}
