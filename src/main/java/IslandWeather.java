public class IslandWeather {
    public Island island;
    public WeatherEntry[] weatherEntries;

    private Integer currentWeatherIndex;

    private WindDirection currentDirection;

    public IslandWeather(Island island, WeatherEntry[] weatherEntries) {
        this.island = island;
        this.weatherEntries = weatherEntries;
    }

    public WeatherEntry getNextWeatherEntry() {
        Integer newIndex;
        if(currentWeatherIndex != null && Util.rollDiceWithPercentage(80)) {
            newIndex = currentWeatherIndex + Util.nextInt(11) - 5;
            while(newIndex < 0) {
                newIndex = newIndex + weatherEntries.length;
            }
            if(newIndex >= weatherEntries.length) {
                newIndex = newIndex % weatherEntries.length;
            }
        }
        else {
            newIndex = Util.nextInt(weatherEntries.length);
        }
        this.currentWeatherIndex = newIndex;
        return weatherEntries[newIndex];
    }

    public WindDirection getNextWindDirection() {
        Integer currentDirectionInt;
        if(currentDirection != null && Util.rollDiceWithPercentage(80)) {
            currentDirectionInt = currentDirection.ordinal() + Util.nextInt(5) - 2;
            if(currentDirectionInt < 0) {
                currentDirectionInt = currentDirectionInt + WindDirection.values().length;
            }
            if(currentDirectionInt >= WindDirection.values().length) {
                currentDirectionInt = currentDirectionInt % WindDirection.values().length;
            }
        }
        else {
            currentDirectionInt = Util.nextInt(8);
        }

        currentDirection = WindDirection.values()[currentDirectionInt];
        return currentDirection;
    }
}
