# Mule PLC Extension
--------------------

Mule extension that enables Mule-applications to communicate with PLC systems.

Different PLC protocols will be supported:
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

M1 release is tested with Modbus and Simulated. 

## Dependencies
The Mule-PLC-connector uses [Apache PLC4X](https://plc4x.apache.org/users/protocols/index.html). 

## Mule supported versions
* Mule 4.1
* Mule 4.2
* Mule 4.3


## Installation

To use this connector add this dependency to your application pom.xml

```
<groupId>nl.teslanet.mule.connectors.plc</groupId>
<artifactId>mule-plc-connector</artifactId>
<version>1.0.0-M1</version>
<classifier>mule-plugin</classifier>
```
