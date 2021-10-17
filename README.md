# Mule PLC Connector
--------------------

Mule PLC Connector enables Mule-applications to communicate with PLC systems.

Different PLC protocols are be supported:
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

## Dependencies
The Mule-PLC-connector uses [Apache PLC4X](https://plc4x.apache.org/users/protocols/index.html). 

## Mule supported versions
* Mule 4.1+

## Installation

To use this connector in a Mule 4 application, add following dependency to your application pom.xml:

```
<dependency>
    <groupId>nl.teslanet.mule.connectors.plc</groupId>
    <artifactId>mule-plc-connector</artifactId>
    <version>1.0.0-M4</version>
    <classifier>mule-plugin</classifier>
</dependency>
```
