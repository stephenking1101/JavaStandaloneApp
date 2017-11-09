package example.test.cf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.cassandraunit.dataset.ParseException;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;

public class ClassPathCQLDataSetWithDiffCharset extends ClassPathCQLDataSet {
	private String dataSetLocation2;

	public String getDataSetLocation() {
		return dataSetLocation2;
	}

	public void setDataSetLocation(String dataSetLocation) {
		this.dataSetLocation2 = dataSetLocation;
	}

	private Charset charset;

	public Charset getCharset() {
		if (charset == null) {
			this.charset = Charset.defaultCharset();
		}
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public ClassPathCQLDataSetWithDiffCharset(String dataSetLocation) {
		super(dataSetLocation, true, true, null);
		setDataSetLocation(dataSetLocation);
	}

	public ClassPathCQLDataSetWithDiffCharset(String dataSetLocation, Charset charset) {
		super(dataSetLocation, true, true, null);
		setCharset(charset);
		setDataSetLocation(dataSetLocation);
		setDataSetLocation(dataSetLocation);
	}

	public ClassPathCQLDataSetWithDiffCharset(String dataSetLocation, boolean keyspaceCreation) {
		super(dataSetLocation, keyspaceCreation, true);
		setDataSetLocation(dataSetLocation);
	}

	public ClassPathCQLDataSetWithDiffCharset(String dataSetLocation, boolean keyspaceCreation, Charset charset) {
		super(dataSetLocation, keyspaceCreation, true);
		setCharset(charset);
		setDataSetLocation(dataSetLocation);
	}

	public ClassPathCQLDataSetWithDiffCharset(String dataSetLocation, boolean keyspaceCreation,
                                              boolean keyspaceDeletion) {
		super(dataSetLocation, keyspaceCreation, keyspaceDeletion);
		setDataSetLocation(dataSetLocation);
	}

	public ClassPathCQLDataSetWithDiffCharset(String dataSetLocation, boolean keyspaceCreation,
                                              boolean keyspaceDeletion, Charset charset) {
		super(dataSetLocation, keyspaceCreation, keyspaceDeletion);
		setCharset(charset);
		setDataSetLocation(dataSetLocation);
	}

	public ClassPathCQLDataSetWithDiffCharset(String dataSetLocation, String keyspaceName) {
		super(dataSetLocation, true, true, keyspaceName);
		setDataSetLocation(dataSetLocation);
	}

	public ClassPathCQLDataSetWithDiffCharset(String dataSetLocation, String keyspaceName, Charset charset) {
		super(dataSetLocation, true, true, keyspaceName);
		setCharset(charset);
		setDataSetLocation(dataSetLocation);
	}

	public ClassPathCQLDataSetWithDiffCharset(String dataSetLocation, boolean keyspaceCreation, String keyspaceName) {
		super(dataSetLocation, keyspaceCreation, true, keyspaceName);
		setDataSetLocation(dataSetLocation);
	}

	public ClassPathCQLDataSetWithDiffCharset(String dataSetLocation, boolean keyspaceCreation, String keyspaceName,
                                              Charset charset) {
		super(dataSetLocation, keyspaceCreation, true, keyspaceName);
		setCharset(charset);
		setDataSetLocation(dataSetLocation);
	}

	public ClassPathCQLDataSetWithDiffCharset(String dataSetLocation, boolean keyspaceCreation,
                                              boolean keyspaceDeletion, String keyspaceName) {
		super(dataSetLocation, keyspaceCreation, keyspaceDeletion, keyspaceName);
		setDataSetLocation(dataSetLocation);
	}

	public ClassPathCQLDataSetWithDiffCharset(String dataSetLocation, boolean keyspaceCreation,
                                              boolean keyspaceDeletion, String keyspaceName, Charset charset) {
		super(dataSetLocation, keyspaceCreation, keyspaceDeletion, keyspaceName);
		setCharset(charset);
		setDataSetLocation(dataSetLocation);
	}

	@Override
	public List<String> getLines() {
		InputStream inputStream = getInputDataSetLocation(getDataSetLocation());
		final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, getCharset());
		BufferedReader br = new BufferedReader(inputStreamReader);
		String line;
		List<String> cqlQueries = new ArrayList<String>();
		try {
			while ((line = br.readLine()) != null) {
				if (StringUtils.isNotBlank(line)) {
					cqlQueries.add(line);
				}
			}
			br.close();
			return cqlQueries;
		} catch (IOException e) {
			throw new ParseException(e);
		}
	}

}
