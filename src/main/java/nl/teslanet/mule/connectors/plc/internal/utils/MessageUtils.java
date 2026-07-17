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
package nl.teslanet.mule.connectors.plc.internal.utils;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.types.PlcValueType;
import org.apache.plc4x.java.api.value.PlcValue;

import nl.teslanet.mule.connectors.plc.api.response.BigIntegerValue;
import nl.teslanet.mule.connectors.plc.api.response.BinaryValue;
import nl.teslanet.mule.connectors.plc.api.response.BooleanValue;
import nl.teslanet.mule.connectors.plc.api.response.ByteValue;
import nl.teslanet.mule.connectors.plc.api.response.CharacterValue;
import nl.teslanet.mule.connectors.plc.api.response.DoubleValue;
import nl.teslanet.mule.connectors.plc.api.response.DurationValue;
import nl.teslanet.mule.connectors.plc.api.response.ShortValue;
import nl.teslanet.mule.connectors.plc.api.response.StringValue;
import nl.teslanet.mule.connectors.plc.api.response.StructValue;
import nl.teslanet.mule.connectors.plc.api.response.FieldResponse;
import nl.teslanet.mule.connectors.plc.api.response.FieldValueResponse;
import nl.teslanet.mule.connectors.plc.api.response.FloatValue;
import nl.teslanet.mule.connectors.plc.api.response.IntegerValue;
import nl.teslanet.mule.connectors.plc.api.response.ListValue;
import nl.teslanet.mule.connectors.plc.api.response.LocalDateTimeValue;
import nl.teslanet.mule.connectors.plc.api.response.LocalDateValue;
import nl.teslanet.mule.connectors.plc.api.response.LocalTimeValue;
import nl.teslanet.mule.connectors.plc.api.response.LongValue;
import nl.teslanet.mule.connectors.plc.api.response.EmptyValue;
import nl.teslanet.mule.connectors.plc.api.response.ResponseCode;
import nl.teslanet.mule.connectors.plc.api.response.ResponseValue;


/**
 * Utilities for testing
 *
 */
public class MessageUtils
{
    /**
     * No instantance.
     */
    private MessageUtils()
    {
        super();
    }

    /**
     * Create Field responses list from a PLC read operation. 
     * @param response The PLC response delivering the field data.
     * @return The list of field responses.
     */
    public static List< FieldValueResponse > createFieldResponses( PlcReadResponse response )
    {
        ArrayList< FieldValueResponse > fieldResponses= new ArrayList<>();
        for ( String name : response.getTagNames() )
        {
            PlcValue plcValue= response.getPlcValue( name );
            String dataType;
            if ( plcValue != null )
            {
                dataType= plcValue.getPlcValueType().name();
            }
            else
            {
                dataType= PlcValueType.NULL.name();
            }
            ResponseValue value= createResponseValue( plcValue );
            fieldResponses
                .add(
                    new FieldValueResponse(
                        name,
                        ResponseCode.valueOf( response.getResponseCode( name ).name() ),
                        dataType,
                        value
                    )
                );
        }
        return Collections.unmodifiableList( fieldResponses );
    }

    /**
     * Create Field responses list from a PLC write operation. 
     * @param response The PLC response delivering the field data.
     * @return The list of field responses.
     */
    public static List< FieldResponse > createFieldResponses( PlcWriteResponse response )
    {
        ArrayList< FieldResponse > tagResponses= new ArrayList<>();
        for ( String name : response.getTagNames() )
        {
            tagResponses.add( new FieldResponse( name, ResponseCode.valueOf( response.getResponseCode( name ).name() ) ) );
        }
        return Collections.unmodifiableList( tagResponses );
    }

    /**
     * Create Field responses list from a PLC subscribe operation. 
     * @param response The PLC response delivering the field data.
     * @return The list of field responses.
     */
    public static List< FieldResponse > createFieldResponses( PlcSubscriptionResponse response )
    {
        ArrayList< FieldResponse > tagResponses= new ArrayList<>();
        for ( String name : response.getTagNames() )
        {
            tagResponses.add( new FieldResponse( name, ResponseCode.valueOf( response.getResponseCode( name ).name() ) ) );
        }
        return Collections.unmodifiableList( tagResponses );
    }

