import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Bot {
    private static Bot bot;
    private  JDA jda;
    private Config config;
    private MessageChannel channel;
    private final ScheduledExecutorService messageService = Executors.newSingleThreadScheduledExecutor();
    private int current_day = 0;
    private int current_year = 1;
    private List<String[]> data;
    private WeatherProvider weatherProvider;
    private String season = "winter";

    public static Bot getBot(){
        if(bot == null){
            bot = new Bot();
        }
        return bot;
    }

    public String getBotSignifier(){
        return this.config.get("BotSignifier");
    }

    public void setChannel( MessageChannel channel){
        this.channel = channel;
    }

    private Bot() {


        this.config = new Config("config.txt");
        try {
            jda = JDABuilder.createDefault(this.config.get("Token"))
                    .addEventListeners(new EventListeners())
                    .build();
            jda.awaitReady();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        try {
            this.data = getData(this.config.get("DataPath"));
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        if(this.data == null){
            System.out.println("Failed to load data. Aborting!");
        }
        int[] date = this.getStartingYearAndDay("date.txt");
        this.current_day = date[0];
        this.current_year = date[1];

        this.weatherProvider = new WeatherProvider(this.config.get("WeatherPath"));

    }

    public void startBot(){
    scheduleNextMessage();
    }

    private void scheduleNextMessage(){
        long untilMidnight = Duration.between(LocalDateTime.now(), LocalDateTime.of(LocalDate.now().plusDays(1),LocalTime.of(0, 0, 0))).toSeconds();
        //System.out.println(untilMidnight/60);
        //long untilMidnight = 30;
        messageService.schedule(new MessageThread(), untilMidnight, TimeUnit.SECONDS);
    }

    public void sendNextMessage(){
        if(this.current_day >= this.data.size()){
            this.current_day = 0;
            this.current_year++;
        }
        String[] day = this.data.get(this.current_day);
        String output;
        if(!day[2].isEmpty()){
            output = day[1] + day[2] + ", " + day[3] + " " + day[4] + " " + day[5]+ " "+ day[6] + " " +this.current_year+ ". It is " +day[7] + ".";
            this.season = day[7];
        }
        else {
            output = day[1] + " " + day[7];
        }
        System.out.println(output);
        if(channel != null) {
            channel.sendMessage(output).queue();
        }
        this.current_day++;
        this.save("date.txt");
        String weatherMessage ;
        try {
            weatherMessage = getWeatherMessage(this.season);
        } catch(Exception e) {
            weatherMessage = "Weather could not be generated!";
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        System.out.println(weatherMessage);
        if(channel != null) {
            channel.sendMessage(weatherMessage).queue();
        }

        //wait a little to prevent it from sending two messages as the delay is still zero
        try {
            TimeUnit.SECONDS.sleep(5);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        scheduleNextMessage();
    }

    private List<String[]> getData(String path) throws Exception{
        Reader reader = new BufferedReader(new FileReader(path));
        CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(new CSVParserBuilder().withSeparator(',').build() ).build();
        List<String[]> list = csvReader.readAll();
        return list;
    }
    private int[] getStartingYearAndDay(String path){
        int[] result = new int[2];

        try {
            Config config = new Config(path);
            result[0] = Integer.parseInt(config.get("Day"));
            result[1] = Integer.parseInt(config.get("Year"));
            return result;
        } catch (Exception e) {
            return new int[]{0, 1};
        }
    }

    private boolean save(String path){
        try {
            String str = "Day: " + this.current_day + "\nYear: " + this.current_year;
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write(str);

            writer.close();
        }
        catch(Exception e) {
            return false;
        }
        return true;
    }

    // Albadon - Raining, 54°F to 66°F (12°C to 18°C), with moderate winds blowing north
    //Solgard - Sunny, 39°F to 58°F (3°C to 14°C), with low winds blowing southwest
    // TODO: convert the all upper case enums to real names
    private String getWeatherAsString(Weather weather) {
        String windStrengthString = "";
        if(weather.windStrength != WindStrength.NONE) {
            windStrengthString = weather.windStrength.toString().toLowerCase();
        } else {
            windStrengthString = "no";
        }

        String result = String.format("%s - %s, %S°F to %S°F (%S°C to %S °C), with %s winds", weather.islandName,
                weather.description, weather.minTempF, weather.maxTempF, weather.minTempC, weather.maxTempC, windStrengthString);

        String windDirectionString = weather.windDirection.toString().replace('_', ' ').toLowerCase();

        if(weather.windStrength != WindStrength.NONE) {
            result += String.format(", blowing %s", windDirectionString);
        }
        return result;
    }

    private String getWeatherMessage(String season) {
        Weather[] weathers = weatherProvider.nextWeatherAllIslands(season);

        StringBuilder builder = new StringBuilder("Weather report: \n");
        for(Weather weather : weathers) {
            builder.append(getWeatherAsString(weather) + "\n");
        }

        return builder.toString();
    }
}
