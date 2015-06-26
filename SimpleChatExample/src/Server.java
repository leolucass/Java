import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * Autor pbl from dreamincode
 * tradutor: enter(=leolucass) (alguns coment�rios foram traduzidos, para facilitar estudo)
 *O server pode ser executado como aplica��o ou como GUI
 *http://www.dreamincode.net/forums/topic/259777-a-simple-chat-program-with-clientserver-gui-optional/
 */
/*coment�rio que ele escreveu no forum
 * Now the Server class.

You can start the Server by typing 
> java Server
at the console prompt. That will execute it in console mode and the server will wait for connection on port 1500. To use another port pass the port number to use as first parameter to the command
> java Server 1200
will ask the Server to listen on port 1200.
You can use <CTRL>C to stop the server.

 */
public class Server {
	// Uma ID �nica para cada conex�o
	private static int uniqueId;
	//Uma ArrayList para manter uma lista de Clinte
	private ArrayList<ClientThread> al;
	//se estou em uma GUI 
	private ServerGUI sg;
	// para exibir o tempo/hora
	private SimpleDateFormat sdf;
	// O n�mero da porta para listen for connection (comunicar/ser comunicado sobre a conex�o)
	private int port;
	// O boolean que vai ser alterado para parar o server
	private boolean keepGoing;
	

	/*
	 *  server constructor that receive the port to listen to for connection as parameter
	 *  in console
	 *  O construtor que recebe a porta para listen to for connection como parametro
	 */
	public Server(int port) {
		this(port, null);
	}
	
	public Server(int port, ServerGUI sg) {
		// GUI ou n�o
		this.sg = sg;
		// a porta
		this.port = port;
		// para exibir hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		// ArrayList para a lista de Cliente
		al = new ArrayList<ClientThread>();
	}
	
	public void start() {
		keepGoing = true;
		/* cria o socket(=encaixe) server e espera por requisi��o de conex�o (connection requests)*/
		try 
		{
			// O socket usado pelo server
			ServerSocket serverSocket = new ServerSocket(port);

			// loop infinito para esperar por conex�es
			while(keepGoing) 
			{
				//formato da mensagem dizendo que estamos esperando.
				display("Server waiting for Clients on port " + port + ".");
				
				Socket socket = serverSocket.accept();  	// conex�o aceita
				// if I was asked to stop: Se eu for perguntado/solicitado para parar
				if(!keepGoing)
					break; //ent�o para
				ClientThread t = new ClientThread(socket);  // faz uma thread disso
				al.add(t);									// salva isso em uma ArrayList
				t.start();
			}
			// I was asked to stop: Eu fui solicitado para parar:
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
						// not much I can do : N�o pode-se fazer muita coisa
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		//algo ocorreu mal/errado
		catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}		
    /*
     * Para GUI parar o server
     */
	protected void stop() {
		keepGoing = false;
		// conecto com eu mesmo como um Cliente para sair statement (to exit statement) connect to myself as Client to exit statement 
		// Socket socket = serverSocket.accept();
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
			// nada que possa fazer : nothing I can really do
		}
	}
	/*
	 *Exibe um evento para o console ou GUI Display an event (not a message) to the console or the GUI
	 */
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		if(sg == null)
			System.out.println(time);
		else
			sg.appendEvent(time + "\n");
	}
	/*
	 *  Para transmitir uma mensagem para todos os clientes to broadcast a message to all Clients
	 */
	private synchronized void broadcast(String message) {
		// add HH:mm:ss and \n to the message
		String time = sdf.format(new Date());
		String messageLf = time + " " + message + "\n";
		// Exibe a mensagem no console GUI display message on console or GUI
		if(sg == null)
			System.out.print(messageLf);
		else
			sg.appendRoom(messageLf);     //Anexar na sala/espa�o da janela  append in the room window
		
		// we loop in reverse order in case we would have to remove a Client
		//loop em ordem reversa no caso de termos que remover o cliente
		// porque ele foi desconectado, because it has disconnected
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			// try to write to the Client if it fails remove it from the list
			//tenta escrever para o Cliente se falhar em remove-lo da lista
			if(!ct.writeMsg(messageLf)) {
				al.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}

	// for a client who logoff using the LOGOUT message
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// found it
			if(ct.id == id) {
				al.remove(i);
				return;
			}
		}
	}
	
	/*
	 *  To run as a console application just open a console window and: 
	 * > java Server
	 * > java Server portNumber
	 * If the port number is not specified 1500 is used
	 */ 
	public static void main(String[] args) {
		// start server on port 1500 unless a PortNumber is specified 
		int portNumber = 1500;
		switch(args.length) {
			case 1:
				try {
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Server [portNumber]");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Server [portNumber]");
				return;
				
		}
		// create a server object and start it
		Server server = new Server(portNumber);
		server.start();
	}

	/** One instance of this thread will run for each client */
	class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client
		String username;
		// the only type of message a will receive
		ChatMessage cm;
		// the date I connect
		String date;

		// Constructore
		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			/* Creating both Data Stream */
			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				display(username + " just connected.");
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			// have to catch ClassNotFoundException
			// but I read a String, I am sure it will work
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}

		// what will run forever
		public void run() {
			// to loop until LOGOUT
			boolean keepGoing = true;
			while(keepGoing) {
				// read a String (which is an object)
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				// the messaage part of the ChatMessage
				String message = cm.getMessage();

				// Switch on the type of message receive
				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					broadcast(username + ": " + message);
					break;
				case ChatMessage.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
					keepGoing = false;
					break;
				case ChatMessage.WHOISIN:
					writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
					// scan al the users connected
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
					}
					break;
				}
			}
			// remove myself from the arrayList containing the list of the
			// connected Clients
			remove(id);
			close();
		}
		
		// try to close everything
		private void close() {
			// try to close the connection
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		/*
		 * Write a String to the Client output stream
		 */
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}
}

