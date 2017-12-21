package Game_Information;


import java.util.LinkedList;
import java.util.Observable;
import java.util.Queue;

/**
 * Queue de mensagens que têm de ser enviadas para o cliente
 * .se foi tirada da queue, presume-se que já foi enviada
 * .se foi colocada na queue, a thread que a possui tem de, quando notificada, as enviar para o cliente
 */
public class MessageBuffer extends Observable {
    Queue<String> messages;

    /**
     * Construtor
     */
    public MessageBuffer() {
        messages = new LinkedList<>();
    }

    /**
     * Adicionar uma mensagem à queue
     * @param line Mensagem a ser enviada ao cliente
     */
    synchronized public void publishMessage(String line) {
        messages.add(line);

        // Notificar leitores: Há mensagens novas!
        setChanged();
        notifyObservers();
    }

    /**
     * Receber Mensagem em primeiro lugar na queue (FIFO)
     * @return Mensagem
     */
    synchronized public String getMessage() {
        return messages.poll();
    }
}
