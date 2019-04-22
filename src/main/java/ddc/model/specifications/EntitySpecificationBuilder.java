package ddc.model.specifications;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntitySpecificationBuilder<T> {

    private static final Pattern pattern = Pattern.compile("(\\w+?)(===|:|<|>|\\||@)(\\w+.*)", Pattern.UNICODE_CHARACTER_CLASS);

    private Collection<SearchCriteria> params;

    public EntitySpecificationBuilder() {
        params = new LinkedList<>();
    }

    public EntitySpecificationBuilder(Collection<SearchCriteria> searchCriteriaCollection) {
        if (searchCriteriaCollection != null)
            this.params = searchCriteriaCollection;
        else
            params = new LinkedList<>();
    }

    public static List<SearchCriteria> parseParams(String params) {
        if (params != null) {
            String[] paramsArray = params.split(",");
            List<SearchCriteria> searchCriteriaList = new LinkedList<>();
            for (String param : paramsArray) {
                Matcher matcher = pattern.matcher(param);
                if (matcher.find())
                    searchCriteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
            }
            return searchCriteriaList;
        }
        return new LinkedList<>();
    }

    public EntitySpecificationBuilder(String filterParams) {
        params = parseParams(filterParams);
    }

    public EntitySpecificationBuilder with(String key, String operation, Object value) {
        params.add(new SearchCriteria(key, operation, value));
        return this;
    }

    public Specification<T> build() {
        if (params.isEmpty()) {
            return new EntitySpecification<>();
        }

        List<Specification<T>> specs = new ArrayList<>();
        for (SearchCriteria param : params) {
            specs.add(new EntitySpecification<>(param));
        }

        Specification<T> result = specs.get(0);
        for (int i = 1; i < specs.size(); i++) {
            result = result.and(specs.get(i));
        }
        return result;
    }
}
