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

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.optionals.OptionalDecoder;
import lombok.SneakyThrows;
import net.beardbot.nhentai.NHentaiApiConfig;
import net.beardbot.nhentai.api.client.GalleryClient;
import net.beardbot.nhentai.api.client.dto.*;
import net.beardbot.nhentai.api.params.SearchParams;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GalleryApi {
    private final NHentaiApiConfig apiConfig;
    private final GalleryClient galleryClient;

    public GalleryApi(NHentaiApiConfig apiConfig) {
        this.apiConfig = apiConfig;
        this.galleryClient = Feign.builder()
                .decoder(new OptionalDecoder(new JacksonDecoder()))
                .decode404()
                .target(GalleryClient.class, apiConfig.getApiBaseUrl());
    }

    public SearchResult search(String query){
        return search(query, SearchParams.builder().build());
    }

    public SearchResult search(String query, SearchParams searchParams){
        var resultDto = galleryClient.search(query, searchParams.getPage(), searchParams.getSortBy().getValue());
        return convert(resultDto, page -> search(query, SearchParams.builder().page(page).sortBy(searchParams.getSortBy()).build()).getGalleries());
    }

    public SearchResult searchByTag(Tag tag){
        return searchByTagId(tag.getId());
    }

    public SearchResult searchByTag(Tag tag, SearchParams searchParams){
        return searchByTagId(tag.getId(), searchParams);
    }

    public SearchResult searchByTagId(long tagId){
        return searchByTagId(tagId, SearchParams.builder().build());
    }

    public SearchResult searchByTagId(long tagId, SearchParams searchParams){
        var resultDto = galleryClient.searchByTagId(tagId, searchParams.getPage(), searchParams.getSortBy().getValue());
        return convert(resultDto, page -> searchByTagId(tagId, SearchParams.builder().page(page).sortBy(searchParams.getSortBy()).build()).getGalleries());
    }

    public Optional<Gallery> getGallery(long galleryId){
        var galleryDto = galleryClient.getGallery(galleryId);
        return galleryDto.map(this::convert);
    }

    @SneakyThrows
    public Gallery getRandomGallery(){
        var randomUrl = apiConfig.getNHentaiBaseUrl() + "/random/";
        var connection = (HttpURLConnection) URI.create(randomUrl).toURL().openConnection();
        connection.setInstanceFollowRedirects(false);

        var redirectLocation = connection.getHeaderField("Location");
        var galleryId = Long.parseLong(redirectLocation.replace("/","").substring(1));

        return getGallery(galleryId).get();
    }

    private SearchResult convert(SearchResultDto searchResultDto, Function<Integer, List<Gallery>> pageResultSupplier){
        var pageResult = searchResultDto.getPageResult().stream().map(this::convert).collect(Collectors.toList());
        return new SearchResult(searchResultDto, pageResult, pageResultSupplier);
    }

    private Gallery convert(GalleryDto galleryDto){
        var pages = convertToPages(galleryDto);
        var cover = getGalleryCover(galleryDto);
        var thumbnail = getGalleryThumbnail(galleryDto);
        var tags = galleryDto.getTagDtos().stream().map(this::convert).collect(Collectors.toList());

        return new Gallery(galleryDto, pages, tags, cover, thumbnail,
                ()->getRelatedGalleries(galleryDto.getId()),
                ()->getGalleryComments(galleryDto.getId()));
    }

    private Tag convert(TagDto tagDto){
        return new Tag(tagDto, TagType.of(tagDto.getType()));
    }

    private List<GalleryPage> convertToPages(GalleryDto galleryDto){
        var pages = new ArrayList<GalleryPage>();
        var pagesImages = galleryDto.getImageInfo().getPages();

        for (int pageNumber = 1; pageNumber <= pagesImages.size(); pageNumber++) {
            var image = getGalleryPage(galleryDto, pageNumber);
            pages.add(new GalleryPage(image, pageNumber));
        }

        return pages;
    }

    private Comment convert(GalleryCommentDto galleryCommentDto){
        var user = convert(galleryCommentDto.getPoster());
        return new Comment(galleryCommentDto, user);
    }

    private User convert(UserDto userDto){
        var image = getAvatarImage(userDto.getAvatarUrl());
        return new User(userDto, image);
    }

    private Image getAvatarImage(String avatarUrl){
        var url = String.format("%s/%s", apiConfig.getImageBaseUrl(), avatarUrl);
        var extension = url.substring(url.lastIndexOf(".") + 1);
        return new Image(url, extension, 0, 0);
    }

    private Image getGalleryCover(GalleryDto galleryDto){
        var imageInfo = galleryDto.getImageInfo().getCover();
        var extension = imageInfo.getImageType().getExtension();
        var url = String.format("%s/galleries/%s/cover.%s", apiConfig.getThumbnailBaseUrl(), galleryDto.getMediaId(), extension);
        return new Image(url, extension, imageInfo.getWidth(), imageInfo.getHeight());
    }

    private Image getGalleryThumbnail(GalleryDto galleryDto){
        var imageInfo = galleryDto.getImageInfo().getThumbnail();
        var extension = imageInfo.getImageType().getExtension();
        var url = String.format("%s/galleries/%s/thumb.%s", apiConfig.getThumbnailBaseUrl(), galleryDto.getMediaId(), extension);
        return new Image(url, extension, imageInfo.getWidth(), imageInfo.getHeight());
    }

    private Image getGalleryPage(GalleryDto galleryDto, int page){
        return getGalleryPage(galleryDto.getImageInfo().getPages().get(page - 1), galleryDto.getMediaId(), page);
    }

    private Image getGalleryPage(ImageInfoDto imageInfoDto, String mediaId, int page){
        var extension = imageInfoDto.getImageType().getExtension();
        var url = String.format("%s/galleries/%s/%d.%s", apiConfig.getImageBaseUrl(), mediaId, page, extension);
        return new Image(url, extension, imageInfoDto.getWidth(), imageInfoDto.getHeight());
    }

    private List<Gallery> getRelatedGalleries(long galleryId){
        return galleryClient.getRelatedGalleries(galleryId).getResult().stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private List<Comment> getGalleryComments(long galleryId){
        return galleryClient.getGalleryComments(galleryId).stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }
}
