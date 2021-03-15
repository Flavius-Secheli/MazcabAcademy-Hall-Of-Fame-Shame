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
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import sheets.Auth;

import java.security.GeneralSecurityException;
import java.util.List;

public class MazcabAcademyBot extends ListenerAdapter {

    public static List<List<Object>> values;

    public static void main(String[] args) throws GeneralSecurityException {
        JDABuilder.createLight(Token.TOKEN, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
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

        // Wait for command and make sure you're in the right channel
        if (msg.getContentRaw().equals("!refresh") && event.getChannel().getName().equalsIgnoreCase(constants.CHANNEL_NAME)) {
            NetHttpTransport HTTP_TRANSPORT = null;
            try {
                HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            } catch (Throwable e) {
            }

            final String range = "Sheet1!E2:E";
            Sheets service = null;
            try {
                service = new Sheets.Builder(HTTP_TRANSPORT, Auth.JSON_FACTORY, Auth.getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(Auth.APPLICATION_NAME)
                        .build();
            } catch (Throwable e) {
            }

            ValueRange response = null;
            try {
                response = service.spreadsheets().values()
                        .get(Token.SPREADSHEET_ID, range)
                        .execute();
            } catch (Throwable e) {
            }
            values = response.getValues();


            MessageChannel channel = event.getChannel();
            List<Message> messages = channel.getHistory().retrievePast(100).complete();

            Guild guild = event.getGuild();

            // Remove the Yaka and BM roles from everyone
            List<Role> goodBM = guild.getRolesByName(constants.ROLE_BM, true);
            List<Role> goodYaka = guild.getRolesByName(constants.ROLE_YAKA, true);

            List<Member> goodBMer = guild.getMembersWithRoles(goodBM.get(0));
            for (Member member : goodBMer) {
                guild.removeRoleFromMember(member, goodBM.get(0)).queue();
            }

            List<Member> goodYakaer = guild.getMembersWithRoles(goodYaka.get(0));
            for (Member member : goodYakaer) {
                guild.removeRoleFromMember(member, goodYaka.get(0)).queue();
            }

            // Delete everything in the channel && remove bmPro role from everyone
            for (Message message : messages) {
                channel.purgeMessages(message);
                try {
                    Thread.sleep(50); // Delete a message every 50 ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Header - Hall of Fame
            StringBuilder hallOfFame = new StringBuilder("**Hall of Fame:**\n\n\nFastest #beastmaster-durzag kills:\n");
            int counter = 1;

            // BM - times
            for (List row : values) {
                if (counter > 3) break;
                hallOfFame.append("#").append(counter).append(": **").append(row.get(0)).append("**\n");
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
                Thread.sleep(300); // Post Hall of Fame and take a small break
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

            channel.sendMessage(hallOfShame.toString()).queue();
            try {
                Thread.sleep(300); // Hall of shame then break
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Last part.
            StringBuilder bestBMTeam = new StringBuilder("---------------------------------------\n\n" +
                    ":trophy: :medal:** Record holding Beastmaster Durzag team **:medal: :trophy:  \n\n");

            // Best BM team
            bestBMTeam.append(spreadsheetImport(56, 65));

            channel.sendMessage(bestBMTeam.toString()).queue();
            try {
                Thread.sleep(2000); // Post team, wait and then retrieve the mentioned members
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Get everyone mentioned in the last message and add them the bm role
            Message message = channel.getHistory().retrievePast(1).complete().get(0);
            List<User> bmPros = message.getMentionedUsers();
            for (User user : bmPros) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                guild.addRoleToMember(user.getId(), goodBM.get(0)).queue();
            }

            try {
                Thread.sleep(300); // Wait a small fraction of a sec
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Best Yaka team
            StringBuilder bestYakaTeam = new StringBuilder("---------------------------------------\n\n" +
                    ":trophy: :medal:** Record holding Yakamaru team **:medal: :trophy: \n\n");

            bestYakaTeam.append(spreadsheetImport(68, 77));

            channel.sendMessage(bestYakaTeam.toString()).queue();
            try {
                Thread.sleep(2000); // Post message and wait a bit. Then retrieve mentioned members and give them the tag
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Get everyone mention and blah, blah, blah (Read line 190)
            List<User> yakaPros = message.getMentionedUsers();
            for (User user : yakaPros) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                guild.addRoleToMember(user.getId(), goodYaka.get(0)).queue();
            }

            channel.sendMessage("Processing done!").queue();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            channel.purgeMessages(channel.getHistory().retrievePast(1).complete());

        }
    }
}
