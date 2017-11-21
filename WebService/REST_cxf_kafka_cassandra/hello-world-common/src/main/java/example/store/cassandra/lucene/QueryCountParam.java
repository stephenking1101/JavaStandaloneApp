package example.store.cassandra.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;

import example.store.cassandra.exception.StoreCassandraException;

/**
*
* The purpose of this class is to construct the lucene search count
* parameter: filter
*
*/
public class QueryCountParam {

   private String filter; // lucene expression, e.g. 'name:Will* AND email:*@ericsson.com'
   private QueryParser parser;

   public QueryCountParam() {
       parser = new QueryParser("", new StandardAnalyzer());
       parser.setAllowLeadingWildcard(true);
   }

   @Override
   public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = (prime * result) + ((filter == null) ? 0 : getFilterHashCode());
       return result;
   }

   private int getFilterHashCode() {
       try {
           return parser.parse(filter).hashCode();
       } catch (ParseException e) {
           throw new StoreCassandraException("parse filter error", e);
       }
   }

   public String getFilter() {
       return filter;
   }

   public void setFilter(String filter) {
       this.filter = filter;
   }

}