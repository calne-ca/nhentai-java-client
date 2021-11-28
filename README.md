<p align="center">
  <a href="https://search.maven.org/artifact/net.beardbot/nhentai-client"><img src="https://maven-badges.herokuapp.com/maven-central/net.beardbot/nhentai-client/badge.svg"></a>
  <a href="https://github.com/calne-ca/nhentai-java-client/actions?query=workflow%3ABuild"><img src="https://github.com/calne-ca/nhentai-java-client/workflows/Build/badge.svg"></a>
  <a href="https://codecov.io/gh/calne-ca/nhentai-java-client"><img src="https://codecov.io/gh/calne-ca/nhentai-java-client/branch/master/graph/badge.svg?token=014VNVNCYN"></a>
  <br>
  <a href="https://nhentai.net"><img src="https://static.nhentai.net/img/logo.090da3be7b51.svg" width="128" height="128" /></a>
</p>

# NHentai Java Client

This is a Java client providing access to [nhentai](https://nhentai.net) using nhentai's REST API as well as web scraping.
You can use this client to search for galleries (manga, doujinshi etc.), download pages, browse through tags among other things.
See [API Overview](#api-overview) and [Usage](#usage) for more details.

## API Overview

| API       | Operation        | Description                              |
|-----------|------------------|------------------------------------------|
| Galleries | search           | Search for galleries by query            |
|           | searchByTag      | Search for galleries by tag              |
|           | searchByTagId    | Search for galleries by tag ID           |
|           | getGallery       | Get gallery for a specific gallery ID    |
|           | getRandomGallery | Get a random gallery                     |
| Tags      | getTags          | Get a list of tags (paginated)           |
|           | getAllTags       | Get a list of all tags                   |
|           | getParodies      | Get a list of parody tags (paginated)    |
|           | getAllParodies   | Get a list of all parody tags            |
|           | getArtists       | Get a list of artist tags (paginated)    |
|           | getAllArtists    | Get a list of all artist tags            |
|           | getCharacters    | Get a list of character tags (paginated) |
|           | getAllCharacters | Get a list of all character tags         |
|           | getGroups        | Get a list of group tags (paginated)     |
|           | getAllGroups     | Get a list of all group tags             |

You can also get gallery images, comments etc., but those are part of the *Gallery* object returned by the Galleries API.
See [API Usage Examples](#api-usage-examples) for reference.

## Usage

### Initialize NHentai:
````java
var nHentai = new NHentai();
````

#### Changing global API options:
You can provide a *NHentaiApiConfig* when creating the *NHentai* instance.
Currently, the api config contains URLs and some throttling / retry options used by the tags API.
````java
var apiConfig = NHentaiApiConfig.builder().requestDelayMs(200).build();
var nHentai = new NHentai(apiConfig);
````

### API usage Examples

#### Get gallery by ID:
````java
var gallery = nHentai.galleries().getGallery(177013);
````
This returns an *Optional*, because depending on where you got th ID from there might not be any gallery with that ID.

#### Get and print gallery comments:
````java
for (Comment comment : gallery.getComments()) {
    var userName = comment.getUser().getUserName();
    System.out.printf("[%s] %s: %s%n", comment.getPostDate().toString(), userName, comment.getText());
}
````

#### Get and print gallery tags:
````java
var groupedTags = gallery.getTags().stream().collect(Collectors.groupingBy(Tag::getType));

groupedTags.forEach((type, tags)-> {
    var tagString = tags.stream().map(Tag::getName).collect(Collectors.joining(","));
    System.out.printf("%s: %s%n", type.getValue(), tagString);
});
````

#### Download gallery pages:
````java
gallery.getPages().forEach(page->{
    var image = page.getImage();
    var file = new File(page.getNumber() + "." + image.getFileExtension());

    try {
        Files.copy(image.toInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
        e.printStackTrace();
    }
});
````

#### Search for galleries and print pretty title:
````java
var query = "tag:sister language:english -tag:netorare uploaded:<30d";

var result = nHentai.galleries().search(query);
result.getPageResult().forEach(g-> System.out.println(g.getPrettyTitle()));
````
The query has the same format as a query you would put into the search bar of nhentai.
See [nhentai documentation](https://nhentai.net/info/) for the syntax of these queries.
If you don't want to create these queries manually you can use the *QueryBuilder*.
See [Create queries with Query Builder](#create-queries-with-query-builder) for an example using the same query.


##### Search for galleries with search params:
````java
var query = "tag:sister language:english -tag:netorare uploaded:<30d";
var params = SearchParams.builder().page(2).sortBy(SortBy.RECENT).build();

var result = nHentai.galleries().search(query, params);
````

##### Create queries with Query Builder:
````java
var query = Query.builder()
    .withTag("sister")
    .withLanguage(Language.ENGLISH)
    .withoutTag("netorare")
    .uploadedAfter(Duration.ofDays(30))
    .build();

var result = nHentai.galleries().search(query);
````

##### Get search results from other pages:
````java
var result = nHentai.galleries().search("artist:Rustle");
var galleries = result.getGalleries();

for (int i = 2; i < result.getNumberOfPages(); i++) {
    galleries.addAll(result.getGalleriesOnPage(i));
}
````

Note that the *search* methods always return paginated results.
The *getGalleries()* method will return the galleries of the page you specified in the search params.
If you don't provide a page search param the galleries from page 1 will be returned.
Regardless of what page you set in the search params, you can get the galleries for any page of your search result using the *getGalleriesOnPage()* method.

You can also use this to iterate over all pages to get all galleries of your search result.
Be careful when doing this though since there might be a lot of results.
The client does not throttle automatically in this case, so this might result in 429 (Too Many Requests) errors.

#### Get tags
````java
var tags = nHentai.tags().getTags();
tags.forEach(tag-> System.out.println(tag.getName()));

var artistTags = nHentai.tags().getArtists(TagSearchParams.builder().page(3).sortBy(TagSortBy.POPULAR).build());
artistTags.forEach(tag-> System.out.println(tag.getName()));
````
These methods will return tags for the page you specify in the search params.
If you don't specify a page it will default to page 1.
If you want to get all tags use [Get all tags](#get-all-tags) instead.


#### Get all tags
````java
var allTheTags = nHentai.tags().getAllTags();
allTheTags.forEach(tag-> System.out.println(tag.getName()));

var allTheArtists = nHentai.tags().getAllArtists();
allTheArtists.forEach(tag-> System.out.println(tag.getName()));
````
These methods basically iterate through all tag pages and fetch the tags for each page.
That means that if there are 50 pages of tags there will be 50 requests to nhentai.
All methods of the tag api will automatically throttle themselves for subsequent HTTP calls.
If nhentai responds with a HTTP 429 (Too Many Requests) error the client will attempt up to 3 retries after waiting for 500ms.
The throttling, max retries and retry delay can also be configured in the *NHentaiApiConfig*.