package ddc.model.specifications;

import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import ddc.util.DateUtils;
import ddc.util.Utils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;

@NoArgsConstructor
public class EntitySpecification<T> implements Specification<T> {
    private SearchCriteria criteria;

    public EntitySpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (criteria == null)
            return builder.conjunction();
        switch (criteria.getOperation().toLowerCase()) {
            case "|":
                //t - true, f - false, n - null
                switch (((String)criteria.getValue()).toLowerCase()) {
                    case "t":
                        return builder.isTrue(root.get(criteria.getKey()));
                    case "f":
                        return builder.isFalse(root.get(criteria.getKey()));
                    case "n":
                        return builder.isNull(root.get(criteria.getKey()));
                    default:
                        return builder.conjunction();
                }
            case ">":
                return builder.greaterThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString());
            case "<":
                return builder.lessThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString());
            case ":":
                final Class<?> type = root.get(criteria.getKey()).getJavaType();
                if (type == String.class) {
                    String value = "%" + criteria.getValue().toString().toUpperCase() + "%";
                    return builder.like(builder.upper(root.get(criteria.getKey())), value);
                } else if(type == Long.class){
                    final long timestamp = Long.parseLong(criteria.getValue().toString());
                    final LocalDate lookupDate = DateUtils.toLocalDate(timestamp);
                    return builder.between(
                            root.get(criteria.getKey()),
                            DateUtils.toSeconds(lookupDate.atTime(0,0,0)),
                            DateUtils.toSeconds(lookupDate.atTime(23,59,59)));
                } else {
                    return builder.equal(root.get(criteria.getKey()), criteria.getValue());
                }
            case "===":
                String value = (String)criteria.getValue();
                if (Utils.isBlank(value)) {
                    return builder.like(builder.upper(root.get(criteria.getKey())),
                            "%" + value.toUpperCase() + "%");
                } else {
                    return builder.equal(root.get(criteria.getKey()), value);
                }
                //TODO тут бы переделать все, поскольку верхний блок для закладных нижний для остального..
            case "@":
                //criteria.getValue() может быть как числом(long), так и строкой
                Object element;
                try {
                    final long timestamp = Long.parseLong(criteria.getValue().toString());
                    element = DateUtils.toLocalDate(timestamp);
                    return builder.and(builder.isMember(element, root.get(criteria.getKey())));
                } catch (NumberFormatException nfe) {
                    element = criteria.getValue();
                    return builder.and(root.get(criteria.getKey()).in(element));
                }
            default:
                return null;
        }
    }
}
