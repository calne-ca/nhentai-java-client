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
package net.beardbot.nhentai;

import com.github.tomakehurst.wiremock.WireMockServer;
import net.beardbot.nhentai.api.TagType;
import net.beardbot.nhentai.api.params.SearchParams;
import net.beardbot.nhentai.api.params.SortBy;
import net.beardbot.nhentai.api.params.TagSearchParams;
import net.beardbot.nhentai.api.params.TagSortBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockUriResolver;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("OptionalGetWithoutIsPresent")
@ExtendWith({
        WiremockResolver.class,
        WiremockUriResolver.class
})
class NHentaiApiTest {

    private String wireMockUri;
    private NHentai nHentai;
    private NHentaiApiConfig apConfig;

    @BeforeEach
    void setUp(@WiremockResolver.Wiremock WireMockServer server, @WiremockUriResolver.WiremockUri String uri) {
        this.wireMockUri = uri;
        this.apConfig = NHentaiApiConfig.builder()
                .apiBaseUrl(wireMockUri + "/api")
                .imageBaseUrl(wireMockUri + "/i")
                .thumbnailBaseUrl(wireMockUri + "/t")
                .nHentaiBaseUrl(wireMockUri)
                .build();
        this.nHentai = new NHentai(apConfig);
    }

    @Test
    void getGallery() throws IOException {
        var gallery = nHentai.galleries().getGallery(177013).get();
        assertThat(gallery.getId()).isEqualTo(177013);
        assertThat(gallery.getScanLator()).isEqualTo("someValue");
        assertThat(gallery.getNumberOfFavorites()).isEqualTo(44548);
        assertThat(gallery.getUploadDate()).isEqualTo(Date.from(Instant.ofEpochSecond(1476793729)));
        assertThat(gallery.getEnglishTitle()).isEqualTo("[ShindoLA] METAMORPHOSIS (Complete) [English]");
        assertThat(gallery.getJapaneseTitle()).isEqualTo("someValue");
        assertThat(gallery.getPrettyTitle()).isEqualTo("METAMORPHOSIS");

        var coverImage = gallery.getCoverImage();
        assertThat(coverImage.getFileExtension()).isEqualTo("jpg");
        assertThat(coverImage.getWidth()).isEqualTo(350);
        assertThat(coverImage.getHeight()).isEqualTo(506);
        assertThat(coverImage.getDownloadUrl()).isEqualTo(apConfig.getThumbnailBaseUrl() + "/galleries/987560/cover.jpg");
        assertThat(coverImage.toInputStream().readAllBytes()).containsSequence(0x4A, 0x46, 0x49, 0x46);

        var thumbnail = gallery.getThumbnail();
        assertThat(thumbnail.getFileExtension()).isEqualTo("jpg");
        assertThat(thumbnail.getWidth()).isEqualTo(250);
        assertThat(thumbnail.getHeight()).isEqualTo(362);
        assertThat(thumbnail.getDownloadUrl()).isEqualTo(apConfig.getThumbnailBaseUrl() + "/galleries/987560/thumb.jpg");
        assertThat(thumbnail.toInputStream().readAllBytes()).containsSequence(0x4A, 0x46, 0x49, 0x46);

        var pages = gallery.getPages();
        assertThat(pages).hasSize(225);

        var firstPage = pages.get(0);
        assertThat(firstPage.getNumber()).isEqualTo(1);

        var firstPageImage = firstPage.getImage();
        assertThat(firstPageImage.getFileExtension()).isEqualTo("jpg");
        assertThat(firstPageImage.getWidth()).isEqualTo(1275);
        assertThat(firstPageImage.getHeight()).isEqualTo(1844);
        assertThat(firstPageImage.getDownloadUrl()).isEqualTo(apConfig.getImageBaseUrl() + "/galleries/987560/1.jpg");
        assertThat(firstPageImage.toInputStream().readAllBytes()).containsSequence(0x4A, 0x46, 0x49, 0x46);

        var tags = gallery.getTags();
        assertThat(tags).hasSize(31);
        assertThat(tags.get(0).getId()).isEqualTo(19018);
        assertThat(tags.get(0).getType()).isEqualTo(TagType.TAG);
        assertThat(tags.get(0).getName()).isEqualTo("dark skin");
        assertThat(tags.get(0).getPath()).isEqualTo("/tag/dark-skin/");
        assertThat(tags.get(0).getGalleryCount()).isEqualTo(21056);

        var relatedGalleries = gallery.getRelatedGalleries();
        assertThat(relatedGalleries).hasSize(5);
        assertThat(relatedGalleries.get(0).getId()).isEqualTo(160816);

        var comments = gallery.getComments();
        assertThat(comments).hasSize(2);

        var comment = comments.get(0);
        assertThat(comment.getId()).isEqualTo(2165205);
        assertThat(comment.getPostDate()).isEqualTo(Date.from(Instant.ofEpochSecond(1637961735)));
        assertThat(comment.getText()).isEqualTo("Damn this messed me up before, couldn't stop thinking about this for weeks");

        var  user = comment.getUser();
        assertThat(user.getId()).isEqualTo(2468150);
        assertThat(user.getUserName()).isEqualTo("User1");
        assertThat(user.getSlug()).isEqualTo("user1");
        assertThat(user.isSuperUser()).isTrue();
        assertThat(user.isStaff()).isTrue();

        var avatar = user.getAvatar();
        assertThat(avatar.getFileExtension()).isEqualTo("png");
        assertThat(avatar.getWidth()).isEqualTo(0);
        assertThat(avatar.getHeight()).isEqualTo(0);
        assertThat(avatar.getDownloadUrl()).isEqualTo(apConfig.getImageBaseUrl() + "/avatars/user1.png");
        assertThat(avatar.toInputStream().readAllBytes()).containsSequence(0xD, 0x49, 0x48, 0x44);
    }

