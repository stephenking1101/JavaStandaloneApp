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
	private List<String> attributes;
    private List<String> excludeAttrs;
    private String filter;
    private String sortBy;
    private SortType sortType;
    private int startIndex;
    private Integer count;
    private QueryParser parser = new QueryParser("", new StandardAnalyzer());

    public QueryParam() {
    }

    public int hashCode() {
        boolean kk = true;
        int result = 1;
        result = 31 * result + (this.attributes == null ? 0 : this.attributes.hashCode());
        result = 31 * result + (this.excludeAttrs == null ? 0 : this.excludeAttrs.hashCode());
        result = 31 * result + (this.filter == null ? 0 : this.getFilterHashCode());
        result = 31 * result + (this.sortBy == null ? 0 : this.sortBy.hashCode());
        if (this.sortBy != null) {
            result = 31 * result + (this.sortType == null ? 0 : this.sortType.hashCode());
        }

        return result;
    }

    private int getFilterHashCode() {
        try {
            return this.parser.parse(this.filter).hashCode();
        } catch (ParseException var2) {
            throw new StoreCassandraException("parse filter error", var2);
        }
    }

    public SortField toSortField() {
        SimpleSortField simpleSortField = new SimpleSortField(this.sortBy);
        return (SortField)(this.sortType != null ? simpleSortField.reverse(this.sortType.isReverse()) : simpleSortField);
    }

    public List<String> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(List<String> attributes) {
        if (attributes != null) {
            Collections.sort(attributes);
        }

        this.attributes = attributes;
    }

    public List<String> getExcludeAttrs() {
        return this.excludeAttrs;
    }

    public void setExcludeAttrs(List<String> excludeAttrs) {
        if (excludeAttrs != null) {
            Collections.sort(excludeAttrs);
        }

        this.excludeAttrs = excludeAttrs;
    }

    public String getFilter() {
        return this.filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getSortBy() {
        return this.sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public int getStartIndex() {
        return this.startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public SortType getSortType() {
        return this.sortType;
    }

    public void setSortType(SortType sortType) {
        this.sortType = sortType;
    }
}
