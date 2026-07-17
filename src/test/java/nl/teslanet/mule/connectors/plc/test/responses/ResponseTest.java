/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2026 (teslanet.nl) Rogier Cobben
 * 
 * Contributors:
 *     (teslanet.nl) Rogier Cobben - initial creation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

package nl.teslanet.mule.connectors.plc.test.responses;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.plc4x.java.api.exceptions.PlcIncompatibleDatatypeException;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.apache.plc4x.java.api.model.PlcSubscriptionTag;
import org.apache.plc4x.java.api.model.PlcTag;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.api.types.PlcSubscriptionType;
import org.apache.plc4x.java.api.types.PlcValueType;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.spi.messages.DefaultPlcReadRequest;
import org.apache.plc4x.java.spi.messages.DefaultPlcReadResponse;
import org.apache.plc4x.java.spi.messages.DefaultPlcSubscriptionRequest;
import org.apache.plc4x.java.spi.messages.DefaultPlcSubscriptionResponse;
import org.apache.plc4x.java.spi.messages.DefaultPlcWriteRequest;
import org.apache.plc4x.java.spi.messages.DefaultPlcWriteResponse;
import org.apache.plc4x.java.spi.messages.utils.DefaultPlcResponseItem;
import org.apache.plc4x.java.spi.messages.utils.DefaultPlcTagItem;
import org.apache.plc4x.java.spi.messages.utils.DefaultPlcTagValueItem;
import org.apache.plc4x.java.spi.messages.utils.PlcResponseItem;
import org.apache.plc4x.java.spi.messages.utils.PlcTagItem;
import org.apache.plc4x.java.spi.messages.utils.PlcTagValueItem;
import org.apache.plc4x.java.spi.model.DefaultPlcSubscriptionTag;
import org.apache.plc4x.java.spi.values.PlcBOOL;
import org.apache.plc4x.java.spi.values.PlcBYTE;
import org.apache.plc4x.java.spi.values.PlcCHAR;
import org.apache.plc4x.java.spi.values.PlcDATE;
import org.apache.plc4x.java.spi.values.PlcDATE_AND_LTIME;
import org.apache.plc4x.java.spi.values.PlcDATE_AND_TIME;
import org.apache.plc4x.java.spi.values.PlcDINT;
import org.apache.plc4x.java.spi.values.PlcDWORD;
import org.apache.plc4x.java.spi.values.PlcINT;
import org.apache.plc4x.java.spi.values.PlcLDATE;
import org.apache.plc4x.java.spi.values.PlcLDATE_AND_TIME;
import org.apache.plc4x.java.spi.values.PlcLINT;
import org.apache.plc4x.java.spi.values.PlcLREAL;
import org.apache.plc4x.java.spi.values.PlcLTIME;
import org.apache.plc4x.java.spi.values.PlcLTIME_OF_DAY;
import org.apache.plc4x.java.spi.values.PlcLWORD;
import org.apache.plc4x.java.spi.values.PlcList;
import org.apache.plc4x.java.spi.values.PlcNull;
import org.apache.plc4x.java.spi.values.PlcREAL;
import org.apache.plc4x.java.spi.values.PlcRawByteArray;
import org.apache.plc4x.java.spi.values.PlcSINT;
import org.apache.plc4x.java.spi.values.PlcSTRING;
import org.apache.plc4x.java.spi.values.PlcStruct;
import org.apache.plc4x.java.spi.values.PlcTIME;
import org.apache.plc4x.java.spi.values.PlcTIME_OF_DAY;
import org.apache.plc4x.java.spi.values.PlcUDINT;
import org.apache.plc4x.java.spi.values.PlcUINT;
import org.apache.plc4x.java.spi.values.PlcULINT;
import org.apache.plc4x.java.spi.values.PlcUSINT;
import org.apache.plc4x.java.spi.values.PlcWCHAR;
import org.apache.plc4x.java.spi.values.PlcWORD;
import org.apache.plc4x.java.spi.values.PlcWSTRING;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.teslanet.mule.connectors.plc.api.response.FieldResponse;
import nl.teslanet.mule.connectors.plc.api.response.FieldValueResponse;
import nl.teslanet.mule.connectors.plc.api.response.ResponseCode;
import nl.teslanet.mule.connectors.plc.api.response.ResponseValue;
import nl.teslanet.mule.connectors.plc.internal.utils.MessageUtils;


/**
 * test response types.
 */
class ResponseTest
{
    private static final Logger LOGGER= LoggerFactory.getLogger( ResponseTest.class );

    /**
     * Maximum DWORD value.
     */
    private final long XDWORD= PlcDWORD.MAX_VALUE;

    /**
     * Maximum LWORD value.
     */
    private final BigInteger XLWORD= PlcLWORD.MAX_VALUE;

    /**
     * Minimum INT value.
     */
    private final short MINT= PlcINT.MIN_VALUE;

    /**
     * Maximum INT value.
     */
    private final short XINT= PlcINT.MAX_VALUE;

    /**
     * Maximum UDINT value.
     */
    private final long XUDINT= PlcUDINT.MAX_VALUE;

    /**
     * Maximum UINT value.
     */
    private final int XUINT= PlcUINT.MAX_VALUE;

    /**
     * Maximum ULINT value.
     */
    private final BigInteger XULINT= PlcULINT.MAX_VALUE;

    /**
     * Maximum USINT value.
     */
    private final short XUSINT= PlcUSINT.MAX_VALUE;

    /**
     * Number of nanoseconds in one second.
     */
    private final BigInteger NANOS= BigInteger.valueOf( 1_000_000_000L );

    /**
     * Test data type.
     */
    private record TestData(
        PlcValueType type,
        Object source,
        /* binary */
        byte[] asBinary,
        /* boolean */
        Boolean asBoolean,
        /* byte */
        Byte asByte,
        /* short */
        Short asShort,
        /* integer */
        Integer asInteger,
        /* long */
        Long asLong,
        /* big integer */
        BigInteger asBigInteger,
        /* float */
        Float asFloat,
        /* double */
        Double asDouble,
        /* date */
        LocalDate asDate,
        /* dateTime */
        LocalDateTime asDateTime,
        /* time */
        LocalTime asTime,
        /* duration */
        Duration asDuration,
        /* string */
        String asString,
        /* list */
        Integer getListLength,
        /* list */
        List< String > getStructKeys
    )
    {
        /* binary */
        public boolean isBinary()
        {
            return null != asBinary();
        }

        /* boolean */
        public boolean isBoolean()
        {
            return null != asBoolean();
        }

        /* byte */
        public boolean isByte()
        {
            return null != asByte();
        }

        /* short */
        public boolean isShort()
        {
            return null != asShort();
        }

        /* integer */
        public boolean isInteger()
        {
            return null != asInteger();
        }

        /* long */
        public boolean isLong()
        {
            return null != asLong();
        }

        /* big integer */
        public boolean isBigInteger()
        {
            return null != asBigInteger();
        }

        /* float */
        public boolean isFloat()
        {
            return null != asFloat();
        }

        /* double */
        public boolean isDouble()
        {
            return null != asDouble();
        }

        /* date */
        public boolean isDate()
        {
            return null != asDate();
        }

        /* dateTime */
        public boolean isDateTime()
        {
            return null != asDateTime();
        }

        /* time */
        public boolean isTime()
        {
            return null != asTime();
        }

        /* duration */
        public boolean isDuration()
        {
            return null != asDuration();
        }

        /* string */
        public boolean isString()
        {
            return null != asString();
        }

        /* list */
        public boolean isList()
        {
            return null != getListLength();
        }

        /* list */
        public boolean isStruct()
        {
            return null != getStructKeys();
        }
    }

    /**
     * Test intermediate test record.
     */
    private record TestPair( TestData testValues, PlcValue plcValue )
    {
    }

    /**
     * Test record.
     */
    private record TestTriple( TestData testValues, PlcValue plcValue, ResponseValue value )
    {
    }

