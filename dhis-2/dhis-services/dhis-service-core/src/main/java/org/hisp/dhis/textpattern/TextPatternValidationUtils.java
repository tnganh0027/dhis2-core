package org.hisp.dhis.textpattern;

/*
 * Copyright (c) 2004-2018, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.hisp.dhis.common.ValueType;

import java.util.regex.Pattern;

/**
 * @author Stian Sandvold
 */
public class TextPatternValidationUtils
{

    public static boolean validateSegmentValue( TextPatternSegment segment, String value )
    {
        return segment.getMethod().getType().validateText( segment.getParameter(), value );
    }

    public static boolean validateTextPatternValue( TextPattern textPattern, String value )
    {
        StringBuilder builder = new StringBuilder();

        builder.append( "^" );

        textPattern.getSegments().forEach(
            ( segment ) -> builder.append( segment.getMethod().getType().getValueRegex( segment.getParameter() ) ) );

        builder.append( "$" );

        return Pattern.compile( builder.toString() ).matcher( value ).matches();
    }

    public static int getTotalValuesPotential( TextPatternSegment generatedSegment )
    {

        if ( generatedSegment != null )
        {

            if ( generatedSegment.getMethod().equals( TextPatternMethod.SEQUENTIAL ) )
            {
                // Subtract by 1 since we don't use all zeroes.
                return ((int) Math.pow( 10, generatedSegment.getParameter().length() )) - 1;
            }
            else if ( generatedSegment.getMethod().equals( TextPatternMethod.RANDOM ) )
            {
                int res = 1;

                for ( char c : generatedSegment.getParameter().toCharArray() )
                {
                    switch ( c )
                    {
                    case '*':
                        res = res * 26;
                        break;
                    case '#':
                        res = res * 10;
                        break;
                    case 'X':
                        res = res * 26;
                        break;
                    case 'x':
                        res = res * 26;
                        break;
                    default:
                        break;
                    }
                }

                return res;
            }
        }

        return 1;
    }

    public static boolean validateValueType( TextPattern textPattern, ValueType valueType )
    {
        if ( valueType.equals( ValueType.TEXT ) )
        {
            return true;
        }
        else if ( valueType.equals( ValueType.NUMBER ) )
        {
            boolean isAllNumbers = true;

            for ( TextPatternSegment segment : textPattern.getSegments() )
            {
                isAllNumbers = isAllNumbers && isNumericOnly( segment );
            }

            return isAllNumbers;
        }
        else
        {
            return false;
        }
    }

    private static boolean isNumericOnly( TextPatternSegment segment )
    {
        if (segment.getMethod().equals( TextPatternMethod.SEQUENTIAL ))
        {
            return true;
        }

        if ( segment.getMethod().equals( TextPatternMethod.RANDOM ) )
        {
            return segment.getParameter().matches( "^#+$" );
        }

        if ( segment.getMethod().equals( TextPatternMethod.TEXT ))
        {
            return segment.getParameter().matches( "^[0-9]*$" );
        }

        return false;
    }
}