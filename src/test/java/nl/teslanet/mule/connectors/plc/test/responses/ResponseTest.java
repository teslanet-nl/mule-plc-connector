/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2026 (teslanet.nl) Rogier Cobben
 * 
 * Contributors:
 *     (teslanet.nl) Rogier Cobben - initial creation
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
public class ResponseTest
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
        boolean isBinary,
        byte[] asBinary,
        /* boolean */
        boolean isBoolean,
        Boolean asBoolean,
        /* byte */
        boolean isByte,
        Short asByte,
        /* short */
        boolean isShort,
        Short asShort,
        /* integer */
        boolean isInteger,
        Integer asInteger,
        /* long */
        boolean isLong,
        Long asLong,
        /* big integer */
        boolean isBigInteger,
        BigInteger asBigInteger,
        /* float */
        boolean isFloat,
        Float asFloat,
        /* double */
        boolean isDouble,
        Double asDouble,
        /* date */
        boolean isDate,
        LocalDate asDate,
        /* dateTime */
        boolean isDateTime,
        LocalDateTime asDateTime,
        /* duration */
        boolean isDuration,
        Duration asDuration,
        /* string */
        boolean isString,
        String asString,
        /* list */
        boolean isList,
        Integer getListLength,
        /* list */
        boolean isStruct,
        List< String > getStructKeys
    )
    {
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
        new TestData( PlcValueType.RAW_BYTE_ARRAY, new byte[] {}      , true, new byte[] {}      , false, false, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, true, "", false, null, false, null ),
        new TestData( PlcValueType.RAW_BYTE_ARRAY, new byte[] {0x3e, 0x4f}, true, new byte[] {0x3e, 0x4f}, false, false, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, true, "3e4f", false, null, false, null ),
        /* BOOL */
        new TestData( PlcValueType.BOOL, Boolean.TRUE , false, null, true, true , true, (short) 1, true, (short) 1, true, 1, true, 1L, true, BigInteger.ONE , true, 1.0f, true, 1.0, false, null, false, null, false, null, true, "true", false, null, false, null ),
        new TestData( PlcValueType.BOOL, Boolean.FALSE, false, null, true, false, true, (short) 0, true, (short) 0, true, 0, true, 0L, true, BigInteger.ZERO, true, 0.0f, true, 0.0, false, null, false, null, false, null, true, "false", false, null, false, null ),
        /* BYTE */                                                                                                                                                                                              
        new TestData( PlcValueType.BYTE, Short.valueOf( (short)    0 ), false, null, true, false, true, (short)    0, true, (short)    0, true,    0, true,    0L, true, BigInteger.ZERO          , true,   0.0f, true,   0.0, false, null, false, null, false, null, true,   "0", false, null, false, null ),
        new TestData( PlcValueType.BYTE, Short.valueOf( (short)    1 ), false, null, true, true , true, (short)    1, true, (short)    1, true,    1, true,    1L, true, BigInteger.ONE           , true,   1.0f, true,   1.0, false, null, false, null, false, null, true,   "1", false, null, false, null ),
        new TestData( PlcValueType.BYTE, Short.valueOf( (short)  128 ), false, null, true, true , true, (short)  128, true, (short)  128, true,  128, true,  128L, true, BigInteger.valueOf(128L) , true, 128.0f, true, 128.0, false, null, false, null, false, null, true, "128", false, null, false, null ),
        new TestData( PlcValueType.BYTE, Short.valueOf( (short) 0xFF ), false, null, true, true , true, (short) 0xFF, true, (short) 0xFF, true, 0xFF, true, 0xFFL, true, BigInteger.valueOf(0xFFL), true, 255.0f, true, 255.0, false, null, false, null, false, null, true, "255", false, null, false, null ),
        /* CHAR */
        new TestData( PlcValueType.CHAR, Short.valueOf( (short)    0 ), false, null, true, false, true, (short)    0, true, (short)    0, true,    0, true,    0L, true, BigInteger.ZERO          ,  true,   0.0f, true,   0.0, false, null, false, null, false, null, true, "\u0000", false, null, false, null ),
        new TestData( PlcValueType.CHAR, Short.valueOf( (short)    1 ), false, null, true, true , true, (short)    1, true, (short)    1, true,    1, true,    1L, true, BigInteger.ONE           ,  true,   1.0f, true,   1.0, false, null, false, null, false, null, true, "\u0001", false, null, false, null ),
        new TestData( PlcValueType.CHAR, Short.valueOf( (short)   97 ), false, null, true, true , true, (short)   97, true, (short)   97, true,   97, true,   97L, true, BigInteger.valueOf(97L)  ,  true,  97.0f, true,  97.0, false, null, false, null, false, null, true,      "a", false, null, false, null ),
        new TestData( PlcValueType.CHAR, Short.valueOf( (short) 0xFF ), false, null, true, true , true, (short) 0xFF, true, (short) 0xFF, true, 0xFF, true, 0xFFL, true, BigInteger.valueOf(0xFFL),  true, 255.0f, true, 255.0, false, null, false, null, false, null, true,      "ÿ", false, null, false, null ),
        /* DATE */
        new TestData( PlcValueType.DATE, LocalDate.of( 0, 1, 1 )     , false, null, false, false, false, null, false, null, false, null, true, -62167219200L, true, BigInteger.valueOf(-62167219200L).multiply( NANOS ), false, null, false, null, true, LocalDate.of( 0, 1, 1 )     , true, LocalDateTime.of( 0, 1, 1, 0, 0 )     , false, null, true, "0000-01-01", false, null, false, null ),
        new TestData( PlcValueType.DATE, LocalDate.of( 1972, 3, 2 )  , false, null, false, false, false, null, false, null, false, null, true,     68342400L, true, BigInteger.valueOf(68342400L).multiply( NANOS )    , false, null, false, null, true, LocalDate.of( 1972, 3, 2 )  , true, LocalDateTime.of( 1972, 3, 2, 0, 0 )  , false, null, true, "1972-03-02", false, null, false, null ),
        new TestData( PlcValueType.DATE, LocalDate.of( 2026, 12, 30 ), false, null, false, false, false, null, false, null, false, null, true,   1798588800L, true, BigInteger.valueOf(1798588800L).multiply( NANOS )  , false, null, false, null, true, LocalDate.of( 2026, 12, 30 ), true, LocalDateTime.of( 2026, 12, 30, 0, 0 ), false, null, true, "2026-12-30", false, null, false, null ),
        new TestData( PlcValueType.DATE, LocalDate.of( 3000, 5, 25 ) , false, null, false, false, false, null, false, null, false, null, true,  32516121600L, true, BigInteger.valueOf(32516121600L).multiply( NANOS ) , false, null, false, null, true, LocalDate.of( 3000, 5, 25 ) , true, LocalDateTime.of( 3000, 5, 25, 0, 0 ) , false, null, true, "3000-05-25", false, null, false, null ),
        /* DATE_AND_LTIME */
        new TestData( PlcValueType.DATE_AND_LTIME, LocalDateTime.of( 0, 1, 1, 0, 0 )                  , false, null, false, false, false, null, false, null, false, null, true, -62167219200L, true, BigInteger.valueOf(-62167219200L).multiply( NANOS )                                , false, null, false, null, false, null, true, LocalDateTime.of( 0, 1, 1, 0, 0 )                , false, null, true, "0000-01-01T00:00"             , false, null, false, null ),
        new TestData( PlcValueType.DATE_AND_LTIME, LocalDateTime.of( 1972, 3, 2, 11, 30 )             , false, null, false, false, false, null, false, null, false, null, true,     68383800L, true, BigInteger.valueOf(68383800L).multiply( NANOS )                                    , false, null, false, null, false, null, true, LocalDateTime.of( 1972, 3, 2, 11, 30 )           , false, null, true, "1972-03-02T11:30"             , false, null, false, null ),
        new TestData( PlcValueType.DATE_AND_LTIME, LocalDateTime.of( 2026, 12, 30, 1, 2, 3, 4 )       , false, null, false, false, false, null, false, null, false, null, true,   1798592523L, true, BigInteger.valueOf(1798592523L).multiply( NANOS ).add( BigInteger.valueOf(4L) )    , false, null, false, null, false, null, true, LocalDateTime.of( 2026, 12, 30, 1, 2, 3, 4 )     , false, null, true, "2026-12-30T01:02:03.000000004", false, null, false, null ),
        new TestData( PlcValueType.DATE_AND_LTIME, LocalDateTime.of( 3000, 5, 25, 23, 59, 59, 65523 ), false, null, false, false, false, null, false, null, false, null, true,  32516207999L, true, BigInteger.valueOf(32516207999L).multiply( NANOS ).add( BigInteger.valueOf(65523L) ), false, null, false, null, false, null, true, LocalDateTime.of( 3000, 5, 25, 23, 59, 59, 65523 ), false, null, true, "3000-05-25T23:59:59.000065523", false, null, false, null ),
        /* DATE_AND_TIME */
        new TestData( PlcValueType.DATE_AND_TIME, LocalDateTime.of( 0, 1, 1, 0, 0 )                 , false, null, false, false, false, null, false, null, false, null, true, -62167219200L, true, BigInteger.valueOf(-62167219200L).multiply( NANOS )                                 , false, null, false, null, false, null, true, LocalDateTime.of( 0, 1, 1, 0, 0 )                 , false, null, true, "0000-01-01T00:00"             , false, null, false, null ),
        new TestData( PlcValueType.DATE_AND_TIME, LocalDateTime.of( 1972, 3, 2, 11, 30 )            , false, null, false, false, false, null, false, null, false, null, true,     68383800L, true, BigInteger.valueOf(68383800L).multiply( NANOS )                                     , false, null, false, null, false, null, true, LocalDateTime.of( 1972, 3, 2, 11, 30 )            , false, null, true, "1972-03-02T11:30"             , false, null, false, null ),
        new TestData( PlcValueType.DATE_AND_TIME, LocalDateTime.of( 2026, 12, 30, 1, 2, 3, 4 )      , false, null, false, false, false, null, false, null, false, null, true,   1798592523L, true, BigInteger.valueOf(1798592523L).multiply( NANOS ).add( BigInteger.valueOf(4L) )     , false, null, false, null, false, null, true, LocalDateTime.of( 2026, 12, 30, 1, 2, 3, 4 )      , false, null, true, "2026-12-30T01:02:03.000000004", false, null, false, null ),
        new TestData( PlcValueType.DATE_AND_TIME, LocalDateTime.of( 3000, 5, 25, 23, 59, 59, 65523 ), false, null, false, false, false, null, false, null, false, null, true,  32516207999L, true, BigInteger.valueOf(32516207999L).multiply( NANOS ).add( BigInteger.valueOf(65523L) ), false, null, false, null, false, null, true, LocalDateTime.of( 3000, 5, 25, 23, 59, 59, 65523 ), false, null, true, "3000-05-25T23:59:59.000065523", false, null, false, null ),
        /* DINT */
        new TestData( PlcValueType.DINT, Integer.valueOf(      0 ), false, null, true, false, true , (short)     0, true , (short)     0, true,      0, true,      0L, true, BigInteger.ZERO            , true,     0.0f, true,     0.0, false, null, false, null, false, null, true,     "0", false, null, false, null ),
        new TestData( PlcValueType.DINT, Integer.valueOf(      1 ), false, null, true, true , true , (short)     1, true , (short)     1, true,      1, true,      1L, true, BigInteger.ONE             , true,     1.0f, true,     1.0, false, null, false, null, false, null, true,     "1", false, null, false, null ),
        new TestData( PlcValueType.DINT, Integer.valueOf(  32565 ), false, null, true, true , true , (short) 32565, true , (short) 32565, true,  32565, true,  32565L, true, BigInteger.valueOf(32565L) , true, 32565.0f, true, 32565.0, false, null, false, null, false, null, true, "32565", false, null, false, null ),
        new TestData( PlcValueType.DINT, Integer.valueOf( 0xFFFF ), false, null, true, true , false,          null, false,          null, true, 0xFFFF, true, 0xFFFFL, true, BigInteger.valueOf(0xFFFFL), true, 65535.0f, true, 65535.0, false, null, false, null, false, null, true, "65535", false, null, false, null ),
        /* DWORD */
        new TestData( PlcValueType.DWORD, Long.valueOf(      0 ), false, null, true, false, true , (short)     0, true , (short)     0,  true,      0, true,      0L, true, BigInteger.ZERO            , true,           0.0f, true,             0.0, false, null, false, null, false, null, true,          "0", false, null, false, null ),
        new TestData( PlcValueType.DWORD, Long.valueOf(      1 ), false, null, true, true , true , (short)     1, true , (short)     1,  true,      1, true,      1L, true, BigInteger.ONE             , true,           1.0f, true,             1.0, false, null, false, null, false, null, true,          "1", false, null, false, null ),
        new TestData( PlcValueType.DWORD, Long.valueOf(  32565 ), false, null, true, true , true , (short) 32565, true , (short) 32565,  true,  32565, true,  32565L, true, BigInteger.valueOf(32565L) , true,       32565.0f, true,         32565.0, false, null, false, null, false, null, true,      "32565", false, null, false, null ),
        new TestData( PlcValueType.DWORD, Long.valueOf( 0xFFFF ), false, null, true, true , false,          null, false,          null,  true, 0xFFFF, true, 0xFFFFL, true, BigInteger.valueOf(0xFFFFL), true,       65535.0f, true,         65535.0, false, null, false, null, false, null, true,      "65535", false, null, false, null ),
        new TestData( PlcValueType.DWORD, Long.valueOf( XDWORD ), false, null, true, true , false,          null, false,          null, false,   null, true,  XDWORD, true, BigInteger.valueOf(XDWORD) , true, (float) XDWORD, true, (double) XDWORD, false, null, false, null, false, null, true, "4294967295", false, null, false, null ),
        /* INT */
        new TestData( PlcValueType.INT, Short.valueOf( (short)       0 ), false, null, true, false, true , (short) 0, true, (short)       0, true,          0, true,          0L, true, BigInteger.ZERO            , true,         0.0f, true,           0.0, false, null, false, null, false, null, true,      "0", false, null, false, null ),
        new TestData( PlcValueType.INT, Short.valueOf( (short)      -1 ), false, null, true, true , false,      null, true, (short)      -1, true,         -1, true,         -1L, true, BigInteger.ONE.negate()    , true,        -1.0f, true,          -1.0, false, null, false, null, false, null, true,     "-1", false, null, false, null ),
        new TestData( PlcValueType.INT, Short.valueOf( (short)       1 ), false, null, true, true , true , (short) 1, true, (short)       1, true,          1, true,          1L, true, BigInteger.ONE             , true,         1.0f, true,           1.0, false, null, false, null, false, null, true,      "1", false, null, false, null ),
        new TestData( PlcValueType.INT, Short.valueOf( (short)   32565 ), false, null, true, true , false,      null, true, (short)   32565, true,      32565, true,      32565L, true, BigInteger.valueOf(32565L) , true,     32565.0f, true,       32565.0, false, null, false, null, false, null, true,  "32565", false, null, false, null ),
        new TestData( PlcValueType.INT, Short.valueOf( (short)  -31965 ), false, null, true, true , false,      null, true, (short)  -31965, true,     -31965, true,     -31965L, true, BigInteger.valueOf(-31965L), true,    -31965.0f, true,      -31965.0, false, null, false, null, false, null, true, "-31965", false, null, false, null ),
        new TestData( PlcValueType.INT, Short.valueOf(            MINT ), false, null, true, true , false,      null, true,            MINT, true, (int) MINT, true, (long) MINT, true, BigInteger.valueOf(MINT)   , true, (float) MINT, true, (double) MINT, false, null, false, null, false, null, true, "-32768", false, null, false, null ),
        new TestData( PlcValueType.INT, Short.valueOf(            XINT ), false, null, true, true , false,      null, true,            XINT, true, (int) XINT, true, (long) XINT, true, BigInteger.valueOf(XINT)   , true, (float) XINT, true, (double) XINT, false, null, false, null, false, null, true,  "32767", false, null, false, null ),
        /* LDATE */                                                                                                                                                                                                                   
        new TestData( PlcValueType.LDATE, LocalDate.of( 0, 1, 1 )     , false, null, false, false, false, null, false, null, false, null, true, -62167219200L, true, BigInteger.valueOf(-62167219200L).multiply( NANOS ), false, null, false, null, true, LocalDate.of( 0, 1, 1 )     , true, LocalDateTime.of( 0, 1, 1, 0, 0 )     , false, null, true, "0000-01-01", false, null, false, null ),
        new TestData( PlcValueType.LDATE, LocalDate.of( 1972, 3, 2 )  , false, null, false, false, false, null, false, null, false, null, true,     68342400L, true, BigInteger.valueOf(68342400L).multiply( NANOS )    , false, null, false, null, true, LocalDate.of( 1972, 3, 2 )  , true, LocalDateTime.of( 1972, 3, 2, 0, 0 )  , false, null, true, "1972-03-02", false, null, false, null ),
        new TestData( PlcValueType.LDATE, LocalDate.of( 2026, 12, 30 ), false, null, false, false, false, null, false, null, false, null, true,   1798588800L, true, BigInteger.valueOf(1798588800L).multiply( NANOS )  , false, null, false, null, true, LocalDate.of( 2026, 12, 30 ), true, LocalDateTime.of( 2026, 12, 30, 0, 0 ), false, null, true, "2026-12-30", false, null, false, null ),
        new TestData( PlcValueType.LDATE, LocalDate.of( 3000, 5, 25 ) , false, null, false, false, false, null, false, null, false, null, true,  32516121600L, true, BigInteger.valueOf(32516121600L).multiply( NANOS ) , false, null, false, null, true, LocalDate.of( 3000, 5, 25 ) , true, LocalDateTime.of( 3000, 5, 25, 0, 0 ) , false, null, true, "3000-05-25", false, null, false, null ),
        /* LDATE_AND_TIME */
        new TestData( PlcValueType.LDATE_AND_TIME, LocalDateTime.of( 0, 1, 1, 0, 0 )                 , false, null, false, false, false, null, false, null, false, null, true, -62167219200L, true, BigInteger.valueOf(-62167219200L).multiply( NANOS )                                 , false, null, false, null, false, null, true, LocalDateTime.of( 0, 1, 1, 0, 0 )                 , false, null, true, "0000-01-01T00:00"             , false, null, false, null ),
        new TestData( PlcValueType.LDATE_AND_TIME, LocalDateTime.of( 1972, 3, 2, 11, 30 )            , false, null, false, false, false, null, false, null, false, null, true,     68383800L, true, BigInteger.valueOf(68383800L).multiply( NANOS )                                     , false, null, false, null, false, null, true, LocalDateTime.of( 1972, 3, 2, 11, 30 )            , false, null, true, "1972-03-02T11:30"             , false, null, false, null ),
        new TestData( PlcValueType.LDATE_AND_TIME, LocalDateTime.of( 2026, 12, 30, 1, 2, 3, 4 )      , false, null, false, false, false, null, false, null, false, null, true,   1798592523L, true, BigInteger.valueOf(1798592523L).multiply( NANOS ).add( BigInteger.valueOf(4L) )     , false, null, false, null, false, null, true, LocalDateTime.of( 2026, 12, 30, 1, 2, 3, 4 )      , false, null, true, "2026-12-30T01:02:03.000000004", false, null, false, null ),
        new TestData( PlcValueType.LDATE_AND_TIME, LocalDateTime.of( 3000, 5, 25, 23, 59, 59, 65523 ), false, null, false, false, false, null, false, null, false, null, true,  32516207999L, true, BigInteger.valueOf(32516207999L).multiply( NANOS ).add( BigInteger.valueOf(65523L) ), false, null, false, null, false, null, true, LocalDateTime.of( 3000, 5, 25, 23, 59, 59, 65523 ), false, null, true, "3000-05-25T23:59:59.000065523", false, null, false, null ),
        /* LINT */
        new TestData( PlcValueType.LINT, Long.valueOf(      0 ), false, null, true, false, true , (short)     0, true , (short)     0, true,      0, true,             0L, true, BigInteger.ZERO                   , true,                   0.0f, true,                     0.0, false, null, false, null, false, null, true,                    "0", false, null, false, null ),
        new TestData( PlcValueType.LINT, Long.valueOf(      1 ), false, null, true, true , true , (short)     1, true , (short)     1, true,      1, true,             1L, true, BigInteger.ONE                    , true,                   1.0f, true,                     1.0, false, null, false, null, false, null, true,                    "1", false, null, false, null ),
        new TestData( PlcValueType.LINT, Long.valueOf(     -1 ), false, null, true, true , true , (short)    -1, true , (short)    -1, true,     -1, true,            -1L, true, BigInteger.ONE.negate()           , true,                  -1.0f, true,                    -1.0, false, null, false, null, false, null, true,                   "-1", false, null, false, null ),
        new TestData( PlcValueType.LINT, Long.valueOf(  32565 ), false, null, true, true , true , (short) 32565, true , (short) 32565, true,  32565, true,         32565L, true, BigInteger.valueOf(32565L)        , true,               32565.0f, true,                 32565.0, false, null, false, null, false, null, true,                "32565", false, null, false, null ),
        new TestData( PlcValueType.LINT, Long.MIN_VALUE        , false, null, true, true , false,          null, false,          null, false,  null, true, Long.MIN_VALUE, true, BigInteger.valueOf(Long.MIN_VALUE), true, (float) Long.MIN_VALUE, true, (double) Long.MIN_VALUE, false, null, false, null, false, null, true, "-9223372036854775808", false, null, false, null ),
        new TestData( PlcValueType.LINT, Long.MAX_VALUE        , false, null, true, true , false,          null, false,          null, false,  null, true, Long.MAX_VALUE, true, BigInteger.valueOf(Long.MAX_VALUE), true, (float) Long.MAX_VALUE, true, (double) Long.MAX_VALUE, false, null, false, null, false, null, true,  "9223372036854775807", false, null, false, null ),
        /* LREAL */
        new TestData( PlcValueType.LREAL, Double.valueOf(       0 ), false, null, false, null, false, null, false, null, false, null, false, null, false, null, true,            0.0f, true,              0.0, false, null, false, null, false, null, true,                    "0.0", false, null, false, null ),
        new TestData( PlcValueType.LREAL, Double.valueOf(       1 ), false, null, false, null, false, null, false, null, false, null, false, null, false, null, true,            1.0f, true,              1.0, false, null, false, null, false, null, true,                    "1.0", false, null, false, null ),
        new TestData( PlcValueType.LREAL, Double.valueOf(      -1 ), false, null, false, null, false, null, false, null, false, null, false, null, false, null, true,           -1.0f, true,             -1.0, false, null, false, null, false, null, true,                   "-1.0", false, null, false, null ),
        new TestData( PlcValueType.LREAL, Double.valueOf( 65.3654 ), false, null, false, null, false, null, false, null, false, null, false, null, false, null, true,        65.3654f, true,          65.3654, false, null, false, null, false, null, true,                "65.3654", false, null, false, null ),
        new TestData( PlcValueType.LREAL, Double.MIN_VALUE         , false, null, false, null, false, null, false, null, false, null, false, null, false, null, true, Float.MIN_VALUE, true, Double.MIN_VALUE, false, null, false, null, false, null, true,               "4.9E-324", false, null, false, null ),
        new TestData( PlcValueType.LREAL, Double.MAX_VALUE         , false, null, false, null, false, null, false, null, false, null, false, null, false, null, true, Float.MAX_VALUE, true, Double.MAX_VALUE, false, null, false, null, false, null, true, "1.7976931348623157E308", false, null, false, null ),
        /* LTIME */
        new TestData( PlcValueType.LTIME, Duration.ZERO           , false, null, false, false, false, null, false, null, false, null, true,        0L, true, BigInteger.ZERO                                , false, null, false, null, false, null, false, null, false, null, true,           "PT0S", false, null, false, null ),
        new TestData( PlcValueType.LTIME, Duration.ofDays( 365 )  , false, null, false, false, false, null, false, null, false, null, true, 31536000L, true, BigInteger.valueOf(31536000L).multiply( NANOS ), false, null, false, null, false, null, false, null, false, null, true,        "PT8760H", false, null, false, null ),
        new TestData( PlcValueType.LTIME, Duration.ofSeconds( 13 ), false, null, false, false, false, null, false, null, false, null, true,       13L, true, BigInteger.valueOf(13L).multiply( NANOS )      , false, null, false, null, false, null, false, null, false, null, true,          "PT13S", false, null, false, null ),
        new TestData( PlcValueType.LTIME, Duration.ofNanos( 3023 ), false, null, false, false, false, null, false, null, false, null, true,        0L, true, BigInteger.valueOf(3023L)                      , false, null, false, null, false, null, false, null, false, null, true, "PT0.000003023S", false, null, false, null ),
        /* LTIME_OF_DAY */
        new TestData( PlcValueType.LTIME_OF_DAY, LocalTime.of( 0, 0 )                   , false, null, false, false, false, null, false, null, true,     0, true,     0L, true, BigInteger.ZERO                                                                     , false, null, false, null, false, null, false, null, false, null, true,              "00:00", false, null, false, null ),
        new TestData( PlcValueType.LTIME_OF_DAY, LocalTime.of( 3, 55 )                  , false, null, false, false, false, null, false, null, true, 14100, true, 14100L, true, BigInteger.valueOf(14100).multiply( NANOS )                                         , false, null, false, null, false, null, false, null, false, null, true,              "03:55", false, null, false, null ),
        new TestData( PlcValueType.LTIME_OF_DAY, LocalTime.of( 23, 59, 59, 999_999_999 ), false, null, false, false, false, null, false, null, true, 86399, true, 86399L, true, BigInteger.valueOf(86399L).multiply( NANOS ).add( BigInteger.valueOf(999_999_999L) ), false, null, false, null, false, null, false, null, false, null, true, "23:59:59.999999999", false, null, false, null ),
        new TestData( PlcValueType.LTIME_OF_DAY, LocalTime.of( 0, 0, 0, 32 )            , false, null, false, false, false, null, false, null, true,     0, true,     0L, true, BigInteger.valueOf(32L)                                                             , false, null, false, null, false, null, false, null, false, null, true, "00:00:00.000000032", false, null, false, null ),
        /* LWORD */
        new TestData( PlcValueType.LWORD, BigInteger.valueOf(      0 ), false, null, true, false, true , (short)     0, true , (short)     0, true ,      0, true ,      0L, true, BigInteger.ZERO            , false, null, false, null, false, null, false, null, false, null, true,                    "0", false, null, false, null ),
        new TestData( PlcValueType.LWORD, BigInteger.valueOf(      1 ), false, null, true, true , true , (short)     1, true , (short)     1, true ,      1, true ,      1L, true, BigInteger.ONE             , false, null, false, null, false, null, false, null, false, null, true,                    "1", false, null, false, null ),
        new TestData( PlcValueType.LWORD, BigInteger.valueOf(  32565 ), false, null, true, true , true , (short) 32565, true , (short) 32565, true ,  32565, true ,  32565L, true, BigInteger.valueOf(32565L) , false, null, false, null, false, null, false, null, false, null, true,                "32565", false, null, false, null ),
        new TestData( PlcValueType.LWORD, BigInteger.valueOf( 0xFFFF ), false, null, true, true , false,          null, false,          null, true , 0xFFFF, true , 0xFFFFL, true, BigInteger.valueOf(0xFFFFL), false, null, false, null, false, null, false, null, false, null, true,                "65535", false, null, false, null ),
        new TestData( PlcValueType.LWORD, XLWORD                      , false, null, true, true , false,          null, false,          null, false,   null, false,    null, true, XLWORD                     , false, null, false, null, false, null, false, null, false, null, true, "18446744073709551615", false, null, false, null ),
        /* NULL */
        new TestData( PlcValueType.NULL, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, true, "null", false, null, false, null ),
        /* REAL */
        new TestData( PlcValueType.REAL, Float.valueOf(        0 ), false, null, false, null, false, null, false, null, false, null, false, null, false, null, true,            0.0f, true,                      0.0, false, null, false, null, false, null, true, "0.0", false, null, false, null ),
        new TestData( PlcValueType.REAL, Float.valueOf(        1 ), false, null, false, null, false, null, false, null, false, null, false, null, false, null, true,            1.0f, true,                      1.0, false, null, false, null, false, null, true, "1.0", false, null, false, null ),
        new TestData( PlcValueType.REAL, Float.valueOf(       -1 ), false, null, false, null, false, null, false, null, false, null, false, null, false, null, true,           -1.0f, true,                     -1.0, false, null, false, null, false, null, true, "-1.0", false, null, false, null ),
        new TestData( PlcValueType.REAL, Float.valueOf( 65.3654f ), false, null, false, null, false, null, false, null, false, null, false, null, false, null, true,        65.3654f, true, (double)        65.3654f, false, null, false, null, false, null, true, "65.3654", false, null, false, null ),
        new TestData( PlcValueType.REAL, Float.MIN_VALUE          , false, null, false, null, false, null, false, null, false, null, false, null, false, null, true, Float.MIN_VALUE, true, (double) Float.MIN_VALUE, false, null, false, null, false, null, true, "1.4E-45", false, null, false, null ),
        new TestData( PlcValueType.REAL, Float.MAX_VALUE          , false, null, false, null, false, null, false, null, false, null, false, null, false, null, true, Float.MAX_VALUE, true, (double) Float.MAX_VALUE, false, null, false, null, false, null, true, "3.4028235E38", false, null, false, null ),
        /* SINT */                                                                                                                                                                                              
        new TestData( PlcValueType.SINT, Byte.valueOf( (byte)   0 ), false, null, true, false, true, (short)    0, true, (short)    0, true,    0, true,    0L, true, BigInteger.ZERO          , true,    0.0f, true,    0.0, false, null, false, null, false, null, true,    "0", false, null, false, null ),
        new TestData( PlcValueType.SINT, Byte.valueOf( (byte)   1 ), false, null, true, true , true, (short)    1, true, (short)    1, true,    1, true,    1L, true, BigInteger.ONE           , true,    1.0f, true,    1.0, false, null, false, null, false, null, true,    "1", false, null, false, null ),
        new TestData( PlcValueType.SINT, Byte.valueOf( (byte)  -1 ), false, null, true, true , true, (short)   -1, true, (short)   -1, true,   -1, true,   -1L, true, BigInteger.ONE.negate()  , true,   -1.0f, true,   -1.0, false, null, false, null, false, null, true,   "-1", false, null, false, null ),
        new TestData( PlcValueType.SINT, Byte.valueOf( (byte) 113 ), false, null, true, true , true, (short)  113, true, (short)  113, true,  113, true,  113L, true, BigInteger.valueOf(113L) , true,  113.0f, true,  113.0, false, null, false, null, false, null, true,  "113", false, null, false, null ),
        new TestData( PlcValueType.SINT, Byte.MIN_VALUE            , false, null, true, true , true, (short) -128, true, (short) -128, true, -128, true, -128L, true, BigInteger.valueOf(-128L), true, -128.0f, true, -128.0, false, null, false, null, false, null, true, "-128", false, null, false, null ),
        new TestData( PlcValueType.SINT, Byte.MAX_VALUE            , false, null, true, true , true, (short) 0x7F, true, (short) 0x7F, true, 0x7F, true, 0x7FL, true, BigInteger.valueOf(0x7FL), true,  127.0f, true,  127.0, false, null, false, null, false, null, true,  "127", false, null, false, null ),
        /* STRING */                                                                                                                                                                                              
        new TestData( PlcValueType.STRING,           "", false, null, true, false, false,        null, false, (short)    0, false, null, false, null, false,                     null, false, null, false,   null, false, null, false, null, false, null, true,           "", false, null, false, null ),
        new TestData( PlcValueType.STRING,       "true", false, null, true, true , false,        null, false, (short)    1, false, null, false, null, false,                     null, false, null, false,   null, false, null, false, null, false, null, true,       "true", false, null, false, null ),
        new TestData( PlcValueType.STRING,        "123", false, null, true, false, true , (short) 123, true , (short)  123, true ,  123, true , 123L, true , BigInteger.valueOf(123L), true , 123f, true ,  123.0, false, null, false, null, false, null, true,        "123", false, null, false, null ),
        new TestData( PlcValueType.STRING, "Some text.", false, null, true, false, false,        null, false,         null, false, null, false, null, false,                     null, false, null, false,   null, false, null, false, null, false, null, true, "Some text.", false, null, false, null ),
        /* TIME */
        new TestData( PlcValueType.TIME, Duration.ZERO           , false, null, false, false, false, null, false, null, false, null, true,        0L, true, BigInteger.ZERO                                , false, null, false, null, false, null, false, null, false, null, true,           "PT0S", false, null, false, null ),
        new TestData( PlcValueType.TIME, Duration.ofDays( 365 )  , false, null, false, false, false, null, false, null, false, null, true, 31536000L, true, BigInteger.valueOf(31536000L).multiply( NANOS ), false, null, false, null, false, null, false, null, false, null, true,        "PT8760H", false, null, false, null ),
        new TestData( PlcValueType.TIME, Duration.ofSeconds( 13 ), false, null, false, false, false, null, false, null, false, null, true,       13L, true, BigInteger.valueOf(13L).multiply( NANOS )      , false, null, false, null, false, null, false, null, false, null, true,          "PT13S", false, null, false, null ),
        new TestData( PlcValueType.TIME, Duration.ofNanos( 3023 ), false, null, false, false, false, null, false, null, false, null, true,        0L, true, BigInteger.valueOf(3023L)                      , false, null, false, null, false, null, false, null, false, null, true, "PT0.000003023S", false, null, false, null ),
        /* TIME_OF_DAY */
        new TestData( PlcValueType.TIME_OF_DAY, LocalTime.of( 0, 0 )                   , false, null, false, false, false, null, false, null, true,     0, true,     0L, true, BigInteger.ZERO                                                                     , false, null, false, null, false, null, false, null, false, null, true,              "00:00", false, null, false, null ),
        new TestData( PlcValueType.TIME_OF_DAY, LocalTime.of( 3, 55 )                  , false, null, false, false, false, null, false, null, true, 14100, true, 14100L, true, BigInteger.valueOf(14100).multiply( NANOS )                                         , false, null, false, null, false, null, false, null, false, null, true,              "03:55", false, null, false, null ),
        new TestData( PlcValueType.TIME_OF_DAY, LocalTime.of( 23, 59, 59, 999_999_999 ), false, null, false, false, false, null, false, null, true, 86399, true, 86399L, true, BigInteger.valueOf(86399L).multiply( NANOS ).add( BigInteger.valueOf(999_999_999L) ), false, null, false, null, false, null, false, null, false, null, true, "23:59:59.999999999", false, null, false, null ),
        new TestData( PlcValueType.TIME_OF_DAY, LocalTime.of( 0, 0, 0, 32 )            , false, null, false, false, false, null, false, null, true,     0, true,     0L, true, BigInteger.valueOf(32L)                                                             , false, null, false, null, false, null, false, null, false, null, true, "00:00:00.000000032", false, null, false, null ),
        /* UDINT */
        new TestData( PlcValueType.UDINT, Long.valueOf(      0 ), false, null, true, false, true , (short)     0, true , (short)     0, true,      0, true,     0L, true, BigInteger.ZERO           , true,           0.0f, true,             0.0, false, null, false, null, false, null, true,          "0", false, null, false, null ),
        new TestData( PlcValueType.UDINT, Long.valueOf(      1 ), false, null, true, true , true , (short)     1, true , (short)     1, true,      1, true,     1L, true, BigInteger.ONE            , true,           1.0f, true,             1.0, false, null, false, null, false, null, true,          "1", false, null, false, null ),
        new TestData( PlcValueType.UDINT, Long.valueOf(  32565 ), false, null, true, true , true , (short) 32565, true , (short) 32565, true,  32565, true, 32565L, true, BigInteger.valueOf(32565L), true,       32565.0f, true,         32565.0, false, null, false, null, false, null, true,      "32565", false, null, false, null ),
        new TestData( PlcValueType.UDINT, XUDINT                , false, null, true, true , false,          null, false,          null, false,  null, true, XUDINT, true, BigInteger.valueOf(XUDINT), true, (float) XUDINT, true, (double) XUDINT, false, null, false, null, false, null, true, "4294967295", false, null, false, null ),
        /* UINT */
        new TestData( PlcValueType.UINT, Integer.valueOf(      0 ), false, null, true, false, true , (short)     0, true , (short)     0, true,     0, true,           0L, true, BigInteger.ZERO           , true,         0.0f, true,           0.0, false, null, false, null, false, null, true,     "0", false, null, false, null ),
        new TestData( PlcValueType.UINT, Integer.valueOf(      1 ), false, null, true, true , true , (short)     1, true , (short)     1, true,     1, true,           1L, true, BigInteger.ONE            , true,         1.0f, true,           1.0, false, null, false, null, false, null, true,     "1", false, null, false, null ),
        new TestData( PlcValueType.UINT, Integer.valueOf(  32565 ), false, null, true, true , true , (short) 32565, true , (short) 32565, true, 32565, true,       32565L, true, BigInteger.valueOf(32565L), true,     32565.0f, true,       32565.0, false, null, false, null, false, null, true, "32565", false, null, false, null ),
        new TestData( PlcValueType.UINT, XUINT                    , false, null, true, true , false,          null, false,          null, true, XUINT, true, (long) XUINT, true, BigInteger.valueOf(XUINT) , true, (float)XUINT, true, (double)XUINT, false, null, false, null, false, null, true, "65535", false, null, false, null ),
        /* ULINT */
        new TestData( PlcValueType.ULINT, BigInteger.valueOf(      0 ), false, null, true, false, true , (short)     0, true , (short)     0, true ,      0, true ,      0L, true, BigInteger.ZERO             , false, null, false, null, false, null, false, null, false, null, true,                    "0", false, null, false, null ),
        new TestData( PlcValueType.ULINT, BigInteger.valueOf(      1 ), false, null, true, true , true , (short)     1, true , (short)     1, true ,      1, true ,      1L, true, BigInteger.ONE              , false, null, false, null, false, null, false, null, false, null, true,                    "1", false, null, false, null ),
        new TestData( PlcValueType.ULINT, BigInteger.valueOf(  32565 ), false, null, true, true , true , (short) 32565, true , (short) 32565, true ,  32565, true ,  32565L, true, BigInteger.valueOf(32565L)  , false, null, false, null, false, null, false, null, false, null, true,                "32565", false, null, false, null ),
        new TestData( PlcValueType.ULINT, BigInteger.valueOf( 0xFFFF ), false, null, true, true , false,          null, false,          null, true , 0xFFFF, true , 0xFFFFL, true, BigInteger.valueOf(0xFFFFL ), false, null, false, null, false, null, false, null, false, null, true,                "65535", false, null, false, null ),
        new TestData( PlcValueType.ULINT, XULINT                      , false, null, true, true , false,          null, false,          null, false,   null, false,    null, true, XULINT                      , false, null, false, null, false, null, false, null, false, null, true, "18446744073709551615", false, null, false, null ),
        /* USINT */
        new TestData( PlcValueType.USINT, Short.valueOf( (short)   0 ), false, null, true, false, true , (short) 0, true, (short)   0, true,            0, true,            0L, true, BigInteger.ZERO           , true,           0.0f, true,             0.0, false, null, false, null, false, null, true,   "0", false, null, false, null ),
        new TestData( PlcValueType.USINT, Short.valueOf( (short)   1 ), false, null, true, true , true , (short) 1, true, (short)   1, true,            1, true,            1L, true, BigInteger.ONE            , true,           1.0f, true,             1.0, false, null, false, null, false, null, true,   "1", false, null, false, null ),
        new TestData( PlcValueType.USINT, Short.valueOf( (short) 213 ), false, null, true, true , false,      null, true, (short) 213, true,          213, true,          213L, true, BigInteger.valueOf(213L)  , true,         213.0f, true,           213.0, false, null, false, null, false, null, true, "213", false, null, false, null ),
        new TestData( PlcValueType.USINT, Short.valueOf(      XUSINT ), false, null, true, true , false,      null, true,      XUSINT, true, (int) XUSINT, true, (long) XUSINT, true, BigInteger.valueOf(XUSINT), true, (float) XUSINT, true, (double) XUSINT, false, null, false, null, false, null, true, "255", false, null, false, null ),
        /* DINT */
        new TestData( PlcValueType.DINT, Integer.valueOf(      0 ), false, null, true, false, true , (short)     0, true , (short)     0, true,      0, true,      0L, true, BigInteger.ZERO            , true,     0.0f, true,     0.0, false, null, false, null, false, null, true,     "0", false, null, false, null ),
        new TestData( PlcValueType.DINT, Integer.valueOf(      1 ), false, null, true, true , true , (short)     1, true , (short)     1, true,      1, true,      1L, true, BigInteger.ONE             , true,     1.0f, true,     1.0, false, null, false, null, false, null, true,     "1", false, null, false, null ),
        new TestData( PlcValueType.DINT, Integer.valueOf(  32565 ), false, null, true, true , true , (short) 32565, true , (short) 32565, true,  32565, true,  32565L, true, BigInteger.valueOf(32565L) , true, 32565.0f, true, 32565.0, false, null, false, null, false, null, true, "32565", false, null, false, null ),
        new TestData( PlcValueType.DINT, Integer.valueOf( 0xFFFF ), false, null, true, true , false,          null, false,          null, true, 0xFFFF, true, 0xFFFFL, true, BigInteger.valueOf(0xFFFFL), true, 65535.0f, true, 65535.0, false, null, false, null, false, null, true, "65535", false, null, false, null ),
        /* WCHAR */
        new TestData( PlcValueType.WCHAR, Integer.valueOf(      0 ), false, null, true, false, true , (short)     0, true , (short)     0, true,      0, true,      0L, true, BigInteger.ZERO            , true,     0.0f, true,     0.0, false, null, false, null, false, null, true, "\u0000", false, null, false, null ),
        new TestData( PlcValueType.WCHAR, Integer.valueOf(      1 ), false, null, true, true , true , (short)     1, true , (short)     1, true,      1, true,      1L, true, BigInteger.ONE             , true,     1.0f, true,     1.0, false, null, false, null, false, null, true, "\u0001", false, null, false, null ),
        new TestData( PlcValueType.WCHAR, Integer.valueOf(     97 ), false, null, true, true , true , (short)    97, true , (short)    97, true,     97, true,     97L, true, BigInteger.valueOf(97L)    , true,    97.0f, true,    97.0, false, null, false, null, false, null, true,      "a", false, null, false, null ),
        new TestData( PlcValueType.WCHAR, Integer.valueOf(  32565 ), false, null, true, true , true , (short) 32565, true , (short) 32565, true,  32565, true,  32565L, true, BigInteger.valueOf(32565L) , true, 32565.0f, true, 32565.0, false, null, false, null, false, null, true, "\u7f35", false, null, false, null ),
        new TestData( PlcValueType.WCHAR, Integer.valueOf( 0xFFFF ), false, null, true, true , false,          null, false,          null, true, 0xFFFF, true, 0xFFFFL, true, BigInteger.valueOf(0xFFFFL), true, 65535.0f, true, 65535.0, false, null, false, null, false, null, true, "\uffff", false, null, false, null ),
        /* WSTRING */                                                                                                                                                                                              
        new TestData( PlcValueType.WSTRING,           "", false, null, true, false, false,        null, false, (short)    0, false, null, false, null, false,                     null, false, null, false,   null, false, null, false, null, false, null, true,           "", false, null, false, null ),
        new TestData( PlcValueType.WSTRING,       "true", false, null, true, true , false,        null, false, (short)    1, false, null, false, null, false,                     null, false, null, false,   null, false, null, false, null, false, null, true,       "true", false, null, false, null ),
        new TestData( PlcValueType.WSTRING,        "123", false, null, true, false, true , (short) 123, true , (short)  123, true ,  123, true , 123L, true , BigInteger.valueOf(123L), true , 123f, true ,  123.0, false, null, false, null, false, null, true,        "123", false, null, false, null ),
        new TestData( PlcValueType.WSTRING, "Some text.", false, null, true, false, false,        null, false,         null, false, null, false, null, false,                     null, false, null, false,   null, false, null, false, null, false, null, true, "Some text.", false, null, false, null ),
        /* List */
        new TestData( PlcValueType.List, new TestData[]{
            new TestData( PlcValueType.INT, Short.valueOf( (short)       1 ), false, null, true, true , true , (short) 1, true, (short) 1, true, 1, true, 1L, true, BigInteger.ONE, true, 1.0f, true, 1.0, false, null, false, null, false, null, true, "1", false, null, false, null ),
            new TestData( PlcValueType.INT, Short.valueOf( (short)       2 ), false, null, true, true , true , (short) 1, true, (short) 2, true, 2, true, 2L, true, BigInteger.TWO, true, 2.0f, true, 2.0, false, null, false, null, false, null, true, "2", false, null, false, null ),
        }, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, true, "[ResponseValue[value=ShortValue[value=1]], ResponseValue[value=ShortValue[value=2]]]", true, 2, false, null ),
        /* Struct */
        new TestData( PlcValueType.Struct, Map.of(
            "one",   new TestData( PlcValueType.INT, Short.valueOf( (short)       1 ), false, null, true, true , true , (short) 1, true, (short) 1, true, 1, true, 1L, true, BigInteger.ONE, true, 1.0f, true, 1.0, false, null, false, null, false, null, true, "1", false, null, false, null ),
            "two",   new TestData( PlcValueType.INT, Short.valueOf( (short)       2 ), false, null, true, true , true , (short) 1, true, (short) 2, true, 2, true, 2L, true, BigInteger.TWO, true, 2.0f, true, 2.0, false, null, false, null, false, null, true, "2", false, null, false, null )
        ), false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, false, null, true, "{one=ResponseValue[value=ShortValue[value=1]], two=ResponseValue[value=ShortValue[value=2]]}", false, null, true, List.of( "one", "two") ),
    };
    // @formatter:on

    /**
     * Test all response values;
     * @return The test stream.
     */
    @TestFactory
    public Stream< DynamicNode > ResponseValueTest()
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
                    shortResponseTest( input ),
                    integerResponseTest( input ),
                    dateResponseTest( input ),
                    dateTimeResponseTest( input ),
                    longResponseTest( input ),
                    bigIntegerResponseTest( input ),
                    doubleResponseTest( input ),
                    stringResponseTest( input ),
                    listResponseTest( input ),
                    structResponseTest( input ),
                    /* plc */
                    binaryResponsePlcTest( input ),
                    booleanResponsePlcTest( input ),
                    shortResponsePlcTest( input ),
                    integerResponsePlcTest( input ),
                    dateResponsePlcTest( input ),
                    dateTimeResponsePlcTest( input ),
                    longResponsePlcTest( input ),
                    bigIntegerResponsePlcTest( input ),
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
                    dynamicTest( "is", () -> assertEquals( input.testValues.isBoolean, input.value.isBoolean() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isBoolean )
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
                        if ( input.testValues.isBoolean != input.plcValue.isBoolean() )
                        {
                            LOGGER
                                .warn( "{} is Boolean differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isBoolean, input.plcValue.isBoolean() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        Boolean plcValue;
                        if ( input.testValues.isBoolean )
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
                        assertEquals( input.testValues.isShort, input.value.isShort() );
                    } ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isShort )
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
                        if ( input.testValues.isShort != input.plcValue.isShort() )
                        {
                            LOGGER.warn( "{} is Short differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isShort, input.plcValue.isShort() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        Short plcValue;
                        if ( input.testValues.isShort )
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
                        assertEquals( input.testValues.isBinary, input.value.isBinary() );
                    } ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isBinary )
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
                        if ( ( input.testValues.isBinary == true ) != ( input.plcValue.getPlcValueType() == PlcValueType.RAW_BYTE_ARRAY ) )
                        {
                            LOGGER.warn( "{} is Binary differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isShort, input.plcValue.isShort() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        byte[] plcValue;
                        if ( input.testValues.isShort )
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
                    dynamicTest( "is", () -> assertEquals( input.testValues.isInteger, input.value.isInteger() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isInteger )
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
                        if ( input.testValues.isInteger != input.plcValue.isInteger() )
                        {
                            LOGGER
                                .warn( "{} is Integer differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isInteger, input.plcValue.isInteger() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        Integer plcValue;
                        if ( input.testValues.isInteger )
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
                    dynamicTest( "is", () -> assertEquals( input.testValues.isLong, input.value.isLong() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isLong )
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
                        if ( input.testValues.isLong != input.plcValue.isLong() )
                        {
                            LOGGER.warn( "{} is Long differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isLong, input.plcValue.isLong() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        Long plcValue;
                        if ( input.testValues.isLong )
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
                    dynamicTest( "is", () -> assertEquals( input.testValues.isBigInteger, input.value.isBigInteger() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isBigInteger )
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
                        if ( input.testValues.isBigInteger != input.plcValue.isBigInteger() )
                        {
                            LOGGER
                                .warn(
                                    "{} is BigInteger differ, expected: {} plc: {}",
                                    input.plcValue.getClass().getName(),
                                    input.testValues.isBigInteger,
                                    input.plcValue.isBigInteger()
                                );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        BigInteger plcValue;
                        if ( input.testValues.isBigInteger )
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
                    dynamicTest( "is", () -> assertEquals( input.testValues.isDouble, input.value.isDouble() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isDouble )
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
                        if ( input.testValues.isDouble != input.plcValue.isDouble() )
                        {
                            LOGGER.warn( "{} is Double differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isDouble, input.plcValue.isDouble() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        Double plcValue;
                        if ( input.testValues.isDouble )
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
                    dynamicTest( "is", () -> assertEquals( input.testValues.isDate, input.value.isDate() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isDate )
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
                        if ( input.testValues.isDate != input.plcValue.isDate() )
                        {
                            LOGGER.warn( "{} is Date differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isDate, input.plcValue.isDate() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        LocalDate plcValue;
                        if ( input.testValues.isDate )
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
                    dynamicTest( "is", () -> assertEquals( input.testValues.isDateTime, input.value.isDateTime() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isDateTime )
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
                        if ( input.testValues.isDateTime != input.plcValue.isDateTime() )
                        {
                            LOGGER
                                .warn(
                                    "{} is DateTime differ, expected: {} plc: {}",
                                    input.plcValue.getClass().getName(),
                                    input.testValues.isDateTime,
                                    input.plcValue.isDateTime()
                                );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        LocalDateTime plcValue;
                        if ( input.testValues.isDateTime )
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
                    dynamicTest( "is", () -> assertEquals( input.testValues.isString, input.value.isString() ) ),
                    dynamicTest( "as", () ->
                    {
                        if ( input.testValues.isString )
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
                        if ( input.testValues.isString != input.plcValue.isString() )
                        {
                            LOGGER
                                .warn( "{} is DateTime differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isDateTime, input.plcValue.isString() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        String plcValue;
                        if ( input.testValues.isString )
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
                    dynamicTest( "is", () -> assertEquals( input.testValues.isList, input.value.isList() ) ),
                    dynamicTest( "length", () ->
                    {
                        if ( input.testValues.isList )
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
                        if ( input.testValues.isList )
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
                        if ( input.testValues.isList != input.plcValue.isList() )
                        {
                            LOGGER
                                .warn( "{} is DateTime differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isDateTime, input.plcValue.isString() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        int length;
                        if ( input.testValues.isList )
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
                        if ( input.testValues.isString )
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
                    dynamicTest( "is", () -> assertEquals( input.testValues.isStruct, input.value.isStruct() ) ),
                    dynamicTest( "length", () ->
                    {
                        if ( input.testValues.isStruct )
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
                        if ( input.testValues.isStruct )
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
                        if ( input.testValues.isStruct )
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
                        if ( input.testValues.isStruct != input.plcValue.isStruct() )
                        {
                            LOGGER
                                .warn( "{} is DateTime differ, expected: {} plc: {}", input.plcValue.getClass().getName(), input.testValues.isDateTime, input.plcValue.isString() );
                        }
                    } ),
                    dynamicTest( "as plc", () ->
                    {
                        int length;
                        if ( input.testValues.isStruct )
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
                        if ( input.testValues.isStruct )
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
                        if ( input.testValues.isStruct )
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
        if ( input.testValues.isStruct )
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
