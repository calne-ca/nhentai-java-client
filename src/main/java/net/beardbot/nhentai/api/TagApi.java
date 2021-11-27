/**
 * Copyright (c) 2021 Joscha Düringer.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.beardbot.nhentai.api;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.beardbot.nhentai.NHentaiApiConfig;
import net.beardbot.nhentai.api.client.dto.TagDto;
import net.beardbot.nhentai.api.params.TagSearchParams;
import net.beardbot.nhentai.api.params.TagSortBy;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class TagApi {
    private static final String TAGS_PATH = "/tags";
    private static final String PARODIES_PATH = "/parodies";
    private static final String ARTISTS_PATH = "/artists";
    private static final String CHARACTERS_PATH = "/characters";
    private static final String GROUPS_PATH = "/groups";

    private final NHentaiApiConfig apiConfig;

    public List<Tag> getAllTags(){
        return getAllTags(TagType.TAG, TAGS_PATH);
    }

    @SneakyThrows
    public List<Tag> getTags(TagSearchParams params){
        return getTags(params, TagType.TAG, TAGS_PATH);
    }

    public List<Tag> getAllParodies(){
        return getAllTags(TagType.PARODY, PARODIES_PATH);
    }

    @SneakyThrows
    public List<Tag> getParodies(TagSearchParams params){
        return getTags(params, TagType.PARODY, PARODIES_PATH);
    }

    public List<Tag> getAllArtists(){
        return getAllTags(TagType.ARTIST, ARTISTS_PATH);
    }

    @SneakyThrows
    public List<Tag> getArtists(TagSearchParams params){
        return getTags(params, TagType.ARTIST, ARTISTS_PATH);
    }

    public List<Tag> getAllCharacters(){
        return getAllTags(TagType.CHARACTER, CHARACTERS_PATH);
    }

    @SneakyThrows
    public List<Tag> getCharacters(TagSearchParams params){
        return getTags(params, TagType.CHARACTER, CHARACTERS_PATH);
    }

    public List<Tag> getAllGroups(){
        return getAllTags(TagType.GROUP, GROUPS_PATH);
    }

    @SneakyThrows
    public List<Tag> getGroups(TagSearchParams params){
        return getTags(params, TagType.GROUP, GROUPS_PATH);
    }

    @SneakyThrows
    private List<Tag> getAllTags(TagType type, String basePath){
        var page = 1;
        var tags = new ArrayList<Tag>();

        List<Tag> fetchedTags;

        do{
            var params = TagSearchParams.builder().page(page).build();
            fetchedTags = getTagsWithRetry(params, type, basePath, apiConfig.getRequestMaxRetries());
            tags.addAll(fetchedTags);
            page++;

            Thread.sleep(apiConfig.getRequestDelayMs());
        } while (!fetchedTags.isEmpty());

        return tags;
    }

    @SneakyThrows
    private List<Tag> getTagsWithRetry(TagSearchParams params, TagType type, String basePath, int maxRetries){
        try {
            return getTags(params, type, basePath);
        } catch (HttpStatusException e){
            if (e.getStatusCode() == 429 && maxRetries > 0){
                var delay = apiConfig.getRequestRetryDelayMs();
                var retriesLeft = maxRetries - 1;

                log.debug("Too many request. Retrying in {}ms. Retries left: {}", delay, retriesLeft);
                Thread.sleep(delay);

                return getTagsWithRetry(params, type, basePath, retriesLeft);
            }

            throw e;
        }
    }

    private List<Tag> getTags(TagSearchParams params, TagType type, String basePath) throws IOException {
        log.debug("Fetching tags of type '{}' from page {}", type.getValue(), params.getPage());

        var additionalPath = params.getSortBy() == TagSortBy.POPULAR ? "/popular" : "";
        var url = String.format("%s%s%s?page=%d", apiConfig.getNHentaiBaseUrl(), basePath, additionalPath, params.getPage());
        var document = Jsoup.connect(url).get();
        var tagElements = document.getElementsByClass("tag");

        return tagElements.stream().map(t->convertToTag(t,type)).collect(Collectors.toList());
    }

    private static Tag convertToTag(Element tagElement, TagType type){
        var tagName = extractTagName(tagElement);
        var tagId = extractTagId(tagElement);
        var galleryCount = extractGalleryCount(tagElement);
        var tagUrl = extractUrl(tagElement);

        var tagDto = new TagDto();
        tagDto.setId(tagId);
        tagDto.setName(tagName);
        tagDto.setGalleryCount(galleryCount);
        tagDto.setUrl(tagUrl);

        return new Tag(tagDto, type);
    }

    private static String extractTagName(Element tagElement){
        return tagElement.getElementsByClass("name").get(0).text();
    }

    private static String extractUrl(Element tagElement){
        return tagElement.attr("href");
    }

    private static Long extractTagId(Element tagElement){
        return tagElement.classNames().stream()
                .filter(c->c.startsWith("tag-"))
                .map(c->c.replaceFirst("tag-",""))
                .map(Long::valueOf).findFirst().get();
    }

    private static Long extractGalleryCount(Element tagElement){
        var countString = tagElement.getElementsByClass("count").get(0).text().toLowerCase();

        if (countString.contains("k")){
            return Long.parseLong(countString.replace("k","")) * 1000;
        }

        return Long.parseLong(countString);
    }
}
