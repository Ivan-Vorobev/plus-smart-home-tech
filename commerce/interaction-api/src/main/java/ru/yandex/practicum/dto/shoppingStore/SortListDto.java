package ru.yandex.practicum.dto.shoppingStore;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class SortListDto {
    private String direction;
    private String property;
}
