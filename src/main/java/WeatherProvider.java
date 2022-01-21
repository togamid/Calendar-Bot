import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeatherProvider {
    Island[] islands;
    Season[] seasons;
    IslandWeather[] islandWeathers;
    public WeatherProvider(String pathToFolder) {
            islands = getData(pathToFolder + File.separator + "islands.csv").stream().map((line) -> {
                if (line.length == 3) {
                    return new Island(line[0], Integer.parseInt(line[1].trim()), Integer.parseInt(line[2].trim()));
                } else {
                    throw new RuntimeException("Invalid island");
                }
            }).toArray(Island[]::new);

            seasons = getData(pathToFolder + File.separator + "seasons.csv").stream().map((line) -> {
                if(line.length == 2) {
                    return new Season(line[0].trim(), Integer.parseInt(line[1].trim()));
                }
                else {
                    for(String elem : line) {
                        System.out.println(elem);
                    }
                    throw new RuntimeException("Couldn't map line with length " + line.length +" to a season: ");
                }
            }).toArray(Season[]::new);

            List<IslandWeather> islandWeathers = new ArrayList<>();
            for(Island island : islands) {
                WeatherEntry[] weatherEntries = getData(pathToFolder + File.separator + island.name + ".csv")
                        .stream()
                        .map((line) -> {
                            if(line.length == 3) {
                                return new WeatherEntry(line[0].trim(), Integer.parseInt(line[1].trim()),
                                        WindStrength.fromValue(line[2].trim()));
                            }
                            else {
                                for(String elem : line) {
                                    System.out.println(elem);
                                }
                                throw new RuntimeException("Couldn't map line with length " + line.length + " in file " +
                                        island.name +".csv to a weather entry");
                            }
                        }).toArray(WeatherEntry[]::new);
                islandWeathers.add(new IslandWeather(island, weatherEntries));
            }
            this.islandWeathers = islandWeathers.toArray(IslandWeather[]::new);
    }

    public Weather[] nextWeatherAllIslands(String seasonName) {
        Season season = Arrays.stream(this.seasons).filter(season1 -> season1.name.toLowerCase().contains(seasonName.toLowerCase()))
                .findFirst().orElseThrow(() -> {return new RuntimeException("Couldn't find season: " + seasonName);});

        return Arrays.stream(this.islandWeathers).map(islandWeather -> getNextWeather(season, islandWeather))
                .toArray(Weather[]::new);
    }

    public Weather getNextWeather(Season season, IslandWeather islandWeather) {
        Weather weather = new Weather();
        weather.islandName = islandWeather.island.name;
        WeatherEntry weatherEntry = islandWeather.getNextWeatherEntry();
        Integer minTempF = islandWeather.island.min_temp + season.modifier +
                weatherEntry.temp_modifier + Util.nextInt(41) - 20;
        Integer maxTempF = islandWeather.island.max_temp + season.modifier +
                weatherEntry.temp_modifier + Util.nextInt(41) - 20;

        if(minTempF > maxTempF) {
            Integer tmp = maxTempF;
            maxTempF = minTempF;
            minTempF = tmp;
        }
        weather.minTempF = minTempF;
        weather.maxTempF = maxTempF;

        weather.minTempC = Util.fahrenheitToCelsius(weather.minTempF);
        weather.maxTempC = Util.fahrenheitToCelsius(weather.maxTempF);

        String description = weatherEntry.name;
        if(description.contains("rain_snow")){
            String target = "rain_snow";
            if(weather.minTempC < 0 && weather.maxTempC < 0) {
                description = description.replace(target, "Snow");
            }
            else  if(weather.minTempC > 0 && weather.maxTempC > 0) {
                description = description.replace(target, "Rain");
            }
            else {
                description = description.replace(target, "Sleet");
            }
        }

        if(description.contains("hurricane_blizzard")){
            String target = "hurricane_blizzard";
            if(weather.minTempC > 0 && weather.maxTempC > 0) {
                description = description.replace(target, "Hurricane");
            }
            else {
                description = description.replace(target, "Blizzard");
            }
        }

        // TODO: handle other special types of weather
        // TODO: hurricane_blizzard

        weather.description = description;

        WindStrength windStrength = weatherEntry.windStrength;
        if(windStrength == null) {
            windStrength = WindStrength.values()[Util.nextInt(WindStrength.values().length - 1)+1];
        }
        else {
            Integer newWindStrength;
            do {
                newWindStrength = windStrength.ordinal() + Util.nextInt(3)-1;
            } while (newWindStrength < 0 || newWindStrength >= WindStrength.values().length);
            windStrength = WindStrength.values()[newWindStrength];
        }
        weather.windStrength = windStrength;

        weather.windDirection = islandWeather.getNextWindDirection();

        return  weather;
    }

    private List<String[]> getData(String path){
        try {
            Reader reader = new BufferedReader(new FileReader(path));
            CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(new CSVParserBuilder().withSeparator(',').build()).build();
            List<String[]> list = csvReader.readAll();
            return list;
        } catch (Exception e) {
            throw new RuntimeException("Couldn't read csv at: " + path);
        }
    }
}