    /**
     * Test data containing test values and expected response content.
     */
    // @formatter:off
    private TestData testData[]=
    {
        /* RAW_BYTE_ARRAY */
        new TestData( PlcValueType.RAW_BYTE_ARRAY, new byte[] {}      , new byte[] {}              , null, null, null, null, null, null, null, null, null, null, null, null, ""    , null, null ),
        new TestData( PlcValueType.RAW_BYTE_ARRAY, new byte[] {0x3e, 0x4f}, new byte[] {0x3e, 0x4f}, null, null, null, null, null, null, null, null, null, null, null, null, "3e4f", null, null ),
        /* BOOL */
        new TestData( PlcValueType.BOOL, Boolean.TRUE , null, true , (byte) 1, (short) 1, 1, 1L, BigInteger.ONE , 1.0f, 1.0, null, null, null, null, "true", null, null ),
        new TestData( PlcValueType.BOOL, Boolean.FALSE, null, false, (byte) 0, (short) 0, 0, 0L, BigInteger.ZERO, 0.0f, 0.0, null, null, null, null, "false", null, null ),
        /* BYTE */                                                                                                                                                                                              
        new TestData( PlcValueType.BYTE, Short.valueOf( (short)    0 ), null, false, (byte)    0, (short)    0, 0, 0L, BigInteger.ZERO          , 0.0f, 0.0, null, null, null, null, "0", null, null ),
        new TestData( PlcValueType.BYTE, Short.valueOf( (short)    1 ), null, true , (byte)    1, (short)    1, 1, 1L, BigInteger.ONE           , 1.0f, 1.0, null, null, null, null, "1", null, null ),
        new TestData( PlcValueType.BYTE, Short.valueOf( (short)  128 ), null, true ,        null, (short)  128, 128, 128L, BigInteger.valueOf(128L) , 128.0f, 128.0, null, null, null, null, "128", null, null ),
        new TestData( PlcValueType.BYTE, Short.valueOf( (short) 0xFF ), null, true ,        null, (short) 0xFF, 0xFF, 0xFFL, BigInteger.valueOf(0xFFL), 255.0f, 255.0, null, null, null, null, "255", null, null ),
        /* CHAR */
        new TestData( PlcValueType.CHAR, Short.valueOf( (short)    0 ), null, false, (byte)    0, (short)    0, 0, 0L, BigInteger.ZERO          ,  0.0f, 0.0, null, null, null, null, "\u0000", null, null ),
        new TestData( PlcValueType.CHAR, Short.valueOf( (short)    1 ), null, true , (byte)    1, (short)    1, 1, 1L, BigInteger.ONE           ,  1.0f, 1.0, null, null, null, null, "\u0001", null, null ),
        new TestData( PlcValueType.CHAR, Short.valueOf( (short)   97 ), null, true , (byte)   97, (short)   97, 97, 97L, BigInteger.valueOf(97L)  ,  97.0f, 97.0, null, null, null, null, "a", null, null ),
        new TestData( PlcValueType.CHAR, Short.valueOf( (short) 0xFF ), null, true ,        null, (short) 0xFF, 0xFF, 0xFFL, BigInteger.valueOf(0xFFL),  255.0f, 255.0, null, null, null, null, "ÿ", null, null ),
        /* DATE */
        new TestData( PlcValueType.DATE, LocalDate.of( 0, 1, 1 )     , null, null, null, null, null, -62167219200L, BigInteger.valueOf(-62167219200L).multiply( NANOS ), null, null, LocalDate.of( 0, 1, 1 )     , LocalDateTime.of( 0, 1, 1, 0, 0 )     , null, null, "0000-01-01", null, null ),
        new TestData( PlcValueType.DATE, LocalDate.of( 1972, 3, 2 )  , null, null, null, null, null, 68342400L, BigInteger.valueOf(68342400L).multiply( NANOS )    , null, null, LocalDate.of( 1972, 3, 2 )  , LocalDateTime.of( 1972, 3, 2, 0, 0 )  , null, null, "1972-03-02", null, null ),
        new TestData( PlcValueType.DATE, LocalDate.of( 2026, 12, 30 ), null, null, null, null, null, 1798588800L, BigInteger.valueOf(1798588800L).multiply( NANOS )  , null, null, LocalDate.of( 2026, 12, 30 ), LocalDateTime.of( 2026, 12, 30, 0, 0 ), null, null, "2026-12-30", null, null ),
        new TestData( PlcValueType.DATE, LocalDate.of( 3000, 5, 25 ) , null, null, null, null, null, 32516121600L, BigInteger.valueOf(32516121600L).multiply( NANOS ) , null, null, LocalDate.of( 3000, 5, 25 ) , LocalDateTime.of( 3000, 5, 25, 0, 0 ) , null, null, "3000-05-25", null, null ),
        /* DATE_AND_LTIME */
        new TestData( PlcValueType.DATE_AND_LTIME, LocalDateTime.of( 0, 1, 1, 0, 0 )                  , null, null, null, null, null, -62167219200L, BigInteger.valueOf(-62167219200L).multiply( NANOS )                                , null, null, LocalDate.of( 0, 1, 1 )     , LocalDateTime.of( 0, 1, 1, 0, 0 )                 , LocalTime.of( 0, 0 )             , null, "0000-01-01T00:00"             , null, null ),
        new TestData( PlcValueType.DATE_AND_LTIME, LocalDateTime.of( 1972, 3, 2, 11, 30 )             , null, null, null, null, null, 68383800L, BigInteger.valueOf(68383800L).multiply( NANOS )                                    , null, null, LocalDate.of( 1972, 3, 2 )  , LocalDateTime.of( 1972, 3, 2, 11, 30 )            , LocalTime.of( 11, 30 )           , null, "1972-03-02T11:30"             , null, null ),
        new TestData( PlcValueType.DATE_AND_LTIME, LocalDateTime.of( 2026, 12, 30, 1, 2, 3, 4 )       , null, null, null, null, null, 1798592523L, BigInteger.valueOf(1798592523L).multiply( NANOS ).add( BigInteger.valueOf(4L) )    , null, null, LocalDate.of( 2026, 12, 30 ), LocalDateTime.of( 2026, 12, 30, 1, 2, 3, 4 )      , LocalTime.of( 1, 2, 3, 4 )       , null, "2026-12-30T01:02:03.000000004", null, null ),
        new TestData( PlcValueType.DATE_AND_LTIME, LocalDateTime.of( 3000, 5, 25, 23, 59, 59, 65523 ), null, null, null, null, null, 32516207999L, BigInteger.valueOf(32516207999L).multiply( NANOS ).add( BigInteger.valueOf(65523L) ), null, null, LocalDate.of( 3000, 5, 25 ) , LocalDateTime.of( 3000, 5, 25, 23, 59, 59, 65523 ), LocalTime.of( 23, 59, 59, 65523 ), null, "3000-05-25T23:59:59.000065523", null, null ),
        /* DATE_AND_TIME */
        new TestData( PlcValueType.DATE_AND_TIME, LocalDateTime.of( 0, 1, 1, 0, 0 )                 , null, null, null, null, null, -62167219200L, BigInteger.valueOf(-62167219200L).multiply( NANOS )                                 , null, null, LocalDate.of( 0, 1, 1 )     , LocalDateTime.of( 0, 1, 1, 0, 0 )                 , LocalTime.of( 0, 0 )             , null, "0000-01-01T00:00"             , null, null ),
        new TestData( PlcValueType.DATE_AND_TIME, LocalDateTime.of( 1972, 3, 2, 11, 30 )            , null, null, null, null, null, 68383800L, BigInteger.valueOf(68383800L).multiply( NANOS )                                     , null, null, LocalDate.of( 1972, 3, 2 )  , LocalDateTime.of( 1972, 3, 2, 11, 30 )            , LocalTime.of( 11, 30 )           , null, "1972-03-02T11:30"             , null, null ),
        new TestData( PlcValueType.DATE_AND_TIME, LocalDateTime.of( 2026, 12, 30, 1, 2, 3, 4 )      , null, null, null, null, null, 1798592523L, BigInteger.valueOf(1798592523L).multiply( NANOS ).add( BigInteger.valueOf(4L) )     , null, null, LocalDate.of( 2026, 12, 30 ), LocalDateTime.of( 2026, 12, 30, 1, 2, 3, 4 )      , LocalTime.of( 1, 2, 3, 4 )       , null, "2026-12-30T01:02:03.000000004", null, null ),
        new TestData( PlcValueType.DATE_AND_TIME, LocalDateTime.of( 3000, 5, 25, 23, 59, 59, 65523 ), null, null, null, null, null, 32516207999L, BigInteger.valueOf(32516207999L).multiply( NANOS ).add( BigInteger.valueOf(65523L) ), null, null, LocalDate.of( 3000, 5, 25 ) , LocalDateTime.of( 3000, 5, 25, 23, 59, 59, 65523 ), LocalTime.of( 23, 59, 59, 65523 ), null, "3000-05-25T23:59:59.000065523", null, null ),
        /* DINT */
        new TestData( PlcValueType.DINT, Integer.valueOf(      0 ), null, false, (byte)     0, (short)     0,      0,      0L, BigInteger.ZERO            ,     0.0f,     0.0, null, null, null, null,     "0", null, null ),
        new TestData( PlcValueType.DINT, Integer.valueOf(      1 ), null, true , (byte)     1, (short)     1,      1,      1L, BigInteger.ONE             ,     1.0f,     1.0, null, null, null, null,     "1", null, null ),
        new TestData( PlcValueType.DINT, Integer.valueOf(  32565 ), null, true ,         null, (short) 32565,  32565,  32565L, BigInteger.valueOf(32565L) , 32565.0f, 32565.0, null, null, null, null, "32565", null, null ),
        new TestData( PlcValueType.DINT, Integer.valueOf( 0xFFFF ), null, true ,         null,          null, 0xFFFF, 0xFFFFL, BigInteger.valueOf(0xFFFFL), 65535.0f, 65535.0, null, null, null, null, "65535", null, null ),
        /* DWORD */
        new TestData( PlcValueType.DWORD, Long.valueOf(      0 ), null, false, (byte)     0, (short)     0,  0, 0L, BigInteger.ZERO            , 0.0f, 0.0, null, null, null, null, "0", null, null ),
        new TestData( PlcValueType.DWORD, Long.valueOf(      1 ), null, true , (byte)     1, (short)     1,  1, 1L, BigInteger.ONE             , 1.0f, 1.0, null, null, null, null, "1", null, null ),
        new TestData( PlcValueType.DWORD, Long.valueOf(  32565 ), null, true ,         null, (short) 32565,  32565, 32565L, BigInteger.valueOf(32565L) , 32565.0f, 32565.0, null, null, null, null, "32565", null, null ),
        new TestData( PlcValueType.DWORD, Long.valueOf( 0xFFFF ), null, true ,         null,          null,  0xFFFF, 0xFFFFL, BigInteger.valueOf(0xFFFFL), 65535.0f, 65535.0, null, null, null, null, "65535", null, null ),
        new TestData( PlcValueType.DWORD, Long.valueOf( XDWORD ), null, true ,         null,          null, null, XDWORD, BigInteger.valueOf(XDWORD) , (float) XDWORD, (double) XDWORD, null, null, null, null, "4294967295", null, null ),
        /* INT */
        new TestData( PlcValueType.INT, Short.valueOf( (short)       0 ), null, false, (byte)  0, (short)       0, 0, 0L, BigInteger.ZERO            , 0.0f, 0.0, null, null, null, null, "0", null, null ),
        new TestData( PlcValueType.INT, Short.valueOf( (short)      -1 ), null, true , (byte) -1, (short)      -1, -1, -1L, BigInteger.ONE.negate()    , -1.0f, -1.0, null, null, null, null, "-1", null, null ),
        new TestData( PlcValueType.INT, Short.valueOf( (short)       1 ), null, true , (byte)  1, (short)       1, 1, 1L, BigInteger.ONE             , 1.0f, 1.0, null, null, null, null, "1", null, null ),
        new TestData( PlcValueType.INT, Short.valueOf( (short)   32565 ), null, true ,      null, (short)   32565, 32565, 32565L, BigInteger.valueOf(32565L) , 32565.0f, 32565.0, null, null, null, null, "32565", null, null ),
        new TestData( PlcValueType.INT, Short.valueOf( (short)  -31965 ), null, true ,      null, (short)  -31965, -31965, -31965L, BigInteger.valueOf(-31965L), -31965.0f, -31965.0, null, null, null, null, "-31965", null, null ),
        new TestData( PlcValueType.INT, Short.valueOf(            MINT ), null, true ,      null, MINT, (int) MINT, (long) MINT, BigInteger.valueOf(MINT)   , (float) MINT, (double) MINT, null, null, null, null, "-32768", null, null ),
        new TestData( PlcValueType.INT, Short.valueOf(            XINT ), null, true ,      null, XINT, (int) XINT, (long) XINT, BigInteger.valueOf(XINT)   , (float) XINT, (double) XINT, null, null, null, null, "32767", null, null ),
        /* LDATE */                                                                                                                                                                                                                   
        new TestData( PlcValueType.LDATE, LocalDate.of( 0, 1, 1 )     , null, null, null, null, null, -62167219200L, BigInteger.valueOf(-62167219200L).multiply( NANOS ), null, null, LocalDate.of( 0, 1, 1 )     , LocalDateTime.of( 0, 1, 1, 0, 0 )     , null, null, "0000-01-01", null, null ),
        new TestData( PlcValueType.LDATE, LocalDate.of( 1972, 3, 2 )  , null, null, null, null, null, 68342400L, BigInteger.valueOf(68342400L).multiply( NANOS )    , null, null, LocalDate.of( 1972, 3, 2 )  , LocalDateTime.of( 1972, 3, 2, 0, 0 )  , null, null, "1972-03-02", null, null ),
        new TestData( PlcValueType.LDATE, LocalDate.of( 2026, 12, 30 ), null, null, null, null, null, 1798588800L, BigInteger.valueOf(1798588800L).multiply( NANOS )  , null, null, LocalDate.of( 2026, 12, 30 ), LocalDateTime.of( 2026, 12, 30, 0, 0 ), null, null, "2026-12-30", null, null ),
        new TestData( PlcValueType.LDATE, LocalDate.of( 3000, 5, 25 ) , null, null, null, null, null, 32516121600L, BigInteger.valueOf(32516121600L).multiply( NANOS ) , null, null, LocalDate.of( 3000, 5, 25 ) , LocalDateTime.of( 3000, 5, 25, 0, 0 ) , null, null, "3000-05-25", null, null ),
        /* LDATE_AND_TIME */
        new TestData( PlcValueType.LDATE_AND_TIME, LocalDateTime.of( 0, 1, 1, 0, 0 )                 , null, null, null, null, null, -62167219200L, BigInteger.valueOf(-62167219200L).multiply( NANOS )                                 , null, null, LocalDate.of( 0, 1, 1 )     , LocalDateTime.of( 0, 1, 1, 0, 0 )                 , LocalTime.of( 0, 0 )             , null, "0000-01-01T00:00"             , null, null ),
        new TestData( PlcValueType.LDATE_AND_TIME, LocalDateTime.of( 1972, 3, 2, 11, 30 )            , null, null, null, null, null, 68383800L, BigInteger.valueOf(68383800L).multiply( NANOS )                                     , null, null, LocalDate.of( 1972, 3, 2 )  , LocalDateTime.of( 1972, 3, 2, 11, 30 )            , LocalTime.of( 11, 30 )           , null, "1972-03-02T11:30"             , null, null ),
        new TestData( PlcValueType.LDATE_AND_TIME, LocalDateTime.of( 2026, 12, 30, 1, 2, 3, 4 )      , null, null, null, null, null, 1798592523L, BigInteger.valueOf(1798592523L).multiply( NANOS ).add( BigInteger.valueOf(4L) )     , null, null, LocalDate.of( 2026, 12, 30 ), LocalDateTime.of( 2026, 12, 30, 1, 2, 3, 4 )      , LocalTime.of( 1, 2, 3, 4 )       , null, "2026-12-30T01:02:03.000000004", null, null ),
        new TestData( PlcValueType.LDATE_AND_TIME, LocalDateTime.of( 3000, 5, 25, 23, 59, 59, 65523 ), null, null, null, null, null, 32516207999L, BigInteger.valueOf(32516207999L).multiply( NANOS ).add( BigInteger.valueOf(65523L) ), null, null, LocalDate.of( 3000, 5, 25 ) , LocalDateTime.of( 3000, 5, 25, 23, 59, 59, 65523 ), LocalTime.of( 23, 59, 59, 65523 ), null, "3000-05-25T23:59:59.000065523", null, null ),
        /* LINT */
        new TestData( PlcValueType.LINT, Long.valueOf(      0 ), null, false, (byte)     0, (short)     0,     0,             0L, BigInteger.ZERO                   , 0.0f, 0.0, null, null, null, null, "0", null, null ),
        new TestData( PlcValueType.LINT, Long.valueOf(      1 ), null, true , (byte)     1, (short)     1,     1,             1L, BigInteger.ONE                    , 1.0f, 1.0, null, null, null, null, "1", null, null ),
        new TestData( PlcValueType.LINT, Long.valueOf(     -1 ), null, true , (byte)    -1, (short)    -1,    -1,            -1L, BigInteger.ONE.negate()           , -1.0f, -1.0, null, null, null, null, "-1", null, null ),
        new TestData( PlcValueType.LINT, Long.valueOf(  32565 ), null, true ,         null, (short) 32565, 32565,         32565L, BigInteger.valueOf(32565L)        , 32565.0f, 32565.0, null, null, null, null, "32565", null, null ),
        new TestData( PlcValueType.LINT, Long.MIN_VALUE        , null, true ,         null,          null,  null, Long.MIN_VALUE, BigInteger.valueOf(Long.MIN_VALUE), (float) Long.MIN_VALUE, (double) Long.MIN_VALUE, null, null, null, null, "-9223372036854775808", null, null ),
        new TestData( PlcValueType.LINT, Long.MAX_VALUE        , null, true ,         null,          null,  null, Long.MAX_VALUE, BigInteger.valueOf(Long.MAX_VALUE), (float) Long.MAX_VALUE, (double) Long.MAX_VALUE, null, null, null, null, "9223372036854775807", null, null ),
        /* LREAL */
        new TestData( PlcValueType.LREAL, Double.valueOf(       0 ), null, null, null, null, null, null, null, null,              0.0, null, null, null, null,                    "0.0", null, null ),
        new TestData( PlcValueType.LREAL, Double.valueOf(       1 ), null, null, null, null, null, null, null, null,              1.0, null, null, null, null,                    "1.0", null, null ),
        new TestData( PlcValueType.LREAL, Double.valueOf(      -1 ), null, null, null, null, null, null, null, null,             -1.0, null, null, null, null,                   "-1.0", null, null ),
        new TestData( PlcValueType.LREAL, Double.valueOf( 65.3654 ), null, null, null, null, null, null, null, null,          65.3654, null, null, null, null,                "65.3654", null, null ),
        new TestData( PlcValueType.LREAL, Double.MIN_VALUE         , null, null, null, null, null, null, null, null, Double.MIN_VALUE, null, null, null, null,               "4.9E-324", null, null ),
        new TestData( PlcValueType.LREAL, Double.MAX_VALUE         , null, null, null, null, null, null, null, null, Double.MAX_VALUE, null, null, null, null, "1.7976931348623157E308", null, null ),
        /* LTIME */
        new TestData( PlcValueType.LTIME, Duration.ZERO           , null, null, null, null, null,        0L, BigInteger.ZERO                                , null, null, null, null, null, Duration.ofSeconds(  0 ),           "PT0S", null, null ),
        new TestData( PlcValueType.LTIME, Duration.ofDays( 365 )  , null, null, null, null, null, 31536000L, BigInteger.valueOf(31536000L).multiply( NANOS ), null, null, null, null, null, Duration.ofHours( 8760 ),        "PT8760H", null, null ),
        new TestData( PlcValueType.LTIME, Duration.ofSeconds( 13 ), null, null, null, null, null,       13L, BigInteger.valueOf(13L).multiply( NANOS )      , null, null, null, null, null, Duration.ofSeconds( 13 ),          "PT13S", null, null ),
        new TestData( PlcValueType.LTIME, Duration.ofNanos( 3023 ), null, null, null, null, null,        0L, BigInteger.valueOf(3023L)                      , null, null, null, null, null, Duration.ofNanos( 3023 ), "PT0.000003023S", null, null ),
        /* LTIME_OF_DAY */
        new TestData( PlcValueType.LTIME_OF_DAY, LocalTime.of( 0, 0 )                   , null, null, null, null,     0,     0L, BigInteger.ZERO                                                                     , null, null, null, null, LocalTime.of( 0, 0 )                 , null,              "00:00", null, null ),
        new TestData( PlcValueType.LTIME_OF_DAY, LocalTime.of( 3, 55 )                  , null, null, null, null, 14100, 14100L, BigInteger.valueOf(14100).multiply( NANOS )                                         , null, null, null, null, LocalTime.of( 3, 55 )                , null,              "03:55", null, null ),
        new TestData( PlcValueType.LTIME_OF_DAY, LocalTime.of( 23, 59, 59, 999_999_999 ), null, null, null, null, 86399, 86399L, BigInteger.valueOf(86399L).multiply( NANOS ).add( BigInteger.valueOf(999_999_999L) ), null, null, null, null, LocalTime.of( 23, 59, 59, 999999999 ), null, "23:59:59.999999999", null, null ),
        new TestData( PlcValueType.LTIME_OF_DAY, LocalTime.of( 0, 0, 0, 32 )            , null, null, null, null,     0,     0L, BigInteger.valueOf(32L)                                                             , null, null, null, null, LocalTime.of( 0, 0, 0, 32 )          , null, "00:00:00.000000032", null, null ),
        /* LWORD */
        new TestData( PlcValueType.LWORD, BigInteger.valueOf(      0 ), null, false, (byte)     0, (short)     0,     0,       0L, BigInteger.ZERO            , null, null, null, null, null, null,                    "0", null, null ),
        new TestData( PlcValueType.LWORD, BigInteger.valueOf(      1 ), null, true , (byte)     1, (short)     1,     1,       1L, BigInteger.ONE             , null, null, null, null, null, null,                    "1", null, null ),
        new TestData( PlcValueType.LWORD, BigInteger.valueOf(  32565 ), null, true ,         null, (short) 32565, 32565,   32565L, BigInteger.valueOf(32565L) , null, null, null, null, null, null,                "32565", null, null ),
        new TestData( PlcValueType.LWORD, BigInteger.valueOf( 0xFFFF ), null, true ,         null,          null, 0xFFFF, 0xFFFFL, BigInteger.valueOf(0xFFFFL), null, null, null, null, null, null,                "65535", null, null ),
        new TestData( PlcValueType.LWORD, XLWORD                      , null, true ,         null,          null,   null,    null, XLWORD                     , null, null, null, null, null, null, "18446744073709551615", null, null ),
        /* NULL */
        new TestData( PlcValueType.NULL, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "empty", null, null ),
        /* REAL */
        new TestData( PlcValueType.REAL, Float.valueOf(        0 ), null, null, null, null, null, null, null,            0.0f,                      0.0, null, null, null, null,          "0.0", null, null ),
        new TestData( PlcValueType.REAL, Float.valueOf(        1 ), null, null, null, null, null, null, null,            1.0f,                      1.0, null, null, null, null,          "1.0", null, null ),
        new TestData( PlcValueType.REAL, Float.valueOf(       -1 ), null, null, null, null, null, null, null,           -1.0f,                     -1.0, null, null, null, null,         "-1.0", null, null ),
        new TestData( PlcValueType.REAL, Float.valueOf( 65.3654f ), null, null, null, null, null, null, null,        65.3654f, (double)        65.3654f, null, null, null, null,      "65.3654", null, null ),
        new TestData( PlcValueType.REAL, Float.MIN_VALUE          , null, null, null, null, null, null, null, Float.MIN_VALUE, (double) Float.MIN_VALUE, null, null, null, null,      "1.4E-45", null, null ),
        new TestData( PlcValueType.REAL, Float.MAX_VALUE          , null, null, null, null, null, null, null, Float.MAX_VALUE, (double) Float.MAX_VALUE, null, null, null, null, "3.4028235E38", null, null ),
        /* SINT */                                                                                                                                                                                              
        new TestData( PlcValueType.SINT, Byte.valueOf( (byte)   0 ), null, false, (byte)    0, (short)    0,    0,    0L, BigInteger.ZERO          ,   0.0f,     0.0, null, null, null, null,    "0", null, null ),
        new TestData( PlcValueType.SINT, Byte.valueOf( (byte)   1 ), null, true , (byte)    1, (short)    1,    1,    1L, BigInteger.ONE           ,   1.0f,     1.0, null, null, null, null,    "1", null, null ),
        new TestData( PlcValueType.SINT, Byte.valueOf( (byte)  -1 ), null, true , (byte)   -1, (short)   -1,   -1,   -1L, BigInteger.ONE.negate()  ,   -1.0f,   -1.0, null, null, null, null,   "-1", null, null ),
        new TestData( PlcValueType.SINT, Byte.valueOf( (byte) 113 ), null, true , (byte)  113, (short)  113,  113,  113L, BigInteger.valueOf(113L) ,  113.0f,  113.0, null, null, null, null,  "113", null, null ),
        new TestData( PlcValueType.SINT, Byte.MIN_VALUE            , null, true , (byte) -128, (short) -128, -128, -128L, BigInteger.valueOf(-128L), -128.0f, -128.0, null, null, null, null, "-128", null, null ),
        new TestData( PlcValueType.SINT, Byte.MAX_VALUE            , null, true , (byte) 0x7F, (short) 0x7F, 0x7F, 0x7FL, BigInteger.valueOf(0x7FL), 127.0f,   127.0, null, null, null, null,  "127", null, null ),
        /* STRING */                                                                                                                                                                                              
        new TestData( PlcValueType.STRING,           "", null, false,       null,         null, null, null, null                    , null,  null, null, null, null, null,           "", null, null ),
        new TestData( PlcValueType.STRING,       "true", null, true ,       null,         null, null, null, null                    , null,  null, null, null, null, null,       "true", null, null ),
        new TestData( PlcValueType.STRING,        "123", null, false, (byte) 123, (short)  123,  123, 123L, BigInteger.valueOf(123L), 123f, 123.0, null, null, null, null,        "123", null, null ),
        new TestData( PlcValueType.STRING, "Some text.", null, false,       null,         null, null, null, null                    , null,  null, null, null, null, null, "Some text.", null, null ),
        /* TIME */
        new TestData( PlcValueType.TIME, Duration.ZERO           , null, null, null, null, null,        0L, BigInteger.ZERO                                , null, null, null, null, null, Duration.ofSeconds(  0 ),           "PT0S", null, null ),
        new TestData( PlcValueType.TIME, Duration.ofDays( 365 )  , null, null, null, null, null, 31536000L, BigInteger.valueOf(31536000L).multiply( NANOS ), null, null, null, null, null, Duration.ofHours( 8760 ),        "PT8760H", null, null ),
        new TestData( PlcValueType.TIME, Duration.ofSeconds( 13 ), null, null, null, null, null,       13L, BigInteger.valueOf(13L).multiply( NANOS )      , null, null, null, null, null, Duration.ofSeconds( 13 ),          "PT13S", null, null ),
        new TestData( PlcValueType.TIME, Duration.ofNanos( 3023 ), null, null, null, null, null,        0L, BigInteger.valueOf(3023L)                      , null, null, null, null, null, Duration.ofNanos( 3023 ), "PT0.000003023S", null, null ),
        /* TIME_OF_DAY */
        new TestData( PlcValueType.TIME_OF_DAY, LocalTime.of( 0, 0 )                   , null, null, null, null, 0, 0L, BigInteger.ZERO                                                                             , null, null, null, null, LocalTime.of( 0, 0 )                 , null,              "00:00", null, null ),
        new TestData( PlcValueType.TIME_OF_DAY, LocalTime.of( 3, 55 )                  , null, null, null, null, 14100, 14100L, BigInteger.valueOf(14100).multiply( NANOS )                                         , null, null, null, null, LocalTime.of( 3, 55 )                , null,              "03:55", null, null ),
        new TestData( PlcValueType.TIME_OF_DAY, LocalTime.of( 23, 59, 59, 999_999_999 ), null, null, null, null, 86399, 86399L, BigInteger.valueOf(86399L).multiply( NANOS ).add( BigInteger.valueOf(999_999_999L) ), null, null, null, null, LocalTime.of( 23, 59, 59, 999999999 ), null, "23:59:59.999999999", null, null ),
        new TestData( PlcValueType.TIME_OF_DAY, LocalTime.of( 0, 0, 0, 32 )            , null, null, null, null, 0, 0L, BigInteger.valueOf(32L)                                                                     , null, null, null, null, LocalTime.of( 0, 0, 0, 32 )          , null, "00:00:00.000000032", null, null ),
        /* UDINT */
        new TestData( PlcValueType.UDINT, Long.valueOf(      0 ), null, false, (byte) 0, (short)     0,     0,     0L, BigInteger.ZERO           ,           0.0f,             0.0, null, null, null, null,          "0", null, null ),
        new TestData( PlcValueType.UDINT, Long.valueOf(      1 ), null, true , (byte) 1, (short)     1,     1,     1L, BigInteger.ONE            ,           1.0f,             1.0, null, null, null, null,          "1", null, null ),
        new TestData( PlcValueType.UDINT, Long.valueOf(  32565 ), null, true ,     null, (short) 32565, 32565, 32565L, BigInteger.valueOf(32565L),       32565.0f,         32565.0, null, null, null, null,      "32565", null, null ),
        new TestData( PlcValueType.UDINT, XUDINT                , null, true ,     null,          null,  null, XUDINT, BigInteger.valueOf(XUDINT), (float) XUDINT, (double) XUDINT, null, null, null, null, "4294967295", null, null ),
        /* UINT */
        new TestData( PlcValueType.UINT, Integer.valueOf(      0 ), null, false, (byte) 0, (short)     0,     0,           0L, BigInteger.ZERO           ,         0.0f,           0.0, null, null, null, null,     "0", null, null ),
        new TestData( PlcValueType.UINT, Integer.valueOf(      1 ), null, true , (byte) 1, (short)     1,     1,           1L, BigInteger.ONE            ,         1.0f,           1.0, null, null, null, null,     "1", null, null ),
        new TestData( PlcValueType.UINT, Integer.valueOf(  32565 ), null, true ,     null, (short) 32565, 32565,       32565L, BigInteger.valueOf(32565L),     32565.0f,       32565.0, null, null, null, null, "32565", null, null ),
        new TestData( PlcValueType.UINT, XUINT                    , null, true ,     null,          null, XUINT, (long) XUINT, BigInteger.valueOf(XUINT) , (float)XUINT, (double)XUINT, null, null, null, null, "65535", null, null ),
        /* ULINT */
        new TestData( PlcValueType.ULINT, BigInteger.valueOf(      0 ), null, false, (byte) 0, (short)     0,      0,      0L, BigInteger.ZERO             , null, null, null, null, null, null,                    "0", null, null ),
        new TestData( PlcValueType.ULINT, BigInteger.valueOf(      1 ), null, true , (byte) 1, (short)     1,      1,      1L, BigInteger.ONE              , null, null, null, null, null, null,                    "1", null, null ),
        new TestData( PlcValueType.ULINT, BigInteger.valueOf(  32565 ), null, true ,     null, (short) 32565,  32565,  32565L, BigInteger.valueOf(32565L)  , null, null, null, null, null, null,                "32565", null, null ),
        new TestData( PlcValueType.ULINT, BigInteger.valueOf( 0xFFFF ), null, true ,     null,          null, 0xFFFF, 0xFFFFL, BigInteger.valueOf(0xFFFFL ), null, null, null, null, null, null,                "65535", null, null ),
        new TestData( PlcValueType.ULINT, XULINT                      , null, true ,     null,          null,   null,    null, XULINT                      , null, null, null, null, null, null, "18446744073709551615", null, null ),
        /* USINT */
        new TestData( PlcValueType.USINT, Short.valueOf( (short)   0 ), null, false, (byte) 0, (short)   0,            0,            0L, BigInteger.ZERO          ,            0.0f,             0.0, null, null, null, null,   "0", null, null ),
        new TestData( PlcValueType.USINT, Short.valueOf( (short)   1 ), null, true , (byte) 1, (short)   1,            1,            1L, BigInteger.ONE           ,            1.0f,             1.0, null, null, null, null,   "1", null, null ),
        new TestData( PlcValueType.USINT, Short.valueOf( (short) 213 ), null, true ,     null, (short) 213,          213,          213L, BigInteger.valueOf(213L) ,          213.0f,           213.0, null, null, null, null, "213", null, null ),
        new TestData( PlcValueType.USINT, Short.valueOf(      XUSINT ), null, true ,     null,      XUSINT, (int) XUSINT, (long) XUSINT, BigInteger.valueOf(XUSINT), (float) XUSINT, (double) XUSINT, null, null, null, null, "255", null, null ),
        /* DINT */
        new TestData( PlcValueType.DINT, Integer.valueOf(      0 ), null, false, (byte) 0, (short)     0,      0,      0L, BigInteger.ZERO            ,     0.0f,     0.0, null, null, null, null,     "0", null, null ),
        new TestData( PlcValueType.DINT, Integer.valueOf(      1 ), null, true , (byte) 1, (short)     1,      1,      1L, BigInteger.ONE             ,     1.0f,     1.0, null, null, null, null,     "1", null, null ),
        new TestData( PlcValueType.DINT, Integer.valueOf(  32565 ), null, true ,     null, (short) 32565,  32565,  32565L, BigInteger.valueOf(32565L) , 32565.0f, 32565.0, null, null, null, null, "32565", null, null ),
        new TestData( PlcValueType.DINT, Integer.valueOf( 0xFFFF ), null, true ,     null,          null, 0xFFFF, 0xFFFFL, BigInteger.valueOf(0xFFFFL), 65535.0f, 65535.0, null, null, null, null, "65535", null, null ),
        /* WCHAR */
        new TestData( PlcValueType.WCHAR, Integer.valueOf(      0 ), null, false, (byte)  0, (short)     0,      0,      0L, BigInteger.ZERO            ,     0.0f,     0.0, null, null, null, null, "\u0000", null, null ),
        new TestData( PlcValueType.WCHAR, Integer.valueOf(      1 ), null, true , (byte)  1, (short)     1,      1,      1L, BigInteger.ONE             ,     1.0f,     1.0, null, null, null, null, "\u0001", null, null ),
        new TestData( PlcValueType.WCHAR, Integer.valueOf(     97 ), null, true , (byte) 97, (short)    97,     97,     97L, BigInteger.valueOf(97L)    ,    97.0f,    97.0, null, null, null, null,      "a", null, null ),
        new TestData( PlcValueType.WCHAR, Integer.valueOf(  32565 ), null, true ,      null, (short) 32565,  32565,  32565L, BigInteger.valueOf(32565L) , 32565.0f, 32565.0, null, null, null, null, "\u7f35", null, null ),
        new TestData( PlcValueType.WCHAR, Integer.valueOf( 0xFFFF ), null, true ,      null,          null, 0xFFFF, 0xFFFFL, BigInteger.valueOf(0xFFFFL), 65535.0f, 65535.0, null, null, null, null, "\uffff", null, null ),
        /* WSTRING */                                                                                                                                                                                              
        new TestData( PlcValueType.WSTRING,           "", null, false,       null,        null, null, null, null                    , null,  null, null, null, null, null,           "", null, null ),
        new TestData( PlcValueType.WSTRING,       "true", null, true ,       null,        null, null, null, null                    , null,  null, null, null, null, null,       "true", null, null ),
        new TestData( PlcValueType.WSTRING,        "123", null, false, (byte) 123, (short) 123,  123, 123L, BigInteger.valueOf(123L), 123f, 123.0, null, null, null, null,        "123", null, null ),
        new TestData( PlcValueType.WSTRING, "Some text.", null, false,       null,        null, null, null, null                    , null,  null, null, null, null, null, "Some text.", null, null ),
        /* List */
        new TestData( PlcValueType.List, new TestData[]{
            new TestData( PlcValueType.INT, Short.valueOf( (short)       1 ), null, true , (byte) 1, (short) 1, 1, 1L, BigInteger.ONE, 1.0f, 1.0, null, null, null, null, "1", null, null ),
            new TestData( PlcValueType.INT, Short.valueOf( (short)       2 ), null, true , (byte) 2, (short) 2, 2, 2L, BigInteger.TWO, 2.0f, 2.0, null, null, null, null, "2", null, null ),
        }, null, null, null, null, null, null, null, null, null, null, null, null, null, "[ResponseValue[value=ShortValue[value=1]], ResponseValue[value=ShortValue[value=2]]]", 2, null ),
        /* Struct */
        new TestData( PlcValueType.Struct, Map.of(
            "one",   new TestData( PlcValueType.INT, Short.valueOf( (short)       1 ), null, true , (byte) 1, (short) 1, 1, 1L, BigInteger.ONE, 1.0f, 1.0, null, null, null, null, "1", null, null ),
            "two",   new TestData( PlcValueType.INT, Short.valueOf( (short)       2 ), null, true , (byte) 2, (short) 2, 2, 2L, BigInteger.TWO, 2.0f, 2.0, null, null, null, null, "2", null, null )
        ), null, null, null, null, null, null, null, null, null, null, null, null, null, "{one=ResponseValue[value=ShortValue[value=1]], two=ResponseValue[value=ShortValue[value=2]]}", null, List.of( "one", "two") ),
    };
    // @formatter:on

