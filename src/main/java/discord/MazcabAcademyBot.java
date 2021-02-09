package discord;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.requests.Route;
import sheets.Auth;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;

public class MazcabAcademyBot extends ListenerAdapter {

    public static List<List<Object>> values;

    public MazcabAcademyBot( ) throws IOException, GeneralSecurityException {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        final String range = "Sheet1!E2:E";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, Auth.JSON_FACTORY, Auth.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(Auth.APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(Token.SPREADSHEET_ID, range)
                .execute();
        values = response.getValues();
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {

        JDABuilder.createLight(Token.TOKEN, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new MazcabAcademyBot())
                .setActivity(Activity.playing("Type !refresh"))
                .build();
    }

    public static String spreadsheetImport(int begin, int end) {

        StringBuilder builder = new StringBuilder();
        int counter = 1;
        for (int i = begin; i <= end; i++) {
            builder.append("#").append(counter).append(": **").append(values.get(i).get(0)).append("**\n");
            counter++;
        }
        return builder.toString();
    }

    @Override

    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        if (msg.getContentRaw().equals("!refresh") && event.getChannel().getName().equalsIgnoreCase("bot-testing")) { // Wait for command and make sure you're in the right channel
            MessageChannel channel = event.getChannel();
            List<Message> messages = channel.getHistory().retrievePast(100).complete();

            // Id the Bm and Yaka roles
            Role bmPro = event.getGuild().getRolesByName("BM PRO", true).get(0);
            Role yakaPro = event.getGuild().getRolesByName("YAKA PRO", true).get(0);

            // Delete everything in the channel && remove bmPro role from everyone
            for (Message message : messages) {
                List<User> users = message.getMentionedUsers();
                for (User user : users) { // Remove BM & Yaka roles from everyone
                    event.getGuild().removeRoleFromMember(user.getId(), bmPro).queue();
                    event.getGuild().removeRoleFromMember(user.getId(), yakaPro).queue();
                }
                channel.purgeMessages(message);
                try {
                    Thread.sleep(50); // Delete a message every 50 ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(1000); // 1 sec break after deleting everything and removing roles
            } catch (InterruptedException ignored) {
            }

            //Header - Hall of Fame
            StringBuilder hallOfFame = new StringBuilder("**Hall of Fame:**\n\n\nFastest #beastmaster-durzag kills:\n");
            int counter = 1;

            // BM - times
            for (List row : values) {
                if (counter > 3) break;
                hallOfFame.append("#").append(counter).append(": **").append(row.get(0)).append("**\n"); // Chained append is optimized better
                counter++;
            }

            // Yaka - times
            hallOfFame.append("\nFastest #yakamaru kills:\n");
            hallOfFame.append(spreadsheetImport(4, 6));

            // Highest kcs, starting with BM
            hallOfFame.append("\n\nHighest kill-counts:\n\n :point_right:  #beastmaster-durzag\n");
            hallOfFame.append(spreadsheetImport(8, 10));

            // Yaka - High kc

            hallOfFame.append("\n :point_right:  #yakamaru\n");
            hallOfFame.append(spreadsheetImport(12, 14));

            // Lowest kc pets
            hallOfFame.append("\n\n Lowest Kill count pet drops:\n\n:point_right: Yakaminu\n");
            hallOfFame.append(spreadsheetImport(16, 18));

            // Lowest Diddy
            hallOfFame.append("\n\n:point_right: Diddyzag\n");
            hallOfFame.append(spreadsheetImport(20, 22));

            // Lowest Tuzzy
            hallOfFame.append("\n\n:point_right: Lil' Tuzzy\n");
            hallOfFame.append(spreadsheetImport(24, 26));

            channel.sendMessage(hallOfFame.toString()).queue();
            try {
                Thread.sleep(150); // Post Hall of Fame and take a small break
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Header - Hall of Shame
            StringBuilder hallOfShame = new StringBuilder("---------------------------------------\n\n" +
                    "**Hall of Shame:**\n\n\nSlowest #beastmaster-durzag kills:\n");
            hallOfShame.append(spreadsheetImport(32, 34));

            // Longest yaka
            hallOfShame.append("\nLongest #yakamaru kills:\n");
            hallOfShame.append(spreadsheetImport(36, 38));

            // Unlucky Yakaminu
            hallOfShame.append("\n\nHighest kill-count pet drops:\n\n:point_right: Yakaminu\n");
            hallOfShame.append(spreadsheetImport(40, 42));

            // Unlucky Diddy
            hallOfShame.append("\n\n:point_right: Diddyzag\n");
            hallOfShame.append(spreadsheetImport(44, 46));

            //Unlucky Tuz
            hallOfShame.append("\n:point_right: Lil' Tuzzy\n");
            hallOfShame.append(spreadsheetImport(48, 50));

            // Unlucky log
            hallOfShame.append("\nSlowest log completions: (Off-loot kills count as well)\n");
            hallOfShame.append(spreadsheetImport(52, 54));

            channel.sendMessage(hallOfShame).queue();
            try {
                Thread.sleep(1000); // Hall of shame then break
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Last part.
            StringBuilder bestBMTeam = new StringBuilder("---------------------------------------\n\n" +
                    ":trophy: :medal:** Record holding Beastmaster Durzag team **:medal: :trophy:  \n\n");

            // Best BM team
            bestBMTeam.append(spreadsheetImport(56, 65));
            channel.sendMessage(bestBMTeam).queue();
            try {
                Thread.sleep(1300); // Post team, wait and then retrieve the mentioned members
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<Message> bmProsMessage = channel.getHistory().retrievePast(1).complete();
            List<User> bmPros = bmProsMessage.get(0).getMentionedUsers();
            for (User user : bmPros) { // Add bm role to everyone
                event.getGuild().addRoleToMember(user.getId(), bmPro).queue();
            }

            try {
                Thread.sleep(150); // Wait a small fraction of a sec
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Best Yaka team
            StringBuilder bestYakaTeam = new StringBuilder("---------------------------------------\n\n" +
                    ":trophy: :medal:** Record holding Yakamaru team **:medal: :trophy: \n\n");

            bestYakaTeam.append(spreadsheetImport(68, 77));
            channel.sendMessage(bestYakaTeam).queue();
            try {
                Thread.sleep(1000); // Post message and wait a sec. Then retrieve mentioned members and give them the tag
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<Message> yakaProsMessage = channel.getHistory().retrievePast(1).complete();
            List<User> yakaPros = yakaProsMessage.get(0).getMentionedUsers();
            for (User user : yakaPros) {
                event.getGuild().addRoleToMember(user.getId(), yakaPro).queue();
            }
        }
    }
}
