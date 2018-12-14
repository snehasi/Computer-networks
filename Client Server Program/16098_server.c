#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h> // for sockaddr_in

// pthread functions
void *handler(void *);
void *commander(void *);

void broadcast(char *, int);

int client_sockets[100];
int last = 0;

pthread_mutex_t lock;

int main()
{
	int port;

	printf("Port to run server on: ");
	scanf("%d", &port);

	// setting up socket addr
	struct sockaddr_in addr;
	memset(&addr, 0, sizeof(addr));

	addr.sin_family      = AF_INET;    // address family
	addr.sin_port        = port;       // port we listen on
	addr.sin_addr.s_addr = INADDR_ANY; // not binding on any specific IP

	// Getting a socket descriptor
	int sd = socket(AF_INET, SOCK_STREAM, 0);

	if (sd < 0)
	{
		printf("[ERROR] Couldn't create socket\n");

		exit(EXIT_FAILURE);
	}

	printf("[DEBUG] Socket descriptor: %d\n", sd);

	int bind_ret = bind(sd, (struct sockaddr *)&addr, (socklen_t) sizeof(addr));

	if (bind_ret == -1)
	{
		printf("[ERROR] Couldn't bind the socket to port %d\n", port);
		exit(EXIT_FAILURE);
	}

	printf("[DEBUG] Bind to %d successful\n", port);

	int listen_ret = listen(sd, 100); // backlog for 100 connections

	if (listen_ret == -1)
	{
		printf("[ERROR] Couldn't listen on given socket :(\n");
		exit(EXIT_FAILURE);
	}

	if (pthread_mutex_init(&lock, NULL) != 0)
	{
		printf("[ERROR] Couldn't initialize lock\n");
		exit(EXIT_FAILURE);
	}

	printf("[DEBUG] Accepting connections on port %d\n", port);

	int addr_len = sizeof(addr);
	int client_sd; // for incoming conenction socker descriptor
	struct sockaddr client_addr;
	long unsigned int tid;

	// starting commander
	if (pthread_create(&tid, NULL, commander, NULL) != 0)
	{
		printf("[ERROR] Couldn't create commander thread :(\n");
		exit(EXIT_FAILURE);
	}

	while ((client_sd = accept(sd, (struct sockaddr *)&client_addr, (socklen_t *)&addr_len)) > 0)
	{
		pthread_mutex_lock(&lock);

		int client_idx = last;

		client_sockets[last++] = client_sd;

		printf("[DEBUG] Received new connection, client_id = %d\n", client_idx);

		if ((pthread_create(&tid, NULL, handler, (void *) &client_idx)) != 0)
		{
			printf("[ERROR] Couldn't start handler for client %d, closing socket\n", client_sd);

			close(client_sd);
		}

		pthread_mutex_unlock(&lock);
	}

	close(sd);

	return 0;
}

void *handler(void *data)
{
	int idx = *(int *) data;
	int sd  = client_sockets[idx];

	printf("[DEBUG] Started client %d thread\n", idx);

	char msg[1024];
	int msg_len;

	char first_msg[100];
	sprintf(first_msg, "Hello Client %d", idx);

	if (send(sd, first_msg, strlen(first_msg), 0) == -1)
	{
		printf("[ERROR] Can't connect to client %d\n", idx);
		return 0;
	}

	while ((msg_len = recv(sd, msg, 1024, 0)) > 0)
	{
		msg[msg_len] = '\0'; 

		if (!strcmp(msg, "exit"))
		{
			break;
		}

		char to_send[2000];
		memset(to_send, 0, 2000);

		sprintf(to_send, "Client %d: ", idx);
		strcat(to_send, msg);

		broadcast(to_send, strlen(to_send));

		memset(msg, 0, 1024);
	}

	pthread_mutex_lock(&lock);

	client_sockets[idx] = 0;

	pthread_mutex_unlock(&lock);

	close(sd);

	printf("[DEBUG] Client %d disconnected\n", idx);
}

// takes server commands
void *commander(void *data)
{
	char command[1000];

	while (scanf("%s", command))
	{
		if (!strcmp(command, "exit"))
		{
			pthread_mutex_lock(&lock);

			for (int i = 0; i < last; i++)
			{
				if (client_sockets[i])
				{
					close(client_sockets[i]);
					printf("[DEBUG] Client %d closed.\n", i);
				}
			}

			pthread_mutex_unlock(&lock);

			exit(EXIT_SUCCESS);
		}
	}
}

void broadcast(char * msg, int len)
{
	printf("[DEBUG] Broadcasting -- %s\n", msg);

	pthread_mutex_lock(&lock);

	for (int i = 0; i < last; i++)
	{
		if (client_sockets[i])
		{
			if (send(client_sockets[i], msg, len, 0) == -1)
			{
				printf("Client %d disconnected\n", i);
				client_sockets[i] = 0;
			}
		}
	}

	pthread_mutex_unlock(&lock);
}