    /**
     * Test all response values;
     * @return The test stream.
     */
    @TestFactory
    Stream< DynamicNode > ResponseValueTest()
    {
        return Arrays.asList( testData ).stream().map( data -> generateTestNode( data ) );
    }

    /**
     * Create node from test data.;
     * @return The node.
     */
    private DynamicNode generateTestNode( TestData data )
    {
        TestTriple input= responseValue( plcValues( data ) );
        return dynamicContainer(
            "value " + input.testValues.type.name() + " = " + input.testValues.source,
            Stream
                .of(
                    /* value not null */
                    dynamicTest( "not null", () -> assertNotNull( input.value ) ),
                    /* read */
                    readResponseTest( input ),
                    /* write */
                    writeResponseTest( input ),
                    /* subscribe */
                    subscribeResponseTest( input ),
                    /* types */
                    binaryResponseTest( input ),
                    booleanResponseTest( input ),
                    byteResponseTest( input ),
                    shortResponseTest( input ),
                    integerResponseTest( input ),
                    dateResponseTest( input ),
                    dateTimeResponseTest( input ),
                    timeResponseTest( input ),
                    durationResponseTest( input ),
                    longResponseTest( input ),
                    bigIntegerResponseTest( input ),
                    floatResponseTest( input ),
                    doubleResponseTest( input ),
                    stringResponseTest( input ),
                    listResponseTest( input ),
                    structResponseTest( input ),
                    /* plc */
                    binaryResponsePlcTest( input ),
                    booleanResponsePlcTest( input ),
                    byteResponsePlcTest( input ),
                    shortResponsePlcTest( input ),
                    integerResponsePlcTest( input ),
                    dateResponsePlcTest( input ),
                    dateTimeResponsePlcTest( input ),
                    timeResponsePlcTest( input ),
                    durationResponsePlcTest( input ),
                    longResponsePlcTest( input ),
                    bigIntegerResponsePlcTest( input ),
                    floatResponsePlcTest( input ),
                    doubleResponsePlcTest( input ),
                    stringResponsePlcTest( input ),
                    listResponsePlcTest( input ),
                    structResponsePlcTest( input ),
                    /* contents */
                    listContentResponseTest( input ),
                    structContentResponseTest( input )
                )
        );
    }

