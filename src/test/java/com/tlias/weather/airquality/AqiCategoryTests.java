package com.tlias.weather.airquality;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AqiCategoryTests {
    @Test
    void mapsBoundaryValuesToExpectedCategories() {
        assertThat(AqiCategory.from(0).level()).isEqualTo("优");
        assertThat(AqiCategory.from(50).level()).isEqualTo("优");
        assertThat(AqiCategory.from(51).level()).isEqualTo("良");
        assertThat(AqiCategory.from(100).level()).isEqualTo("良");
        assertThat(AqiCategory.from(101).level()).isEqualTo("轻度污染");
        assertThat(AqiCategory.from(150).level()).isEqualTo("轻度污染");
        assertThat(AqiCategory.from(151).level()).isEqualTo("中度污染");
        assertThat(AqiCategory.from(200).level()).isEqualTo("中度污染");
        assertThat(AqiCategory.from(201).level()).isEqualTo("重度污染");
        assertThat(AqiCategory.from(300).level()).isEqualTo("重度污染");
        assertThat(AqiCategory.from(301).level()).isEqualTo("严重污染");
        assertThat(AqiCategory.from(500).level()).isEqualTo("严重污染");
    }

    @Test
    void clampsOutOfRangeValues() {
        assertThat(AqiCategory.from(-1).level()).isEqualTo("优");
        assertThat(AqiCategory.from(999).level()).isEqualTo("严重污染");
    }

    @Test
    void providesColorAndAdvice() {
        AqiCategory good = AqiCategory.from(80);
        assertThat(good.color()).isNotBlank();
        assertThat(good.healthAdvice()).isNotBlank();
    }
}
