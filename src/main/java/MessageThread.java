
import java.util.TimerTask;


public class MessageThread extends TimerTask {
    @Override
    public void run() {

                Bot.getBot().sendNextMessage();


    }
}
