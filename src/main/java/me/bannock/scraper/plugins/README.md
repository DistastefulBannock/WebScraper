# Main uses for plugins
Plugin guice modules are able to override any service in the scraper. However, the main uses for plugins are the following:
1. Add data savers, which is what handles scraped data
2. Add crawlers, which processes the scraping of the data
3. Add link providers, which tells the crawler which links to scrape

# Getting started with plugin development
1. Create a new Maven project and add the following repository and dependency:
```
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>
```
```
<dependency>
    <groupId>com.github.distastefulbannock</groupId>
    <artifactId>WebScraper</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```
2. Create a main class that extends off of `me.bannock.scraper.plugins.api.Plugin`. This class is used to provide the plugin loader with your plugin's guice module(s). It is not created by guice and so you cannot inject dependencies into it.
3. Create a file named `plug.json` in the root of your jar's resources. The json file should contain the following:
```
{
  "name": "Your plugin name",
  "version": "Your plugin version",
  "pluginMain": "Path to main plugin class"
}
```
Once filled out, it should look something like this:
```
{
  "name": "Dutchie Scraper Plugin",
  "version": "1.0.0-TESTING",
  "pluginMain": "me.bannock.scraper.dutchie.DutchieScraper"
}
```
4. Compile and package the plugin into a jar. Then, move the jar to the plugins directory in your scraper's working directory.
5. Once the scraper is run, you should see the plugin get loaded in the logs

## Where to go from there
You're now ready to write your own plugins.
The following are how-tos for some of the main things you may want to add:
1. Crawlers
2. Link providers
3. Data savers
4. OptionManager

### Crawlers
There is one `Crawler` used per scraper instance. You can configure which Crawler to use in the scraper's `config.json` file located in the scraper's working directory

This crawler will receive links in its `crawlLink(Ljava/lang/String;)` method. It is expected for the crawler to block the thread in this method while it scrapes for data

The crawler can send data to the scraper's `DataService` for storage using its `saveLine(Ljava/lang/String;)` method. You can get the `DataService` instance by injecting it into your crawler's constructor using guice

You can register a crawler by adding the following to your plugin's guice module's `configure` method:
```
Multibinder<Crawler> crawlerMultibinder = Multibinder.newSetBinder(binder(), Crawler.class);
crawlerMultibinder.addBinding().to(YourCrawlerImpl.class);
```

### Link providers
The scraper's `LinkService` will manage all `LinkProvider` instances. The service will go through all providers until each one returns `false` from its `hasMoreLinks()` method. Once this method returns false, the provider will never be queried for links again.

Links from these providers will be passed to the scraper's crawler for processing 

You can register a link provider by adding the following to your plugin's guice module's `configure` method:
```
Multibinder<LinkProvider> linkProviderMultibinder = Multibinder.newSetBinder(binder(), LinkProvider.class);
linkProviderMultibinder.addBinding().to(YourLinkProviderImpl.class);
```

### Data savers
The scraper's `DataService` will manage all `DataSaver` instances. Every time a scraped line is "saved", it will be sent to each registered `DataSaver` instance for storage

You can register a data saver by adding the following to your plugin's guice module's `configure` method:
```
Multibinder<DataSaver> dataSaverMultibinder = Multibinder.newSetBinder(binder(), DataSaver.class);
dataSaverMultibinder.addBinding().to(YourDataSaverImpl.class);
```

### OptionManager
You're able to inject an instance of the `OptionManager` into the constructors of scraper-created objects. This service is useful for adding configuration values in the scraper's `config.json` file for use in your own code.

Option keys should be unique so that it does not clash with other keys. It is recommended to use values such as `yourGroup.yourPlugin.awesomeString1`. It is also recommended to create constants for the option keys.

Here is an example of how to use the option manager in your own code
```
if (optionManager.getVariable(YourConfigKeys.awesomeString1).isEmpty()) {
    optionManager.setVariable(YourConfigKeys.awesomeString1, "default value");
    optionManager.saveVariables(); // Saves updated config
}
String awesomeString = optionManager.getVariable(YourConfigKeys.awesomeString1).get();
```