    @Test
    void getRandomGallery() {
        var gallery = nHentai.galleries().getRandomGallery();
        assertThat(gallery.getId()).isEqualTo(177013);
    }

    @Test
    void searchGalleries(){
        var result = nHentai.galleries().search("artist:tsuttsu", SearchParams.builder().page(4).sortBy(SortBy.RECENT).build());
        assertThat(result.getNumberOfPages()).isEqualTo(5);

        var resultGalleries = result.getGalleries();
        assertThat(resultGalleries).hasSize(25);
        assertThat(resultGalleries.get(0).getId()).isEqualTo(146896);

        var nextPageGalleries = result.getGalleriesOnPage(5);
        assertThat(nextPageGalleries).hasSize(6);
        assertThat(nextPageGalleries.get(0).getId()).isEqualTo(109527);
    }

    @Test
    void searchGalleriesByTag(){
        var tags = nHentai.tags().getTags(TagSearchParams.builder().build());
        var tag = tags.get(1);

        var result = nHentai.galleries().searchByTag(tag, SearchParams.builder().page(2).build());
        assertThat(result.getNumberOfPages()).isEqualTo(3);

        var resultGalleries = result.getGalleries();
        assertThat(resultGalleries).hasSize(25);
        assertThat(resultGalleries.get(4).getId()).isEqualTo(292971);

        var nextPageGalleries = result.getGalleriesOnPage(3);
        assertThat(nextPageGalleries).hasSize(15);
        assertThat(nextPageGalleries.get(6).getId()).isEqualTo(302947);
    }

    @Test
    void searchGalleriesByTagId(){
        var result = nHentai.galleries().searchByTagId(80551, SearchParams.builder().page(2).build());
        assertThat(result.getNumberOfPages()).isEqualTo(3);

        var resultGalleries = result.getGalleries();
        assertThat(resultGalleries).hasSize(25);
        assertThat(resultGalleries.get(4).getId()).isEqualTo(292971);

        var nextPageGalleries = result.getGalleriesOnPage(3);
        assertThat(nextPageGalleries).hasSize(15);
        assertThat(nextPageGalleries.get(6).getId()).isEqualTo(302947);
    }

    @Test
    void getTags() {
        var tags = nHentai.tags().getTags(TagSearchParams.builder().build());

        assertThat(tags).hasSize(120);

        var tag = tags.get(2);
        assertThat(tag.getId()).isEqualTo(135963);
        assertThat(tag.getName()).isEqualTo("6channel");
        assertThat(tag.getType()).isEqualTo(TagType.TAG);
        assertThat(tag.getPath()).isEqualTo("/tag/6channel/");
        assertThat(tag.getGalleryCount()).isEqualTo(2);
    }

    @Test
    void getTags_sortByPopular() {
        var tags = nHentai.tags().getTags(TagSearchParams.builder().page(5).sortBy(TagSortBy.POPULAR).build());

        assertThat(tags).hasSize(120);

        var tag = tags.get(2);
        assertThat(tag.getId()).isEqualTo(17337);
        assertThat(tag.getName()).isEqualTo("stretching");
        assertThat(tag.getType()).isEqualTo(TagType.TAG);
        assertThat(tag.getPath()).isEqualTo("/tag/stretching/");
        assertThat(tag.getGalleryCount()).isEqualTo(111);
    }

    @Test
    void getAllTags() {
        var tags = nHentai.tags().getAllTags();
        assertThat(tags).hasSize(240);

        assertThat(tags.get(0).getId()).isEqualTo(134818);
        assertThat(tags.get(120).getId()).isEqualTo(10794);
    }

    @Test
    void getArtists() {
        var tags = nHentai.tags().getArtists(TagSearchParams.builder().build());

        assertThat(tags).hasSize(120);

        var tag = tags.get(5);
        assertThat(tag.getId()).isEqualTo(110062);
        assertThat(tag.getName()).isEqualTo("02junks");
        assertThat(tag.getType()).isEqualTo(TagType.ARTIST);
        assertThat(tag.getPath()).isEqualTo("/artist/02junks/");
        assertThat(tag.getGalleryCount()).isEqualTo(1);
    }

