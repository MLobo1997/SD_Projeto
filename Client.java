import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


class Client{

    public static void main(String[] args){
        try{
            Socket s = new Socket("localhost", 1997);
            //BufferedReader buff = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String line;

            while((line = input.readLine()) != null){
                pw.println(line);
            }
            s.close();
        }catch(Exception e){System.out.println("ERRO NO CLIENTE!!");}
    }
}