    /**
     * Create a response value.
     * @param plcValue The PLC value to use.
     * @return The created response value.
     */
    public static ResponseValue createResponseValue( PlcValue plcValue )
    {
        if ( plcValue == null ) return new ResponseValue( new EmptyValue() );
        ResponseValue value= null;
        switch ( plcValue.getPlcValueType() )
        {
            case BOOL:
                value= new ResponseValue( new BooleanValue( plcValue.getBoolean() ) );
                break;
            case BYTE:
                value= new ResponseValue( new ShortValue( plcValue.getShort() ) );
                break;
            case CHAR:
                value= new ResponseValue( new CharacterValue( (char) plcValue.getShort() ) );
                break;
            case DATE:
                value= new ResponseValue( new LocalDateValue( plcValue.getDate() ) );
                break;
            case DATE_AND_LTIME:
                value= new ResponseValue( new LocalDateTimeValue( plcValue.getDateTime() ) );
                break;
            case DATE_AND_TIME:
                value= new ResponseValue( new LocalDateTimeValue( plcValue.getDateTime() ) );
                break;
            case DINT:
                value= new ResponseValue( new IntegerValue( plcValue.getInteger() ) );
                break;
            case DWORD:
                value= new ResponseValue( new LongValue( plcValue.getLong() ) );
                break;
            case INT:
                value= new ResponseValue( new ShortValue( plcValue.getShort() ) );
                break;
            case LDATE:
                value= new ResponseValue( new LocalDateValue( plcValue.getDate() ) );
                break;
            case LDATE_AND_TIME:
                value= new ResponseValue( new LocalDateTimeValue( plcValue.getDateTime() ) );
                break;
            case LINT:
                value= new ResponseValue( new LongValue( plcValue.getLong() ) );
                break;
            case LREAL:
                value= new ResponseValue( new DoubleValue( plcValue.getDouble() ) );
                break;
            case LTIME:
                value= new ResponseValue( new DurationValue( plcValue.getDuration() ) );
                break;
            case LTIME_OF_DAY:
                value= new ResponseValue( new LocalTimeValue( plcValue.getTime() ) );
                break;
            case LWORD:
                value= new ResponseValue( new BigIntegerValue( plcValue.getBigInteger() ) );
                break;
            case List:
                value= new ResponseValue(
                    new ListValue( plcValue.getList().stream().map( MessageUtils::createResponseValue ).collect( Collectors.toUnmodifiableList() ) )
                );
                break;
            case RAW_BYTE_ARRAY:
                value= new ResponseValue( new BinaryValue( plcValue.getRaw() ) );
                break;
            case REAL:
                value= new ResponseValue( new FloatValue( plcValue.getFloat() ) );
                break;
            case SINT:
                value= new ResponseValue( new ByteValue( plcValue.getByte() ) );
                break;
            case STRING:
                value= new ResponseValue( new StringValue( plcValue.getString() ) );
                break;
            case Struct:
                value= new ResponseValue(
                    new StructValue(
                        plcValue.getStruct().entrySet().stream().collect( Collectors.toMap( Entry::getKey, entry -> createResponseValue( entry.getValue() ) ) )
                    )
                );
                break;
            case TIME:
                value= new ResponseValue( new DurationValue( plcValue.getDuration() ) );
                break;
            case TIME_OF_DAY:
                value= new ResponseValue( new LocalTimeValue( plcValue.getTime() ) );
                break;
            case UDINT:
                value= new ResponseValue( new LongValue( plcValue.getLong() ) );
                break;
            case UINT:
                value= new ResponseValue( new IntegerValue( plcValue.getInteger() ) );
                break;
            case ULINT:
                value= new ResponseValue( new BigIntegerValue( plcValue.getBigInteger() ) );
                break;
            case USINT:
                value= new ResponseValue( new ShortValue( plcValue.getShort() ) );
                break;
            case WCHAR:
                value= new ResponseValue( new CharacterValue( (char) plcValue.getInteger() ) );
                break;
            case WORD:
                value= new ResponseValue( new IntegerValue( plcValue.getInteger() ) );
                break;
            case WSTRING:
                value= new ResponseValue( new StringValue( plcValue.getString() ) );
                break;
            default:
            case NULL:
                value= new ResponseValue( new EmptyValue() );
                break;
        }
        return value;
    }

}
