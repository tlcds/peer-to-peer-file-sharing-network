# P2P File Sharing Network Implementation
In this project, the network implements a protocol modified from the popular P2P protocol, BitTorrent.
Among the interesting features of BitTorrent protocol, the network uses the choking-unchoking mechanism of BitTorrent to share the file.
The shared file would be split into multiple pieces and each piece has the same size defined in the ``` Common.cfg ```

## Configuration files
* ```Common.cfg:``` </br>

  This file defines the values of the variables: </br>
  ```NumberOfPreferredNeighbors```, 
  ```UnchokingInterval```, 
  ```OptimisticUnchokingInterval```, 
  ```FileName```, 
  ```FileSize```, 
  ```PieceSize```.

  Example:
  ```
  NumberOfPreferredNeighbors 3
  UnchokingInterval 5
  OptimisticUnchokingInterval 10
  FileName thefile
  FileSize 26591178
  PieceSize 32768
  ```

* ```PeerInfo.cfg:``` </br>

  This file provides the information of all the peers in the following format: </br> 
  ``` [peerID] [hostName] [listeningPort] [hasFile]```
  
  Example:
  ```
  1001 127.0.0.1 6001 1 
  1002 127.0.0.1 6002 0 
  1003 127.0.0.1 6003 0 
  1004 127.0.0.1 6004 0 
  1005 127.0.0.1 6005 0 
  ```


## Getting Started

One could run multiple processes by modifying  **```[peerID]```**  of the command below.<br />

```
java peerProcess [peerID]
```
Example:
```
java peerProcess 1001
```
