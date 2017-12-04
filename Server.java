import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.net.Socket;




class Jogo implements Runnable{
    HashSet<Worker> jogadores; //em vez de worker passar jogadores para saber onde se tem que ler e escrever para comunicar com eles 

    public Jogo(HashSet<Worker> j){
        this.jogadores = j;
    } 
    
    public void run(){
        //divide jogadores 
        //começar chat
        // escolher herois
        System.out.println("Jogo acabou");
    }
}

class RealizaJogo{
    int esp;
    int group;
    int size;
    HashSet<Worker> jogadores;

    
    public RealizaJogo(){
        this.esp = 0;
        this.group = 0;
        this.size = 2;
        this.jogadores = null;
    }

    synchronized void esperaJogo(Worker e){//passar de synchornized para lock, quando fazer novo jogo desbloquear o metodo esperaJogo
        int myGroup = this.group;
        this.esp++;

        if(esp % this.size == 0){
            jogadores.add(e);
            this.group++;

            try{
                Thread t = new Thread(new Jogo(jogadores));
                this.jogadores = null;
                t.start();
                t.join(); //esperar pela thread para não libertar enquando está a ocorrer o jogo 
                notifyAll();
            }catch(Exception ex){}
        }
        else {
            if (esp % this.size == 1) this.jogadores = new HashSet<Worker>();
            jogadores.add(e);
            try{
                while(myGroup == this.group) wait(); 
            }
            catch(Exception ex){}
        }
    }

}

class Worker implements Runnable{
    private BufferedReader b;
    private PrintWriter pw;
    private RealizaJogo rj;

    public Worker(Socket s, RealizaJogo rj){
        try{
            this.b = new BufferedReader(new InputStreamReader(s.getInputStream()));
            this.pw = new PrintWriter(s.getOutputStream(), true);
            this.rj = rj;
        }
        catch(Exception e){System.out.println("ERRO NO WORKER!!");}
    }

    public void run(){  
        String line;
        try {
            while((line = this.b.readLine()) != null){
                System.out.println(line);
                if(line.equals("jogar")){
                    this.rj.esperaJogo(this);
                }
            }
        }catch(Exception e){System.out.println("ERRO NO RUN DO WORKER!!");}
    }
}

class Server{
    HashMap<String, Player> players;
    
    public static void main(String[] args){
        try{
            ServerSocket server = new ServerSocket(1997);
            Socket s;
            RealizaJogo rj = new RealizaJogo();
            
            while((s = server.accept()) != null){
                new Thread(new Worker(s, rj)).start();
            }
        }catch(Exception e){System.out.println("ERRO NO MAIN DO SEVER!!");}
    }

}

