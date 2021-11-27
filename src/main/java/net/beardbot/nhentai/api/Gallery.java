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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.beardbot.nhentai.api.client.dto.GalleryDto;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Gallery {

    private final GalleryDto galleryDto;

    @Getter private final List<GalleryPage> pages;
    @Getter private final List<Tag> tags;
    @Getter private final Image coverImage;
    @Getter private final Image thumbnail;

    private final Supplier<List<Gallery>> relatedGalleriesSupplier;
    private final Supplier<List<Comment>> commentSupplier;

    private List<Gallery> relatedGalleries;
    private List<Comment> comments;

    public long getId(){
        return galleryDto.getId();
    }

    public String getEnglishTitle(){
        return galleryDto.getTitles().getEnglishTitle();
    }

    public String getJapaneseTitle(){
        return galleryDto.getTitles().getJapaneseTitle();
    }

    public String getPrettyTitle(){
        return galleryDto.getTitles().getPrettyTitle();
    }

    public String getScanLator(){
        return galleryDto.getScanLator();
    }

    public int getNumberOfFavorites(){
        return galleryDto.getNumberOfFavorites();
    }

    public Date getUploadDate(){
        return galleryDto.getUploadDate();
    }

    public List<Gallery> getRelatedGalleries(){
        if (relatedGalleries == null){
            relatedGalleries = relatedGalleriesSupplier.get();
        }
        return relatedGalleries;
    }

    public List<Comment> getComments(){
        if (comments == null){
            comments = commentSupplier.get();
        }
        return comments;
    }
}
