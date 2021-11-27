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
package net.beardbot.nhentai.api.client;

import feign.Param;
import feign.RequestLine;
import net.beardbot.nhentai.api.client.dto.GalleryDto;
import net.beardbot.nhentai.api.client.dto.GalleryCommentDto;
import net.beardbot.nhentai.api.client.dto.RelatedGalleriesDto;
import net.beardbot.nhentai.api.client.dto.SearchResultDto;

import java.util.List;
import java.util.Optional;

public interface GalleryClient {

    @RequestLine("GET /gallery/{gallery_id}")
    Optional<GalleryDto> getGallery(@Param("gallery_id") Long galleryId);

    @RequestLine("GET /gallery/{gallery_id}/related")
    RelatedGalleriesDto getRelatedGalleries(@Param("gallery_id") Long galleryId);

    @RequestLine("GET /gallery/{gallery_id}/comments")
    List<GalleryCommentDto> getGalleryComments(@Param("gallery_id") Long galleryId);

    @RequestLine("GET /galleries/search?query={query}&page={page}&sort={sort}")
    SearchResultDto search(@Param("query") String query, @Param("page") Integer page, @Param("sort") String sortBy);

    @RequestLine("GET /galleries/tagged?tag_id={tag_id}&page={page}&sort={sort}")
    SearchResultDto searchByTagId(@Param("tag_id") Long tagId, @Param("page") Integer page, @Param("sort") String sortBy);
}
