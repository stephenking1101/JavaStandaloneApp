package example.store.cassandra.lucene;

import java.util.Collections;
import java.util.List;

import com.stratio.cassandra.lucene.builder.search.sort.SimpleSortField;
import com.stratio.cassandra.lucene.builder.search.sort.SortField;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import example.store.cassandra.exception.StoreCassandraException;

public class QueryParam {
	private List<String> attributes; //the attributes need to returned
    private List<String> excludeAttrs; //the attributs not to returned
    private String filter; //lucene expression , e.g, 'name:Pet* AND email:*@ericsson.com'
    private String sortBy; //the attribute used to sort, only one attribute can be sort
    private SortType sortType;
    private int startIndex; //from the start index of the result set to return
    private Integer count; //the number of results to return

    private QueryParser parser = new QueryParser("", new StandardAnalyzer());

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((attributes == null) ? 0 : attributes.hashCode());
        result = (prime * result) + ((excludeAttrs == null) ? 0 : excludeAttrs.hashCode());
        result = (prime * result) + ((filter == null) ? 0 : getFilterHashCode());
        result = (prime * result) + ((sortBy == null) ? 0 : sortBy.hashCode());
        if (sortBy != null) {
            result = (prime * result) + ((sortType == null) ? 0 : sortType.hashCode());
        }
        return result;
    }

    private int getFilterHashCode() {
        try {
            return parser.parse(filter).hashCode();
        } catch (ParseException e) {
            throw new StoreCassandraException("parse filter error", e);
        }
    }

    public SortField toSortField() {
        SimpleSortField simpleSortField = new SimpleSortField(sortBy);
        if (sortType != null) {
            return simpleSortField.reverse(sortType.isReverse());
        }
        return simpleSortField;
    }


    public List<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<String> attributes) {
        // sort the attributes list first , so that we can get the same hash code no matter the entries sequence
        if (attributes != null) {
            Collections.sort(attributes);
        }
        this.attributes = attributes;
    }

    public List<String> getExcludeAttrs() {
        return excludeAttrs;
    }

    public void setExcludeAttrs(List<String> excludeAttrs) {
        // sort the attributes list first , so that we can get the same hash code no matter the entries sequence
        if (excludeAttrs != null) {
            Collections.sort(excludeAttrs);
        }
        this.excludeAttrs = excludeAttrs;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }


    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public SortType getSortType() {
        return sortType;
    }

    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }


}
