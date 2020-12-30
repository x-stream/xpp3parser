package io.github.xstream.xpp3;

/*
 * Copyright The Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class Xpp3ParserTest
{
    @Test
    public void testHexadecimalEntities()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        parser.defineEntityReplacementText( "test", "replacement" );

        String input = "<root>&#x41;</root>";
        parser.setInput( new StringReader( input ) );

        assertEquals( XmlPullParser.START_TAG, parser.next() );
        assertEquals( XmlPullParser.TEXT, parser.next() );
        assertEquals( "A", parser.getText() );
        assertEquals( XmlPullParser.END_TAG, parser.next() );
    }

    @Test
    public void testDecimalEntities()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        parser.defineEntityReplacementText( "test", "replacement" );

        String input = "<root>&#65;</root>";
        parser.setInput( new StringReader( input ) );

        assertEquals( XmlPullParser.START_TAG, parser.next() );
        assertEquals( XmlPullParser.TEXT, parser.next() );
        assertEquals( "A", parser.getText() );
        assertEquals( XmlPullParser.END_TAG, parser.next() );
    }

    @Test
    public void testPredefinedEntities()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        parser.defineEntityReplacementText( "test", "replacement" );

        String input = "<root>&lt;&gt;&amp;&apos;&quot;</root>";
        parser.setInput( new StringReader( input ) );

        assertEquals( XmlPullParser.START_TAG, parser.next() );
        assertEquals( XmlPullParser.TEXT, parser.next() );
        assertEquals( "<>&'\"", parser.getText() );
        assertEquals( XmlPullParser.END_TAG, parser.next() );
    }

    @Test
    public void testCustomEntities()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        String input = "<root>&myentity;</root>";
        parser.setInput( new StringReader( input ) );
        parser.defineEntityReplacementText( "myentity", "replacement" );
        assertEquals( XmlPullParser.START_TAG, parser.next() );
        assertEquals( XmlPullParser.TEXT, parser.next() );
        assertEquals( "replacement", parser.getText() );
        assertEquals( XmlPullParser.END_TAG, parser.next() );

        parser = new Xpp3Parser();
        input = "<root>&myCustom;</root>";
        parser.setInput( new StringReader( input ) );
        parser.defineEntityReplacementText( "fo", "&#65;" );
        parser.defineEntityReplacementText( "myCustom", "&fo;" );
        assertEquals( XmlPullParser.START_TAG, parser.next() );
        assertEquals( XmlPullParser.TEXT, parser.next() );
        assertEquals( "&#65;", parser.getText() );
        assertEquals( XmlPullParser.END_TAG, parser.next() );
    }

    @Test
    public void testUnicodeEntities()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        String input = "<root>&#x1d7ed;</root>";
        parser.setInput( new StringReader( input ) );

        assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
        assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
        assertEquals( "\uD835\uDFED", parser.getText() );
        assertEquals( XmlPullParser.END_TAG, parser.nextToken() );

        parser = new Xpp3Parser();
        input = "<root>&#x159;</root>";
        parser.setInput( new StringReader( input ) );

        assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
        assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
        assertEquals( "\u0159", parser.getText() );
        assertEquals( XmlPullParser.END_TAG, parser.nextToken() );
    }

    @Test
    public void testInvalidCharacterReferenceHexa()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        String input = "<root>&#x110000;</root>";
        parser.setInput( new StringReader( input ) );

        try
        {
            assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            fail( "Should fail since &#x110000; is an illegal character reference" );
        }
        catch ( XmlPullParserException e )
        {
            assertTrue( e.getMessage().contains( "character reference (with hex value 110000) is invalid" ) );
        }
    }

    @Test
    public void testValidCharacterReferenceHexa()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        String input = "<root>&#x9;&#xA;&#xD;&#x20;&#x200;&#xD7FF;&#xE000;&#xFFA2;&#xFFFD;&#x10000;&#x10FFFD;&#x10FFFF;</root>";
        parser.setInput( new StringReader( input ) );

        try
        {
            assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 0x9, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 0xA, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 0xD, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 0x20, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 0x200, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 0xD7FF, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 0xE000, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 0xFFA2, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 0xFFFD, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 0x10000, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 0x10FFFD, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 0x10FFFF, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.END_TAG, parser.nextToken() );
        }
        catch ( XmlPullParserException e )
        {
            fail( "Should success since the input represents all legal character references" );
        }
    }

    @Test
    public void testInvalidCharacterReferenceDecimal()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        String input = "<root>&#1114112;</root>";
        parser.setInput( new StringReader( input ) );

        try
        {
            assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            fail( "Should fail since &#1114112; is an illegal character reference" );
        }
        catch ( XmlPullParserException e )
        {
            assertTrue( e.getMessage().contains( "character reference (with decimal value 1114112) is invalid" ) );
        }
    }

    @Test
    public void testValidCharacterReferenceDecimal()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        String input = "<root>&#9;&#10;&#13;&#32;&#512;&#55295;&#57344;&#65442;&#65533;&#65536;&#1114109;&#1114111;</root>";
        parser.setInput( new StringReader( input ) );

        try
        {
            assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 9, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 10, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 13, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 32, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 512, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 55295, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 57344, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 65442, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 65533, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 65536, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 1114109, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.ENTITY_REF, parser.nextToken() );
            assertEquals( 1114111, parser.getText().codePointAt( 0 ) );
            assertEquals( XmlPullParser.END_TAG, parser.nextToken() );
        }
        catch ( XmlPullParserException e )
        {
            fail( "Should success since the input represents all legal character references" );
        }
    }

    @Test
    public void testValidCDATA()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        String input = "<root><![CDATA[x]],if(c[a]>3&&a.v)]]></root>";
        parser.setInput( new StringReader( input ) );

        assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
        assertEquals( XmlPullParser.CDSECT, parser.nextToken() );
        assertEquals( "x]],if(c[a]>3&&a.v)", parser.getText() );
        assertEquals( XmlPullParser.END_TAG, parser.nextToken() );
    }

    @Test
    public void testProcessingInstruction()
        throws Exception
    {
        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><test>nnn</test>";

        Xpp3Parser parser = new Xpp3Parser();
        parser.setInput( new StringReader( input ) );

        assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.nextToken() );
        assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
        assertEquals( XmlPullParser.TEXT, parser.nextToken() );
        assertEquals( XmlPullParser.END_TAG, parser.nextToken() );
    }

    @Test
    public void testProcessingInstructionsContainingXml()
        throws Exception
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        sb.append( "<project>\n" );
        sb.append( " <?pi\n" );
        sb.append( "   <tag>\n" );
        sb.append( "   </tag>\n" );
        sb.append( " ?>\n" );
        sb.append( "</project>" );

        Xpp3Parser parser = new Xpp3Parser();
        parser.setInput( new StringReader( sb.toString() ) );

        assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.nextToken() );
        assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
        assertEquals( XmlPullParser.TEXT, parser.nextToken() ); // whitespace
        assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.nextToken() );
        assertEquals( XmlPullParser.TEXT, parser.nextToken() ); // whitespace
        assertEquals( XmlPullParser.END_TAG, parser.nextToken() );
    }

    @Test
    public void testMalformedProcessingInstructionsContainingXmlNoClosingQuestionMark()
        throws Exception
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
        sb.append( "<project />\n" );
        sb.append( "<?pi\n" );
        sb.append( "   <tag>\n" );
        sb.append( "   </tag>>\n" );

        Xpp3Parser parser = new Xpp3Parser();
        parser.setInput( new StringReader( sb.toString() ) );

        try
        {
            assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.nextToken() );
            assertEquals( XmlPullParser.IGNORABLE_WHITESPACE, parser.nextToken() );
            assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
            assertEquals( XmlPullParser.END_TAG, parser.nextToken() );
            assertEquals( XmlPullParser.IGNORABLE_WHITESPACE, parser.nextToken() );
            assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.nextToken() );

            fail( "Should fail since it has invalid PI" );
        }
        catch ( XmlPullParserException ex )
        {
            assertTrue( ex.getMessage().contains( "processing instruction started on line 3 and column 3 was not closed" ) );
        }
    }

    @Test
    public void testSubsequentProcessingInstructionShort()
        throws Exception
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        sb.append( "<project>" );
        sb.append( "<!-- comment -->" );
        sb.append( "<?m2e ignore?>" );
        sb.append( "</project>" );

        Xpp3Parser parser = new Xpp3Parser();
        parser.setInput( new StringReader( sb.toString() ) );

        assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.nextToken() );
        assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
        assertEquals( XmlPullParser.COMMENT, parser.nextToken() );
        assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.nextToken() );
        assertEquals( XmlPullParser.END_TAG, parser.nextToken() );
    }

    @Test
    public void testSubsequentProcessingInstructionMoreThan8k()
        throws Exception
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        sb.append( "<project>" );

        // add ten times 1000 chars as comment
        for ( int j = 0; j < 10; j++ )
        {

            sb.append( "<!-- " );
            for ( int i = 0; i < 2000; i++ )
            {
                sb.append( "ten bytes " );
            }
            sb.append( " -->" );
        }

        sb.append( "<?m2e ignore?>" );
        sb.append( "</project>" );

        Xpp3Parser parser = new Xpp3Parser();
        parser.setInput( new StringReader( sb.toString() ) );

        assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.nextToken() );
        assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
        assertEquals( XmlPullParser.COMMENT, parser.nextToken() );
        assertEquals( XmlPullParser.COMMENT, parser.nextToken() );
        assertEquals( XmlPullParser.COMMENT, parser.nextToken() );
        assertEquals( XmlPullParser.COMMENT, parser.nextToken() );
        assertEquals( XmlPullParser.COMMENT, parser.nextToken() );
        assertEquals( XmlPullParser.COMMENT, parser.nextToken() );
        assertEquals( XmlPullParser.COMMENT, parser.nextToken() );
        assertEquals( XmlPullParser.COMMENT, parser.nextToken() );
        assertEquals( XmlPullParser.COMMENT, parser.nextToken() );
        assertEquals( XmlPullParser.COMMENT, parser.nextToken() );
        assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.nextToken() );
        assertEquals( XmlPullParser.END_TAG, parser.nextToken() );
    }

    @Test
    public void testLargeText_NoOverflow()
        throws Exception
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        sb.append( "<largetextblock>" );
        // Anything above 33,554,431 would fail without a fix for
        // https://web.archive.org/web/20070831191548/http://www.extreme.indiana.edu/bugzilla/show_bug.cgi?id=228
        // with java.io.IOException: error reading input, returned 0
        sb.append( new String( new char[33554432] ) );
        sb.append( "</largetextblock>" );

        Xpp3Parser parser = new Xpp3Parser();
        parser.setInput( new StringReader( sb.toString() ) );

        assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.nextToken() );
        assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
        assertEquals( XmlPullParser.TEXT, parser.nextToken() );
        assertEquals( XmlPullParser.END_TAG, parser.nextToken() );
    }

    @Test
    public void testMalformedProcessingInstructionAfterTag()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        String input = "<project /><?>";

        parser.setInput( new StringReader( input ) );

        try
        {
            assertEquals( XmlPullParser.START_TAG, parser.next() );
            assertEquals( XmlPullParser.END_TAG, parser.next() );
            assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.next() );

            fail( "Should fail since it has an invalid Processing Instruction" );
        }
        catch ( XmlPullParserException ex )
        {
            assertTrue( ex.getMessage().contains( "processing instruction PITarget name not found" ) );
        }
    }

    @Test
    public void testMalformedProcessingInstructionBeforeTag()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        String input = "<?><project />";

        parser.setInput( new StringReader( input ) );

        try
        {
            assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.next() );
            assertEquals( XmlPullParser.START_TAG, parser.next() );
            assertEquals( XmlPullParser.END_TAG, parser.next() );

            fail( "Should fail since it has invalid PI" );
        }
        catch ( XmlPullParserException ex )
        {
            assertTrue( ex.getMessage().contains( "processing instruction PITarget name not found" ) );
        }
    }

    @Test
    public void testMalformedProcessingInstructionSpaceBeforeName()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        StringBuilder sb = new StringBuilder();
        sb.append( "<? shouldhavenospace>" );
        sb.append( "<project />" );

        parser.setInput( new StringReader( sb.toString() ) );

        try
        {
            assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.next() );
            assertEquals( XmlPullParser.START_TAG, parser.next() );
            assertEquals( XmlPullParser.END_TAG, parser.next() );

            fail( "Should fail since it has invalid PI" );
        }
        catch ( XmlPullParserException ex )
        {
            assertTrue( ex.getMessage().contains( "processing instruction PITarget must be exactly after <? and not white space character" ) );
        }
    }

    @Test
    public void testMalformedProcessingInstructionNoClosingQuestionMark()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        StringBuilder sb = new StringBuilder();
        sb.append( "<?shouldhavenospace>" );
        sb.append( "<project />" );

        parser.setInput( new StringReader( sb.toString() ) );

        try
        {
            assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.next() );
            assertEquals( XmlPullParser.START_TAG, parser.next() );
            assertEquals( XmlPullParser.END_TAG, parser.next() );

            fail( "Should fail since it has invalid PI" );
        }
        catch ( XmlPullParserException ex )
        {
            assertTrue( ex.getMessage().contains( "processing instruction started on line 1 and column 3 was not closed" ) );
        }
    }

    @Test
    public void testSubsequentMalformedProcessingInstructionNoClosingQuestionMark()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        StringBuilder sb = new StringBuilder();
        sb.append( "<project />" );
        sb.append( "<?shouldhavenospace>" );

        parser.setInput( new StringReader( sb.toString() ) );

        try
        {
            assertEquals( XmlPullParser.START_TAG, parser.next() );
            assertEquals( XmlPullParser.END_TAG, parser.next() );
            assertEquals( XmlPullParser.PROCESSING_INSTRUCTION, parser.next() );

            fail( "Should fail since it has invalid PI" );
        }
        catch ( XmlPullParserException ex )
        {
            assertTrue( ex.getMessage().contains( "processing instruction started on line 1 and column 14 was not closed" ) );
        }
    }

    @Test
    public void testMalformedXMLRootElement()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        String input = "<Y";
        parser.setInput( new StringReader( input ) );

        try
        {
            assertEquals( XmlPullParser.START_TAG, parser.nextToken() );

            fail( "Should throw EOFException" );
        }
        catch ( EOFException e )
        {
            assertTrue( e.getMessage().contains( "no more data available - expected the opening tag <Y...>" ) );
        }
    }

    @Test
    public void testMalformedXMLRootElement2()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        String input = "<hello";
        parser.setInput( new StringReader( input ) );

        try
        {
            assertEquals( XmlPullParser.START_TAG, parser.nextToken() );

            fail( "Should throw EOFException" );
        }
        catch ( EOFException e )
        {
            assertTrue( e.getMessage().contains( "no more data available - expected the opening tag <hello...>" ) );
        }
    }

    @Test
    public void testMalformedXMLRootElement3()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        String input = "<hello><how";
        parser.setInput( new StringReader( input ) );

        try
        {
            assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
            assertEquals( XmlPullParser.START_TAG, parser.nextToken() );

            fail( "Should throw EOFException" );
        }
        catch ( EOFException e )
        {
            assertTrue( e.getMessage().contains( "no more data available - expected the opening tag <how...>" ) );
        }
    }

    @Test
    public void testMalformedXMLRootElement4()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        String input = "<hello>some text<how";
        parser.setInput( new StringReader( input ) );

        try
        {
            assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
            assertEquals( XmlPullParser.TEXT, parser.nextToken() );
            assertEquals( "some text", parser.getText() );
            assertEquals( XmlPullParser.START_TAG, parser.nextToken() );

            fail( "Should throw EOFException" );
        }
        catch ( EOFException e )
        {
            assertTrue( e.getMessage().contains( "no more data available - expected the opening tag <how...>" ) );
        }
    }

    @Test
    public void testMalformedXMLRootElement5()
        throws Exception
    {
        Xpp3Parser parser = new Xpp3Parser();
        String input = "<hello>some text</hello";
        parser.setInput( new StringReader( input ) );

        try
        {
            assertEquals( XmlPullParser.START_TAG, parser.nextToken() );
            assertEquals( XmlPullParser.TEXT, parser.nextToken() );
            assertEquals( "some text", parser.getText() );
            assertEquals( XmlPullParser.END_TAG, parser.nextToken() );

            fail( "Should throw EOFException" );
        }
        catch ( EOFException e )
        {
            assertTrue( e.getMessage().contains( "no more data available - expected end tag </hello> to close start tag <hello>" ) );
        }
    }

}
