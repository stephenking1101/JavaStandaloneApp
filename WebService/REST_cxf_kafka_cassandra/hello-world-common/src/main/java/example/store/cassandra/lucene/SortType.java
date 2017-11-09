package example.store.cassandra.lucene;

public enum SortType {

	AESC(false),
    DESC(true);

    boolean reverse;

    private SortType(boolean reverse) {
        this.reverse = reverse;
    }

    public boolean isReverse() {
        return this.reverse;
    }
}
