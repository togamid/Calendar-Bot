import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListeners extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msgContent = event.getMessage().getContentRaw();
        if (msgContent.equalsIgnoreCase(Bot.getBot().getBotSignifier() + "start")) {
            Bot.getBot().setChannel(event.getChannel());
            Bot.getBot().startBot();
            event.getChannel().sendMessage("Started Bot in this channel!").queue();
        } else if(msgContent.equalsIgnoreCase(Bot.getBot().getBotSignifier() + "next")) {
            Bot.getBot().sendNextMessage();
        }
    }
}
