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
import net.beardbot.nhentai.api.client.dto.TagDto;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Tag {

    private final TagDto tagDto;
    @Getter private final TagType type;

    public long getId(){
        return tagDto.getId();
    }

    public String getName(){
        return tagDto.getName();
    }

    public String getPath(){
        return tagDto.getUrl();
    }

    public long getGalleryCount(){
        return tagDto.getGalleryCount();
    }
}