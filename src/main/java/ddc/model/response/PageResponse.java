package ddc.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {

    private Integer currentPage;
    private Integer totalPages;
    private Integer size;
    private Integer numberOfElements;
    private Long totalElements;

    private List<T> items;


    /**
     * @param sourcePage
     * @param mapper
     * @param <E>        entity
     * @param <R>        response
     * @return
     */
    public static <E, R> PageResponse<R> getFrontPageFromServerPage(Page<E> sourcePage, Function<E, R> mapper) {
        PageResponse<R> pageResponse = new PageResponse<>();
        pageResponse.setCurrentPage(sourcePage.getNumber());
        pageResponse.setTotalPages(sourcePage.getTotalPages());
        pageResponse.setSize(sourcePage.getSize());
        pageResponse.setNumberOfElements(sourcePage.getNumberOfElements());
        pageResponse.setItems(sourcePage.getContent().stream().map(mapper).collect(Collectors.toList()));
        pageResponse.setTotalElements(sourcePage.getTotalElements());
        return pageResponse;
    }

    /**
     * @param pageNumber
     * @param sourcePage
     * @param mapper
     * @param <E>        entity
     * @param <R>        response
     * @return
     */
    public static <E, R> PageResponse<R> getFrontPageFromServerPage(
            int pageNumber, Page<E> sourcePage, Function<E, R> mapper) {
        return getFrontPageFromServerPage(sourcePage, mapper);
    }

}