    @Test
    void getArtists_sortByPopular() {
        var tags = nHentai.tags().getArtists(TagSearchParams.builder().page(144).sortBy(TagSortBy.POPULAR).build());

        assertThat(tags).hasSize(120);

        var tag = tags.get(3);
        assertThat(tag.getId()).isEqualTo(53880);
        assertThat(tag.getName()).isEqualTo("osa");
        assertThat(tag.getType()).isEqualTo(TagType.ARTIST);
        assertThat(tag.getPath()).isEqualTo("/artist/osa/");
        assertThat(tag.getGalleryCount()).isEqualTo(2);
    }

    @Test
    void getAllArtists() {
        var tags = nHentai.tags().getAllArtists();
        assertThat(tags).hasSize(240);

        assertThat(tags.get(0).getId()).isEqualTo(6727);
        assertThat(tags.get(120).getId()).isEqualTo(115053);
    }

    @Test
    void getCharacters() {
        var tags = nHentai.tags().getCharacters(TagSearchParams.builder().build());

        assertThat(tags).hasSize(120);

        var tag = tags.get(3);
        assertThat(tag.getId()).isEqualTo(8665);
        assertThat(tag.getName()).isEqualTo("2k-tan");
        assertThat(tag.getType()).isEqualTo(TagType.CHARACTER);
        assertThat(tag.getPath()).isEqualTo("/character/2k-tan/");
        assertThat(tag.getGalleryCount()).isEqualTo(19);
    }

    @Test
    void getCharacters_sortByPopular() {
        var tags = nHentai.tags().getCharacters(TagSearchParams.builder().page(8).sortBy(TagSortBy.POPULAR).build());

        assertThat(tags).hasSize(120);

        var tag = tags.get(1);
        assertThat(tag.getId()).isEqualTo(22352);
        assertThat(tag.getName()).isEqualTo("sunny milk");
        assertThat(tag.getType()).isEqualTo(TagType.CHARACTER);
        assertThat(tag.getPath()).isEqualTo("/character/sunny-milk/");
        assertThat(tag.getGalleryCount()).isEqualTo(89);
    }

    @Test
    void getAllCharacters() {
        var tags = nHentai.tags().getAllCharacters();
        assertThat(tags).hasSize(240);

        assertThat(tags.get(0).getId()).isEqualTo(127235);
        assertThat(tags.get(120).getId()).isEqualTo(52358);
    }

    @Test
    void getParodies() {
        var tags = nHentai.tags().getParodies(TagSearchParams.builder().build());

        assertThat(tags).hasSize(120);

        var tag = tags.get(1);
        assertThat(tag.getId()).isEqualTo(13128);
        assertThat(tag.getName()).isEqualTo("07-ghost");
        assertThat(tag.getType()).isEqualTo(TagType.PARODY);
        assertThat(tag.getPath()).isEqualTo("/parody/07-ghost/");
        assertThat(tag.getGalleryCount()).isEqualTo(4);
    }

    @Test
    void getParodies_popular() {
        var tags = nHentai.tags().getParodies(TagSearchParams.builder().page(3).sortBy(TagSortBy.POPULAR).build());

        assertThat(tags).hasSize(120);

        var tag = tags.get(2);
        assertThat(tag.getId()).isEqualTo(14755);
        assertThat(tag.getName()).isEqualTo("tekken");
        assertThat(tag.getType()).isEqualTo(TagType.PARODY);
        assertThat(tag.getPath()).isEqualTo("/parody/tekken/");
        assertThat(tag.getGalleryCount()).isEqualTo(148);
    }

    @Test
    void getAllParodies() {
        var tags = nHentai.tags().getAllParodies();
        assertThat(tags).hasSize(240);

        assertThat(tags.get(0).getId()).isEqualTo(8285);
        assertThat(tags.get(120).getId()).isEqualTo(30988);
    }

    @Test
    void getGroups() {
        var tags = nHentai.tags().getGroups(TagSearchParams.builder().build());

        assertThat(tags).hasSize(120);

        var tag = tags.get(10);
        assertThat(tag.getId()).isEqualTo(20969);
        assertThat(tag.getName()).isEqualTo("04th heaven");
        assertThat(tag.getType()).isEqualTo(TagType.GROUP);
        assertThat(tag.getPath()).isEqualTo("/group/04th-heaven/");
        assertThat(tag.getGalleryCount()).isEqualTo(2);
    }

    @Test
    void getGroups_popular() {
        var tags = nHentai.tags().getGroups(TagSearchParams.builder().page(106).sortBy(TagSortBy.POPULAR).build());

        assertThat(tags).hasSize(120);

        var tag = tags.get(2);
        assertThat(tag.getId()).isEqualTo(78410);
        assertThat(tag.getName()).isEqualTo("yano");
        assertThat(tag.getType()).isEqualTo(TagType.GROUP);
        assertThat(tag.getPath()).isEqualTo("/group/yano/");
        assertThat(tag.getGalleryCount()).isEqualTo(2);
    }

    @Test
    void getAllGroups() {
        var tags = nHentai.tags().getAllGroups();
        assertThat(tags).hasSize(240);

        assertThat(tags.get(0).getId()).isEqualTo(54671);
        assertThat(tags.get(120).getId()).isEqualTo(30777);
    }
}