    /**
     * Enrich testdata with plc value 
     * @param input The test data to use.
     * @return The test pair of testdata and values 
     */
    private TestPair plcValues( TestData input )
    {
        return new TestPair( input, plcValue( input ) );
    }

    /**
     * Create plc value. 
     * @param input The test data to use.
     * @return The plc value. 
     */
    @SuppressWarnings( "unchecked" )
    private PlcValue plcValue( TestData input )
    {
        PlcValue value= null;
        switch ( input.type )
        {
            case BOOL:
                value= PlcBOOL.of( input.source );
                break;
            case BYTE:
                value= PlcBYTE.of( input.source );
                break;
            case CHAR:
                value= PlcCHAR.of( input.source );
                break;
            case DATE:
                value= PlcDATE.of( input.source );
                break;
            case DATE_AND_LTIME:
                value= PlcDATE_AND_LTIME.of( input.source );
                break;
            case DATE_AND_TIME:
                value= PlcDATE_AND_TIME.of( input.source );
                break;
            case DINT:
                value= PlcDINT.of( input.source );
                break;
            case DWORD:
                value= PlcDWORD.of( input.source );
                break;
            case INT:
                value= PlcINT.of( input.source );
                break;
            case LDATE:
                value= PlcLDATE.of( input.source );
                break;
            case LDATE_AND_TIME:
                value= PlcLDATE_AND_TIME.of( input.source );
                break;
            case LINT:
                value= PlcLINT.of( input.source );
                break;
            case LREAL:
                value= PlcLREAL.of( input.source );
                break;
            case LTIME:
                value= PlcLTIME.of( input.source );
                break;
            case LTIME_OF_DAY:
                value= PlcLTIME_OF_DAY.of( input.source );
                break;
            case LWORD:
                value= PlcLWORD.of( input.source );
                break;
            case List:
                value= new PlcList( Arrays.stream( (TestData[]) input.source ).map( data -> plcValue( data ) ).collect( Collectors.toList() ) );
                break;
            case NULL:
            default:
                value= new PlcNull();
                break;
            case RAW_BYTE_ARRAY:
                value= PlcRawByteArray.of( input.source );
                break;
            case REAL:
                value= PlcREAL.of( input.source );
                break;
            case SINT:
                value= PlcSINT.of( input.source );
                break;
            case STRING:
                value= PlcSTRING.of( input.source );
                break;
            case Struct:
                value= new PlcStruct(
                    ( (Map< String, TestData >) input.source ).entrySet().stream().collect( Collectors.toMap( entry -> entry.getKey(), entry -> plcValue( entry.getValue() ) ) )
                );
                break;
            case TIME:
                value= PlcTIME.of( input.source );
                break;
            case TIME_OF_DAY:
                value= PlcTIME_OF_DAY.of( input.source );
                break;
            case UDINT:
                value= PlcUDINT.of( input.source );
                break;
            case UINT:
                value= PlcUINT.of( input.source );
                break;
            case ULINT:
                value= PlcULINT.of( input.source );
                break;
            case USINT:
                value= PlcUSINT.of( input.source );
                break;
            case WCHAR:
                value= PlcWCHAR.of( input.source );
                break;
            case WORD:
                value= PlcWORD.of( input.source );
                break;
            case WSTRING:
                value= PlcWSTRING.of( input.source );
                break;
        }
        return value;
    }

