package ru.yandex.practicum.dto.shoppingStore;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class PageProductDto {
    private List<ProductDto> content;
    private List<SortListDto> sort;
}
