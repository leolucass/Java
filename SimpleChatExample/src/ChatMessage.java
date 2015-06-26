import java.io.*;
/*
*Essa classe define os diferentes tipos de mensagens
*que vão ser trocados entre os Clientes e o Server.
*Quando falando de um Java Client para um Java Server é 
*muito facil passar Java Objects, não há necessidade para 
*contar os bytes ou para esperar para esperar uma linha
*seguida no final do frame.
*/

/*comentário que ele escreveu no forum
 * Autor pbl from dreamincode
 * Actually, if you want to run the application in console mode, you only need the first 3 classes. The two GUI classes can be used as a bonus, it is a very simple GUI. You can run both the Client and the Server in GUI mode or only one of the two in GUI mode.

The ChatMessage class.

When you establish connections over TCP it is only a serie of bytes that are actually sent over the wire. If you have a Java application that talks to a C++ application you need to send series of bytes and have both the sender and the receiver to agree on what these bytes represent.

When talking between two Java applications, if both have access to the same code, I personally prefer to send Java Object between the two applications. Actually it will still a stream of bytes that will be sent over the internet but Java will do the job of serializing and deserializing the Java objects for you. To do that you have to create an ObjectInputStream and an ObjectOutputStream from the Socket InputStream and the Socket OutputStream. 

The objects sent of the sockets have to implements Serializable.
In this application, all the messages sent from the Server to the Client are String objects. All the messages sent from the Client to the Server (but the first one which is a String) are ChatMessage. ChatMessage have a type and a String that contains the actual message.
 */
public class ChatMessage implements Serializable{

protected static final long serialVersionUID = 1112122200L;

//Os diferentes tipos de mensagem enviados pelo Cliente
//WHOISIN para receber uma lista de usuários conectados
//MESSAGE uma mensagem ordinária
//LOGOUT para desconectar do Server

static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2;
private int type; //tipo de mensagem
private String message; //mensagem

//constructor
ChatMessage(int type, String message){
this.type = type;
this.message = message;
}
//getters
int getType(){
return getType();
}
String getMessage(){
return message;
}
}
