#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <fcntl.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

void *send_handler(void *);
void *recv_handler(void *);

int main()
{
	char server_addr[100];
	int port;

	printf("Server Address: ");
	scanf("%s", server_addr);

	printf("Server Port: ");
	scanf("%d", &port);

	struct sockaddr_in addr;
	memset(&addr, 0, sizeof(addr));

	addr.sin_family = AF_INET;
	addr.sin_port   = port;

	if (inet_pton(AF_INET, server_addr, &(addr.sin_addr)) != 1)
	{
		printf("[ERROR] Couldn't resolve server address\n");
		exit(EXIT_FAILURE);
	}

	int sd = socket(AF_INET, SOCK_STREAM, 0);

	if (sd < 0)
	{
		printf("[ERROR] Couldn't create socket\n");
		exit(EXIT_FAILURE);
	}

	if (connect(sd, (struct sockaddr *) &addr, (socklen_t) sizeof(addr)) == -1)
	{
		printf("[ERROR] Couldn't connect to server\n");
		exit(EXIT_FAILURE);
	}

	printf(" ******* Connected to Sever %s:%d ******* \n", server_addr, port);

	long unsigned int send_thread;
	long unsigned int recv_thread;

	if (pthread_create(&send_thread, NULL, send_handler, (void *) &sd) < 0)
	{
		printf("[ERROR] Couldn't start send handler");
		exit(EXIT_FAILURE);
	}

	if (pthread_create(&recv_thread, NULL, recv_handler, (void *) &sd) < 0)
	{
		printf("[ERROR] Couldn't start recv handler");
		exit(EXIT_FAILURE);
	}

	pthread_join(send_thread, NULL);
	pthread_join(recv_thread, NULL);

	return 0;
}

void * send_handler(void *data)
{
	int sd = * (int *)data;

	char msg[1024];

	while (1)
	{
		if ( fgets(msg, 1024, stdin) )
		{
			// removing newline
			msg[strlen(msg) - 1] = '\0';

			if ( send(sd, msg, strlen(msg), 0) == -1)
			{
				printf("[ERROR] Send failed");
				exit(EXIT_FAILURE);
			}

			memset(msg, 0, 1024);
		}
	}
}

void * recv_handler(void *data)
{
	int sd = * (int *)data;

	char msg[1024];

	while ( recv(sd, msg, 1024, 0) > 0 )
	{
		printf("> %s\n", msg);
		memset(msg, 0, 1024);
	}

	printf("Socket closed\n");
	exit(EXIT_SUCCESS);
}