    /**
     * Enrich testdata with response value 
     * @param input The test pair to use.
     * @return The test pair of testdata and values 
     */
    private TestTriple responseValue( TestPair input )
    {
        return new TestTriple( input.testValues, input.plcValue, MessageUtils.createResponseValue( input.plcValue ) );
    }

    /**
     * Create the read response test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicTest readResponseTest( TestTriple input )
    {
        return dynamicTest( "read", () -> {
            String alias= "alias" + String.valueOf( input.testValues.source );
            LinkedHashMap< String, PlcTagItem< PlcTag > > tags= new LinkedHashMap<>();
            tags.put( alias, new DefaultPlcTagItem< PlcTag >( new TestTag( alias, input.plcValue.getPlcValueType() ) ) );
            HashMap< String, PlcResponseItem< PlcValue > > values= new HashMap< String, PlcResponseItem< PlcValue > >();
            values.put( alias, new DefaultPlcResponseItem< PlcValue >( PlcResponseCode.OK, input.plcValue ) );
            DefaultPlcReadResponse response= new DefaultPlcReadResponse( new DefaultPlcReadRequest( null, tags ), values );
            List< FieldValueResponse > result= MessageUtils.createFieldResponses( response );
            assertEquals( alias, result.get( 0 ).getAlias(), "wrong alias" );
            assertEquals( input.plcValue.getPlcValueType().name(), result.get( 0 ).getFieldType(), "wrong type" );
            assertEquals( ResponseCode.OK, result.get( 0 ).getResult(), "wrong result" );
            assertInstanceOf( ResponseValue.class, result.get( 0 ).getValue(), "wrong value class" );
        } );
    }

    /**
     * Create the write response test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicTest writeResponseTest( TestTriple input )
    {
        return dynamicTest( "write", () -> {
            String alias= "alias" + String.valueOf( input.testValues.source );
            LinkedHashMap< String, PlcTagValueItem< PlcTag > > tags= new LinkedHashMap<>();
            tags.put( alias, new DefaultPlcTagValueItem< PlcTag >( new TestTag( alias, input.plcValue.getPlcValueType() ), input.plcValue ) );
            Map< String, PlcResponseCode > values= new HashMap< String, PlcResponseCode >();
            values.put( alias, PlcResponseCode.OK );
            DefaultPlcWriteResponse response= new DefaultPlcWriteResponse( new DefaultPlcWriteRequest( null, tags ), values );
            List< FieldResponse > result= MessageUtils.createFieldResponses( response );
            assertEquals( alias, result.get( 0 ).getAlias(), "wrong alias" );
            assertEquals( ResponseCode.OK, result.get( 0 ).getResult(), "wrong result" );
        } );
    }

    /**
     * Create the write response test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicTest subscribeResponseTest( TestTriple input )
    {
        return dynamicTest( "subscribe", () -> {
            String alias= "alias" + String.valueOf( input.testValues.source );
            LinkedHashMap< String, PlcTagItem< PlcSubscriptionTag > > tags= new LinkedHashMap<>();
            tags
                .put(
                    alias,
                    new DefaultPlcTagItem< PlcSubscriptionTag >(
                        new DefaultPlcSubscriptionTag( PlcSubscriptionType.CHANGE_OF_STATE, new TestTag( alias, PlcValueType.BOOL ), Duration.ZERO )
                    )
                );
            Map< String, PlcResponseItem< PlcSubscriptionHandle > > values= new HashMap< String, PlcResponseItem< PlcSubscriptionHandle > >();
            values.put( alias, new DefaultPlcResponseItem< PlcSubscriptionHandle >( PlcResponseCode.OK, new TestSubscriptionHandle() ) );
            DefaultPlcSubscriptionResponse response= new DefaultPlcSubscriptionResponse( new DefaultPlcSubscriptionRequest( null, tags, null, null ), values );
            List< FieldResponse > result= MessageUtils.createFieldResponses( response );
            assertEquals( alias, result.get( 0 ).getAlias(), "wrong alias" );
            assertEquals( ResponseCode.OK, result.get( 0 ).getResult(), "wrong result" );
        } );
    }

    /**
     * Create the boolean response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer booleanResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "boolean",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () -> assertEquals( input.testValues.isBoolean(), input.value.isBoolean() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isBoolean() )
                        {
                            assertEquals( input.testValues.asBoolean, input.value.getAsBoolean() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsBoolean() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the boolean plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer booleanResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc boolean",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isBoolean() != input.plcValue.isBoolean() )
                        {
                            LOGGER
                                .warn(
                                    "{} is Boolean differ, expected: {} plc: {}",
                                    input.plcValue.getClass().getName(),
                                    input.testValues.isBoolean(),
                                    input.plcValue.isBoolean()
                                );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        Boolean plcValue;
                        if ( input.testValues.isBoolean() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getBoolean();
                                if ( !Objects.equals( input.testValues.asBoolean, plcValue ) )
                                {
                                    LOGGER.warn( "{} get Boolean value differs, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.asBoolean, plcValue );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} get Boolean throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getBoolean();
                                LOGGER.warn( "{} get Boolean value unexpected: {}", input.plcValue.getClass().getName(), plcValue );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} get Boolean throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the short response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer shortResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "short",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () ->
                    {
                        assertEquals( input.testValues.isShort(), input.value.isShort() );
                    } ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isShort() )
                        {
                            assertEquals( input.testValues.asShort, input.value.getAsShort() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsShort() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the short plc response value test. 
     * @param input The test data to use.
     * @return The test.
    */
    private DynamicContainer shortResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc short",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isShort() != input.plcValue.isShort() )
                        {
                            LOGGER.warn( "{} is Short differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isShort(), input.plcValue.isShort() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        Short plcValue;
                        if ( input.testValues.isShort() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getShort();
                                if ( !Objects.equals( input.testValues.asShort, plcValue ) )
                                {
                                    LOGGER.warn( "{} get Short value differs, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.asShort, plcValue );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} get Short throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getShort();
                                LOGGER.warn( "{} get Short value unexpected: {}", input.plcValue.getClass().getName(), plcValue );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} get Short throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the byte response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer byteResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "byte",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () ->
                    {
                        assertEquals( input.testValues.isByte(), input.value.isByte() );
                    } ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isByte() )
                        {
                            assertEquals( input.testValues.asByte(), input.value.getAsByte() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsByte() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the byte plc response value test. 
     * @param input The test data to use.
     * @return The test.
    */
    private DynamicContainer byteResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc byte",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isByte() != input.plcValue.isByte() )
                        {
                            LOGGER.warn( "{} is Byte differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isShort(), input.plcValue.isShort() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        byte plcValue;
                        if ( input.testValues.isByte() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getByte();
                                if ( !Objects.equals( input.testValues.asByte(), plcValue ) )
                                {
                                    LOGGER.warn( "{} get Byte value differs, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.asByte(), plcValue );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} get Byte throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getByte();
                                LOGGER.warn( "{} get Byte value unexpected: {}", input.plcValue.getClass().getName(), plcValue );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} get Byte throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the binary response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer binaryResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "binary",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () ->
                    {
                        assertEquals( input.testValues.isBinary(), input.value.isBinary() );
                    } ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isBinary() )
                        {
                            assertArrayEquals( input.testValues.asBinary, IOUtils.toByteArray( input.value.getAsBinary() ) );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsBinary() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the binary plc response value test. 
     * @param input The test data to use.
     * @return The test.
    */
    private DynamicContainer binaryResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc binary",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( ( input.testValues.isBinary() == true ) != ( input.plcValue.getPlcValueType() == PlcValueType.RAW_BYTE_ARRAY ) )
                        {
                            LOGGER.warn( "{} is Binary differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isShort(), input.plcValue.isShort() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        byte[] plcValue;
                        if ( input.testValues.isBinary() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getRaw();
                                if ( !Arrays.equals( input.testValues.asBinary, plcValue ) )
                                {
                                    LOGGER.warn( "{} get Binary value differs, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.asShort, plcValue );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} get Binary throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getRaw();
                                LOGGER.warn( "{} get Binary value unexpected: {}", input.plcValue.getClass().getName(), plcValue );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} get Binary throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the integer response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer integerResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "integer",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () -> assertEquals( input.testValues.isInteger(), input.value.isInteger() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isInteger() )
                        {
                            assertEquals( input.testValues.asInteger, input.value.getAsInteger() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsInteger() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the integer plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer integerResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc integer",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isInteger() != input.plcValue.isInteger() )
                        {
                            LOGGER
                                .warn(
                                    "{} is Integer differ, expected: {} plc: {}",
                                    input.plcValue.getClass().getName(),
                                    input.testValues.isInteger(),
                                    input.plcValue.isInteger()
                                );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        Integer plcValue;
                        if ( input.testValues.isInteger() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getInteger();
                                if ( !Objects.equals( input.testValues.asInteger, plcValue ) )
                                {
                                    LOGGER.warn( "{} get Integer value differs, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.asInteger, plcValue );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} get Integer throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getInteger();
                                LOGGER.warn( "{} get Integer value unexpected: {}", input.plcValue.getClass().getName(), plcValue );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} get Integer throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the long response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer longResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "long",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () -> assertEquals( input.testValues.isLong(), input.value.isLong() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isLong() )
                        {
                            assertEquals( input.testValues.asLong, input.value.getAsLong() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsLong() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the long plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer longResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc long",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isLong() != input.plcValue.isLong() )
                        {
                            LOGGER.warn( "{} is Long differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isLong(), input.plcValue.isLong() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        Long plcValue;
                        if ( input.testValues.isLong() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getLong();
                                if ( !Objects.equals( input.testValues.asLong, plcValue ) )
                                {
                                    LOGGER.warn( "{} get Long value differs, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.asLong, plcValue );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} get Long throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getLong();
                                LOGGER.warn( "{} get Long value unexpected: {}", input.plcValue.getClass().getName(), plcValue );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} get Long throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the long response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer bigIntegerResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "biginteger",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () -> assertEquals( input.testValues.isBigInteger(), input.value.isBigInteger() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isBigInteger() )
                        {
                            assertEquals( input.testValues.asBigInteger, input.value.getAsBigInteger() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsBigInteger() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the long plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer bigIntegerResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc biginteger",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isBigInteger() != input.plcValue.isBigInteger() )
                        {
                            LOGGER
                                .warn(
                                    "{} is BigInteger differ, expected: {} plc: {}",
                                    input.plcValue.getClass().getName(),
                                    input.testValues.isBigInteger(),
                                    input.plcValue.isBigInteger()
                                );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        BigInteger plcValue;
                        if ( input.testValues.isBigInteger() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getBigInteger();
                                if ( !Objects.equals( input.testValues.asBigInteger, plcValue ) )
                                {
                                    LOGGER
                                        .warn(
                                            "{} get BigInteger value differs, expected: {} plc: {}",
                                            input.plcValue.getClass().getName(),
                                            input.testValues.asBigInteger,
                                            plcValue
                                        );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} get BigInteger throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getBigInteger();
                                LOGGER.warn( "{} get BigInteger value unexpected: {}", input.plcValue.getClass().getName(), plcValue );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} get BigInteger throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the float response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer floatResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "float",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () -> assertEquals( input.testValues.isFloat(), input.value.isFloat() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isFloat() )
                        {
                            assertEquals( input.testValues.asFloat, input.value.getAsFloat() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsFloat() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the float plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer floatResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc float",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isFloat() != input.plcValue.isFloat() )
                        {
                            LOGGER.warn( "{} is Float differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isFloat(), input.plcValue.isFloat() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        float plcValue;
                        if ( input.testValues.isFloat() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getFloat();
                                if ( !Objects.equals( input.testValues.asFloat, plcValue ) )
                                {
                                    LOGGER.warn( "{} get Float value differs, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.asFloat, plcValue );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} get Float throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getFloat();
                                LOGGER.warn( "{} get Float value unexpected: {}", input.plcValue.getClass().getName(), plcValue );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} get Float throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the double response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer doubleResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "double",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () -> assertEquals( input.testValues.isDouble(), input.value.isDouble() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isDouble() )
                        {
                            assertEquals( input.testValues.asDouble, input.value.getAsDouble() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsDouble() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the double plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer doubleResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc double",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isDouble() != input.plcValue.isDouble() )
                        {
                            LOGGER.warn( "{} is Double differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isDouble(), input.plcValue.isDouble() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        Double plcValue;
                        if ( input.testValues.isDouble() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getDouble();
                                if ( !Objects.equals( input.testValues.asDouble, plcValue ) )
                                {
                                    LOGGER.warn( "{} get Double value differs, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.asDouble, plcValue );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} get Double throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getDouble();
                                LOGGER.warn( "{} get Double value unexpected: {}", input.plcValue.getClass().getName(), plcValue );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} get Double throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the local date response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer dateResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "localdate",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () -> assertEquals( input.testValues.isDate(), input.value.isDate() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isDate() )
                        {
                            assertEquals( input.testValues.asDate, input.value.getAsDate() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsDate() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the local date plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer dateResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc localdate",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isDate() != input.plcValue.isDate() )
                        {
                            LOGGER.warn( "{} is Date differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isDate(), input.plcValue.isDate() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        LocalDate plcValue;
                        if ( input.testValues.isDate() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getDate();
                                if ( !Objects.equals( input.testValues.asDate, plcValue ) )
                                {
                                    LOGGER.warn( "{} get Date value differs, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.asDate, plcValue );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} get Date throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getDate();
                                LOGGER.warn( "{} get Date value unexpected: {}", input.plcValue.getClass().getName(), plcValue );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} get Date throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the local dateTime response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer dateTimeResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "localdatetime",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () -> assertEquals( input.testValues.isDateTime(), input.value.isDateTime() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isDateTime() )
                        {
                            assertEquals( input.testValues.asDateTime, input.value.getAsDateTime() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsDateTime() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the local dateTime plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer dateTimeResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc localdatetime",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isDateTime() != input.plcValue.isDateTime() )
                        {
                            LOGGER
                                .warn(
                                    "{} is DateTime differ, expected: {} plc: {}",
                                    input.plcValue.getClass().getName(),
                                    input.testValues.isDateTime(),
                                    input.plcValue.isDateTime()
                                );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        LocalDateTime plcValue;
                        if ( input.testValues.isDateTime() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getDateTime();
                                if ( !Objects.equals( input.testValues.asDateTime, plcValue ) )
                                {
                                    LOGGER
                                        .warn( "{} get DateTime value differs, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.asDateTime, plcValue );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} get DateTime throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getDateTime();
                                LOGGER.warn( "{} get DateTime value unexpected: {}", input.plcValue.getClass().getName(), plcValue );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} get DateTime throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the local time response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer timeResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "localtime",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () -> assertEquals( input.testValues.isTime(), input.value.isTime() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isTime() )
                        {
                            assertEquals( input.testValues.asTime, input.value.getAsTime() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsTime() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the local time plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer timeResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc localtime",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isTime() != input.plcValue.isTime() )
                        {
                            LOGGER.warn( "{} is Time differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isTime(), input.plcValue.isTime() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        LocalTime plcValue;
                        if ( input.testValues.isTime() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getTime();
                                if ( !Objects.equals( input.testValues.asTime, plcValue ) )
                                {
                                    LOGGER.warn( "{} get Time value differs, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.asTime, plcValue );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} get Time throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getTime();
                                LOGGER.warn( "{} get Time value unexpected: {}", input.plcValue.getClass().getName(), plcValue );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} get Time throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the duration response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer durationResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "duration",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () -> assertEquals( input.testValues.isDuration(), input.value.isDuration() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isDuration() )
                        {
                            assertEquals( input.testValues.asDuration, input.value.getAsDuration() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsDuration() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the duration plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer durationResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc duration",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isDuration() != input.plcValue.isDuration() )
                        {
                            LOGGER
                                .warn(
                                    "{} is Duration differ, expected: {} plc: {}",
                                    input.plcValue.getClass().getName(),
                                    input.testValues.isDuration(),
                                    input.plcValue.isDuration()
                                );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        Duration plcValue;
                        if ( input.testValues.isDuration() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getDuration();
                                if ( !Objects.equals( input.testValues.asDuration, plcValue ) )
                                {
                                    LOGGER
                                        .warn( "{} get Duration value differs, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.asDuration, plcValue );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} get Duration throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getDuration();
                                LOGGER.warn( "{} get Duration value unexpected: {}", input.plcValue.getClass().getName(), plcValue );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} get Duration throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the string response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer stringResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "string",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () -> assertEquals( input.testValues.isString(), input.value.isString() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isString() )
                        {
                            assertEquals( input.testValues.asString, input.value.getAsString() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsString() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the string plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer stringResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc string",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isString() != input.plcValue.isString() )
                        {
                            LOGGER.warn( "{} is String differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isString(), input.plcValue.isString() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        String plcValue;
                        if ( input.testValues.isString() )
                        {
                            try
                            {
                                plcValue= input.plcValue.toString();
                                if ( !Objects.equals( input.testValues.asString, plcValue ) )
                                {
                                    LOGGER.warn( "{} to String value differs, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.asString, plcValue );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} to String throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.toString();
                                LOGGER.warn( "{} to String value unexpected: {}", input.plcValue.getClass().getName(), plcValue );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} to String throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the list response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer listResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "list",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () -> assertEquals( input.testValues.isList(), input.value.isList() ) ),
                    dynamicTest( "length", () ->
                    {
                        if ( input.testValues.isList() )
                        {
                            assertEquals( input.testValues.getListLength, input.value.getAsList().size() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsList() );
                        } ;
                    } ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isList() )
                        {
                            assertTrue( input.value.getAsList() instanceof List< ResponseValue > );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsList() );
                        } ;
                    } )
                )
        );
    }

    /**
     * Create the list plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer listResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc list",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isList() != input.plcValue.isList() )
                        {
                            LOGGER.warn( "{} is List differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isList(), input.plcValue.isList() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        int length;
                        if ( input.testValues.isList() )
                        {
                            try
                            {
                                length= input.plcValue.getLength();
                                if ( !Objects.equals( input.testValues.getListLength, length ) )
                                {
                                    LOGGER.warn( "{} list length differs, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.getListLength, length );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} list length throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                length= input.plcValue.getLength();
                                LOGGER.warn( "{} list length unexpected: {}", input.plcValue.getClass().getName(), length );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} list length throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        List< ? extends PlcValue > plcValue;
                        if ( input.testValues.isList() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getList();
                                if ( !( plcValue instanceof List< ? extends PlcValue > ) )
                                {
                                    LOGGER
                                        .warn( "{} plc list class differs, expected: List< ? extends PlcValue plc: {}", input.plcValue.getClass().getName(), plcValue.getClass() );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} plc list throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getList();
                                LOGGER.warn( "{} plc list value unexpected: {}", input.plcValue.getClass().getName(), plcValue.toString() );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} plc list throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } )
                )
        );
    }

    /**
     * Create the list plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer listContentResponseTest( TestTriple input )
    {
        TestData[] listData= {};
        if ( input.testValues.type == PlcValueType.List )
        {
            listData= (TestData[]) input.testValues().source();
        }
        return dynamicContainer( "list-content", Arrays.asList( listData ).stream().map( data -> generateTestNode( data ) ) );
    }

    /**
     * Create the struct response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer structResponseTest( TestTriple input )
    {
        return dynamicContainer(
            "struct",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is", () -> assertEquals( input.testValues.isStruct(), input.value.isStruct() ) ),
                    dynamicTest( "length", () ->
                    {
                        if ( input.testValues.isStruct() )
                        {
                            assertEquals( input.testValues.getStructKeys().size(), input.value.getAsStruct().size() );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsStruct() );
                        } ;
                    } ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isStruct() )
                        {
                            assertTrue( input.value.getAsStruct() instanceof Map< String, ResponseValue > );
                        }
                        else
                        {
                            assertThrows( UnsupportedOperationException.class, () -> input.value.getAsStruct() );
                        } ;
                    } ),
                    dynamicTest( "keys", () ->
                    {
                        if ( input.testValues.isStruct() )
                        {
                            assertIterableEquals( input.value.getAsStruct().keySet(), input.testValues.getStructKeys );
                        }
                    } )
                )
        );
    }

    /**
     * Create the Struct plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer structResponsePlcTest( TestTriple input )
    {
        return dynamicContainer(
            "plc struct",
            Stream
                .of(
                    /* tests */
                    dynamicTest( "is plc ", () ->
                    {
                        if ( input.testValues.isStruct() != input.plcValue.isStruct() )
                        {
                            LOGGER.warn( "{} is Struct differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isStruct(), input.plcValue.isString() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        int length;
                        if ( input.testValues.isStruct() )
                        {
                            try
                            {
                                length= input.plcValue.getLength();
                                if ( !Objects.equals( input.testValues.getStructKeys().size(), length ) )
                                {
                                    LOGGER
                                        .warn(
                                            "{} Struct length differs, expected: {} plc: {}",
                                            input.plcValue.getClass().getName(),
                                            input.testValues.getStructKeys().size(),
                                            length
                                        );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} Struct length throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                length= input.plcValue.getLength();
                                LOGGER.warn( "{} Struct length unexpected: {}", input.plcValue.getClass().getName(), length );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} Struct length throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        Map< String, ? extends PlcValue > plcValue;
                        if ( input.testValues.isStruct() )
                        {
                            try
                            {
                                plcValue= input.plcValue.getStruct();
                                if ( !( plcValue instanceof Map< String, ? extends PlcValue > ) )
                                {
                                    LOGGER
                                        .warn( "{} plc struct class differs, expected: Map< ? extends PlcValue plc: {}", input.plcValue.getClass().getName(), plcValue.getClass() );
                                }
                            }
                            catch ( Exception e )
                            {
                                LOGGER.warn( "{} plc struct throws unexpected: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                            }

                        }
                        else
                        {
                            try
                            {
                                plcValue= input.plcValue.getStruct();
                                LOGGER.warn( "{} plc struct value unexpected: {}", input.plcValue.getClass().getName(), plcValue.toString() );
                            }
                            catch ( Exception e )
                            {
                                if ( !( e instanceof PlcIncompatibleDatatypeException ) )
                                {
                                    LOGGER.warn( "{} plc struct throws wrong: {}", input.plcValue.getClass().getName(), e.getClass().getName() );
                                }
                            }

                        }
                    } ),
                    dynamicTest( "keys plc", () ->
                    {
                        if ( input.testValues.isStruct() )
                        {
                            assertIterableEquals( input.plcValue.getKeys(), input.testValues.getStructKeys );
                        }
                    } )

                )
        );
    }

    /**
     * Create the struct plc response value test. 
     * @param input The test data to use.
     * @return The test.
     */
    private DynamicContainer structContentResponseTest( TestTriple input )
    {
        if ( input.testValues.isStruct() )
        {
            @SuppressWarnings( "unchecked" )
            Map< String, TestData > structData= (Map< String, TestData >) input.testValues().source();
            return dynamicContainer(
                "struct content",
                structData.entrySet().stream().map( entry -> generateTestNode( entry.getValue() ) )

            );
        }
        else
        {
            return dynamicContainer( "struct content", Stream.empty() );
        }
    }

}
