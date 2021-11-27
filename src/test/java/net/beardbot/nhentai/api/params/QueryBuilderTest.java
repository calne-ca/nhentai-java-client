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
package net.beardbot.nhentai.api.params;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class QueryBuilderTest {

    @Test
    void emptyQuery() {
        var query = Query.builder().build();
        assertThat(query.isEmpty());
    }

    @Test
    void keyword() {
        assertThat(Query.builder().withKeyword("k1").build()).isEqualTo("\"k1\"");
        assertThat(Query.builder().withoutKeyword("k1").build()).isEqualTo("-\"k1\"");
    }

    @Test
    void tag() {
        assertThat(Query.builder().withTag("t1").build()).isEqualTo("tag:\"t1\"");
        assertThat(Query.builder().withoutTag("t1").build()).isEqualTo("-\"tag:t1\"");
    }

    @Test
    void artist() {
        assertThat(Query.builder().withArtist("a1").build()).isEqualTo("artist:\"a1\"");
        assertThat(Query.builder().withoutArtist("a1").build()).isEqualTo("-\"artist:a1\"");
    }

    @Test
    void group() {
        assertThat(Query.builder().withGroup("g1").build()).isEqualTo("group:\"g1\"");
        assertThat(Query.builder().withoutGroup("g1").build()).isEqualTo("-\"group:g1\"");
    }

    @Test
    void parody() {
        assertThat(Query.builder().withParody("p1").build()).isEqualTo("parodies:\"p1\"");
        assertThat(Query.builder().withoutParody("p1").build()).isEqualTo("-\"parodies:p1\"");
    }

    @Test
    void category() {
        assertThat(Query.builder().withCategory(Category.MISC).build()).isEqualTo("category:\"misc\"");
        assertThat(Query.builder().withoutCategory(Category.MISC).build()).isEqualTo("-\"category:misc\"");
    }

    @Test
    void language() {
        assertThat(Query.builder().withLanguage(Language.JAPANESE).build()).isEqualTo("language:\"japanese\"");
        assertThat(Query.builder().withoutLanguage(Language.JAPANESE).build()).isEqualTo("-\"language:japanese\"");
    }

    @Test
    void pages() {
        assertThat(Query.builder().withPages(10).build()).isEqualTo("pages:10");
        assertThat(Query.builder().withPagesMaximum(10).build()).isEqualTo("pages:<=10");
        assertThat(Query.builder().withPagesMinimum(10).build()).isEqualTo("pages:>9");
    }

    @Test
    void uploaded() {
        assertUploadedQueryHasDuration(Duration.ofHours(0), "2h");
        assertUploadedQueryHasDuration(Duration.ofHours(1), "2h");
        assertUploadedQueryHasDuration(Duration.ofHours(3), "3h");

        assertUploadedQueryHasDuration(Duration.ofDays(1), "24h");
        assertUploadedQueryHasDuration(Duration.ofDays(3), "3d");
        assertUploadedQueryHasDuration(Duration.ofDays(3).plusHours(1), "73h");

        assertUploadedQueryHasDuration(Duration.ofHours(24), "24h");
        assertUploadedQueryHasDuration(Duration.ofHours(48), "2d");
        assertUploadedQueryHasDuration(Duration.ofDays(500), "500d");
    }

    private void assertUploadedQueryHasDuration(Duration duration, String expectedDuration){
        assertThat(Query.builder().uploadedAfter(duration).build()).isEqualTo("uploaded:<" + expectedDuration);
        assertThat(Query.builder().uploadedBefore(duration).build()).isEqualTo("uploaded:>" + expectedDuration);
    }

    @Test
    void complexQuery() {
        var query = Query.builder()
                .withKeyword("k1").withoutKeyword("k2")
                .withTag("t1").withoutGroup("g2")
                .withPagesMinimum(5)
                .build();

        assertThat(query).isEqualTo("\"k1\" tag:\"t1\" pages:>4 -\"k2\" -\"group:g2\"");
    }
}