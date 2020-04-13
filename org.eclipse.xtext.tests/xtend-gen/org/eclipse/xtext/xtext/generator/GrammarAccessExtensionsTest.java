/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.binder.AnnotatedBindingBuilder;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.XtextRuntimeModule;
import org.eclipse.xtext.common.TerminalsStandaloneSetup;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xtext.generator.grammarAccess.GrammarAccessExtensions;
import org.eclipse.xtext.xtext.generator.model.project.IXtextProjectConfig;
import org.eclipse.xtext.xtext.generator.model.project.StandardProjectConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@SuppressWarnings("all")
public class GrammarAccessExtensionsTest {
  @Extension
  private GrammarAccessExtensions _grammarAccessExtensions = new GrammarAccessExtensions();
  
  @Test
  public void testToJavaIdentifier() {
    Assert.assertEquals("FooBar", this._grammarAccessExtensions.toJavaIdentifier("foo Bar", true));
    Assert.assertEquals("Foo", this._grammarAccessExtensions.toJavaIdentifier("foo;", true));
    Assert.assertEquals("foo", this._grammarAccessExtensions.toJavaIdentifier("foo;", false));
    Assert.assertEquals("Colon", this._grammarAccessExtensions.toJavaIdentifier(":", true));
    Assert.assertEquals("Colon", this._grammarAccessExtensions.toJavaIdentifier(":", false));
    Assert.assertEquals("Semicolon", this._grammarAccessExtensions.toJavaIdentifier(";", false));
    Assert.assertEquals("CommercialAtApostrophe", this._grammarAccessExtensions.toJavaIdentifier("@\'", false));
    Assert.assertEquals("Grün", this._grammarAccessExtensions.toJavaIdentifier("Grün", true));
    Assert.assertEquals("DollarSign", this._grammarAccessExtensions.toJavaIdentifier("$", true));
    Assert.assertEquals("_", this._grammarAccessExtensions.toJavaIdentifier("_", true));
  }
  
  /**
   * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=298492
   */
  @Test
  public void testBug() throws Exception {
    Assert.assertEquals("HiraganaLetterRu", this._grammarAccessExtensions.toJavaIdentifier("\u308b", true));
  }
  
  @Test
  public void testSmoke() throws Exception {
    for (int i = 0; (i < 4000); i++) {
      {
        final String identifier = this._grammarAccessExtensions.toJavaIdentifier(String.valueOf(((char) i)), false);
        for (int j = 0; (j < identifier.length()); j++) {
          {
            final char charAt = identifier.charAt(j);
            Assert.assertTrue(((identifier + ":") + Character.valueOf(charAt)), this._grammarAccessExtensions.isValidJavaLatinIdentifier(charAt, (j == 0)));
          }
        }
      }
    }
  }
  
  @Test
  public void testGrammarFragmentToString() {
    try {
      TerminalsStandaloneSetup.doSetup();
      XtextRuntimeModule _xtextRuntimeModule = new XtextRuntimeModule();
      final Injector injector = Guice.createInjector(_xtextRuntimeModule, new com.google.inject.Module() {
        @Override
        public void configure(final Binder binder) {
          AnnotatedBindingBuilder<IXtextProjectConfig> _bind = binder.<IXtextProjectConfig>bind(IXtextProjectConfig.class);
          StandardProjectConfig _standardProjectConfig = new StandardProjectConfig();
          _bind.toInstance(_standardProjectConfig);
        }
      });
      final GrammarAccessExtensions grammarAccessExtension = injector.<GrammarAccessExtensions>getInstance(GrammarAccessExtensions.class);
      final ParseHelper parserHelper = injector.<ParseHelper>getInstance(ParseHelper.class);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("grammar org.xtext.example.mydsl.MyDsl");
      _builder.newLine();
      _builder.newLine();
      _builder.append("generate myDsl \"http://www.xtext.org/example/mydsl/MyDsl\"");
      _builder.newLine();
      _builder.newLine();
      _builder.append("OpOther:");
      _builder.newLine();
      _builder.append("\t  ");
      _builder.append("\'->\'");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("| \'..<\'");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("| \'>\' \'..\'");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("| \'..\'");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("| \'=>\'");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("| \'>\' (=>(\'>\' \'>\') | \'>\')");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("| \'<\' (=>(\'<\' \'<\') | \'<\' | \'=>\')");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("| \'<>\'");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("| \'?:\';");
      _builder.newLine();
      EObject _parse = parserHelper.parse(Strings.toUnixLineSeparator(_builder.toString()));
      Grammar grammar = ((Grammar) _parse);
      final AbstractRule rule = IterableExtensions.<AbstractRule>head(grammar.getRules());
      final String actual = grammarAccessExtension.grammarFragmentToString(rule, "//");
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("//OpOther:");
      _builder_1.newLine();
      _builder_1.append("//\t\'->\'");
      _builder_1.newLine();
      _builder_1.append("//\t| \'..<\'");
      _builder_1.newLine();
      _builder_1.append("//\t| \'>\' \'..\'");
      _builder_1.newLine();
      _builder_1.append("//\t| \'..\'");
      _builder_1.newLine();
      _builder_1.append("//\t| \'=>\'");
      _builder_1.newLine();
      _builder_1.append("//\t| \'>\' (=> (\'>\' \'>\') | \'>\') | \'<\' (=> (\'<\' \'<\') | \'<\' | \'=>\') | \'<>\'");
      _builder_1.newLine();
      _builder_1.append("//\t| \'?:\';");
      _builder_1.newLine();
      Assert.assertEquals(_builder_1.toString().trim(), actual);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
