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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Query {

    public static QueryBuilder builder(){
        return new QueryBuilder();
    }

    public static class QueryBuilder {
        private final List<String> includedParts = new ArrayList<>();
        private final List<String> excludedParts = new ArrayList<>();

        public String build(){
            var sb = new StringBuilder();
            includedParts.forEach(s->sb.append(s).append(" "));
            excludedParts.forEach(s->sb.append("-").append(s).append(" "));
            return sb.toString().trim();
        }

        public QueryBuilder withKeyword(String keyword){
            includedParts.add(createKeywordPart(keyword));
            return this;
        }

        public QueryBuilder withoutKeyword(String keyword){
            excludedParts.add(createKeywordPart(keyword));
            return this;
        }

        public QueryBuilder withTag(String tag){
            return withProperty("tag", tag);
        }

        public QueryBuilder withoutTag(String tag){
            return withoutProperty("tag", tag);
        }

        public QueryBuilder withParody(String parody){
            return withProperty("parodies", parody);
        }

        public QueryBuilder withoutParody(String parody){
            return withoutProperty("parodies", parody);
        }

        public QueryBuilder withCharacter(String character){
            return withProperty("character", character);
        }

        public QueryBuilder withoutCharacter(String character){
            return withoutProperty("character", character);
        }

        public QueryBuilder withArtist(String artist){
            return withProperty("artist", artist);
        }

        public QueryBuilder withoutArtist(String artist){
            return withoutProperty("artist", artist);
        }

        public QueryBuilder withGroup(String group){
            return withProperty("group", group);
        }

        public QueryBuilder withoutGroup(String group){
            return withoutProperty("group", group);
        }

        public QueryBuilder withLanguage(Language language){
            return withProperty("language", language.getValue());
        }

        public QueryBuilder withoutLanguage(Language language){
            return withoutProperty("language", language.getValue());
        }

        public QueryBuilder withCategory(Category category){
            return withProperty("category", category.getValue());
        }

        public QueryBuilder withoutCategory(Category category){
            return withoutProperty("category", category.getValue());
        }

        public QueryBuilder withPages(Integer pages){
            return withUnescapedProperty("pages", String.valueOf(pages));
        }

        public QueryBuilder withPagesMinimum(Integer pages){
            return withUnescapedProperty("pages", ">" + (pages - 1));
        }

        public QueryBuilder withPagesMaximum(Integer pages){
            return withUnescapedProperty("pages", "<=" + pages);
        }

        public QueryBuilder uploadedBefore(Duration duration){
            return withUnescapedProperty("uploaded", ">" + durationToString(duration));
        }

        public QueryBuilder uploadedAfter(Duration duration){
            return withUnescapedProperty("uploaded", "<" + durationToString(duration));
        }

        private QueryBuilder withProperty(String name, String value){
            includedParts.add(createIncludedPropertyPart(name, value));
            return this;
        }

        private QueryBuilder withUnescapedProperty(String name, String value){
            includedParts.add(createIncludedUnescapedPropertyPart(name, value));
            return this;
        }

        private QueryBuilder withoutProperty(String name, String value){
            excludedParts.add(createExcludedPropertyPart(name, value));
            return this;
        }

        private static String createKeywordPart(String keyword){
            return String.format("\"%s\"", keyword);
        }

        private static String createIncludedUnescapedPropertyPart(String name, String value){
            return String.format("%s:%s", name, value);
        }

        private static String createIncludedPropertyPart(String name, String value){
            return String.format("%s:\"%s\"", name, value);
        }

        private static String createExcludedPropertyPart(String name, String value){
            return String.format("\"%s:%s\"", name, value);
        }

        private String durationToString(Duration duration){
            var days = duration.toDays();
            var hours = duration.toHours();
            var hoursPart = duration.toHoursPart();

            if (days < 2 || hoursPart > 0){
                return Math.max(hours, 2) + "h";
            }

            return days + "d";
        }
    }
}
