package ddc.model.specifications;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchCriteria<T> {
    private String key;
    private String operation;
    private T value;
}
