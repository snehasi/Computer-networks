Sneha Sinha, 2016098

Run "make all" to generate executables. It executes the following commands:
gcc 16098_server.c -lpthread -o 16098_server
gcc 16098_client.c -lpthread -o 16098_client

Run "./16098_server" to start the server, input the port to listen on.
Run "./16098_client" to start the client, input server address and port to connect to.
Input "exit" to inform before cancelling the connection.

For localhost, use 127.0.0.1 as server address.

Run "make clean" to remove the exe files.
