## Client-Server program using sockets
Tasks:
1. Develop a client server program using sockets
2. The server must be able to accept and maintain connection with multiple clients. Each client will have a unique identity
3. The client "c" will send a message "s" and the server will broadcast the message " received from c: s" to all the clients, including the sender.
4. The clients can come and go in between i.e. they can cancel their connection by informing the server and also abruptly.
5. The server can also go offline after informing the clients as well as abruptly.


## Implementing reliable data communication over UDP
Tasks:

1. Create a socket connection using UDP. You should be able to send a number of packets (e.g. 1000) and on the receiving side, you should be able to show the received messages as well as lost messages?
2. Implement reliable data communication on top of this basic UDP communication. As a result, the receiver must receive all the packets and in the order that they have beens sent. To implement:
a) Timeouts
b) Acknowledgements 
c) Retransmissions 
d) Bufferring at receiver's end for in-order delivery
e) Receiver window to manage the flow 
f) It must not be a "hold and wait" protocol i.e. only one message is being sent and next message is sent only after first message has been received correctly. The Window size should be "n" and taken as parameter in the beginning of running your program 


## Implementing routing protocols
Tasks:

Input: A network topology in a given format as shown here: 1:2, 10; 2:3, 20; 1:3:5;
For example the above string represents a network of 3 nodes where node 1 and 2 are connected with a link cost of 10 and so on.
To implement:
1. Link-state routing algorithms and calculate shortest paths for each node. You should be able to print intermediate steps and the final forwarding tables. 

2. Implement Distance-vector protocol; you should print internediate steps and the final forwarding table. Your program should also be able to receive a change in the cost of a link and then recalculate. For a network of 3 nodes also implement poison reverse. 

Should be able to receive networks of upto 50 nodes, so choose your data-structures carefully.
