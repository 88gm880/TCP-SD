package Cliente;

import Servidor.Protocolo;
import Servidor.Servico;
import com.google.gson.Gson;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cliente {

    DataInputStream in = null;                  // cria um duto de entrada
    DataOutputStream out = null;
    String receber;
    Socket clientSocket = null;
    boolean flagSair = false;
    public boolean flagMsg = false;
    private String msg = null;

    Gson gson = new Gson();
    Protocolo protocolo;
    String enviar;

    public Cliente(String nome, String serverHostname, int port, String tipo) {
        protocolo = new Protocolo("login", nome, tipo);
        enviar = gson.toJson(protocolo);
        System.out.println("Attemping to connect to host "
                + serverHostname + " on port " + port);

        try {
            clientSocket = new Socket(serverHostname, port);
            in = new DataInputStream(clientSocket.getInputStream());    // aponta o duto de entrada para o socket do cliente
            out = new DataOutputStream(clientSocket.getOutputStream());       // aponta o duto de saída para o socket do cliente

            out.writeUTF(enviar);

        } catch (UnknownHostException e) {
            System.err.println("Host desconhecido: " + serverHostname);
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Não foi possível I/O para"
                    + "a conexão com:" + serverHostname);
            System.exit(0);
        }
        new Thread(getMessage).start();
    }

    public void sendMessage(Usuario user, String msg) {
        protocolo = new Protocolo(msg, user);
        try {
            out.writeUTF(gson.toJson(protocolo));
        } catch (IOException ex) {
            System.out.println("Não foi possivel enviar a mensagem ao servidor");
        }
    }

    public void sendBroadcast(String msg) {
        protocolo = new Protocolo(msg);
        try {
            out.writeUTF(gson.toJson(protocolo));
        } catch (IOException ex) {
            System.out.println("Desconectado do servidor");
            //Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void cadastrarServico(String cargo, float salario, String descricao)
    {
        protocolo = new Protocolo(gson.toJson(new Servico(cargo, descricao, salario)));
        try {
            out.writeUTF(gson.toJson(protocolo));
        } catch (IOException ex) {
            System.out.println("Desconectado do servidor");
            //Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Runnable getMessage = new Runnable() {
        public void run() {
            try {
                while ((receber = in.readUTF()) != null) {
                    System.out.println("Servidor retornou: " + receber);
                    protocolo = gson.fromJson(receber, Protocolo.class);
                    switch (protocolo.getAction()) {
                        case "listarUsuarios":

                            break;
                        case "broadcast":

                            msg = protocolo.getNome();
                            break;
                        case "logout":
                            return;
                        default:
                            break;
                    }
                }
            } catch (IOException ex) {
                System.out.println("Desconectado do servidor");
                //Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(0);
            }

        }
    };

    public void logout() {
        protocolo.setAction("logout");
        enviar = gson.toJson(protocolo);
        try {
            out.writeUTF(enviar);
            clientSocket.close();
            out.close();
            in.close();
        } catch (IOException ex) {
            System.out.println("Desconectado do servidor");
        }

    }

    /*public static void main(String[] args) throws IOException {

        while ((receber = in.readUTF()) != null) {
            System.out.println("Servidor retornou: " + receber);
            if (input.next().equals("fim")) {
                protocolo.setAction("logout");
                enviar = gson.toJson(protocolo);
                out.writeUTF(enviar);
                System.out.println("Servidor retornou: " + receber);
                break;
            }
        }

    }*/
}
