# Mule PLC Connector
--------------------

## Introduction

Mule PLC Connector enables Mule 4 applications to communicate with PLC systems.

Several PLC protocols that are supported by the [Apache PLC4X project](https://plc4x.apache.org/) can be used:
- AB-ETH
- ADS/AMS
- BACnet/IP
- CANopen
- DeltaV
- DF1
- EtherNet/IP
- Firmata
- KNXnet/IP
- Modbus
- OPC UA
- S7 (Step7)
- Simulated

### Dependencies
The Mule-PLC-connector uses Apache PLC4X version 0.9.0

### Mule supported versions
* Mule 4.1+

## Installation

The connector is installed by adding its Maven dependency your Mule 4 application. Add following dependency to the pom:

```
<dependency>
    <groupId>nl.teslanet.mule.connectors.plc</groupId>
    <artifactId>mule-plc-connector</artifactId>
    <version>1.0.0-M4</version>
    <classifier>mule-plugin</classifier>
</dependency>
```

When this dependency is added to your application in AnypointStudio 7, the connector will be downloaded from Maven Central. 
AnypointStudio Mule Palette will show the connectors operations, ready for use. 

For every PLC protocol needed in your application, the corresponding PLC4X module has to be added to the pom as well, as additional dependency of the Mule PLC connector. Find the PLC4X module dependency on the [PLC4X site](https://plc4x.apache.org/users/protocols/) and add this to the configuration of the Mule Maven Plugin. 

For example the Modbus and Simulated modules are added to the pom like this:
```
    <plugin>
         <groupId>org.mule.tools.maven</groupId>
         <artifactId>mule-maven-plugin</artifactId>
         <version>${mule.maven.plugin.version}</version>
         <extensions>true</extensions>
         <configuration>
             <additionalPluginDependencies>
                 <!-- The connector for which the dependency is defined -->
                 <plugin>
                     <groupId>nl.teslanet.mule.connectors.plc</groupId>
                     <artifactId>mule-plc-connector</artifactId>
                     <!-- Dependencies definition for the connector -->
                     <additionalDependencies>
                         <dependency>
                             <groupId>org.apache.plc4x</groupId>
                             <artifactId>plc4j-driver-modbus</artifactId>
                             <version>0.9.0</version>
                         </dependency>
                         <dependency>
                             <groupId>org.apache.plc4x</groupId>
                             <artifactId>plc4j-driver-simulated</artifactId>
                             <version>0.9.0</version>
                         </dependency>
                     </additionalDependencies>
                 </plugin>
             </additionalPluginDependencies>
         </configuration>
     </plugin>
```

## Usage

The connector's operations such as _Read_ and _Write_,  can be added to your application by dragging these from the AnypointStudios Mule Palette into the application flows, 
or by editing the application xml configuration directly.

Which operation is supported depends on the protocol used. Also the address-format of PLC fields are protocol specific.
See [PLC4X documentation](https://plc4x.apache.org/users/protocols/).

### Configuration

The Config element configures how to connect to a PLC instance. The configuration is referenced by every operation that accesses the PLC.

For example the configuration of a Modbus PLC:

![Image](src/site/images/plc_config.png "config")

Xml configuration:
```
    <plc:config name="PLC_Config_Modbus">
        <plc:connection
            connectionUri="modbus:tcp://plc.host.name:502" />
    </plc:config>
```

### Ping operation

The ping operation tests available access to the PLC. When the PLC is accessible a boolean value True is returned, otherwise False.

Example:

![Image](src/site/images/plc_ping.png "ping")

Xml configuration:
```
    <plc:ping doc:name="Ping" config-ref="PLC_Config" />
```

### Read operation

The read operation reads actual field values from the PLC. Multiple fields can be read in one request. Every field has an alias to correlate the values that are read and returned in the response. 

Example:

![Image](src/site/images/plc_read.png "read")

Xml configuration:
```
    <plc:read doc:name="Read" config-ref="PLC_Config_Modbus">
        <plc:read-fields>
            <plc:read-field alias="input1" address="discrete-input:1" />
            <plc:read-field alias="coil2" address="coil:2" />
            <plc:read-field alias="register34array2" address="holding-register:34[2]" />
        </plc:read-fields>
    </plc:read>
```

The result of the operation is a plcReadResponse XMLmessage describing the values are read. It contains an entry for every field. For each field the responseCode, alias and one or more values are given.
The response for the request above could show:
```
    <plcReadResponse>
        <field alias="input1" count="1" responseCode="OK" type="BOOL">
            <value>true</value>
        </field>
        <field alias="coil2" count="1" responseCode="OK" type="BOOL">
            <value>false</value>
        </field>
        <field alias="register34array2" count="2" responseCode="OK" type="INT">
            <values>
                <value>31</value>
                <value>45</value>
            </values>
        </field>
    </plcReadResponse>

```

### Write operation

The write operation writes values to one or more fields of the PLC. The request contains for every field, the alias, the address and an array of one or more values.

Example: 

![Image](src/site/images/plc_write.png "write")

Xml configuration:
```
    <plc:write doc:name="Write" config-ref="PLC_Config_Modbus">
        <plc:write-fields>
            <plc:write-field alias="coil1" address="coil:1"
                values="#[ [ true ] ]" />
            <plc:write-field alias="register34array2" address="holding-register:34[2]"
                values="#[ [ 11, 22 ] ]" />
        </plc:write-fields>
    </plc:write>
```

The result of the write operation is a plcWriteResponse XML message describing which values are successfully written. The written values are returned in the response for information.
The response for the request above could show:

```
    <plcWriteResponse>
        <field alias="coil1" count="1" responseCode="OK" type="BOOL">
            <value>true</value>
        </field>
        <field alias="register34array2" count="2" responseCode="OK"
            type="INT">
            <values>
                <value>11</value>
                <value>22</value>
            </values>
        </field>
    </plcWriteResponse>

```
