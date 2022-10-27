# Protocol Dump Tool
Xdump is a Java library for capturing, crafting and sending packets with RPC protocol ([gRPC](https://github.com/grpc/grpc) for default) inspire of [Dubbo](https://github.com/apache/dubbo)    

```
[INFO] new tcp connection: 1.1.1.1 63894 -> 2.2.2.2 5908
[INFO] new tcp connection: 2.2.2.2 5908 -> 1.1.1.1 63894
channel       [S:/1.1.1.1:63894 -> R:/2.2.2.2:5908]                                                                                                
requestId     3                                                                                                                                                
transport     gRPC                                                                                                                                             
starLine      /com.xx.yy.HelloWord/say                                                                                                       
content-type  application/grpc+proto                                                                                                                                                                                                                                                                       
grpc-timeout  100000m                                                                                                                                                                                                                                                                                                                                                                                               
te            trailers                                                                                                                                         
ua            iOS/10.0.3 (iPhone9,1;en_US) App/3.0.1.67 Channel/201200                                                                                                                                                            
len           49                                                                                                                                               
data          hQKhMwOpNC4xLjQuMTMxBadhbmRyb2lkBrE2NjQzNzM4M0BkaW5nZGluZwfOA/m3YA==

channel        [S:/2.2.2.2:5908 -> R:/1.1.1.1:63894]                                                                                               
requestId      3                                                                                                                                               
transport      gRPC                                                                                                                                            
status         200                                                                                                                                             
content-type   application/grpc+proto                                                                                                                                                                                                                                     
len            384                                                                                                                                             
data           gwHOAAFRgALaARl7ImRvbWFpbkxpc3QiOlt7ImV4cGlyZSI6ODY0MDAsImlwTGlzdCI6WyJsd3M6Ly80Ny4yNDYuMTM3LjIwMzo0NDM/cm09dXMiLCJsd3M6Ly80Ny4yNDYuMTM3LjE5Nzo
0NDM/cm09dXMiXSwibmFtZSI6Imx3cy5sYWl3YW5nLmNvbSIsInNob3J0TGlzdCI6WyJsd3M6Ly80Ny4yNDYuMTM3LjIwOTo0NDM/cm09dXMiLCJsd3M6Ly80Ny4yNDYuMTM3LjIxMTo0ND
M/cm09dXMiLCJsd3M6Ly80Ny4yNDYuMTM3LjIxMjo0NDM/cm09dXMiLCJsd3M6Ly80Ny4yNDYuMTM3LjIxMDo0NDM/cm09dXMiXX1dfQPaAFhLSEJzQ0pvOWlJRDhaV1R4M3VkcU5hSE5Ue
ForeWNuNjAwWkRlc1dJQ3NId3NVY0R1Qk5XUTRUSGJZdHN2MkVuUnRpU1F1WGZLcmpsMHhJTXAyWElEQT09
```
## Requirement
+ OS version > 7u
+ glibc

## Quick Start

### Bootstrap
```
sudo java -jar xdump.jar -h 
usage:  [-f <arg>] [-h] [--host <arg>] [-i <arg>] [--ignoreHeartbeat] [-p <arg>] [--port <arg>]
```
+ --host: target IP
+ --port: target port
+ --p: protocol (not implement yet , only support gRPC now)
+ --networkInterface: network interface, entry select condition when not set
+ --ignoreHeartbeat: ignore heart beat frame (not implement yet)
+ --f: bfpFilter expression, host and port will be ignored when set value 

### Reserved Key
+ channel: peer address
+ requestId: unique RPC ID , etc. streamId for gRPC
+ transport: protocol, etc. gRPC/RSocket/xRPC...
+ startLine: service unique key , etc :path for gRPC
+ len: data length
+ data: bytes (uncompress if compressed)base64

## Example

set networkInterface value
```
java -jar xdump.jar --host 1.1.1.1 --port 5908 -i en0
java -jar xdump.jar -i en0 -f "(host 1.1.1.1 or host 2.2.2.2) and port 5908"
```

not set 
```
java -jar xdump.jar --host 1.1.1.1 --port 5908
NIF[0]: en0
      : link layer address: xxx.xxx.xx.xx
      : address: /xxx:x:x:x:xxx:xxxx:xxxx:xxx
      : address: /1.1.1.1
NIF[1]: p2p0
      : link layer address: xx:xxx:xx:xx
NIF[2]: awdl0
      : link layer address: xxx:xx:xxxx:xx
      : address: /xxx:x:x:xxxxxxxx
Select a device number to capture packets, or enter 'q' to quit > 
```

## Extension Protocol
 to be continued

## Reference
+ Pcap4j [https://github.com/kaitoy/pcap4j](https://github.com/kaitoy/pcap4j)
+ gRPC [https://github.com/grpc/grpc](https://github.com/grpc/grpc)
+ Dubbo [https://github.com/apache/dubbo](https://github.com/apache/dubbo)

